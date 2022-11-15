package com.example.sns_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sns_project.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener { loginBtnClick() }
        binding.signUpBtn.setOnClickListener { signUpBtnClick() }
    }

    // 로그인 버튼 클릭
    private fun loginBtnClick() {
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()

        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if(it.isSuccessful) { // 로그인 성공
                    startActivity(Intent(this, FeedActivity::class.java)) // FeedActivity로 이동
                    finish()
                } else { // 로그인 실패

                }
            }
    }

    // 회원가입 버튼 클릭
    private fun signUpBtnClick() {
        startActivity(Intent(this, SignUpActivity::class.java)) // 회원가입 Activity로 이동
    }
}