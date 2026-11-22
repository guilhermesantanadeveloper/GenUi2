package com.guilherme.santana.genui2.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guilherme.santana.genui2.R
import com.guilherme.santana.genui2.sdui.state.GenListItem
import com.guilherme.santana.genui2.sdui.state.UiAction
import com.guilherme.santana.genui2.ui.theme.LocalSpacing

@Composable
fun GenList(items: List<GenListItem>, onAction: (UiAction) -> Unit, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            key(item.id) {
                val action = item.action
                val interaction = if (action != null) {
                    Modifier.clickable(role = Role.Button) { onAction(action) }
                } else {
                    Modifier
                }
                Row(
                    modifier = Modifier.fillMaxWidth().then(interaction)
                        .heightIn(min = 56.dp).padding(vertical = spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(spacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(item.title, modifier = Modifier.weight(1f))
                    item.value?.let { value ->
                        Text(
                            text = value,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                        )
                    }
                    if (action != null) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_end),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (index < items.lastIndex) HorizontalDivider()
            }
        }
    }
}
