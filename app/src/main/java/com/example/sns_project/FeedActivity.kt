package com.example.sns_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sns_project.databinding.ActivityFeedBinding

class FeedActivity : AppCompatActivity() {
    lateinit var binding : ActivityFeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.postingBtn.setOnClickListener {
            val intent = Intent(this, PostingActivity::class.java)
            startActivity(intent)
        }
        binding.friendBtn.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }
    }
}