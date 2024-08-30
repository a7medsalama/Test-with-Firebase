package com.example.shoppingapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.shoppingapp.databinding.ActivityUploadBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

private const val REQUEST_CODE_TO_UPLOAD = 1

class UploadActivity : AppCompatActivity() {

    var currFile: Uri? = null
    lateinit var binding: ActivityUploadBinding

    private val storageRef = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listImagesInRecycler()

        binding.imageToUpload.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {intent ->
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_CODE_TO_UPLOAD)
            }
        }

        binding.uploadBtn.setOnClickListener {
            currFile?.let {
                uploadImageToStorage("image_${System.currentTimeMillis()}")
            } ?: Toast.makeText(this@UploadActivity, "No Images Selected", Toast.LENGTH_SHORT).show()

        }

        binding.downloadBtn.setOnClickListener {
            binding.imageName.text?.let {
                downloadImageFromStorage(it.toString())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TO_UPLOAD && resultCode == RESULT_OK) {
            data?.data?.let {
                currFile = it
                binding.imageToUpload.setImageURI(it)
            }
        }
    }

    private fun uploadImageToStorage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            currFile?.let {
                storageRef.child("images/$fileName").putFile(it).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UploadActivity, "Uploaded Image Successfully", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UploadActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadImageFromStorage(fileName: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val imageRef = storageRef.child("images/$fileName")
            val maxDownloadSize = 5L * 1024 *1024
            val bytes = imageRef.getBytes(maxDownloadSize).await()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            withContext(Dispatchers.Main) {
                binding.imageToUpload.setImageBitmap(bitmap)
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UploadActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun listImagesInRecycler() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val imageRef = storageRef.child("images/").listAll().await()
            val imageUrls = mutableListOf<String>()

            for (image in imageRef.items) {
                val url = image.downloadUrl.await()
                imageUrls.add(url.toString())
            }

            withContext(Dispatchers.Main) {
                val adapter = ImageAdapter(imageUrls)
                binding.recyclerView.apply {
                    this.adapter = adapter
                    this.layoutManager = LinearLayoutManager(this@UploadActivity)
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UploadActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }


}