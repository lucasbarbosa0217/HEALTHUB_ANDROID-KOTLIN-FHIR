package com.fiap.healthhub

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fiap.healthhub.Entry

class DetalhesEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes_entry)

        val entry = intent.getParcelableExtra<Entry>("entry")

        val clinicalStatusTextView: TextView = findViewById(R.id.clinicalStatusTextView)
        val recordedDateTextView: TextView = findViewById(R.id.recordedDateTextView)
        val noteTextTextView: TextView = findViewById(R.id.noteTextTextView)
        val abatDateTextView: TextView = findViewById(R.id.abatDateTextView)
        val verificationCodeTextView: TextView = findViewById(R.id.verificationCodeTextView)

        if (entry != null) {
            clinicalStatusTextView.text = "Clinical Status: ${entry.clinicalStatusCode}"
            recordedDateTextView.text = "Recorded Date: ${entry.recordedDate}"
            noteTextTextView.text = "${entry.noteText}"
            abatDateTextView.text = "Abatement Date: ${entry.abatDate}"
            verificationCodeTextView.text = "Verification Code: ${entry.verificationCode}"
        }
    }
}
