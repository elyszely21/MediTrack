package edu.cit.mabini.meditrack.session

import android.content.Context
import android.content.SharedPreferences
import edu.cit.mabini.meditrack.model.LoginResponse

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("meditrack_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "userId"
        private const val KEY_FULL_NAME = "fullName"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
    }

    fun save(response: LoginResponse) {
        prefs.edit().apply {
            putString(KEY_TOKEN, response.token)
            putLong(KEY_USER_ID, response.id)
            putString(KEY_FULL_NAME, response.fullName)
            putString(KEY_EMAIL, response.email)
            putString(KEY_ROLE, response.role)
            apply()
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, 0L)
    fun getFullName(): String? = prefs.getString(KEY_FULL_NAME, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun isLoggedIn(): Boolean = getToken() != null

    fun isSuperAdmin(): Boolean = getRole() == "SUPER_ADMIN"

    fun clear() {
        prefs.edit().clear().apply()
    }
}
