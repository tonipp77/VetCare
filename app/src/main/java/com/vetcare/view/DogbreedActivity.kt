package com.vetcare.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.vetcare.R
import com.vetcare.databinding.ActivityDogbreedBinding
import com.vetcare.model.IDogsAPIService
import com.vetcare.viewmodel.DogAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DogbreedActivity : AppCompatActivity(),
    AdapterView.OnItemClickListener {

    private lateinit var binding: ActivityDogbreedBinding
    // adaptador para el recyclerView y la lista
    private lateinit var adapter: DogAdapter
    private val dogImages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDogbreedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // iniciamos el recyclerview
        initRecyclerView()

        // cogemos los valores de array de razas de perro
        val breeds = resources.getStringArray(R.array.dog_breed)
        // adaptador para utilizar el autocompleteTV
        val adapterAutocomplete = ArrayAdapter(this, R.layout.list_item , breeds)

        with(binding.actvDogs){
            setAdapter(adapterAutocomplete)
            onItemClickListener = this@DogbreedActivity
        }

        //binding.svDogs.setOnQueryTextListener(this)
    }

    private fun initRecyclerView() {
        adapter = DogAdapter(dogImages)
        binding.rvDogs.layoutManager = LinearLayoutManager(this@DogbreedActivity)
        binding.rvDogs.adapter = adapter
    }

    // método para hacer el get de retrofit2
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/breed/") // la parte de url sin raza
            // usamos la librería de conversión de JSON
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // corrutina asyncrona para pedir a la API la info
    private fun searchByName(query: String){
        CoroutineScope(Dispatchers.IO).launch{
            // hacemos la petición del query
            val call = getRetrofit().create(IDogsAPIService::class.java).getDogsByBreeds("$query/images")
            val puppies = call.body()
            // volvemos al hilo principal
            runOnUiThread {
                if(call.isSuccessful){
                    // puede ser null, aplicamos elvies
                    val images = puppies?.images ?: emptyList()
                    dogImages.clear()
                    dogImages.addAll(images)
                    adapter.notifyDataSetChanged()
                }else{
                    showError()
                }
                hideKeyboard()
            }
        }
    }
    // oculta el teclado al buscar
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.clDogs.windowToken, 0)
    }

    private fun showError(){
        Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = parent?.getItemAtPosition(position).toString()
        if(!item.isNullOrEmpty()){
            searchByName(item)
        }
    }
}