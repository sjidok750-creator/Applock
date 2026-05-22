package com.example.applock

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.applock.data.PinStore
import com.example.applock.service.AppLockService
import com.example.applock.ui.AppListScreen
import com.example.applock.ui.PinEntryScreen
import com.example.applock.ui.PinSetupScreen
import com.example.applock.ui.theme.ApplockTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var pinStore: PinStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pinStore = PinStore(applicationContext)

        setContent {
            ApplockTheme {
                AppRoot(
                    pinStore = pinStore,
                    hasUsagePermission = { hasUsageStatsPermission() },
                    hasOverlayPermission = { Settings.canDrawOverlays(this) },
                    onRequestUsage = {
                        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    },
                    onRequestOverlay = {
                        startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                        )
                    },
                    onReady = { AppLockService.start(this) }
                )
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val aom = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            aom.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            aom.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

private enum class Screen { SETUP, UNLOCK, MAIN }

@Composable
private fun AppRoot(
    pinStore: PinStore,
    hasUsagePermission: () -> Boolean,
    hasOverlayPermission: () -> Boolean,
    onRequestUsage: () -> Unit,
    onRequestOverlay: () -> Unit,
    onReady: () -> Unit
) {
    var screen by remember {
        mutableStateOf(if (pinStore.hasPin()) Screen.UNLOCK else Screen.SETUP)
    }

    when (screen) {
        Screen.SETUP -> PinSetupScreen { pin ->
            pinStore.setPin(pin)
            screen = Screen.MAIN
        }

        Screen.UNLOCK -> PinEntryScreen(
            title = "AppLock",
            subtitle = "PIN을 입력해 잠금을 해제하세요",
            onPinEntered = { pin ->
                val ok = pinStore.verify(pin)
                if (ok) screen = Screen.MAIN
                ok
            }
        )

        Screen.MAIN -> {
            var needsPerm by remember {
                mutableStateOf(!hasUsagePermission() || !hasOverlayPermission())
            }
            LaunchedEffect(Unit) {
                while (true) {
                    val missing = !hasUsagePermission() || !hasOverlayPermission()
                    needsPerm = missing
                    if (!missing) onReady()
                    delay(1000)
                }
            }
            AppListScreen(
                needsPermission = needsPerm,
                onRequestPermission = {
                    when {
                        !hasUsagePermission() -> onRequestUsage()
                        !hasOverlayPermission() -> onRequestOverlay()
                    }
                }
            )
        }
    }
}
