package com.example.shoppingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shoppingapp.databinding.ActivityHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.StringBuilder

class HomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityHomeBinding

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDataFromReg()

        binding.saveBtn.setOnClickListener {
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val age = binding.etAge.text.toString().toInt()

            val person = Person(firstName, lastName, age)
            savePerson(person)
        }

        binding.retrieveBtn.setOnClickListener {
            retrievePerson()
        }

        binding.updateBtn.setOnClickListener {
            val intent = Intent(this, UpdateActivity::class.java)
            startActivity(intent)
        }

        binding.uploadBtn.setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }

    }

    private fun getDataFromReg() {
        val uriImage = intent.getStringExtra("IMAGE_KEY")?.toUri()
        val userName = intent.getStringExtra("NAME_KEY") ?: "User"

        if (uriImage != null) {
            binding.profileImage.setImageURI(uriImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.ic_launcher_foreground)
        }

        binding.userNameTxt.text = userName
    }

    private fun savePerson(person: Person) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("persons").add(person)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, "Successfully Upload Data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    } // End of savePerson() FuNction

    private fun retrievePerson() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val fromAge = binding.etFromAge.text.toString().toInt()
                val toAge = binding.etToAge.text.toString().toInt()

                val querySnapshot = db.collection("persons")
                    .whereLessThan("age", toAge)
                    .whereGreaterThan("age", fromAge)
                    .get().await()

                val strBuilder = StringBuilder()

                for (document in querySnapshot.documents) {
                    val person = document.toObject<Person>()
                    strBuilder.append("$person \n")
                }
                withContext(Dispatchers.Main) {
                    binding.retrieveTxt.text = strBuilder
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    } // End of retrievePerson() FuNction

    private fun updateDataAuto() {
        db.collection("persons").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            querySnapshot?.let {
                val strBuilder = StringBuilder()

                for (document in it.documents) {
                    val person = document.toObject(Person::class.java)
                    strBuilder.append("$person \n")
                }
                binding.retrieveTxt.text = strBuilder
            }
        }
    } // End of updateDataAuto() Function

}