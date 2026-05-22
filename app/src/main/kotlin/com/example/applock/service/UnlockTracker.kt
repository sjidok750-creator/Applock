package com.example.applock.service

object UnlockTracker {

    private const val GRACE_MS = 5 * 60 * 1000L
    private val unlockedAt = mutableMapOf<String, Long>()

    @Synchronized
    fun markUnlocked(packageName: String) {
        unlockedAt[packageName] = System.currentTimeMillis()
    }

    @Synchronized
    fun isWithinGrace(packageName: String): Boolean {
        val ts = unlockedAt[packageName] ?: return false
        return System.currentTimeMillis() - ts < GRACE_MS
    }

    @Synchronized
    fun clear(packageName: String) {
        unlockedAt.remove(packageName)
    }
}
