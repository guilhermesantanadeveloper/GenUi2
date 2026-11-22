package com.guilherme.santana.genui2.sdui.state

import androidx.compose.ui.text.input.KeyboardType

sealed interface UiNode {
    val id: String

    data class Text(
        override val id: String,
        val text: String,
        val role: TextRole = TextRole.Body,
    ) : UiNode

    data class Input(
        override val id: String,
        val fieldId: String,
        val label: String,
        val rawValue: String,
        val keyboardType: KeyboardType = KeyboardType.Text,
        val suffix: String? = null,
        val error: String? = null,
    ) : UiNode

    data class ListItems(
        override val id: String,
        val items: List<GenListItem>,
    ) : UiNode

    data class Button(
        override val id: String,
        val label: String,
        val action: UiAction,
        val variant: ButtonVariant = ButtonVariant.Primary,
        val enabled: Boolean = true,
    ) : UiNode

    data class Card(
        override val id: String,
        val children: List<UiNode>,
    ) : UiNode
}

enum class TextRole { Title, Body, Highlight }

enum class ButtonVariant { Primary, Text }

data class UiAction(
    val name: String,
    val parameters: Map<String, String> = emptyMap(),
)

data class GenListItem(
    val id: String,
    val title: String,
    val value: String? = null,
    val action: UiAction? = null,
)
