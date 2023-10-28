package com.fiap.healthhub

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class CadastrarPaciente : AppCompatActivity() {
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var cpfEditText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var birthDateEditText: EditText
    private lateinit var contactNumberEditText: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastrar_paciente)

        firstNameEditText = findViewById(R.id.firstName)
        lastNameEditText = findViewById(R.id.lastName)
        cpfEditText = findViewById(R.id.cpf)
        genderSpinner = findViewById(R.id.genderSpinner)
        contactNumberEditText = findViewById(R.id.contactNumber)
        submitButton = findViewById(R.id.submitButton)
        birthDateEditText = findViewById(R.id.registroDateEditText)
        configureDatePicker()

        val genderOptions = listOf("male", "female", "other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter


        fun createPatientObject(): JSONObject {
            val patient = JSONObject()

            patient.put("resourceType", "Patient")

            val meta = JSONObject()
            val profile = JSONArray()
            profile.put("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient")
            meta.put("profile", profile)
            patient.put("meta", meta)

            val identifiers = JSONArray()
            val identifier = JSONObject()
            identifier.put("system", "cpf")
            identifier.put("value", cpfEditText.text.toString())
            identifiers.put(identifier)
            patient.put("identifier", identifiers)

            val names = JSONArray()
            val name = JSONObject()
            name.put("use", "official")
            name.put("text", "${firstNameEditText.text} ${lastNameEditText.text}")
            name.put("family", lastNameEditText.text.toString())
            val givenArray = JSONArray()
            givenArray.put(firstNameEditText.text.toString())
            name.put("given", givenArray)
            names.put(name)
            patient.put("name", names)

            val telecom = JSONArray()
            val contactNumber = JSONObject()
            contactNumber.put("system", "phone")
            contactNumber.put("value", contactNumberEditText.text.toString())
            contactNumber.put("use", "mobile")
            telecom.put(contactNumber)
            patient.put("telecom", telecom)

            patient.put("gender", genderOptions[genderSpinner.selectedItemPosition])

            val birthDateStr = birthDateEditText.text.toString()
            val sdfInput = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val sdfOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdfInput.parse(birthDateStr)
            val birthDateFormatted = sdfOutput.format(date)
            patient.put("birthDate", birthDateFormatted)

            return patient
        }



        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val selectedGender = genderOptions[position]
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        submitButton.setOnClickListener {
            try {

                val patientJson = createPatientObject()

                ApiClient(this).createPatient(patientJson) { response ->
                    if (response != null) {
                        Log.w("RESPOSTA", "Sucesso: $response")
                        Toast.makeText(
                            this,
                            "Paciente Criado",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish();
                    } else {
                        Log.e("RESPOSTA", "Erro: Resposta nula")
                        Toast.makeText(
                            this,
                            "Erro ao criar paciente, resposta nula",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CadastrarPaciente", "Erro ao criar JSON: $e")
                Toast.makeText(this, "Erro ao criar paciente", Toast.LENGTH_SHORT).show()
            }
        }
    }

        fun getMillisFromYear(year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        return calendar.timeInMillis
    }

    private fun configureDatePicker() {
        val calendarConstraints = CalendarConstraints.Builder()
            .setStart(getMillisFromYear(1900))
            .build()

        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a Data de Nascimento")
            .setCalendarConstraints(calendarConstraints)

        val datePicker = builder.build()

        birthDateEditText.setOnClickListener {
            datePicker.show(supportFragmentManager, datePicker.toString())
        }

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            val selectedDateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            birthDateEditText.setText(selectedDateString)
        }
    }



}
