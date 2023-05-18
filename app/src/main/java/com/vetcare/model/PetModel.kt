package com.vetcare.model

import com.google.firebase.firestore.Exclude


data class PetModel(
    @get:Exclude var id_mascota: String? = null,
    var nombre: String? = null,
    var especie: String? = null,
    var genero: String? = null,
    var foto: String? = null,
    var raza: String? = null,
    val propietario: String? = null,
    var es_peligroso: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PetModel

        if (id_mascota != other.id_mascota) return false

        return true
    }

    override fun hashCode(): Int {
        return id_mascota?.hashCode() ?: 0
    }
}