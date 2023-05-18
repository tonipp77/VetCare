package com.vetcare.model

import android.app.Application
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // persistencia de la info offline FB
        Firebase.database.setPersistenceEnabled(true)
    }
}