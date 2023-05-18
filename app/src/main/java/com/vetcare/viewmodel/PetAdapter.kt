package com.vetcare.viewmodel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vetcare.R
import com.vetcare.databinding.ItemPetBinding
import com.vetcare.model.IOnPetListener
import com.vetcare.model.PetModel

class PetAdapter(private val petList: MutableList<PetModel>, private val listener: IOnPetListener) :
    RecyclerView.Adapter<PetAdapter.ViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pet = petList[position]

        holder.setListener(pet)

        holder.binding.tvNameItemPet.text = pet.nombre

        Glide.with(context).load(pet.foto)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_access_time)// icono mientras carga
            .error(R.drawable.ic_broken_image)// icono error
            .centerCrop()
            .into(holder.binding.ivItemPet)
    }

    override fun getItemCount(): Int = petList.size

    fun add(pet: PetModel) {
        if (!petList.contains(pet)) {
            petList.add(pet)
            notifyItemInserted(petList.size - 1)
        }else {
            update(pet)
        }
    }

    fun update(pet: PetModel) {
        val index = petList.indexOf(pet)
        if (index != -1) {
            petList.set(index, pet)
            notifyItemChanged(index)
        }
    }

    fun delete(pet: PetModel) {
        val index = petList.indexOf(pet)
        if (index != -1) {
            petList.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemPetBinding.bind(view)

        fun setListener(pet: PetModel) {
            binding.root.setOnClickListener {
                listener.onClick(pet)
            }
        }
    }
}