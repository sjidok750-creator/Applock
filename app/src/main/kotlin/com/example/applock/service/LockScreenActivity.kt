package com.example.applock.service

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import com.example.applock.data.PinStore
import com.example.applock.ui.PinEntryScreen
import com.example.applock.ui.theme.ApplockTheme

class LockScreenActivity : ComponentActivity() {

    private lateinit var pinStore: PinStore
    private var targetPackage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pinStore = PinStore(applicationContext)
        targetPackage = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
                finish()
            }
        })

        setContent {
            ApplockTheme {
                PinEntryScreen(
                    title = "잠금 해제",
                    subtitle = "PIN을 입력하세요",
                    onPinEntered = { pin ->
                        if (pinStore.verify(pin)) {
                            UnlockTracker.markUnlocked(targetPackage)
                            finish()
                            true
                        } else {
                            false
                        }
                    }
                )
            }
        }
    }

    private fun goHome() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(home)
    }

    companion object {
        const val EXTRA_PACKAGE = "target_package"
    }
}
