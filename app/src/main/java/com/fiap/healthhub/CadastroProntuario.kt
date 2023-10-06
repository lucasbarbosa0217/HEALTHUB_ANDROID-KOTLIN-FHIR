package com.fiap.healthhub

import android.annotation.SuppressLint
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class CadastroProntuario : AppCompatActivity() {
    private lateinit var cpfPacienteEditText: EditText
    private lateinit var problemaEditText: EditText
    private lateinit var dataRegistroEditText: TextInputEditText
    private lateinit var registroDateInputLayout: TextInputLayout
    private lateinit var dataAbatimentoEditText: TextInputEditText
    private lateinit var abatimentoDateInputLayout: TextInputLayout
    private lateinit var statusSpinner: Spinner
    private lateinit var statusVerificacaoSpinner: Spinner
    private lateinit var adicionarMedicamentoButton: Button
    private lateinit var adicionarProcedimentoButton: Button
    private lateinit var cadastrarButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_prontuario)

        cpfPacienteEditText = findViewById(R.id.cpfPaciente)
        problemaEditText = findViewById(R.id.problemaEditText)
        dataRegistroEditText = findViewById(R.id.dataRegistroEditText)
        registroDateInputLayout = findViewById(R.id.registroDateInputLayoutid)
        dataAbatimentoEditText = findViewById(R.id.dataAbatimentoEditText)
        abatimentoDateInputLayout = findViewById(R.id.abatimentoDateInputLayout)
        statusSpinner = findViewById(R.id.statusSpinner)
        statusVerificacaoSpinner = findViewById(R.id.statusVerificacaoSpinner)
        adicionarMedicamentoButton = findViewById(R.id.adicionarMedicamentoButton)
        adicionarProcedimentoButton = findViewById(R.id.adicionarProcedimentoButton)
        cadastrarButton = findViewById(R.id.cadastrarButton)

        configureDatePickerForDataRegistro()
        configureDatePickerForDataAbatimento()
        configureSpinners()

        adicionarMedicamentoButton.setOnClickListener {
            // Lógica para adicionar medicamento
        }

        adicionarProcedimentoButton.setOnClickListener {
            // Lógica para adicionar procedimento
        }

        cadastrarButton.setOnClickListener {
            try {
                val patientData = createPatientDataFromForm()
                val apiClient = ApiClient(this)
                Log.w("RESPOSTA", "onCreate: "+patientData, )
                apiClient.createMedicalRegistration(patientData) { response ->
                    runOnUiThread {
                        if (response != null) {
                            Log.w("RESPOSTA", "sucesso" +response, )
                            Toast.makeText(this, "Prontuário cadastrado!", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.w("RESPOSTA", "erro" +response, )
                            Toast.makeText(this, "Erro ao criar prontuário.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao criar prontuário: $e", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createPatientDataFromForm(): JSONObject {
        val patientData = JSONObject()
        patientData.put("cpfPaciente", cpfPacienteEditText.text.toString())
        patientData.put("problema", problemaEditText.text.toString())
        patientData.put("dataRegistro", dataRegistroEditText.text.toString())
        patientData.put("dataAbatimento", dataAbatimentoEditText.text.toString()) // Adiciona a data de abatimento
        patientData.put("status", mapStatusValue(statusSpinner.selectedItem.toString()))
        patientData.put("statusVerificacao", mapStatusVerificacaoValue(statusVerificacaoSpinner.selectedItem.toString()))
        // Adicione outros campos e dados ao objeto `patientData` conforme necessário
        return patientData
    }

    private fun configureDatePickerForDataRegistro() {
        val calendarConstraints = CalendarConstraints.Builder()
            .setStart(getMillisFromYear(1900))
            .build()

        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a Data de Registro")
            .setCalendarConstraints(calendarConstraints)

        val datePicker = builder.build()

        dataRegistroEditText.setOnClickListener {
            datePicker.show(supportFragmentManager, datePicker.toString())
        }

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            val selectedDateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            dataRegistroEditText.setText(selectedDateString)
        }
    }

    private fun configureDatePickerForDataAbatimento() {
        val calendarConstraints = CalendarConstraints.Builder()
            .setStart(getMillisFromYear(1900))
            .build()

        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Selecione a Data de Abatimento")
            .setCalendarConstraints(calendarConstraints)

        val datePicker = builder.build()

        dataAbatimentoEditText.setOnClickListener {
            datePicker.show(supportFragmentManager, datePicker.toString())
        }

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            val selectedDateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
            dataAbatimentoEditText.setText(selectedDateString)
        }
    }

    private fun mapStatusValue(statusLabel: String): String {
        return when (statusLabel) {
            "Ativo" -> "active"
            "Recorrência" -> "recurrence"
            "Recaída" -> "relapse"
            "Inativo" -> "inactive"
            "Remissão" -> "remission"
            "Resolvido" -> "resolved"
            else -> ""
        }
    }

    private fun mapStatusVerificacaoValue(statusLabel: String): String {
        return when (statusLabel) {
            "Não confirmado" -> "unconfirmed"
            "Provisório" -> "provisional"
            "Diferencial" -> "differential"
            "Confirmado" -> "confirmed"
            "Refutado" -> "refuted"
            "Erro de digitação" -> "entered-in-error"
            else -> ""
        }
    }

    private fun configureSpinners() {
        val statusOptions = listOf("Ativo", "Recorrência", "Recaída", "Inativo", "Remissão", "Resolvido")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusSpinner.adapter = statusAdapter

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Ação quando um status é selecionado
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Nada selecionado
            }
        }

        val statusVerificacaoOptions = listOf("Não confirmado", "Provisório", "Diferencial", "Confirmado", "Refutado", "Erro de digitação")
        val statusVerificacaoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusVerificacaoOptions)
        statusVerificacaoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusVerificacaoSpinner.adapter = statusVerificacaoAdapter

        statusVerificacaoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Ação quando um status de verificação é selecionado
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Nada selecionado
            }
        }
    }

    private fun getMillisFromYear(year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        return calendar.timeInMillis
    }
}
