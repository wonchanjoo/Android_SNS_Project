package com.example.sns_project

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.sns_project.databinding.ActivityPostingBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PostingActivity : AppCompatActivity() {
    lateinit var binding: ActivityPostingBinding

    // 이미지 변수 선언(null로 초기화), 게시할때 이게 null이면 사진 없는거임
    private var selectedImageURI : Uri? = null

    // firebase storage
    private val storage by lazy {
        Firebase.storage
    }
    // firebase database
    private val db: FirebaseFirestore = Firebase.firestore
    private val postsCollectionRef = db.collection("posts")

    companion object {
        const val REQUEST_GET_IMAGE = 2000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 권한
        requestSinglePermission(Manifest.permission.READ_EXTERNAL_STORAGE)

        // 이미지 삽입 버튼 클릭
        binding.uploadPictureBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_GET_IMAGE) //registerForActivityResult(
        }

        // 게시 버튼 클릭
        binding.postBtn.setOnClickListener {
            val writing = binding.postingContent.text.toString()

            // 게시글 내용 있는지 검사, 없으면 입력하라고 알림
            if (writing.trim().isEmpty()) //writing.length==0 //writing.equals("") //writing == ""
                Toast.makeText(this, "텍스트를 입력해주세요.", Toast.LENGTH_SHORT).show()

            // 사진 있는지 검사, 없으면 사진 선택하라고 알림
            else if (selectedImageURI == null)
                Toast.makeText(this, "사진을 선택해주세요.", Toast.LENGTH_SHORT).show()

            // 글,사진 모두 있는 경우 - firebase에 업로드 -> feedActivity로 이동
            else {
                // writing, selectedImage(URI) + date(timestamp), userID(publisher) firebase에 저장
                //val imgUrl = getImgUrl(selectedImageURI!!)
                uploadPhoto(selectedImageURI!!,
                    mSuccessHandler = { uri ->
                        // date
                        val timestamp = Timestamp.now()

                        // publisher
                        val userEmail = intent.getStringExtra("userEmail")

                        uploadPost(timestamp, uri, userEmail!!, writing)
                    },
                    mErrorHandler = {
                        Toast.makeText(this, "게시글 업로드에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // 뒤로가기 버튼 클릭 -> FeedActivity로 이동
        binding.toFeedBtn.setOnClickListener {
            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /*
    private fun getImgUrl(imgUri: Uri) : String {
        // storage에 이미지 저장하고 url 가져오기 (database에 경로 저장하기 위해)
        //storage = Firebase.storage
        //val storageRef = storage.reference
        val fileName = "${System.currentTimeMillis()}.jpg" // png?
        val imageRef = storage.reference.child(fileName)
        lateinit var imgUrl : Uri

        Log.d("test", "imageRef:"+imageRef)
        Log.d("test", "imgUri:"+imgUri);
        imageRef.putFile(imgUri)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    Log.d("test", "4");
                    imageRef.downloadUrl.addOnSuccessListener { imgUrl = it }
                    Log.d("test", "5");
                    Toast.makeText(this, imgUrl.toString(), Toast.LENGTH_SHORT).show()
                    Log.d("test", "6");
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
                Log.d("test", "putFile Fail")
            }
        return imgUrl.toString()
    }
    */
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
                        .addOnSuccessListener { uri ->
                            //mSuccessHandler(uri.toString())
                            mSuccessHandler(fileName)
                        }.addOnFailureListener {
                            mErrorHandler()
                        }
                } else {
                    mErrorHandler()
                }
            }
    }
    private fun uploadPost(timestamp: Timestamp, uri: String, userEmail: String, writing: String) {
        val postMap = hashMapOf( // 여러 자식(키,값)을 한번에 쓰기
            "date" to timestamp,
            "image" to uri,
            "like" to 0,
            "publisher" to userEmail,
            "text" to writing
        )

        postsCollectionRef.add(postMap) // document id 지정 : postsCollectionRef.document(postID).set(postMap)
            .addOnSuccessListener {
                Toast.makeText(this, "upload success", Toast.LENGTH_SHORT).show()
                // feedActivity로 이동
                val intent = Intent(this, FeedActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
            .addOnFailureListener { Toast.makeText(this, "upload failure", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "upload failure", Toast.LENGTH_SHORT).show()
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
                selectedImageURI = data?.data
                if( selectedImageURI != null ) {
                    val imageBtn = binding.uploadPictureBtn
                    imageBtn.setImageURI(selectedImageURI)
                }else {
                    Toast.makeText(this,"이미지를 가져오지 못했습니다1",Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this,"이미지를 가져오지 못했습니다2",Toast.LENGTH_SHORT).show()
            }
        }
    }
}