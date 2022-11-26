package com.example.sns_project_watch

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.sns_project_watch.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.loginBtn.setOnClickListener {
            val email = binding.emailEdit.text.toString()
            val password = binding.passwordEdit.text.toString()
            Log.e("LoginActivity", "email = ${email} password = ${password}")

            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) {
                    if(it.isSuccessful) {
                        Log.e("LoginActivity", "로그인 성공")
                        startActivity(Intent(this, FeedActivity::class.java)) // FeedActivity로 이동
                        finish()
                    } else {
                        Log.e("LoginActivity", "로그인 실패")
                        binding.loginWarning.text = "로그인 실패!"
                    }
                }
        }
    }
}