package com.vetcare.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.vetcare.R
import com.vetcare.databinding.FragmentDialogAddpetBinding
import com.vetcare.model.EventPost
import com.vetcare.model.PetModel
import com.vetcare.viewmodel.Constants
import com.vetcare.viewmodel.MainAux
import com.vetcare.viewmodel.Utils

class AddpetDialogFragment : DialogFragment(), DialogInterface.OnShowListener {

    private var binding: FragmentDialogAddpetBinding? = null

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null

    private var pet: PetModel? = null

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog
    // gestionar subir imágenes de la galería
    private var photoSelectedUri: Uri? = null

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            photoSelectedUri = it.data?.data

            //binding?.imgRegistrationPet?.setImageURI(photoSelectedUri)
            binding?.let{
                Glide.with(this).load(photoSelectedUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.imgRegistrationPet)
            }

        }
    }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // conectamos a una instacia de FB Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = Utils.createProgressDialog(requireActivity())

        activity?.let { activity ->
            binding = FragmentDialogAddpetBinding.inflate(LayoutInflater.from(context))

            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle("Añadir mascota")
                    .setPositiveButton("Añadir", null)
                    .setNegativeButton("Cancelar", null)
                    .setView(it.root)

                val dialog = builder.create()
                dialog.setOnShowListener(this)

                return dialog
            }
        }

        return super.onCreateDialog(savedInstanceState)
    }

    override fun onShow(dialogInterface: DialogInterface?) {
        val user = firebaseAuth.currentUser

        initPet()
        configButtons()

        val dialog = dialog as? AlertDialog
        dialog?.let {
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            pet?.let {positiveButton?.setText(getString(R.string.update))}

            positiveButton?.setOnClickListener {
                binding?.let { bin ->
                enableUI(false)

                    uploadImage(pet?.id_mascota){eventPost ->
                        if(eventPost.isSuccess){
                            if (pet == null) {
                                val name = bin.etNameRegistrationPet.text.toString().trim()
                                val species = bin.etSpeciesRegistrationPet.text.toString().trim()
                                lateinit var gender: String
                                val urlImg = eventPost.photoUrl
                                val maleChecked = bin.rb1GenderRegistrationPet.isChecked
                                val femaleChecked = bin.rb2GenderRegistrationPet.isChecked
                                val breed = bin.etBreedRegistrationPet.text.toString().trim()
                                val isDagerous = bin.cbDangerousRegistrationPet.isChecked
                                val owner = user!!.uid

                                // validamos que no están vacios los campos y que al menos está marcado
                                // macho o hembra
                                if (name.isNotEmpty() && species.isNotEmpty() && breed.isNotEmpty()) {
                                    if (maleChecked || femaleChecked) {
                                        gender = getGenderValue()
                                        val pet = PetModel(
                                            nombre = name,
                                            especie = species,
                                            genero = gender,
                                            foto = urlImg,
                                            raza = breed,
                                            es_peligroso = isDagerous,
                                            propietario = owner
                                        )

                                        save(pet, eventPost.documentId!!)

                                    } else {
                                        Toast.makeText(
                                            activity,
                                            getString(R.string.not_valid),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        activity,
                                        getString(R.string.not_valid),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }else{
                                pet?.apply {
                                    nombre = bin.etNameRegistrationPet.text.toString().trim()
                                    especie = bin.etSpeciesRegistrationPet.text.toString().trim()
                                    genero = getGenderValue()
                                    foto = eventPost.photoUrl
                                    raza = bin.etBreedRegistrationPet.text.toString().trim()
                                    es_peligroso = bin.cbDangerousRegistrationPet.isChecked

                                    update(this)
                                }
                            }
                        }
                    }
                }
            }

            negativeButton?.setOnClickListener {
                dismiss()
            }
        }

    }

    private fun initPet() {
        // cuando es nulo
        pet = (activity as? MainAux)?.getPetSelected()
        // cuando no es nulo
        pet?.let { pet ->
            binding?.let {
                it.etNameRegistrationPet.setText(pet.nombre)
                it.etSpeciesRegistrationPet.setText(pet.especie)
                it.etBreedRegistrationPet.setText(pet.raza)
                it.cbDangerousRegistrationPet.isChecked = pet.es_peligroso
                when {
                    pet.genero!!.toLowerCase() == "macho" -> it.rb1GenderRegistrationPet.isChecked =
                        true
                    else -> it.rb2GenderRegistrationPet.isChecked = true
                }

                Glide.with(this).load(pet.foto)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(it.imgRegistrationPet)
            }
        }
    }

    private fun configButtons(){
        binding?.let {
            it.ibRegistrationPet.setOnClickListener{
                openGallery()
            }
        }
    }
    // intent para coger una imagen de la galería
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun uploadImage(petId: String?, callback: (EventPost)->Unit){
        val eventPost = EventPost()
        // conseguimos un id tipo Firestore para la img
        // usamos elvis para si no es null se quede con el valor id actual
        eventPost.documentId = petId ?: FirebaseFirestore.getInstance()
            .collection(Constants.COLL_PETS)
            .document().id

        FirebaseAuth.getInstance().currentUser?.let { user ->
            val imagesRef = FirebaseStorage.getInstance().reference.child(user.uid)
                .child(Constants.PATH_PET_IMGES)

            photoSelectedUri?.let {uri ->
                binding?.let {binding ->
                    binding.pbRegistrationPet.visibility = View.VISIBLE

                    // lo publicamos con id único
                    val photoRef = imagesRef.child(eventPost.documentId!!)

                    photoRef.putFile(uri)
                        // listener para la barra de progreso
                        .addOnProgressListener {
                            val progress = (100*it.bytesTransferred / it.totalByteCount).toInt()
                            it.run{
                                binding.pbRegistrationPet.progress = progress
                                binding.tvProgressRegistrationPet.text = String.format("%s%%", progress)
                            }
                        }
                        .addOnSuccessListener{
                            it.storage.downloadUrl.addOnSuccessListener { downloadUrl ->
                                eventPost.isSuccess = true
                                eventPost.photoUrl = downloadUrl.toString()
                                callback(eventPost)
                            }
                        }
                        .addOnFailureListener{
                            Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                            enableUI(true)

                            eventPost.isSuccess = false
                            callback(eventPost)
                        }
                }
            }
        }
    }

    private fun save(pet: PetModel, documentId: String) {
        progressDialog.show()

        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.COLL_PETS)
            .document(documentId)
            .set(pet)
            //.add(pet)
            .addOnSuccessListener {
                Toast.makeText(activity, getString(R.string.add_pet_success), Toast.LENGTH_SHORT)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                progressDialog.dismiss()
                enableUI(true)
                binding?.pbRegistrationPet?.visibility = View.INVISIBLE
                dismiss()
            }
    }

    private fun update(pet: PetModel) {
        progressDialog.show()

        val db = FirebaseFirestore.getInstance()

        pet.id_mascota?.let {id ->
            db.collection(Constants.COLL_PETS)
                .document(id)
                .set(pet)
                .addOnSuccessListener {
                    Toast.makeText(activity, getString(R.string.update_pet_success), Toast.LENGTH_SHORT)
                        .show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    progressDialog.dismiss()
                    enableUI(true)
                    binding?.pbRegistrationPet?.visibility = View.INVISIBLE
                    dismiss()
                }
        }
    }

    private fun getGenderValue(): String {
        var gender = ""
        gender = when {
            binding!!.rb1GenderRegistrationPet.isChecked -> {
                binding!!.rb1GenderRegistrationPet.text.toString().toLowerCase()
            }
            else -> {
                binding!!.rb2GenderRegistrationPet.text.toString().toLowerCase()
            }
        }
        return gender
    }
    // método para bloquear las opciones y no duplicar valores al cargar
    private fun enableUI(enable: Boolean) {
        positiveButton?.isEnabled = enable
        negativeButton?.isEnabled = enable
        binding?.let{
            with(it){
                etNameRegistrationPet.isEnabled = enable
                etSpeciesRegistrationPet.isEnabled = enable
                etBreedRegistrationPet.isEnabled = enable
                cbDangerousRegistrationPet.isEnabled = enable
                rb1GenderRegistrationPet.isEnabled = enable
                rb2GenderRegistrationPet.isEnabled = enable
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}