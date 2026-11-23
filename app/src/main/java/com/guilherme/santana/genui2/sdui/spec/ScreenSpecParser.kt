package com.guilherme.santana.genui2.sdui.spec

import com.guilherme.santana.genui2.sdui.state.TextRole
import com.guilherme.santana.genui2.sdui.state.UiAction
import org.json.JSONArray
import org.json.JSONObject

internal fun parseScreenSpec(json: String): ScreenSpec {
    val root = JSONObject(json)
    val version = root.getInt("schemaVersion")
    require(version in 1..2) { "Versão de tela incompatível" }
    val fractionAsPercent = mutableSetOf<String>()
    val fields = root.getJSONObject("fields").entries { name ->
        val field = getJSONObject(name)
        if (field.optBoolean("fractionAsPercent")) {
            require(version >= 2) { "Escala percentual incompatível com a versão da tela" }
            fractionAsPercent.add(name)
        }
        when (field.getString("type")) {
            "integer" -> FieldType.Integer
            "decimal" -> FieldType.Decimal
            else -> error("Tipo de campo desconhecido: $name")
        }
    }
    val initial = root.getJSONObject("initialState").entries { getString(it) }
    require(initial.keys.all { it in fields }) { "Estado inicial contém campo desconhecido" }
    val computed = root.getJSONObject("computed").entries { expression(getJSONObject(it)) }
    require(fields.keys.intersect(computed.keys).isEmpty()) { "Campo e cálculo com o mesmo nome" }
    val bindings = fields.keys + computed.keys
    fun value(obj: JSONObject): DisplayValue {
        require(obj.has("text") != obj.has("bind")) { "Informe text ou bind" }
        if (obj.has("text")) return DisplayValue.Literal(obj.getString("text"))
        val name = obj.getString("bind")
        require(name in bindings) { "Referência desconhecida: $name" }
        val format = obj.optString("format")
        require(format.isEmpty() || format == "BRL") { "Formato desconhecido: $format" }
        return DisplayValue.Bound(name, format == "BRL")
    }
    fun children(array: JSONArray): List<ComponentSpec> = array.objectsWithIds { id, obj ->
        when (val type = obj.getString("type")) {
            "text" -> {
                val role = when (val roleName = obj.optString("role", "body")) {
                    "body" -> TextRole.Body
                    "title" -> TextRole.Title
                    "highlight" -> TextRole.Highlight
                    else -> error("Papel de texto desconhecido: $roleName")
                }
                ComponentSpec.Text(id, value(obj), role)
            }
            "input" -> {
                val field = obj.getString("bind")
                require(field in fields) { "Campo de entrada desconhecido: $field" }
                ComponentSpec.Input(id, field, obj.getString("label"), obj.optString("suffix").ifEmpty { null })
            }
            "card" -> ComponentSpec.Card(id, children(obj.getJSONArray("children")))
            "list" -> ComponentSpec.Rows(id, obj.getJSONArray("items").objectsWithIds { rowId, row ->
                RowSpec(rowId, row.getString("title"), value(row))
            })
            "button" -> {
                val action = obj.getString("action")
                require(action == RESET_FIELDS) { "Ação desconhecida: $action" }
                ComponentSpec.Button(id, obj.getString("label"), UiAction(action))
            }
            else -> error("Componente desconhecido: $type")
        }
    }
    return ScreenSpec(fields, initial, computed, children(root.getJSONArray("children")), fractionAsPercent)
}

private fun expression(obj: JSONObject): Expression {
    if (obj.has("ref")) return Expression.Ref(obj.getString("ref"))
    val args = obj.getJSONArray("args")
    val op = when (val name = obj.getString("op")) {
        "add" -> Operation.Add
        "multiply" -> Operation.Multiply
        "divide" -> Operation.Divide
        else -> error("Operação desconhecida: $name")
    }
    require(args.length() >= 2 && (op != Operation.Divide || args.length() == 2)) {
        "Quantidade inválida de argumentos para $op"
    }
    return Expression.Call(op, List(args.length()) { expression(args.getJSONObject(it)) })
}

private fun <T> JSONObject.entries(read: JSONObject.(String) -> T): Map<String, T> =
    keys().asSequence().map { it.toString() }.associateWith { read(it) }

private fun <T> JSONArray.objectsWithIds(read: (String, JSONObject) -> T): List<T> {
    val ids = mutableSetOf<String>()
    return List(length()) { i ->
        val obj = getJSONObject(i)
        val id = obj.getString("id")
        require(id.isNotBlank() && ids.add(id)) { "ID vazio ou repetido: $id" }
        read(id, obj)
    }
}
