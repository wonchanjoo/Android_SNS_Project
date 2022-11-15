package com.example.sns_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.sns_project.databinding.ActivitySignUpBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpBtn.setOnClickListener { signUpBtnClick() }
        binding.returnBtn.setOnClickListener { returnBtnClick() }
    }

    private fun signUpBtnClick() {
        val email = binding.signupEmail.text.toString()
        val password = binding.signupPasswd.text.toString()
        Firebase.auth.createUserWithEmailAndPassword(email, password) // 회원가입하고
        startActivity(Intent(this, LoginActivity::class.java)) // LoginActivity로 돌아가기
        finish()
    }

    private fun returnBtnClick(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}