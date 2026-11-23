package com.guilherme.santana.genui2.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guilherme.santana.genui2.R
import com.guilherme.santana.genui2.data.ScreenApiException
import com.guilherme.santana.genui2.data.ScreenPreview
import com.guilherme.santana.genui2.data.reviseScreen
import com.guilherme.santana.genui2.data.requestScreen
import com.guilherme.santana.genui2.sdui.ScreenEvent
import com.guilherme.santana.genui2.sdui.spec.RESET_FIELDS
import com.guilherme.santana.genui2.sdui.spec.ScreenSpec
import com.guilherme.santana.genui2.sdui.spec.parseScreenSpec
import com.guilherme.santana.genui2.sdui.spec.resolve
import com.guilherme.santana.genui2.sdui.state.UiNode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException

internal const val MAX_PROMPT_LENGTH = 4000

internal data class ScreenUiState(
    val prompt: String = "",
    val feedback: String? = null,
    val canRevise: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
    val preview: List<UiNode>? = null,
)

internal class GenUiViewModel(private val app: Application) : AndroidViewModel(app) {
    private val mutableUiState = MutableStateFlow(ScreenUiState())
    val uiState = mutableUiState.asStateFlow()

    private var currentPreview: ScreenPreview? = null
    private var loadJob: Job? = null
    private var fields = emptyMap<String, String>()

    fun changePrompt(value: String) {
        mutableUiState.value = uiState.value.copy(prompt = value, error = null)
    }

    fun generate() {
        val prompt = uiState.value.prompt.trim()
        if (prompt.length !in 1..MAX_PROMPT_LENGTH) return
        loadPreview(R.string.connection_error) { requestScreen(prompt) }
    }

    fun openExample() = loadPreview(R.string.screen_load_error) {
        app.assets.open("meu_carro.json").bufferedReader().use { ScreenPreview(parseScreenSpec(it.readText())) }
    }

    fun changeFeedback(value: String) {
        mutableUiState.value = uiState.value.copy(feedback = value, error = null)
    }

    fun dismissFeedback() {
        loadJob?.cancel()
        mutableUiState.value = uiState.value.copy(feedback = null, loading = false, error = null)
    }

    fun revise() {
        val draft = currentPreview?.draftJson ?: return
        val feedback = uiState.value.feedback?.trim().orEmpty()
        if (feedback.length !in 1..MAX_PROMPT_LENGTH) return
        loadPreview(R.string.connection_error, preserveFields = true) { reviseScreen(draft, feedback) }
    }

    private fun loadPreview(ioErrorMessage: Int, preserveFields: Boolean = false, load: () -> ScreenPreview) {
        if (uiState.value.loading) return
        mutableUiState.value = uiState.value.copy(loading = true, error = null)
        loadJob = viewModelScope.launch {
            try {
                val loaded = withContext(Dispatchers.IO) { load() }
                val screen = loaded.screen
                if (preserveFields) {
                    val previous = requireNotNull(currentPreview).screen
                    require(screen.fields == previous.fields && screen.computed == previous.computed &&
                        screen.initialState == previous.initialState) { "O ajuste alterou a definição da calculadora" }
                }
                val next = if (preserveFields) fields else screen.initialState
                val nodes = resolve(screen, next)
                currentPreview = loaded
                fields = next
                mutableUiState.value = uiState.value.copy(
                    preview = nodes, loading = false, feedback = null, canRevise = loaded.draftJson != null,
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                ensureActive()
                Log.e("GenUiScreen", "Falha ao abrir a prévia", failure)
                val message = when (failure) {
                    is ScreenApiException -> failure.detail ?: app.getString(R.string.server_error)
                    is SocketTimeoutException -> app.getString(R.string.request_timeout)
                    is IOException -> app.getString(ioErrorMessage)
                    else -> app.getString(R.string.screen_load_error)
                }
                mutableUiState.value = uiState.value.copy(loading = false, error = message)
            }
        }
    }

    fun closePreview() {
        loadJob?.cancel()
        currentPreview = null
        fields = emptyMap()
        mutableUiState.value = ScreenUiState(prompt = uiState.value.prompt)
    }

    fun onEvent(event: ScreenEvent) {
        val screen = currentPreview?.screen ?: return
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
        mutableUiState.value = uiState.value.copy(preview = nodes)
    }
}
