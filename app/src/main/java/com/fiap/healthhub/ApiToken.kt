package com.fiap.healthhub

import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ApiToken(private val context: Context) {

    private val tokenUrl = "https://heathhub-auth.vercel.app/token"
    private val authTokenKey = "auth_token"
    private val authTokenExpKey = "auth_token_exp"
    private val sharedPreferences = context.getSharedPreferences("token_prefs", Context.MODE_PRIVATE)

    fun requestToken(callback: (String?) -> Unit) {
        val savedToken = sharedPreferences.getString(authTokenKey, null)
        val expiresOn = sharedPreferences.getLong(authTokenExpKey, 0)
        val currentTimeInMillis = System.currentTimeMillis()

        if (savedToken != null && currentTimeInMillis < expiresOn) {
            // O token salvo ainda é válido, então o retornamos
            Log.w("TOKEN RESPOSTA", "Usando token salvo: $savedToken")
            callback(savedToken)
        } else {
            // O token salvo é nulo ou expirado, fazemos uma nova solicitação de token
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(tokenUrl)
                .get()
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val json = JSONObject(responseBody)
                            // Extrair o token de acesso do JSON
                            val accessToken = json.optString("access_token", null)
                            val expiresIn = json.optLong("expires_in", 0)

                            if (accessToken != null && expiresIn > 0) {
                                // Salvar o novo token e tempo de expiração no SharedPreferences
                                val editor = sharedPreferences.edit()
                                editor.putString(authTokenKey, accessToken)
                                editor.putLong(authTokenExpKey, currentTimeInMillis + expiresIn * 1000)
                                editor.apply()

                                Log.w("TOKEN RESPOSTA", "Gerado novo token: $accessToken")
                                // Chama o callback com o novo token
                                callback(accessToken)
                            } else {
                                callback(null)
                            }
                        } else {
                            callback(null)
                        }
                    } else {
                        callback(null)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback(null)
                }
            })
        }
    }
}
