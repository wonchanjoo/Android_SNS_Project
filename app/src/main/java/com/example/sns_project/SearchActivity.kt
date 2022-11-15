package com.example.sns_project

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sns_project.databinding.ActivitySearchBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SearchActivity :AppCompatActivity() {
    lateinit var binding: ActivitySearchBinding
    private var adapter: IdAdapter? = null
    private val db:FirebaseFirestore = Firebase.firestore
    private val idsCollectionRef = db.collection("users")
    private var snapshotListener :ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = IdAdapter(this, emptyList())

        binding.recyclerView.adapter = adapter


        binding.searchButton.setOnClickListener{
            var id = binding.searchEditText.text.toString()
            updateList(id)
        }

    }

    private fun updateList(id : String)  {
        idsCollectionRef.whereEqualTo("id",id).get()
            .addOnSuccessListener {
                val ids = mutableListOf<ID>()
                for (doc in it) {
                    ids.add(ID(doc))
                }
                adapter?.updateList(ids)
            }
            .addOnFailureListener {
            }
    }
}
