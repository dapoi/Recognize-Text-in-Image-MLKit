package com.dapascript.recognizetext

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.dapascript.recognizetext.databinding.ActivityResultBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        database = Firebase.firestore

        val photoResult = database.collection("photoResult")
        photoResult.document("text").get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    binding.etResult.setText(document.getString("resultText"))
                }
            }
            .addOnFailureListener { exception ->
                binding.etResult.setText(exception.toString())
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}