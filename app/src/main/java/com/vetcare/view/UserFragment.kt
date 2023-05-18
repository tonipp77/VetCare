package com.vetcare.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vetcare.R
import com.vetcare.databinding.FragmentUserBinding
import com.vetcare.viewmodel.Constants

class UserFragment : Fragment() {

    // binding para fragment
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    // variable para el Firebase Authentication
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // damos valor a la variable de Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        activateCurrentUser()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun activateCurrentUser() {

        val currentUser = firebaseAuth.currentUser
        val uid = currentUser!!.uid

        val db = Firebase.firestore

        db.collection(Constants.COLL_USERS).document(uid).get().addOnSuccessListener{

            binding.tvNickNameUser.text = it.get("nick_name") as String?
            binding.tvEmailUser.text = it.get("email") as String?

            Glide.with(this).load(it.get("imagen_perfil") as String?)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_access_time)
                .error(R.drawable.ic_broken_image)
                .centerCrop().into(binding.ivUser)
        }
    }

}