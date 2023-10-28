package com.fiap.healthhub

import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import okhttp3.Headers.Companion.toHeaders
import org.json.JSONArray
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class ApiClient(private val context: Context) {

    private val baseUrl = "https://fiaphealthhub-healthhub.fhir.azurehealthcareapis.com"
    private val defaultHeaders = mapOf(
        "Accept" to "application/json",
        "Content-Type" to "application/json",
        "Access-Control-Allow-Methods" to "*",
        "Access-Control-Allow-Origin" to "*",
        "Access-Control-Allow-Headers" to "*"
    )

    private val httpClient = OkHttpClient.Builder()
        .build()

    fun metadata(callback: (JSONObject?) -> Unit) {
        makeApiCall("metadata", "GET", null, callback)
    }

    fun listBundles(callback: (JSONObject?) -> Unit) {
        makeApiCall("Bundle", "GET", null, callback)
    }

    fun createPatient(patient: JSONObject, callback: (JSONObject?) -> Unit) {

        makeApiCall("Patient", "POST", patient, callback)
    }

    fun getPatientByCpf(cpf: String, callback: (JSONObject?) -> Unit) {
        makeApiCall("Patient?identifier=cpf|$cpf", "GET", null, callback)
    }
    fun createMedicalRegistration(data: JSONObject, callback: (JSONObject?) -> Unit) {

        makeApiCall("Bundle", "POST", data, callback)
    }


    // Função para formatar a data no formato "yyyy-MM-dd"
    private fun formatDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }



    fun getMedicalRegistrationByCpf(cpf: String, callback: (JSONObject?) -> Unit) {
        Log.w("RESPOSTA", "chegou aqui")
        makeApiCall("Bundle?identifier=cpf|$cpf", "GET", null, callback)
    }

    private fun makeApiCall(path: String, method: String, requestBody: JSONObject?, callback: (JSONObject?) -> Unit) {
        // Chama o ApiToken para obter um novo token
        val apiToken = ApiToken(context)
        apiToken.requestToken { authToken ->
            if (authToken != null) {
                val requestBuilder = Request.Builder()
                    .url("$baseUrl/$path")
                    .headers(defaultHeaders.toHeaders())
                    .addHeader("Authorization", "Bearer $authToken")

                when (method) {
                    "GET" -> requestBuilder.get()
                    "POST" -> {
                        if (requestBody != null) {
                            val jsonMediaType = "application/json".toMediaType()
                            val jsonRequestBody = requestBody.toString().toRequestBody(jsonMediaType)
                            requestBuilder.post(jsonRequestBody)
                        }
                    }
                }

                val request = requestBuilder.build()
                httpClient.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            if (responseBody != null) {
                                val json = JSONObject(responseBody)

                                callback(json)
                            } else {
                                Log.w("RESPOSTA", "erro" +response, )

                                callback(responseBody)
                            }
                        } else {
                            Log.w("RESPOSTA", "erro" +response, )

                            callback(null)
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        callback(null)
                    }
                })
            } else {
                // Lógica para lidar com a ausência de token
                callback(null)
            }
        }
    }
}
