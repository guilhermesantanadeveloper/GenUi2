package com.guilherme.santana.genui2.presentation.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.guilherme.santana.genui2.sdui.state.ButtonVariant

@Composable
fun GenButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
) {
    val size = modifier.heightIn(min = 48.dp)
    val shape = RoundedCornerShape(8.dp)
    when (variant) {
        ButtonVariant.Primary -> Button(onClick, size, enabled, shape) { Text(label) }
        ButtonVariant.Text -> TextButton(onClick, size, enabled, shape) { Text(label) }
    }
}

@Preview
@Composable
private fun GenButtonPrev() {
    MaterialTheme {
        GenButton(
            label = "Teste",
            onClick = {}
        )
    }
}
