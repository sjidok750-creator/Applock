package com.example.applock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.applock.MainActivity
import com.example.applock.R
import com.example.applock.data.LockedAppsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppLockService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var lockedStore: LockedAppsStore
    private lateinit var usageStats: UsageStatsManager

    private var lastForegroundPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        lockedStore = LockedAppsStore(this)
        usageStats = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIF_ID, buildNotification(), type)
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun startMonitoring() {
        scope.launch {
            while (isActive) {
                runCatching { tick() }
                delay(POLL_MS)
            }
        }
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        val events = usageStats.queryEvents(now - QUERY_WINDOW_MS, now)
        val event = UsageEvents.Event()
        var latestForeground: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                latestForeground = event.packageName
            } else if (event.eventType == UsageEvents.Event.ACTIVITY_PAUSED ||
                event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND
            ) {
                if (lockedStore.isLocked(event.packageName)) {
                    UnlockTracker.clear(event.packageName)
                }
            }
        }
        val current = latestForeground ?: return
        if (current == lastForegroundPackage) return
        lastForegroundPackage = current

        if (current == packageName) return
        if (!lockedStore.isLocked(current)) return
        if (UnlockTracker.isWithinGrace(current)) return

        launchLockScreen(current)
    }

    private fun launchLockScreen(targetPackage: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
            putExtra(LockScreenActivity.EXTRA_PACKAGE, targetPackage)
        }
        startActivity(intent)
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setSmallIcon(R.drawable.ic_lock)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIF_ID = 1001
        private const val CHANNEL_ID = "applock_service"
        private const val POLL_MS = 400L
        private const val QUERY_WINDOW_MS = 2_000L

        fun start(context: Context) {
            val intent = Intent(context, AppLockService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
