package com.vetcare.model

import com.google.gson.annotations.SerializedName

data class DogsModel(
    // los campos tienen que tener el mismo nombre que en la API
    // pero podemos usar SerializedName para nosotros internamente
    // darle el nombre que queramos y mandar en la petición
    // el nombre correcto
    @SerializedName("status") var status: String,
    @SerializedName("message") var images: List<String>
)