package com.example.shoppingapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.shoppingapp.databinding.ActivityUpdateBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateBinding

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.updateBtn.setOnClickListener {
            val oldPerson = getOldPerson()
            val newPersonMap = getNewPersonMap()

            updatePerson(oldPerson, newPersonMap)
        }

        binding.deleteBtn.setOnClickListener {
            val person = getOldPerson()
            deletePerson(person)
        }

    }

// 3 fun -> 1 - getOldPerson ,2 - getNewPersonMap, 3 - UpdatePerson

    private fun getOldPerson() :Person {
        val firstName = binding.oldFirstName.text.toString()
        val lastName = binding.oldLastName.text.toString()
        val age = binding.oldAge.text.toString().toInt()

        return Person(firstName, lastName, age)
    } ////////////////

    private fun getNewPersonMap() :Map<String, Any> {
        val firstName = binding.newFirstName.text.toString()
        val lastName = binding.newLastName.text.toString()
        val age = binding.newAge.text.toString()

        val map = mutableMapOf<String, Any>()

        if (firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }
        if (lastName.isNotEmpty()) {
            map["lastName"] = lastName
        }
        if (age.isNotEmpty()) {
            age.toInt()
            map["age"] = age
        }
        return map
    } ////////////////

    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = db.collection("persons")
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    db.collection("persons").document(document.id).set(newPersonMap, SetOptions.merge())

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UpdateActivity, "Updated Successfully ..", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UpdateActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateActivity, "No Matched data with Person",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = db.collection("persons")
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()

        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    db.collection("persons").document(document.id).delete().await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UpdateActivity, "Deleted Successfully ..", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UpdateActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@UpdateActivity, "No Matched data with Person",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }


}