package com.example.applock.service

object UnlockTracker {

    @Volatile
    private var authorized: String? = null

    fun authorize(packageName: String) {
        authorized = packageName
    }

    fun isAuthorized(packageName: String): Boolean =
        authorized == packageName

    fun revoke() {
        authorized = null
    }
}
