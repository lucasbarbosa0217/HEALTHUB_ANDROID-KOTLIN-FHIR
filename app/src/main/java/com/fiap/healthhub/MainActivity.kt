package com.fiap.healthhub

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import kotlin.reflect.typeOf

class MainActivity : AppCompatActivity(), OnItemClickListener { // Implemente a interface OnItemClickListener

    private lateinit var cpfEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var apiClient: ApiClient
    private lateinit var entryAdapter: EntryAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiClient = ApiClient(this)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val entryList: MutableList<Entry> = mutableListOf()
        entryAdapter = EntryAdapter(entryList, this) // Passe a instância da atividade como ouvinte de cliques
        val fab: FloatingActionButton = findViewById(R.id.fab)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = entryAdapter
        cpfEditText = findViewById(R.id.cpfEditText)
        searchButton = findViewById(R.id.sendButton)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        fab.setOnClickListener { view ->
            val popupMenu = PopupMenu(this@MainActivity, view)
            popupMenu.menuInflater.inflate(R.menu.fab_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
                when (item?.itemId) {
                    R.id.menu_criar_paciente -> {
                        val intent = Intent(this@MainActivity, CadastrarPaciente::class.java)
                        startActivity(intent)

                        true
                    }
                    R.id.menu_criar_prontuario -> {
                        val intent = Intent(this@MainActivity, CadastroProntuario::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.show()
        }

        cpfEditText.filters = arrayOf(InputFilter.LengthFilter(11))

        searchButton.setOnClickListener {
            val cpf = cpfEditText.text.toString()
            if (cpf.length == 11) {
                entryAdapter.notifyDataSetChanged()
                recyclerView.visibility = View.GONE
                loadingProgressBar.visibility = View.VISIBLE

                apiClient.getMedicalRegistrationByCpf(cpf) { response ->
                    runOnUiThread {
                        loadingProgressBar.visibility = View.GONE

                        if (response != null) {
                            Log.w("RESPOSTA", response.toString())
                            val parsedEntryList: List<Entry> = parseJsonToEntryList(response)
                            entryList.addAll(parsedEntryList)
                            entryAdapter.notifyDataSetChanged()
                            recyclerView.visibility = View.VISIBLE
                        } else {

                            Log.w("RESPOSTA", "onCreate deu errado: " + response)
                        }
                    }
                }
            }
        }
    }

    fun parseJsonToEntryList(json: JSONObject): List<Entry> {
        val entryList = mutableListOf<Entry>()

        if (json.has("entry")) {
            val entryArray = json.getJSONArray("entry")
            for (i in 0 until entryArray.length()) {
                val entryObj = entryArray.getJSONObject(i)

                val clinicalStatus = entryObj
                    .getJSONObject("resource")
                    .getJSONArray("entry")
                    .getJSONObject(0)
                    .getJSONObject("resource")
                    .getJSONObject("clinicalStatus")
                    .getJSONArray("coding")
                    .getJSONObject(0)
                    .getString("code")

                val recordedDate = entryObj
                    .getJSONObject("resource")
                    .getJSONArray("entry")
                    .getJSONObject(0)
                    .getJSONObject("resource")
                    .getString("recordedDate")

                val noteText = entryObj
                    .getJSONObject("resource")
                    .getJSONArray("entry")
                    .getJSONObject(0)
                    .getJSONObject("resource")
                    .getJSONArray("note")
                    .getJSONObject(0)
                    .getString("text")

                val abatDate = entryObj
                    .getJSONObject("resource")
                    .getJSONArray("entry")
                    .getJSONObject(0)
                    .getJSONObject("resource")
                    .getString("abatementDateTime")

                val verificationCode = entryObj
                    .getJSONObject("resource")
                    .getJSONArray("entry")
                    .getJSONObject(0)
                    .getJSONObject("resource")
                    .getJSONObject("verificationStatus")
                    .getJSONArray("coding")
                    .getJSONObject(0)
                    .getString("code")

                val entry = Entry(clinicalStatus, recordedDate, noteText, abatDate, verificationCode)
                if (clinicalStatus != null && recordedDate != null && noteText != null) {
                    entryList.add(entry)
                }
            }
        }

        return entryList
    }

    // Implemente o método onItemClick da interface OnItemClickListener
    override fun onItemClick(entry: Entry) {
        val intent = Intent(this, DetalhesEntryActivity::class.java)
        intent.putExtra("entry", entry)
        startActivity(intent)
    }
}
