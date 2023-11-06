package com.erionna.eternalreturninfo.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.SignupInformationActivityBinding
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.model.Notice
import com.erionna.eternalreturninfo.model.SignUpData
import com.erionna.eternalreturninfo.retrofit.BoardSingletone
import com.erionna.eternalreturninfo.retrofit.RetrofitInstance
import com.erionna.eternalreturninfo.ui.adapter.NoticeBannerListAdapter
import com.erionna.eternalreturninfo.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpPage : AppCompatActivity() {
    private lateinit var binding: SignupInformationActivityBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var selectedImageURI: Uri
    private val PICK_IMAGE = 1111
    private lateinit var database: DatabaseReference
    val storage = Firebase.storage
    var ImageCheck = 0

    var nickNameCheck = 0
    private var signup_nickname:String = ""

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupInformationActivityBinding.inflate(layoutInflater)
        auth = Firebase.auth
        database = Firebase.database.reference
        setContentView(binding.root)

        binding.signupProfileImg.setOnClickListener {
            selectProfile()
        }

        binding.signupBtnNicknameCheck.setOnClickListener {
            nicknameCheck(binding.signupNickNameEt.text.toString())
            Handler(Looper.getMainLooper()).postDelayed({
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val nickname = binding.signupNickNameEt.text.toString()
                        Log.d("닉네임","눌렸어?")
                        //수정 : 로그인한 사람 닉네임 가져오기
                        val userID_call = RetrofitInstance.search_userID_api.getUserByNickname(Constants.MAIN_APIKEY, nickname)
                        val userID_response = userID_call.execute()

                        if (userID_response.isSuccessful) {
                            val gameResponse = userID_response.body()
                            Log.d("닉네임","성공했어?")
                            withContext(Dispatchers.Main) {
                                if (gameResponse?.user == null) {
                                    binding.signupTvCheckMessage.visibility = View.VISIBLE
                                    binding.signupTvCheckMessage.setTextColor(ContextCompat.getColor(this@SignUpPage, R.color.highlight_color2))
                                    binding.signupTvCheckMessage.text = "닉네임이 존재하지 않습니다."
                                    signup_nickname = ""
                                } else if(nickNameCheck == 1){
                                    binding.signupTvCheckMessage.visibility = View.VISIBLE
                                    binding.signupTvCheckMessage.setTextColor(ContextCompat.getColor(this@SignUpPage, R.color.highlight_color2))
                                    binding.signupTvCheckMessage.text = "중복된 닉네임입니다."
                                    signup_nickname = ""
                                }else {
                                    binding.signupTvCheckMessage.visibility = View.VISIBLE
                                    binding.signupTvCheckMessage.setTextColor(ContextCompat.getColor(this@SignUpPage, R.color.highlight_color))
                                    binding.signupTvCheckMessage.text = "사용가능한 닉네임입니다."
                                    signup_nickname = gameResponse.user.nickname
                                }
                            }
                            Log.d("닉네임체크",signup_nickname)
                        }

                    } catch (e: Exception) {
                        // 오류 처리
                        e.printStackTrace()
                    }
                }
            }, 2000)

        }


        // 실험체 스피너
        val characterlist = resources.getStringArray(R.array.characterName)
        val adapter =
            ArrayAdapter<String>(this, R.layout.signup_spinner, R.id.spinner_tv, characterlist)
        var selectCharacter = characterlist[0]
        binding.signupCharacterSp.adapter = adapter

        binding.signupCharacterSp.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectCharacter = characterlist[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }


        binding.signupSignupBtn.setOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                createAccount(
                    binding.signupIDEt.text.toString(),
                    binding.signupPWEt.text.toString(),
                    binding.signupPWCheckEt.text.toString(),
                    signup_nickname,
                    selectCharacter,
//                selectedImageURI
                )
            }, 2000)
        }
    }


    private fun createAccount(
        email: String,
        password: String,
        passwordCheck: String,
        nickname: String,
        character: String,
//        uri: Uri
    ) {
        if (email.isNotEmpty() && password.isNotEmpty() && passwordCheck.isNotEmpty() && nickname.isNotEmpty()) {
            if (password == passwordCheck) {
                Log.d("닉네임체크", "$nickNameCheck")
                if (nickNameCheck == 0) {
                    auth?.createUserWithEmailAndPassword(email, password)
                        ?.addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this, "계정 생성 완료.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                var baseImage =
                                    "https://firebasestorage.googleapis.com/v0/b/eternalreturninfo-4dc4b.appspot.com/o/ic_baseImage.jpg?alt=media&token=50e58bfe-873f-4772-bddc-a3401dc3d8a3&_gl=1*lgw3h7*_ga*MjY4NTI2NjgxLjE2OTY5MzI3ODU.*_ga_CW55HF8NVT*MTY5OTIzNDQwMS42Ny4xLjE2OTkyMzQ2NjcuOS4wLjA."
                                setDocument(
                                    SignUpData(
                                        Email = email,
                                        PW = password,
                                        NickName = nickname,
                                        Character = character,
                                        profile = baseImage
                                    )
                                )
                                database.child("user").child(auth.uid!!)
                                    .setValue(
                                        ERModel(
                                            profilePicture = baseImage,
                                            email = email,
                                            password = password,
                                            name = nickname,
                                            uid = auth.uid!!
                                        )
                                    )
                                if (ImageCheck == 1) {
                                    upload(selectedImageURI, email)
                                }
                                var intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this, "계정 생성 실패",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "중복된 닉네임입니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "같은 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "회원가입 정보를 입력하세요", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setDocument(data: SignUpData) {
        FirebaseFirestore.getInstance()
            .collection("EternalReturnInfo")
            .document(auth.uid!!)
            .set(data)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
                Toast.makeText(this, "닉네임 값 저장 실패", Toast.LENGTH_SHORT).show()
            }
    }

    fun selectProfile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE) {
            val uri: Uri? = data?.data
            if (uri != null) {
                selectedImageURI = uri
                binding.signupProfileImg.setImageURI(uri)
                ImageCheck = 1
            }
        }
    }

    fun upload(
        uri: Uri,
        email: String,
    ) {
        val storageRef = storage.reference
        val fileName = email + ".jpg"
        val riversRef = storageRef.child("/$fileName")

        riversRef.putFile(uri)
            .addOnProgressListener { taskSnapshot ->
                riversRef.downloadUrl.addOnSuccessListener { uri ->
                    FirebaseFirestore.getInstance()
                        .collection("EternalReturnInfo")
                        .document(auth.uid!!)
                        .update("profile", uri.toString())
                }
            }
            .addOnFailureListener { Log.i("업로드 실패", "") }
            .addOnSuccessListener { Log.i("업로드 성공", "") }
    }

    fun nicknameCheck(nickname: String) {
        nickNameCheck = 0
        db.collection("EternalReturnInfo")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (nickname == document.data["nickName"]) {
                        nickNameCheck = 1
                    }
                }
            }
            .addOnFailureListener { exception ->
            }
    }

}

