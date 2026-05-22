package com.example.applock.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val PIN_LENGTH = 4

@Composable
fun PinEntryScreen(
    title: String,
    subtitle: String? = null,
    onPinEntered: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(32.dp))
            PinDots(filled = pin.length, total = PIN_LENGTH, error = error)
            if (error) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "PIN이 일치하지 않아요",
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.height(40.dp))
            Keypad(
                onDigit = { d ->
                    error = false
                    if (pin.length < PIN_LENGTH) {
                        pin += d
                        if (pin.length == PIN_LENGTH) {
                            val accepted = onPinEntered(pin)
                            if (!accepted) {
                                error = true
                            }
                            pin = ""
                        }
                    }
                },
                onBackspace = {
                    error = false
                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                }
            )
        }
    }
}

@Composable
private fun PinDots(filled: Int, total: Int, error: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(total) { i ->
            val color = when {
                error -> MaterialTheme.colorScheme.error
                i < filled -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun Keypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        ).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { d -> KeypadButton(d) { onDigit(d) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.size(72.dp))
            KeypadButton("0") { onDigit("0") }
            KeypadButton(label = "⌫", filled = false, onClick = onBackspace)
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    filled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (filled) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        modifier = Modifier.size(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontSize = 28.sp, fontWeight = FontWeight.Medium)
        }
    }
}
