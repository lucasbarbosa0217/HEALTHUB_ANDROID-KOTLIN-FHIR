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
        // Você pode construir o objeto JSON de acordo com a estrutura do código React
        val patientObject = JSONObject()
        patientObject.put("resourceType", "Patient")

        val meta = JSONObject()
        val profile = JSONArray()
        profile.put("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient")
        meta.put("profile", profile)

        val identifiers = JSONArray()
        val identifier = JSONObject()
        identifier.put("system", "cpf")
        identifier.put("value", patient.optString("value"))
        Log.w("reposta", "createPatient: "+ patient.optString("value"), )
        identifiers.put(identifier)

        val names = JSONArray()
        val name = JSONObject()
        name.put("use", "official")
        name.put("text", "${patient.optString("firstName", "")} ${patient.optString("lastName", "")}")
        name.put("family", patient.optString("family", ""))
        val given = JSONArray()
        given.put(patient.optString("firstName", ""))
        name.put("given", given)
        names.put(name)

        val telecom = JSONArray()
        val contactNumber = JSONObject()
        contactNumber.put("system", "phone")
        contactNumber.put("value", patient.optString("value", ""))
        contactNumber.put("use", "mobile")
        telecom.put(contactNumber)

        patientObject.put("meta", meta)
        patientObject.put("identifier", identifiers)
        patientObject.put("name", names)
        patientObject.put("telecom", telecom)
        patientObject.put("gender", patient.optString("gender", ""))
        patientObject.put("birthDate", patient.optString("birthDate", ""))

        // Agora você pode usar o objeto `patientObject` no seu pedido
        val tipoDoObjeto = patientObject.javaClass
        Log.w("RESPOSTA", "createPatient: "+tipoDoObjeto+"\n"+patientObject, )
        makeApiCall("Patient", "POST", patientObject, callback)
    }

    fun getPatientByCpf(cpf: String, callback: (JSONObject?) -> Unit) {
        makeApiCall("Patient?identifier=cpf|$cpf", "GET", null, callback)
    }
    fun createMedicalRegistration(data: JSONObject, callback: (JSONObject?) -> Unit) {
        val medicalRecordObject = JSONObject()
        medicalRecordObject.put("resourceType", "Bundle")
        medicalRecordObject.put("type", "document")

        val identifierObject = JSONObject()
        identifierObject.put("system", "cpf")
        identifierObject.put("value", data.optString("cpfPaciente", "X")) // Preencha com 'X' se estiver vazio

        medicalRecordObject.put("identifier", identifierObject)

        val entryArray = JSONArray()

        val conditionObject = JSONObject()
        conditionObject.put("resourceType", "Condition")

        val clinicalStatusObject = JSONObject()
        clinicalStatusObject.put("coding", JSONArray().put(JSONObject().apply {
            put("system", "http://hl7.org/fhir/ValueSet/condition-clinical")
            put("code", data.optString("status", "X")) // Preencha com 'X' se estiver vazio
        }))
        conditionObject.put("clinicalStatus", clinicalStatusObject)

        val verificationStatusObject = JSONObject()
        verificationStatusObject.put("coding", JSONArray().put(JSONObject().apply {
            put("system", "http://hl7.org/fhir/ValueSet/condition-ver-status")
            put("code", data.optString("statusVerificacao", "X")) // Preencha com 'X' se estiver vazio
        }))
        conditionObject.put("verificationStatus", verificationStatusObject)

        conditionObject.put("recordedDate", data.optString("dataRegistro", "X")) // Preencha com 'X' se estiver vazio
        conditionObject.put("abatementDateTime", data.optString("dataAbatimento", "X")) // Preencha com 'X' se estiver vazio

        val noteArray = JSONArray()
        noteArray.put(JSONObject().apply {
            put("text", data.optString("problema", "X")) // Preencha com 'X' se estiver vazio
        })
        conditionObject.put("note", noteArray)

        val conditionEntry = JSONObject()
        conditionEntry.put("resource", conditionObject)
        entryArray.put(conditionEntry)

        medicalRecordObject.put("entry", entryArray)

        val tipoDoObjeto = medicalRecordObject.javaClass
        Log.w("RESPOSTA", "createPatient: "+tipoDoObjeto+"\n"+medicalRecordObject, )
        makeApiCall("Bundle", "POST", medicalRecordObject, callback)
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
