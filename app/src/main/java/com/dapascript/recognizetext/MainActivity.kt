package com.dapascript.recognizetext

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dapascript.recognizetext.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseFirestore

    private var getFile: File? = null
    private var bitmap: Bitmap? = null
    private var resultText = ""

    private val launchIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_CODE) {
            val myFile = result.data?.getSerializableExtra("picture") as File
            val isBackCamera = result.data?.getBooleanExtra("isBackCamera", true) as Boolean

            getFile = myFile
            bitmap = rotateBitmap(
                BitmapFactory.decodeFile(myFile.absolutePath),
                isBackCamera
            )

            binding.apply {
                tvEmpty.visibility = View.GONE
                ivPhoto.visibility = View.VISIBLE
                ivPhoto.setImageBitmap(bitmap)
                btnRecognize.isEnabled = true
            }

            val textRecognized = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            textRecognized.process(InputImage.fromBitmap(bitmap!!, 0))
                .addOnSuccessListener { data ->
                    resultText = data.text
                    if (resultText.isNotEmpty()) {
                        val photoResult = hashMapOf(
                            "resultText" to resultText,
                        )
                        database.collection("photoResult").document("text").set(photoResult)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Berhasil menyimpan hasil",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Gagal menyimpan hasil",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        binding.btnRecognize.isEnabled = false
                        Toast.makeText(
                            this@MainActivity,
                            "Tidak ada teks yang terdeteksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Firebase.firestore

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_PERMISSIONS_CODE
            )
        }

        binding.apply {
            btnCamera.setOnClickListener {
                launchIntentCamera.launch(Intent(this@MainActivity, CameraActivity::class.java))
            }

            btnRecognize.setOnClickListener {
                startActivity(Intent(this@MainActivity, ResultActivity::class.java))
            }
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
        val matrix = Matrix()
        return if (isBackCamera) {
            matrix.postRotate(90f)
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        } else {
            matrix.postRotate(-90f)
            matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
            Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )
        }
    }

    companion object {
        const val RESULT_CODE = 200

        private const val REQUEST_PERMISSIONS_CODE = 10
        private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)
    }
}