package com.vetcare.view

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vetcare.R
import com.vetcare.databinding.ActivityLoginBinding
import com.vetcare.viewmodel.Utils
import kotlinx.coroutines.awaitAll

class LoginActivity : AppCompatActivity() {
    // variable para usar ViewBinding
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = Utils.createProgressDialog(this@LoginActivity)

        // abrimos la instancia de la DB de FB authentication
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnRegisterLogin.setOnClickListener {
            moveToRegistrationActivity()
        }

        binding.btnLoginLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString()
            val password = binding.etPasswordLogin.text.toString()

            // confirmamos password e email no están vacios
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // si no están vacios intentamos acceder con el email y password facilitados


                signIn(email, password)
            }else{
                Toast.makeText(this, getString(R.string.not_valid), Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnForgotLogin.setOnClickListener{
            moveToResetPasswordActivity()
        }

    }

    private fun signIn(email: String, password: String) {

        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                // si se registra con exito se lo informamos y le movemos a inicio de sesión
                if(it.isSuccessful){
                    progressDialog.dismiss()

                    Toast.makeText(this, getString(R.string.ok_login), Toast.LENGTH_SHORT).show()

                    moveToMainActivity()
                }else{

                    progressDialog.dismiss()

                    Toast.makeText(this, getString(R.string.error_data_not_signup), Toast.LENGTH_SHORT).show()
                }
            }

    }

    override fun onStart() {
        super.onStart()
        
        val currentUser = firebaseAuth.currentUser

        checkUserAlreadySignedIn(currentUser)
    }

    private fun checkUserAlreadySignedIn(currentUser: FirebaseUser?) {

        if(currentUser != null){
            Toast.makeText(this, getString(R.string.welcome_private_zone), Toast.LENGTH_SHORT).show()
            moveToMainActivity()
        }

    }

    private fun moveToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToRegistrationActivity() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToResetPasswordActivity() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        startActivity(intent)
    }

}