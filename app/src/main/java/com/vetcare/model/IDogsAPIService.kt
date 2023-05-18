package com.vetcare.model

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface IDogsAPIService {
    @GET // petición de retrofit2
    suspend fun getDogsByBreeds(@Url url:String): Response<DogsModel>
    // suspend porque será llamada asyncronamente
}