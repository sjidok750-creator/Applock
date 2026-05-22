package com.example.applock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.applock.data.PinStore

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED &&
            PinStore(context).hasPin()
        ) {
            AppLockService.start(context)
        }
    }
}
