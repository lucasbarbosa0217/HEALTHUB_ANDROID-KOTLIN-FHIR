package com.fiap.healthhub

import org.json.JSONArray
import org.json.JSONObject

class Bundle(
    val cpf: String,
    val condition: Condition,
    val procedures: List<Procedure>,
    val medications: List<Medication>
) {
    data class Condition(
        val clinicalStatus: String,
        val verificationStatus: String,
        val recordedDate: String,
        val abatementDateTime: String,
        val note: String
    )

    data class Procedure(
        val status: String,
        val category: String,
        val note: String,
        val performedDateTime: String
    )

    data class Medication(
        val status: String,
        val note: String,
        val effectiveDateTime: String,
        val dosage: String
    )

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("cpf", cpf)

        val conditionJson = JSONObject()
        conditionJson.put("clinicalStatus", condition.clinicalStatus)
        conditionJson.put("verificationStatus", condition.verificationStatus)
        conditionJson.put("recordedDate", condition.recordedDate)
        conditionJson.put("abatementDateTime", condition.abatementDateTime)
        conditionJson.put("note", condition.note)

        json.put("condition", conditionJson)

        val proceduresArray = JSONArray()
        procedures.forEach { procedure ->
            val procedureJson = JSONObject()
            procedureJson.put("status", procedure.status)
            procedureJson.put("category", procedure.category)
            procedureJson.put("note", procedure.note)
            procedureJson.put("performedDateTime", procedure.performedDateTime)
            proceduresArray.put(procedureJson)
        }
        json.put("procedures", proceduresArray)

        val medicationsArray = JSONArray()
        medications.forEach { medication ->
            val medicationJson = JSONObject()
            medicationJson.put("status", medication.status)
            medicationJson.put("note", medication.note)
            medicationJson.put("effectiveDateTime", medication.effectiveDateTime)
            medicationJson.put("dosage", medication.dosage)
            medicationsArray.put(medicationJson)
        }
        json.put("medications", medicationsArray)

        return json
    }
}
