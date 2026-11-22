package com.guilherme.santana.genui2.sdui.spec

import com.guilherme.santana.genui2.sdui.state.TextRole
import com.guilherme.santana.genui2.sdui.state.UiAction

internal data class ScreenSpec(
    val fields: Map<String, FieldType>,
    val initialState: Map<String, String>,
    val computed: Map<String, Expression>,
    val children: List<ComponentSpec>,
)

internal enum class FieldType { Integer, Decimal }

internal sealed interface Expression {
    data class Ref(val name: String) : Expression
    data class Call(val op: Operation, val args: List<Expression>) : Expression
}

internal enum class Operation { Add, Multiply, Divide }

internal sealed interface DisplayValue {
    data class Literal(val text: String) : DisplayValue
    data class Bound(val name: String, val currency: Boolean) : DisplayValue
}

internal sealed interface ComponentSpec {
    val id: String

    data class Text(override val id: String, val value: DisplayValue, val role: TextRole) : ComponentSpec
    data class Input(override val id: String, val field: String, val label: String, val suffix: String?) : ComponentSpec
    data class Card(override val id: String, val children: List<ComponentSpec>) : ComponentSpec
    data class Rows(override val id: String, val items: List<RowSpec>) : ComponentSpec
    data class Button(override val id: String, val label: String, val action: UiAction) : ComponentSpec
}

internal data class RowSpec(val id: String, val title: String, val value: DisplayValue)

internal const val RESET_FIELDS = "reset_fields"
