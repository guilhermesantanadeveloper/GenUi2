package com.guilherme.santana.genui2.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guilherme.santana.genui2.R
import com.guilherme.santana.genui2.sdui.RenderScreen
import com.guilherme.santana.genui2.ui.theme.LocalSpacing

@Composable
internal fun GenUiScreen(viewModel: GenUiViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val nodes = state.preview
    val spacing = LocalSpacing.current.medium
    if (nodes != null) {
        BackHandler(onBack = viewModel::closePreview)
        Column(modifier.fillMaxSize()) {
            TextButton(onClick = viewModel::closePreview) {
                Text(stringResource(R.string.back_to_request))
            }
            RenderScreen(nodes, viewModel::onEvent, Modifier.weight(1f))
        }
        return
    }

    val focus = LocalFocusManager.current
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        Text(stringResource(R.string.request_title), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = state.prompt,
            onValueChange = viewModel::changePrompt,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.request_label)) },
            enabled = !state.loading,
            minLines = 2,
            maxLines = 6,
            isError = state.prompt.trim().length > MAX_PROMPT_LENGTH,
        )
        if (state.prompt.trim().length > MAX_PROMPT_LENGTH) {
            Text(stringResource(R.string.prompt_too_long, MAX_PROMPT_LENGTH))
        }
        Button(
            onClick = {
                focus.clearFocus()
                viewModel.generate()
            },
            enabled = !state.loading && state.prompt.trim().length in 1..MAX_PROMPT_LENGTH,
        ) {
            Text(stringResource(R.string.generate_screen))
        }
        if (state.loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        TextButton(onClick = viewModel::openExample, enabled = !state.loading) {
            Text(stringResource(R.string.open_example))
        }
    }
}
