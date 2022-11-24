package com.example.sns_project

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sns_project.databinding.RecyclerviewPostingBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

data class Post(val id: String, val date: Timestamp, val image: String, val like: Int, val publisher: String, val text: String) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc.id, doc["date"] as Timestamp, doc["image"].toString(), doc["like"].toString().toIntOrNull() ?: 0, doc["publisher"].toString(), doc["text"].toString())
}

class PostingViewHolder(val binding: RecyclerviewPostingBinding) : RecyclerView.ViewHolder(binding.root)

class PostingAdapter(private val context: Context, private var posts: List<Post>) : RecyclerView.Adapter<PostingViewHolder>() {
    private var like_posts: ArrayList<String>? = null
    private lateinit var storageReference: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostingViewHolder {
        storageReference = Firebase.storage.reference
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerviewPostingBinding.inflate(inflater, parent, false)
        return PostingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostingViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.id.text = post.publisher.split("@")[0]
        holder.binding.id2.text = post.publisher.split("@")[0]
        holder.binding.content.text = post.text // 게시물 내용
        holder.binding.likes.text = "${post.like} likes" // 좋아요 수 표시
        holder.binding.heartBtn.setOnClickListener { heartClick(holder, post) }
        if(like_posts == null) return;
        if(like_posts!!.contains(post.id)) {
            holder.binding.heartBtn.isSelected = true
            holder.binding.heartBtn.text = "♥"
        }

        val imageRef = storageReference.child(post.image)
        imageRef.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            holder.binding.image.setImageBitmap(bmp)
        }?.addOnFailureListener {
            Log.e("PostingAdapter", "image error")
        }

        Firebase.firestore.collection("users").document(post.publisher).get().addOnSuccessListener { it ->
            val filename = it["image"].toString()
            val userImageRef = storageReference.child(filename)
            userImageRef.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                holder.binding.userImage.setImageBitmap(bmp)
                //Glide.with(context).load(holder.binding.userImage.resources).circleCrop().into(holder.binding.userImage)
            }?.addOnFailureListener {
                Log.e("PostingAdapter", "user image error")
            }
        }
    }

    override fun getItemCount() = posts.size

    fun updateList(newList: List<Post>, like_posts: ArrayList<String>?) {
        posts = newList
        this.like_posts = like_posts
        notifyDataSetChanged()
    }

    private fun heartClick(holder: PostingViewHolder, post: Post) {
        val usersCollectionRef = Firebase.firestore.collection("users") // users collection
        val myEmail = Firebase.auth.currentUser?.email // 나의 이메일
        if(myEmail == null) {
            Log.e("PostingAdapter", "get current user email error")
            return;
        }
        val userDocumentRef = usersCollectionRef.document(myEmail) // 나의 Document Reference

        val postsCollectionRef = Firebase.firestore.collection("posts") // posts collection

        if(holder.binding.heartBtn.isSelected) { // 버튼이 눌려져 있으면 (좋아요 취소)
            holder.binding.heartBtn.text = "♡" // 버튼 text를 빈 하트로 변경
            holder.binding.heartBtn.isSelected = false // 선택되지 않은 상태로 변경
            userDocumentRef.update("like_posts", FieldValue.arrayRemove(post.id)) // like_post에 post.id 삭제
            postsCollectionRef.document(post.id).get().addOnSuccessListener {
                postsCollectionRef.document(post.id).update("like", FieldValue.increment(-1)) // 좋아요 감소
                holder.binding.likes.text = (it["like"].toString().toInt() - 1).toString() + " likes"
            }
        } else if(!holder.binding.heartBtn.isSelected){ // 버튼이 눌려져 있지 않으면 (좋아요)
            holder.binding.heartBtn.text = "♥" // 버튼 text를 채워진 하트로 변경
            holder.binding.heartBtn.isSelected = true // 선택된 상태로 변경
            userDocumentRef.update("like_posts", FieldValue.arrayUnion(post.id)) // like_posts에 post.id 추가
            postsCollectionRef.document(post.id).get().addOnSuccessListener {
                postsCollectionRef.document(post.id).update("like", FieldValue.increment(1)) // 좋아요 증가
                holder.binding.likes.text = (it["like"].toString().toInt() + 1).toString() + " likes"
            }
        }
    }
}