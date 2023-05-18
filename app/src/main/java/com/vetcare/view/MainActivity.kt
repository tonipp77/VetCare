package com.vetcare.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vetcare.R
import com.vetcare.databinding.ActivityMainBinding
import com.vetcare.model.IOnPetListener
import com.vetcare.model.PetModel
import com.vetcare.model.UserModel
import com.vetcare.viewmodel.Constants
import com.vetcare.viewmodel.MainAux
import com.vetcare.viewmodel.MainUserAux
import com.vetcare.viewmodel.PetAdapter

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    IOnPetListener, MainAux, MainUserAux {

    private lateinit var binding: ActivityMainBinding

    // drawer del navigationDrawer
    private lateinit var drawer: DrawerLayout

    // toggle del navigationDrawer
    private lateinit var toggle: ActionBarDrawerToggle

    // variable para el Firebase Authentication
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestoreListener: ListenerRegistration

    private lateinit var adapter: PetAdapter

    private var petSelected: PetModel? = null
    private var userSelected: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // para acceder a los componentes del nav_header_main
        val viewHeader = binding.navView.getHeaderView(0)
        val header: ConstraintLayout = viewHeader.findViewById(R.id.nav_header)
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // damos valor a la variable de Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        activateCurrentUser()

        configRecyclerView()
        // configFirestoreRealtime()

        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        // damos valor al toggle para que contenga mas tarde el NavigationDrawer
        toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        navigationView.setNavigationItemSelectedListener(this)

        // al clickar la zona de usuario del navigationDrawer se va al fragment usuario
        header.setOnClickListener {
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.fcvMain, UserFragment())
                addToBackStack(null)
                commit()
            }
            // cuando se clickea una opción esto esconde el NavigationDrawer
            drawer.closeDrawer(GravityCompat.START)
        }

        binding.btnAddPetUser.setOnClickListener() {
            goToAddpetDialogFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        // abrimos el listener de Firestore
        configFirestoreRealtime()
    }

    override fun onPause() {
        super.onPause()
        // cerramos el listener de Firestore
        firestoreListener.remove()
    }

    private fun configRecyclerView() {
        adapter = PetAdapter(mutableListOf(), this)
        binding.rvPetsMain.apply {
            layoutManager = GridLayoutManager(
                this@MainActivity, 1,
                GridLayoutManager.HORIZONTAL, false
            )
            adapter = this@MainActivity.adapter
        }

    }

    private fun goToAddpetDialogFragment() {
        petSelected = null
        AddpetDialogFragment().show(
            supportFragmentManager,
            AddpetDialogFragment::class.java.simpleName
        )
    }

    private fun activateCurrentUser() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser!!.uid

        val db = Firebase.firestore

        db.collection(Constants.COLL_USERS).document(uid).get().addOnSuccessListener {
            val viewHeader = binding.navView.getHeaderView(0)
            val nick: TextView = viewHeader.findViewById(R.id.tvHeaderNavigation)
            val email: TextView = viewHeader.findViewById(R.id.tvEmailNavigation)
            val image: ImageView = viewHeader.findViewById(R.id.ivHeaderNavigation)

            nick.text = it.get("nick_name") as String?
            email.text = it.get("email") as String?

            Glide.with(this).load(it.get("imagen_perfil") as String?)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_access_time)
                .error(R.drawable.ic_broken_image)
                .centerCrop().into(image)

            userSelected = UserModel(
                email = it.get("email") as String?,
                nick_name = it.get("nick_name") as String?
            )
        }
    }

    // aquí van la llamadas a métodos de cada opción de menú
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val mapActivity = MapActivity()
        val dogbreedActivity = DogbreedActivity()

        when (item.itemId) {
            R.id.nav_item_modify -> getUserSelected()?.let { openUserModifyFragmentDialog(it) }
            R.id.nav_item_delete -> deleteAccount()
            R.id.nav_item_close -> sendSignoutAlertDialog(
                this, getString(R.string.signout), getString(
                    R.string.confirm_signout
                ), getString(R.string.signout), getString(R.string.cancel)
            )
            R.id.nav_item_image -> moveToAnotherActivity(dogbreedActivity)
            R.id.nav_item_maps -> moveToAnotherActivity(mapActivity)
        }
        // cuando se clickea una opción esto esconde el NavigationDrawer
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun moveToAnotherActivity(activityToMove: Activity) {
        val intent = Intent(this, activityToMove::class.java)
        startActivity(intent)
    }

    private fun moveToAnotherActivityFinish(activityToMove: Activity) {
        val intent = Intent(this, activityToMove::class.java)
        startActivity(intent)
        finish()
    }

    private fun moveToLoginActivity(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openUserModifyFragmentDialog(user: UserModel) {
        userSelected = user
        UserModifyDialogFragment().show(
            supportFragmentManager,
            UserModifyDialogFragment::class.java.simpleName
        )
    }

    private fun sendSignoutAlertDialog(
        context: Context,
        title: String,
        message: String,
        positiveButton: String,
        negativeButton: String
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton((positiveButton), { dialogInterface, i -> Signout() })
        builder.setNegativeButton((negativeButton), null)

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun sendPetAlertDialog(
        context: Context,
        title: String,
        message: String,
        positiveButton: String,
        negativeButton: String,
        pet: PetModel
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton((positiveButton), { dialogInterface, i -> modifyPet(pet) })
        builder.setNegativeButton((negativeButton), { dialogInterface, i -> deletePet(pet) })

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun Signout() {
        firebaseAuth.signOut()
        moveToLoginActivity()
    }

    private fun configFirestoreRealtime() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser!!.uid
        val petRef = db.collection(Constants.COLL_PETS).whereEqualTo("propietario", uid)

        firestoreListener = petRef.addSnapshotListener { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, getString(R.string.error_db), Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            for (snapshot in snapshots!!.documentChanges) {
                val pet = snapshot.document.toObject(PetModel::class.java)
                pet.id_mascota = snapshot.document.id
                when (snapshot.type) {
                    DocumentChange.Type.ADDED -> adapter.add(pet)
                    DocumentChange.Type.MODIFIED -> adapter.update(pet)
                    DocumentChange.Type.REMOVED -> adapter.delete(pet)
                }

            }
        }
    }

    override fun onClick(pet: PetModel) {
        sendPetAlertDialog(
            this, "Gestión de su mascota", "¿Que desea hacer con su mascota?",
            "Modificar", "Eliminar", pet
        )
    }

    private fun modifyPet(pet: PetModel) {
        petSelected = pet
        AddpetDialogFragment().show(
            supportFragmentManager,
            AddpetDialogFragment::class.java.simpleName
        )
    }

    private fun deletePet(pet: PetModel) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar mascota")
        builder.setMessage("¿Está seguro que desea eliminar su mascota?")
        builder.setPositiveButton(("Cancelar"), null)
        builder.setNegativeButton(
            ("Confirmar")
        ) { dialogInterface, i -> deletePetFromDataBase(pet) }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun deletePetFromDataBase(pet: PetModel) {
        val db = FirebaseFirestore.getInstance()
        val petRef = db.collection(Constants.COLL_PETS)
        pet.id_mascota?.let { id ->
            petRef.document(id)
                .delete()
                .addOnFailureListener {
                    Toast.makeText(this, getString(R.string.error_db), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteAccount() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.delete_acount_title))
        builder.setMessage(getString(R.string.delete_acount_message))
        builder.setPositiveButton((R.string.cancel), null)
        builder.setNegativeButton((R.string.acept)) { dialogInterface, i -> deleteAccountFromAutFirestore() }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    override fun getPetSelected(): PetModel? = petSelected

    override fun getUserSelected(): UserModel? = userSelected

    private fun deleteAccountFromAutFirestore() {
        val user = firebaseAuth.currentUser
        // eliminar de Firestore
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection(Constants.COLL_USERS)

        userRef.document(user!!.uid).delete()
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.error_db), Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.delete_acount_doit), Toast.LENGTH_SHORT)
                    .show()
            }

        // eliminar de authentication
        user.delete().addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, getString(R.string.delete_acount_doit), Toast.LENGTH_SHORT)
                    .show()

                moveToLoginActivity()
            } else {
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }
    }
}