package com.example.sns_project

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sns_project.databinding.RecyclerviewSearchBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class ID(val id: String, val email : String) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc.id , doc["email"].toString())
}

class FriendViewHolder(val binding: RecyclerviewSearchBinding) : RecyclerView.ViewHolder(binding.root)

class SearchAdapter(private val context: Context, private var ids: List<ID>)
    : RecyclerView.Adapter<FriendViewHolder>() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollectionRef = db.collection("users") //users collection
    private  val useremail = Firebase.auth.currentUser?.email.toString() // 현재 로그인된 사용자 email
    private var followings : ArrayList<String>?=null
    fun updateList(newList: List<ID> , followings:ArrayList<String>?) {
        ids = newList
        this.followings = followings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: RecyclerviewSearchBinding = RecyclerviewSearchBinding.inflate(inflater, parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder:FriendViewHolder, position: Int) {
        val otheruser = ids[position]
        holder.binding.userId.text = otheruser.email
       if(followings==null) {
             holder.binding.followBtn.text = "팔로우"
        }
        if(followings!!.contains(otheruser.email)) {
            holder.binding.followBtn.text = "팔로잉"
        } else
        {
            holder.binding.followBtn.text = "팔로우"
        }
        holder.binding.followBtn.setOnClickListener { followCheck(holder,otheruser) }
    }

    private fun followCheck(holder: FriendViewHolder, otheruser: ID){ //팔로우 상태를 확인 및 팔로우, 팔로우 취소 동작
        if(holder.binding.followBtn.text == "팔로잉"){ //팔로우 된 상태라면
            holder.binding.followBtn.text = "팔로우"
            usersCollectionRef.document(otheruser.email).update("followers",FieldValue.arrayRemove(useremail))
            usersCollectionRef.document(useremail).update("followings", FieldValue.arrayRemove(otheruser.email))
        }
        else if(holder.binding.followBtn.text =="팔로우"){ //팔로우 안한 상태라면
            holder.binding.followBtn.text = "팔로잉"
            usersCollectionRef.document(otheruser.email).update("followers",FieldValue.arrayUnion(useremail))
            usersCollectionRef.document(useremail).update("followings", FieldValue.arrayUnion(otheruser.email))
        }
    }

    override fun getItemCount() = ids.size
}

