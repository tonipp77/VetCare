package com.vetcare.view

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vetcare.R
import com.vetcare.databinding.ActivityAnimationBinding.inflate
import com.vetcare.databinding.ActivityLoginBinding
import com.vetcare.databinding.ActivityRegistrationBinding
import com.vetcare.viewmodel.Utils

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // conectamos a una instacia de FB Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = Utils.createProgressDialog(this@RegistrationActivity)

        binding.btnRegistrationRegristration.setOnClickListener{
            val email = binding.etEmailRegistration.text.toString()
            val password = binding.etPasswordRegistration.text.toString()
            val confirmPassword = binding.etPasswordConfirmRegistration.text.toString()
            val nick_name = binding.etNickRegistration.text.toString()

            // confirmamos la validación de los campos y que el password de confirmación no está vacío
            if (Utils.isLoginValidated(email, password, this) && confirmPassword.isNotEmpty() && nick_name.isNotEmpty()) {
                // confirmamos que las password son iguales
                if(password == confirmPassword){
                    // si están bien los datos los añadimos a la db de FB Authentication y de Firestore Database
                   registerUser(email, password, nick_name)
                }else{
                    Toast.makeText(this, getString(R.string.password_notmatch), Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, getString(R.string.not_valid), Toast.LENGTH_SHORT).show()
            }
        }
        // botón para ir al Login
        binding.btnLoginRegistration.setOnClickListener {
            moveToLoginActivity()
        }
    }

    override fun onStart() {
        super.onStart()

        val currentUser = firebaseAuth.currentUser
        if(currentUser != null){
            checkUserAlreadySignedIn(currentUser)
        }
    }

    private fun checkUserAlreadySignedIn(currentUser: FirebaseUser?) {
        if(currentUser != null){
            Toast.makeText(this, getString(R.string.welcome_private_zone), Toast.LENGTH_SHORT).show()
            moveToMainActivity()
        }
    }

    private fun registerUser(
        email: String,
        password: String,
        nick_name: String
    ){
        progressDialog.show()
        // creamos el usuario en Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{

                // si se registra con exito en Authentication le añadiremos también a FirestoreDatabase
            if(it.isSuccessful){
                val user = firebaseAuth.currentUser
                // guardamos el id del usuario registrado en authentication
                val uid = user!!.uid

                val map = hashMapOf(
                    "email" to email,
                    // "password" to password,
                    "nick_name" to nick_name
                )

                val db = Firebase.firestore

                db.collection("usuario").document(uid).set(map)
                    .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, getString(R.string.exit_registration), Toast.LENGTH_SHORT).show()
                    moveToLoginActivity()
                }
                    .addOnFailureListener{
                        Toast.makeText(this, getString(R.string.error_db), Toast.LENGTH_SHORT).show()
                    }

            }else{
                progressDialog.dismiss()
                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }

    }
    // función que lleva a la RegistrationActivity
    fun moveToLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
    private fun moveToMainActivity(){
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}