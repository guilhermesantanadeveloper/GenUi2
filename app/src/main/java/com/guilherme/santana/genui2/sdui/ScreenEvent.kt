package com.guilherme.santana.genui2.sdui

import com.guilherme.santana.genui2.sdui.state.UiAction

sealed interface ScreenEvent {
    data class FieldChanged(val fieldId: String, val rawValue: String) : ScreenEvent
    data class ActionRequested(val action: UiAction) : ScreenEvent
}
