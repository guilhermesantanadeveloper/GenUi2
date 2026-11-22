package com.guilherme.santana.genui2.sdui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.guilherme.santana.genui2.presentation.components.GenButton
import com.guilherme.santana.genui2.presentation.components.GenCard
import com.guilherme.santana.genui2.presentation.components.GenInput
import com.guilherme.santana.genui2.presentation.components.GenList
import com.guilherme.santana.genui2.presentation.components.GenText
import com.guilherme.santana.genui2.sdui.state.GenListItem
import com.guilherme.santana.genui2.sdui.state.TextRole
import com.guilherme.santana.genui2.sdui.state.UiAction
import com.guilherme.santana.genui2.sdui.state.UiNode
import com.guilherme.santana.genui2.ui.theme.GenUi2Theme
import com.guilherme.santana.genui2.ui.theme.LocalSpacing

@Composable
fun RenderScreen(
    nodes: List<UiNode>,
    onEvent: (ScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current.medium
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        RenderNodes(nodes, onEvent)
    }
}

@Composable
private fun RenderNodes(
    nodes: List<UiNode>,
    onEvent: (ScreenEvent) -> Unit,
) {
    for (node in nodes) {
        key(node.id) {
            when (node) {
                is UiNode.Text -> GenText(
                    text = node.text,
                    role = node.role,
                )

                is UiNode.Input -> GenInput(
                    label = node.label,
                    value = node.rawValue,
                    suffix = node.suffix,
                    keyboardType = node.keyboardType,
                    error = node.error,
                    onValueChange = { raw ->
                        onEvent(ScreenEvent.FieldChanged(node.fieldId, raw))
                    },
                )

                is UiNode.ListItems -> GenList(
                    items = node.items,
                    onAction = { action ->
                        onEvent(ScreenEvent.ActionRequested(action))
                    },
                )

                is UiNode.Button -> GenButton(
                    modifier = Modifier.fillMaxWidth(),
                    label = node.label,
                    variant = node.variant,
                    enabled = node.enabled,
                    onClick = {
                        onEvent(ScreenEvent.ActionRequested(node.action))
                    },
                )

                is UiNode.Card -> GenCard {
                    RenderNodes(node.children, onEvent)
                }
            }
        }
    }
}

@Preview(showBackground = true, locale = "pt-rBR")
@Composable
private fun MeuCarroPreview() {
    GenUi2Theme(dynamicColor = false) {
        RenderScreen(
            nodes = listOf(
                UiNode.Text("titulo", "Meu carro", TextRole.Title),
                UiNode.Text("subtitulo", "Estimativa mensal"),
                UiNode.Card(
                    id = "resumo",
                    children = listOf(
                        UiNode.Text("rotulo_total", "Total por mês"),
                        UiNode.Text("total", "R$ 780,00", TextRole.Highlight),
                    ),
                ),
                UiNode.Input(
                    id = "distancia",
                    fieldId = "km_por_mes",
                    label = "Distância mensal",
                    rawValue = "800",
                    keyboardType = KeyboardType.Number,
                    suffix = "km",
                ),
                UiNode.Text("titulo_despesas", "Despesas"),
                UiNode.ListItems(
                    id = "despesas",
                    items = listOf(
                        GenListItem("combustivel", "Combustível", "R$ 480"),
                        GenListItem("seguro", "Seguro", "R$ 180", UiAction("editar_seguro")),
                        GenListItem("estacionamento", "Estacionamento", "R$ 120"),
                    ),
                ),
                UiNode.Button("salvar", "Salvar cenário", UiAction("salvar_cenario")),
            ),
            onEvent = {},
        )
    }
}
