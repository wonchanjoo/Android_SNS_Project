package com.example.sns_project

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.sns_project.databinding.ActivitySignUpBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SignUpActivity : AppCompatActivity() {
    private val defaultFileName = "user.png"
    private lateinit var binding: ActivitySignUpBinding
    private var uri: Uri? = null
    private var filename: String = ""
    private val storage by lazy {
        Firebase.storage
    }
    private val db : FirebaseFirestore = Firebase.firestore

    companion object {
        const val REQUEST_GET_IMAGE= 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestSinglePermission(Manifest.permission.READ_EXTERNAL_STORAGE)

        binding.signUpBtn.setOnClickListener { signUpBtnClick() }
        binding.returnBtn.setOnClickListener { returnBtnClick() }
        binding.signupImage.setOnClickListener { addImage() }
    }

    private fun signUpBtnClick() {
        val email = binding.signupEmail.text.toString()
        val password = binding.signupPasswd.text.toString()
        val passwordConfirm = binding.signupPasswdConfirm.text.toString()

        // 이메일에 @가 없는 경우
        if(!email.contains("@")) {
            binding.warning.text = "이메일 형식이 올바르지 않습니다."
            binding.signupEmail.setText("")
            binding.signupEmail.focusable
            return
        }
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
        // 비밀번호 확인이 맞은 경우 - 회원가입 성공!
        else {
            Firebase.auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (uri == null) {
                        filename = defaultFileName
                        addAccountToDatabase(email)
                        startActivity(
                            Intent(
                                this,
                                LoginActivity::class.java
                            )
                        ) // LoginActivity로 돌아가기
                        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        uploadPhoto(uri!!,
                            mSuccessHandler = { filename ->
                                this.filename = filename
                                addAccountToDatabase(email)// Fire store에 계정 정보 넣기
                                startActivity(
                                    Intent(
                                        this,
                                        LoginActivity::class.java
                                    )
                                ) // LoginActivity로 돌아가기
                                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                                finish()
                            },
                            mErrorHandler = {

                            })
                    }
                } else
                {
                    binding.warning.text = "이미 존재하는 이메일입니다."
                    binding.signupEmail.setText("")
                    binding.signupPasswd.setText("")
                    binding.signupPasswdConfirm.setText("")
                    binding.signupEmail.focusable
                }
            }
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
            "uid" to uid,
            "image" to filename,
            "followers" to emptyList<String>(),
            "followings" to emptyList<String>(),
            "like_posts" to emptyList<String>()
        )
        val db: FirebaseFirestore = Firebase.firestore
        val usersCollectionRef = db.collection("users")
        usersCollectionRef.document(email).set(userMap)
            .addOnSuccessListener { Log.e("SignUpActivity", "DB에 계정 추가 성공") }
            .addOnFailureListener { Log.e("SignUpActivity", "DB에 계정 추가 실패") }
    }

    private fun addImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GET_IMAGE)
    }

    private fun uploadPhoto(
        imageURI: Uri,
        mSuccessHandler: (String) -> Unit,
        mErrorHandler: () -> Unit,
    ) {
        val fileName = "${System.currentTimeMillis()}.jpg"
        storage.reference.child(fileName)
            .putFile(imageURI)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    // 파일 업로드에 성공했기 때문에 파일을 다시 받아 오도록 해야함
                    storage.reference.child(fileName).downloadUrl
                        .addOnSuccessListener {
                            mSuccessHandler(fileName)
                        }.addOnFailureListener {
                            mErrorHandler()
                        }
                } else {
                    mErrorHandler()
                }
            }
    }

    private fun requestSinglePermission(permission: String) { // 한번에 하나의 권한만 요청하는 예제
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) // 권한 유무 확인
            return
        val requestPermLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { // 권한 요청 컨트랙트
            if (it == false) { // permission is not granted!
                AlertDialog.Builder(this).apply {
                    setTitle("Warning")
                    setMessage(getString(R.string.no_permission, permission))
                }.show()
            }
        }
        if (shouldShowRequestPermissionRationale(permission)) { // 권한 설명 필수 여부 확인
            // you should explain the reason why this app needs the permission.
            AlertDialog.Builder(this).apply {
                setTitle("Reason")
                setMessage(getString(R.string.req_permission_reason, permission))
                setPositiveButton("동의") { _, _ -> requestPermLauncher.launch(permission) }
                setNegativeButton("거부") { _, _ -> }
            }.show()
        } else {
            // should be called in onCreate()
            requestPermLauncher.launch(permission) // 권한 요청 시작
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode != Activity.RESULT_OK) {
            return
        }
        when(requestCode){
            REQUEST_GET_IMAGE -> {
                uri = data?.data
                if( uri != null ) {
                    binding.signupImage.setImageURI(uri)
                }else {
                    Toast.makeText(this,"이미지를 가져오지 못했습니다1", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this,"이미지를 가져오지 못했습니다2", Toast.LENGTH_SHORT).show()
            }
        }
    }
}