package com.example.applock.data

import android.content.Context

class LockedAppsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    fun isLocked(packageName: String): Boolean =
        lockedPackages().contains(packageName)

    fun setLocked(packageName: String, locked: Boolean) {
        val updated = lockedPackages().toMutableSet().apply {
            if (locked) add(packageName) else remove(packageName)
        }
        prefs.edit().putStringSet(KEY_PACKAGES, updated).apply()
    }

    fun lockedPackages(): Set<String> =
        prefs.getStringSet(KEY_PACKAGES, emptySet())!!.toSet()

    companion object {
        private const val FILE_NAME = "applock_locked_apps"
        private const val KEY_PACKAGES = "packages"
    }
}
