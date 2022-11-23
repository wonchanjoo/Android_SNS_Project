package com.example.sns_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sns_project.databinding.ActivityFeedBinding
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class User(var email: String, var followers: ArrayList<String>?, var followings: ArrayList<String>?, var like_posts: ArrayList<String>?, var uid: String)
class FeedActivity : AppCompatActivity() {
    private lateinit var binding : ActivityFeedBinding
    private lateinit var currentUser: FirebaseUser
    private var adapter: PostingAdapter? = null
    private var myAccount = User("", null, null, null, "")
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollectionRef = db.collection("users")
    private val postsCollectionRef = db.collection("posts")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 로그인이 되지 않은 경우
        if(Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.postingBtn.setOnClickListener {
            val intent = Intent(this, PostingActivity::class.java)
            intent.putExtra("userEmail", myAccount.email);
            startActivity(intent)
        }
        binding.friendBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        binding.postingRecyclerView.setHasFixedSize(true)
        binding.postingRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostingAdapter(this, emptyList())
        binding.postingRecyclerView.adapter = adapter

        currentUser = Firebase.auth.currentUser!! // 로그인 된 유저 정보 받아오기
        getMyAccountFromDB(currentUser.email!!) // 데이터베이스에서 내 정보 가져오기 -> 게시물 보여주기까지
    }

    // DB에서 로그인 된 계정의 email, followings, followers, uid 정보를 가져온다.
    private fun getMyAccountFromDB(email: String) {
        val myDocumentRef = usersCollectionRef.document(email)
        myDocumentRef.get().addOnSuccessListener {
            myAccount.email = it["email"].toString()
            myAccount.followers = it["followers"] as ArrayList<String>?
            myAccount.followings = it["followings"] as ArrayList<String>?
            myAccount.like_posts = it["like_posts"] as ArrayList<String>?
            myAccount.uid = it["uid"].toString()

            updateFollowingPost() // 내가 팔로우한 사람의 게시물들을 화면에 보여준다
        }
    }

    // 전체 post 중에서 내가 팔로우 하는 사람이 올린 post만 가져와 recyclerView에 넣어준다.
    private fun updateFollowingPost() {
        if(myAccount.followings == null) return; // 내가 팔로우하는 사람이 없으면 return;

        postsCollectionRef.get().addOnSuccessListener {
            val posts = mutableListOf<Post>()
            // 내가 팔로우 하는 사람이나 나의 게시물을 posts에 추가한다.
            for(doc in it) {
                if(myAccount.followings!!.contains(doc["publisher"]) || doc["publisher"] == myAccount.email)
                    posts.add(Post(doc))
            }
            adapter?.updateList(posts, myAccount.like_posts) // recyclerView 업데이트
        }
    }
}