package com.vetcare.view

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.google.firebase.storage.FirebaseStorage
import com.vetcare.R
import com.vetcare.databinding.FragmentDialogUsermodifyBinding
import com.vetcare.model.EventPost
import com.vetcare.model.UserModel
import com.vetcare.viewmodel.Constants
import com.vetcare.viewmodel.MainUserAux

class UserModifyDialogFragment : DialogFragment(), DialogInterface.OnShowListener {

    private var binding: FragmentDialogUsermodifyBinding? = null

    private var positiveButton: Button? = null
    private var negativeButton: Button? = null

    private var user: UserModel? = null

    private lateinit var firebaseAuth: FirebaseAuth

    private var photoSelectedUri: Uri? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                photoSelectedUri = it.data?.data

                binding?.let {
                    Glide.with(this).load(photoSelectedUri)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop().into(it.imgUserModify)
                }
                //binding?.imgUserModify?.setImageURI(photoSelectedUri)
            }
        }

    @SuppressLint("UseGetLayoutInflater")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        activity?.let { activity ->
            binding = FragmentDialogUsermodifyBinding.inflate(LayoutInflater.from(context))

            binding?.let {
                val builder = AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.user_modify))
                    .setPositiveButton(getString(R.string.modify), null)
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setView(it.root)

                val dialog = builder.create()
                dialog.setOnShowListener(this)

                return dialog
            }
        }

        return super.onCreateDialog(savedInstanceState)
    }

    override fun onShow(dialog: DialogInterface?) {
        initUser()
        configButtons()

        // damos valor a la variable de Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        val dialog = dialog as? AlertDialog
        dialog?.let {
            positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton = it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                binding?.let {
                    enableUI(false)

                    uploadImage(user?.id_usuario) { eventPost ->
//                    uploadReducedImage(user?.id_usuario) { eventPost ->
                        if (eventPost.isSuccess) {
                            user?.apply {
                                nick_name = it.etNickNameUserModify.text.toString().trim()
                                id_usuario = firebaseAuth.currentUser!!.uid
                                imagen_perfil = eventPost.photoUrl

                                updateUser(this)
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

    private fun uploadImage(userId: String?, callback: (EventPost) -> Unit) {
        val eventPost = EventPost()
        // reservamos un documentId al subir la imagen, y así luego usarlo para asignar
        // la url a Firestore
        // usamos elvis para en caso de ser null haga lo siguiente
        eventPost.documentId =
            userId ?: FirebaseFirestore.getInstance().collection(Constants.COLL_USERS).document().id
        val storageRef = FirebaseStorage.getInstance().reference.child(Constants.PATH_USER_IMGES)

        photoSelectedUri?.let { uri ->
            binding?.let { binding ->
                // hacemos visible la barra de progreso
                binding.pbUserModify.visibility = View.VISIBLE

                val photoRef = storageRef.child(eventPost.documentId!!)

                photoRef.putFile(uri)
                    .addOnProgressListener {
                        val progress = (100*it.bytesTransferred / it.totalByteCount).toInt()
                        it.run {
                            binding.pbUserModify.progress = progress
                            binding.tvProgressUserModify.text = String.format("%s%%", progress)
                        }
                    }
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener { dowloadUrl ->
                            // le damos valor al url de la foto
                            eventPost.isSuccess = true
                            eventPost.photoUrl = dowloadUrl.toString()
                            callback(eventPost)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                        enableUI(true)

                        eventPost.isSuccess = false
                        callback(eventPost)
                    }
            }
        }
    }

    private fun uploadReducedImage(userId: String?, callback: (EventPost) -> Unit) {
        val eventPost = EventPost()
        // reservamos un documentId al subir la imagen, y así luego usarlo para asignar
        // la url a Firestore
        // usamos elvis para en caso de ser null haga lo siguiente
        eventPost.documentId =
            userId ?: FirebaseFirestore.getInstance().collection(Constants.COLL_USERS).document().id

        FirebaseAuth.getInstance().currentUser?.let { user ->
            val imagesRef = FirebaseStorage.getInstance().reference.child(user.uid)
                .child(Constants.PATH_USER_IMGES)
            val photoRef = imagesRef.child(eventPost.documentId!!)

            photoSelectedUri?.let { uri ->
                binding?.let { binding ->
                    // hacemos visible la barra de progreso
                    binding.pbUserModify.visibility = View.VISIBLE

                    photoRef.putFile(uri)
                        .addOnProgressListener {
                            val progress = (100*it.bytesTransferred / it.totalByteCount).toInt()
                            it.run {
                                binding.pbUserModify.progress = progress
                                binding.tvProgressUserModify.text = String.format("%s%%", progress)
                            }
                        }
                        .addOnSuccessListener {
                            it.storage.downloadUrl.addOnSuccessListener { dowloadUrl ->
                                // le damos valor al url de la foto
                                eventPost.isSuccess = true
                                eventPost.photoUrl = dowloadUrl.toString()
                                callback(eventPost)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                            enableUI(true)

                            eventPost.isSuccess = false
                            callback(eventPost)
                        }
                }
            }
        }
    }

    private fun updateUser(user: UserModel) {
        val db = FirebaseFirestore.getInstance()

        user.id_usuario?.let { id ->
            db.collection(Constants.COLL_USERS)
                .document(id)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(
                        activity,
                        getString(R.string.user_modify_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                    moveToLoginActivity()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    enableUI(true)
                    binding?.pbUserModify?.visibility = View.INVISIBLE
                    dismiss()
                }
        }
    }

    private fun initUser() {
        user = (activity as? MainUserAux)?.getUserSelected()
        user?.let { user ->
            binding?.let {
                it.etNickNameUserModify.setText(user.nick_name)

                Glide.with(this).load(user.imagen_perfil)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop().into(it.imgUserModify)
            }
        }
    }

    private fun configButtons() {
        binding?.let {
            it.ibUserModify.setOnClickListener {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun enableUI(enable: Boolean) {
        positiveButton?.isEnabled = enable
        negativeButton?.isEnabled = enable
        binding?.let {
            with(it) {
                etNickNameUserModify.isEnabled = enable
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    // función que lleva a la RegistrationActivity
    private fun moveToLoginActivity() {
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        dismiss()
    }
}