package com.example.applock.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun PinSetupScreen(onComplete: (String) -> Unit) {
    var step by remember { mutableStateOf(SetupStep.ENTER) }
    var firstPin by remember { mutableStateOf("") }

    when (step) {
        SetupStep.ENTER -> PinEntryScreen(
            title = "PIN 설정",
            subtitle = "4자리 숫자를 입력하세요",
            onPinEntered = { pin ->
                firstPin = pin
                step = SetupStep.CONFIRM
                true
            }
        )
        SetupStep.CONFIRM -> PinEntryScreen(
            title = "PIN 확인",
            subtitle = "방금 입력한 PIN을 다시 입력하세요",
            onPinEntered = { pin ->
                if (pin == firstPin) {
                    onComplete(pin)
                    true
                } else {
                    firstPin = ""
                    step = SetupStep.ENTER
                    false
                }
            }
        )
    }
}

private enum class SetupStep { ENTER, CONFIRM }
