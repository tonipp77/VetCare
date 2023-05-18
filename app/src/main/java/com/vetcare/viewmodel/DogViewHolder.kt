package com.vetcare.viewmodel

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.vetcare.databinding.ItemDogBinding

class DogViewHolder(view: View) : RecyclerView.ViewHolder(view){

    private val binding = ItemDogBinding.bind(view)

    fun bind(image: String){
        // con Picasso leemos la imagen
        Picasso.get().load(image).into(binding.ivDog)
    }
}