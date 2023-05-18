package com.vetcare.model

import com.google.firebase.firestore.Exclude

data class UserModel(
    @get:Exclude var id_usuario: String? = null,
    @get:Exclude var password: String? = null,
    var imagen_perfil: String? = null,
    var email: String? = null,
    var nick_name: String? = null
)
// para cambiar atributos usaremos la función Copy()