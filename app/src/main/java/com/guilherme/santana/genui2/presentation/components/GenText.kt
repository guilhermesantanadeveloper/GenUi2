package com.guilherme.santana.genui2.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.guilherme.santana.genui2.sdui.state.TextRole

@Composable
fun GenText(text: String, modifier: Modifier = Modifier, role: TextRole = TextRole.Body) {
    val typography = MaterialTheme.typography
    val style = when (role) {
        TextRole.Title -> typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
        TextRole.Body -> typography.bodyLarge
        TextRole.Highlight -> typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
    }
    Text(text = text, modifier = modifier, style = style)
}
