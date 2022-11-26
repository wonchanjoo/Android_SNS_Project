package com.example.sns_project

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sns_project_watch.databinding.RecyclerviewWatchpostingBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

data class Post(val id: String, val date: Timestamp, val image: String, val like: Int, val publisher: String, val text: String) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc.id, doc["date"] as Timestamp, doc["image"].toString(), doc["like"].toString().toIntOrNull() ?: 0, doc["publisher"].toString(), doc["text"].toString())
}

class PostingViewHolder(val binding: RecyclerviewWatchpostingBinding) : RecyclerView.ViewHolder(binding.root)

class WatchPostingAdapter(private val context: Context, private var posts: List<Post>) : RecyclerView.Adapter<PostingViewHolder>() {
    private lateinit var storageReference: StorageReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostingViewHolder {
        storageReference = Firebase.storage.reference
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecyclerviewWatchpostingBinding.inflate(inflater, parent, false)
        return PostingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostingViewHolder, position: Int) {
        val post = posts[position]
        holder.binding.id.text = post.publisher.split("@")[0]
        holder.binding.description.text = post.text // 게시물 내용

        val imageRef = storageReference.child(post.image)
        imageRef.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            holder.binding.PostImage.setImageBitmap(bmp)
        }?.addOnFailureListener {
            Log.e("PostingAdapter", "image error")
        }

        Firebase.firestore.collection("users").document(post.publisher).get().addOnSuccessListener { it ->
            val filename = it["image"].toString()
            val userImageRef = storageReference.child(filename)
            userImageRef.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                holder.binding.profileImage.setImageBitmap(bmp)
            }?.addOnFailureListener {
                Log.e("PostingAdapter", "user image error")
            }
        }
    }

    override fun getItemCount() = posts.size

    fun updateList(newList: List<Post>) {
        posts = newList
        notifyDataSetChanged()
    }

}