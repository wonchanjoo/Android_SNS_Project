package com.example.sns_project

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sns_project.databinding.SearchBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app

data class ID(val id: String, val email : String) {
    constructor(doc: QueryDocumentSnapshot) :
            this(doc.id , doc["email"].toString())

}

class MyViewHolder(val binding: SearchBinding) : RecyclerView.ViewHolder(binding.root)

class IdAdapter(private val context: Context, private var ids: List<ID>)
    : RecyclerView.Adapter<MyViewHolder>() {
    private val db: FirebaseFirestore = Firebase.firestore
    private val idsCollectionRef = db.collection("users")


    fun interface OnItemClickListener {
        fun onItemClick(student_id: String)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        itemClickListener = listener
    }

    fun updateList(newList: List<ID>) {
        ids = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: SearchBinding = SearchBinding.inflate(inflater, parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = ids[position]
        holder.binding.userId.text = item.email
        holder.binding.followBtn.setOnClickListener {
            val usermail = Firebase.auth.currentUser?.email.toString()
            idsCollectionRef.document(item.id).update("followers",FieldValue.arrayUnion(usermail))
            idsCollectionRef.document(usermail).update("followings",FieldValue.arrayUnion(item.email))

        }
        /* holder.binding.textID.setOnClickListener {
             //AlertDialog.Builder(context).setMessage("You clicked ${student.name}.").show()
             itemClickListener?.onItemClick(item.id)
         }*/
        /*holder.binding.textName.setOnClickListener {
            //AlertDialog.Builder(context).setMessage("You clicked ${student.name}.").show()
            itemClickListener?.onItemClick(item.id)
        }*/
    }

    override fun getItemCount() = ids.size
}