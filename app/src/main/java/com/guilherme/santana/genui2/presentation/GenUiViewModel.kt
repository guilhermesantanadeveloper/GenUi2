package com.guilherme.santana.genui2.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guilherme.santana.genui2.sdui.ScreenEvent
import com.guilherme.santana.genui2.sdui.spec.RESET_FIELDS
import com.guilherme.santana.genui2.sdui.spec.ScreenSpec
import com.guilherme.santana.genui2.sdui.spec.parseScreenSpec
import com.guilherme.santana.genui2.sdui.spec.resolve
import com.guilherme.santana.genui2.sdui.state.UiNode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal sealed interface ScreenUiState {
    object Loading : ScreenUiState
    object LoadError : ScreenUiState
    data class Ready(val nodes: List<UiNode>) : ScreenUiState
}

internal class GenUiViewModel(application: Application) : AndroidViewModel(application) {
    private val mutableUiState = MutableStateFlow<ScreenUiState>(ScreenUiState.Loading)
    val uiState = mutableUiState.asStateFlow()

    private var spec: ScreenSpec? = null
    private var fields = emptyMap<String, String>()

    init {
        viewModelScope.launch {
            try {
                val loaded = withContext(Dispatchers.IO) {
                    application.assets.open("meu_carro.json").bufferedReader().use {
                        parseScreenSpec(it.readText())
                    }
                }
                applyFields(loaded, loaded.initialState)
                spec = loaded
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                Log.e("GenUiScreen", "Erro ao abrir meu_carro.json", failure)
                mutableUiState.value = ScreenUiState.LoadError
            }
        }
    }

    fun onEvent(event: ScreenEvent) {
        val screen = spec ?: return
        when (event) {
            is ScreenEvent.FieldChanged -> {
                if (event.fieldId in screen.fields) {
                    applyFields(screen, fields + (event.fieldId to event.rawValue))
                }
            }
            is ScreenEvent.ActionRequested -> {
                if (event.action.name == RESET_FIELDS) applyFields(screen, screen.initialState)
            }
        }
    }

    private fun applyFields(screen: ScreenSpec, next: Map<String, String>) {
        val nodes = resolve(screen, next)
        fields = next
        mutableUiState.value = ScreenUiState.Ready(nodes)
    }
}
