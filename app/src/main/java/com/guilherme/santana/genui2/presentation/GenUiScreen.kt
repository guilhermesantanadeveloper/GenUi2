package com.guilherme.santana.genui2.presentation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guilherme.santana.genui2.R
import com.guilherme.santana.genui2.sdui.RenderScreen

@Composable
internal fun GenUiScreen(viewModel: GenUiViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    when (val current = state) {
        ScreenUiState.Loading -> CircularProgressIndicator(modifier = modifier)
        ScreenUiState.LoadError -> Text(stringResource(R.string.screen_load_error), modifier = modifier)
        is ScreenUiState.Ready -> RenderScreen(current.nodes, viewModel::onEvent, modifier)
    }
}
