package com.example.sns_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sns_project.databinding.ActivitySearchBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SearchActivity :AppCompatActivity() {
    lateinit var binding: ActivitySearchBinding
    private var adapter: IdAdapter? = null
    private val db:FirebaseFirestore = Firebase.firestore
    private val idsCollectionRef = db.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = IdAdapter(this, emptyList())

        binding.recyclerView.adapter = adapter

        friendList()

        binding.searchButton.setOnClickListener{
            var id = binding.searchEditText.text.toString()
            updateList(id)
        }
        binding.toFeedBtn.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

   private fun friendList(){
        var uemail = Firebase.auth.currentUser?.email.toString()
        //println("###############3 ${uemail}")
       idsCollectionRef.whereArrayContains("followers",uemail).get()
           .addOnSuccessListener {
               val ids= mutableListOf<ID>()
               for(doc in it) {
                   ids.add(ID(doc))
               }
               adapter?.updateList(ids)
           }
           .addOnFailureListener{
           }
    }


    private fun updateList(id : String)  {
        idsCollectionRef.whereEqualTo("uid",id).get()
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
