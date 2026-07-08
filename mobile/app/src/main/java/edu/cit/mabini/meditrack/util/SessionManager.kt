package edu.cit.mabini.meditrack.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("meditrack_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }

    fun saveSession(token: String, fullName: String, email: String, role: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_EMAIL, email)
            putString(KEY_ROLE, role)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
