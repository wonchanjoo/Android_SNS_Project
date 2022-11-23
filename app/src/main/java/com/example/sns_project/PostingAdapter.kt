package com.example.sns_project

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sns_project.databinding.RecyclerviewPostingBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class Post(val date: Timestamp, val image: String, val like: Int, val publisher: String, val text: String) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc["date"] as Timestamp, doc["image"].toString(), doc["like"].toString().toIntOrNull() ?: 0, doc["publisher"].toString(), doc["text"].toString())
    constructor(map: Map<*, *>) :
            this(map["date"] as Timestamp, map["image"].toString(), map["like"].toString().toIntOrNull() ?: 0, map["publisher"].toString(), map["text"].toString())
}

class PostingViewHolder(val binding: RecyclerviewPostingBinding) : RecyclerView.ViewHolder(binding.root)

class PostingAdapter(private val context: Context, private var posts: List<Post>) : RecyclerView.Adapter<PostingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostingViewHolder {
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
        holder.binding.heartBtn.setOnClickListener { heartClick(holder) }
    }

    override fun getItemCount() = posts.size

    fun updateList(newList: List<Post>) {
        posts = newList
        notifyDataSetChanged()
    }

    private fun heartClick(holder: PostingViewHolder) {
        if(holder.binding.heartBtn.isSelected) { // 버튼이 눌려져 있으면 (좋아요 취소)
            holder.binding.heartBtn.text = "♡"
            holder.binding.heartBtn.isSelected = false
        } else { // 버튼이 눌려져 있지 않으면 (좋아요)
            holder.binding.heartBtn.text = "♥"
            holder.binding.heartBtn.isSelected = true
        }
    }
}