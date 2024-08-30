package com.example.shoppingapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shoppingapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private val REQUEST_CODE_PICK_IMAGE = 0
    private var currFile: Uri? = null
    lateinit var binding: ActivityRegisterBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.ivProfile.setOnClickListener {
            updateProfile()

            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                startActivityForResult(it, REQUEST_CODE_PICK_IMAGE)
            }

        }

        binding.tvLoginTxt.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.registerButton.setOnClickListener {
            registerUser()

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {
            data?.data.let {
                currFile = it
                binding.ivProfile.setImageURI(it)
            }
        }
    }

//    override fun onStart() {
//        super.onStart()
//
//        if (auth.currentUser != null) {
//            val intent = Intent(this, LoginActivity::class.java)
//            startActivity(intent)
//        }
//    }

    private fun registerUser() {
        val email = binding.etEmailReg.text.toString()
        val pass = binding.etPassReg.text.toString()

        if (email.isNotEmpty() && pass.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    auth.createUserWithEmailAndPassword(email, pass).await()
                    withContext(Dispatchers.Main) {
                        getDataToHome(email)

                        Toast.makeText(this@RegisterActivity, "create new user : Done",
                            Toast.LENGTH_SHORT).show()

                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }
    } //end of registerUser() function

    private fun getDataToHome(name: String) {
//        var user = auth.currentUser

        val myName = binding.etUserName.text.toString()

        Intent(this@RegisterActivity, HomeActivity::class.java).also {
            it.putExtra("IMAGE_KEY", currFile.toString())
            it.putExtra("NAME_KEY", myName)
            startActivity(it)
        }
    }

    private fun updateProfile() {
        auth.currentUser?.let {user ->
            val userName = binding.etUserName.text.toString()

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userName)
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    user.updateProfile(profileUpdates).await()

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

    } // end of updateProfile() function

}