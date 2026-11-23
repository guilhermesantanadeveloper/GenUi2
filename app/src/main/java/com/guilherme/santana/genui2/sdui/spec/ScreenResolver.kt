package com.guilherme.santana.genui2.sdui.spec

import androidx.compose.ui.text.input.KeyboardType
import com.guilherme.santana.genui2.sdui.state.GenListItem
import com.guilherme.santana.genui2.sdui.state.UiNode
import java.math.BigDecimal
import java.math.MathContext
import java.text.NumberFormat
import java.util.Locale

private val brazil = Locale.forLanguageTag("pt-BR")
private val integer = Regex("[0-9]+")
private val decimal = Regex("[0-9]+([,.][0-9]+)?")

internal class IncompletePercentInput : IllegalArgumentException()

// UI state keeps exactly what was typed, including a trailing decimal separator.
internal fun convertInputScale(
    state: Map<String, String>,
    fromPercent: Set<String>,
    toPercent: Set<String>,
): Map<String, String> = state.mapValues { (id, raw) ->
    val wasPercent = id in fromPercent
    val isPercent = id in toPercent
    if (wasPercent == isPercent || raw.isEmpty()) {
        raw
    } else {
        if (!decimal.matches(raw)) throw IncompletePercentInput()
        val amount = BigDecimal(raw.replace(',', '.'))
        val displayed = if (isPercent) amount.movePointRight(2) else amount.movePointLeft(2)
        displayed.stripTrailingZeros().toPlainString().replace('.', ',')
    }
}

internal fun ScreenSpec.initialInputs(): Map<String, String> =
    convertInputScale(initialState, emptySet(), fractionAsPercent)

internal fun resolve(spec: ScreenSpec, state: Map<String, String>): List<UiNode> {
    val values = mutableMapOf<String, BigDecimal?>()
    for ((id, type) in spec.fields) {
        val raw = state[id].orEmpty()
        val scaled = id in spec.fractionAsPercent
        val pattern = if (type == FieldType.Integer && !scaled) integer else decimal
        val input = raw.takeIf(pattern::matches)?.replace(',', '.')?.toBigDecimalOrNull()
        val value = if (scaled) input?.movePointLeft(2) else input
        values[id] = value?.takeIf { type == FieldType.Decimal || it.stripTrailingZeros().scale() <= 0 }
    }

    val visiting = mutableSetOf<String>()
    fun number(name: String): BigDecimal? {
        if (name in values) return values[name]
        val expression = spec.computed[name] ?: error("Referência desconhecida: $name")
        require(visiting.add(name)) { "Ciclo no cálculo: $name" }
        val result = expression.evaluate(::number)
        visiting.remove(name)
        values[name] = result
        return result
    }
    spec.computed.keys.forEach { number(it) }

    val currency = NumberFormat.getCurrencyInstance(brazil)
    fun display(value: DisplayValue): String = when (value) {
        is DisplayValue.Literal -> value.text
        is DisplayValue.Bound -> values[value.name]?.let { amount ->
            if (value.currency) currency.format(amount)
            else amount.stripTrailingZeros().toPlainString().replace('.', ',')
        } ?: "—"
    }

    fun node(component: ComponentSpec): UiNode = when (component) {
        is ComponentSpec.Text -> UiNode.Text(component.id, display(component.value), component.role)
        is ComponentSpec.Input -> {
            val field = component.field
            val raw = state[field].orEmpty()
            val type = spec.fields.getValue(field)
            UiNode.Input(
                id = component.id,
                fieldId = field,
                label = component.label,
                rawValue = raw,
                suffix = component.suffix,
                keyboardType = if (type == FieldType.Integer) KeyboardType.Number else KeyboardType.Decimal,
                error = if (raw.isNotEmpty() && values[field] == null) "Informe um número válido" else null,
            )
        }
        is ComponentSpec.Card -> UiNode.Card(component.id, component.children.map(::node))
        is ComponentSpec.Rows -> UiNode.ListItems(component.id, component.items.map {
            GenListItem(it.id, it.title, display(it.value))
        })
        is ComponentSpec.Button -> UiNode.Button(component.id, component.label, component.action)
    }
    return spec.children.map(::node)
}

private fun Expression.evaluate(lookup: (String) -> BigDecimal?): BigDecimal? {
    return when (this) {
        is Expression.Ref -> lookup(name)
        is Expression.Call -> {
            val numbers = args.mapNotNull { it.evaluate(lookup) }
            if (numbers.size != args.size) return null
            when (op) {
                Operation.Add -> numbers.reduce(BigDecimal::add)
                Operation.Multiply -> numbers.reduce(BigDecimal::multiply)
                Operation.Divide -> {
                    val divisor = numbers[1]
                    if (divisor.signum() == 0) null else numbers[0].divide(divisor, MathContext.DECIMAL128)
                }
            }
        }
    }
}
