package com.example.sns_project

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sns_project.databinding.ActivitySearchBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SearchActivity :AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private var adapter: SearchAdapter? = null
    private val db:FirebaseFirestore = Firebase.firestore
    private val usersCollectionRef = db.collection("users")
    private var useremail = Firebase.auth.currentUser?.email.toString() //현재 로그인된 사용자 email
    private var following : ArrayList<String>? = null  // 사용자가 팔로우한 사람의 목록 리스트
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SearchAdapter(this, emptyList())

        binding.recyclerView.adapter = adapter

        friendList()

        binding.searchButton.setOnClickListener {
            var id = binding.searchEditText.text.toString()
            updateList(id.lowercase()) //아이디에 포함된 대문자 소문자로 변경
        }
    }

     private fun friendList(){ // 사용자가 follow 하는 리스트 출력
         usersCollectionRef.document(useremail).get().addOnSuccessListener {
             following=it["followings"] as ArrayList<String>?
         }
            usersCollectionRef.whereArrayContains("followers",useremail).get()
           .addOnSuccessListener {
               val ids= mutableListOf<ID>()
               for(doc in it) {
                   ids.add(ID(doc))
               }
               adapter?.updateList(ids,following)
           }
           .addOnFailureListener{
           }
    }

    private fun updateList(id : String)  { //id 를 입력받아 검색된 id 리스트출력
        usersCollectionRef.document(useremail).get().addOnSuccessListener {
            following= it["followings"] as ArrayList<String>?
        }
        usersCollectionRef.whereEqualTo("uid",id).get()
            .addOnSuccessListener {
                val ids = mutableListOf<ID>()
                for (doc in it) {
                    if(doc["email"]!=useremail) //자기자신을 검색하지 못하게
                        ids.add(ID(doc))
                }
                adapter?.updateList(ids,following)
            }
            .addOnFailureListener {
            }
    }
}
