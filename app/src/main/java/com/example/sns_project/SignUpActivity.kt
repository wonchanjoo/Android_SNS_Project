package com.example.sns_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import com.example.sns_project.databinding.ActivitySignUpBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

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
        val passwordConfirm = binding.signupPasswdConfirm.text.toString()
        // 비밀번호가 6자 미만인 경우
        if(password.length < 6) {
            binding.warning.text = "비밀번호는 6자 이상이어야 합니다."
            binding.signupPasswd.setText("")
            binding.signupPasswdConfirm.setText("")
            binding.signupPasswd.focusable
            return
        }
        // 비밀번호 확인이 틀린 경우
        if(password != passwordConfirm) {
            binding.warning.text = "비밀번호가 맞지 않습니다."
            binding.signupPasswdConfirm.setText("")
            binding.signupPasswdConfirm.focusable
        }
        // 비밀번호 확인이 맞은 경우
        else {
            Firebase.auth.createUserWithEmailAndPassword(email, password) // 회원가입하고
            addAccountToDatabase(email)// Fire store에 계정 정보 넣기
            startActivity(Intent(this, LoginActivity::class.java)) // LoginActivity로 돌아가기
            finish()
        }
    }

    private fun returnBtnClick(){
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun addAccountToDatabase(email: String) {
        val uid = email.split("@")[0] // uid 분리
        val userMap = hashMapOf(
            "email" to email,
            "followers" to emptyList<String>(),
            "followings" to emptyList<String>(),
            "uid" to uid
        )
        val db: FirebaseFirestore = Firebase.firestore
        val usersCollectionRef = db.collection("users")
        usersCollectionRef.document(email).set(userMap)
            .addOnSuccessListener { Log.e("SignUpActivity", "DB에 계정 추가 성공") }
            .addOnFailureListener { Log.e("SignUpActivity", "DB에 계정 추가 실패") }
    }
}