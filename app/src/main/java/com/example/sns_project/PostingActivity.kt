package com.example.sns_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sns_project.databinding.ActivityPostingBinding

class PostingActivity : AppCompatActivity() {
    lateinit var binding: ActivityPostingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}