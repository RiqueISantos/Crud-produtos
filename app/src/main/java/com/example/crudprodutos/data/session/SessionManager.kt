package com.example.crudprodutos.data.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("crud_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "access_token"
        private const val KEY_RESTAURANTE_ID = "restaurante_id"
        private const val KEY_RESTAURANTE_NOME = "restaurante_nome"
        private const val KEY_EMAIL = "restaurante_email"
    }

    fun salvarSessao(token: String, restauranteId: Long, nome: String, email: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_RESTAURANTE_ID, restauranteId)
            .putString(KEY_RESTAURANTE_NOME, nome)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getBearerToken(): String = "Bearer ${getToken()}"

    fun getRestauranteId(): Long = prefs.getLong(KEY_RESTAURANTE_ID, -1L)

    fun getRestauranteNome(): String? = prefs.getString(KEY_RESTAURANTE_NOME, null)

    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun isLogado(): Boolean = getToken() != null && getRestauranteId() != -1L

    fun logout() {
        prefs.edit().clear().apply()
    }
}
