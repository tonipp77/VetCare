package com.vetcare.viewmodel

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseUser
import com.vetcare.R
import com.vetcare.view.ResetPasswordActivity

public class Utils {
    // para poder acceder desde fuera de la clase a los métodos
    companion object {
        // método que valida que un email tiene aspecto de tal
        // primero viendo que no está vacío con isEmpty, y luego
        // comprobando el patrón de Patterns
        fun isEmail(email: String?): Boolean =
            !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()

        // método que valida que el password tenga mínimo 6 caracteres
        // mínimo una letra mayúscula, y mínimo un caracter especial
        fun isGoodPassword(password: String): Boolean {
            var valid = true
            // Patrón reGex que controla que se tenga al menos una letra mayuscula
            // y un caracter especial y mínimo 6 caracteres y sin espacios en blanco
            val pattern = Regex("^(?=.*[A-Z])(?=.*[^A-Za-z0-9])(?=\\S+\$).{6,}\$")
            valid = pattern.matches(password)

            return valid
        }

        // función que compruba que el Login está validado
        fun isLoginValidated(email: String, password: String, context: Context): Boolean {

            var emailValid = isEmail(email)
            var passwordValid = isGoodPassword(password)

            if (!emailValid) {
                Toast.makeText(context,"Email no válido", Toast.LENGTH_LONG).show()
            }

            if (!passwordValid) {
                Toast.makeText(context,"Formato de contraseña incorrecto", Toast.LENGTH_LONG).show()
            }

            return emailValid && passwordValid

        }

        fun createProgressDialog(context: Context) : ProgressDialog{
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("VetCAre")
            progressDialog.setMessage("Cargando, por favor espere")

            return progressDialog
        }

        fun monthConverter(month: Int) : String{
            var monthLetter = ""

            when(month) {
                1-> monthLetter = "ene"
                2-> monthLetter = "feb"
                3-> monthLetter = "mar"
                4-> monthLetter = "abr"
                5-> monthLetter = "may"
                6-> monthLetter = "jun"
                7-> monthLetter = "jul"
                8-> monthLetter = "ago"
                9-> monthLetter = "sept"
                10-> monthLetter = "oct"
                11 -> monthLetter = "nov"
                12 -> monthLetter = "dic"
            }
            return monthLetter
        }

    }

}