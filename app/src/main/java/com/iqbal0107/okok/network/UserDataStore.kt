package com.iqbal0107.okok.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.iqbal0107.okok.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_preference")

class UserDataStore(private val context: Context) {

    companion object {
        private val TOKEN = stringPreferencesKey("token")
        private val USER_NAME = stringPreferencesKey("name")
        private val USER_EMAIL = stringPreferencesKey("email")
        private val USER_PHOTO = stringPreferencesKey("photoUrl")
    }

    val tokenFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TOKEN] ?: ""
    }

    val userFlow: Flow<User> = context.dataStore.data.map { preferences ->
        User(
            name = preferences[USER_NAME] ?: "",
            email = preferences[USER_EMAIL] ?: "",
            photo_url = preferences[USER_PHOTO] ?: ""
        )
    }

    suspend fun saveSession(token: String, user: User) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN] = token
            preferences[USER_NAME] = user.name
            preferences[USER_EMAIL] = user.email
            preferences[USER_PHOTO] = user.photo_url
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[TOKEN] = ""
            preferences[USER_NAME] = ""
            preferences[USER_EMAIL] = ""
            preferences[USER_PHOTO] = ""
        }
    }
}