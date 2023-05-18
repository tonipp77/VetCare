package com.vetcare.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.vetcare.R
import com.vetcare.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnResetReset.setOnClickListener {
            val email = binding.etEmailReset.text.toString()

            if(email.isNotEmpty()){
                // si el email se encuentra en la db FB authentication
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(this, getString(R.string.send_email), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, getString(R.string.password_error_noregister), Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText(this, getString(R.string.password_error), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLoginReset.setOnClickListener {
            moveToLoginActivity()
        }

    }

    fun moveToLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}