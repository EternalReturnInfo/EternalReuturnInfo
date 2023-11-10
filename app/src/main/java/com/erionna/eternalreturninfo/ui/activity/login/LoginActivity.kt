package com.erionna.eternalreturninfo.ui.activity.login

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.BuildConfig
import com.erionna.eternalreturninfo.databinding.FindpwDialogBinding
import com.erionna.eternalreturninfo.databinding.GoogleDialogBinding
import com.erionna.eternalreturninfo.databinding.LoginActivityBinding
import com.erionna.eternalreturninfo.databinding.LoginGoogleNicknameDialogBinding
import com.erionna.eternalreturninfo.model.ERModel
import com.erionna.eternalreturninfo.model.SignUpData
import com.erionna.eternalreturninfo.retrofit.BoardSingletone
import com.erionna.eternalreturninfo.retrofit.RetrofitInstance
import com.erionna.eternalreturninfo.ui.activity.login.SignUpActivity.Companion.collection
import com.erionna.eternalreturninfo.ui.activity.main.MainActivity
import com.erionna.eternalreturninfo.util.Constants
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: LoginActivityBinding
    private lateinit var findpwBinding: FindpwDialogBinding
    private lateinit var googleNicknameBinding: LoginGoogleNicknameDialogBinding
    private var auth: FirebaseAuth? = FirebaseAuth.getInstance()
    var firestore: FirebaseFirestore? = FirebaseFirestore.getInstance()
    var mGoogleSignInClient: GoogleSignInClient? = null
    var db = Firebase.firestore
    var key = BuildConfig.google_Token
    private lateinit var database: DatabaseReference
    var emailCheck = false
    var nickNameCheck = false
    var signupNickname = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = Firebase.database.reference
        binding = LoginActivityBinding.inflate(layoutInflater)
        findpwBinding = FindpwDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //구글로그인 기본 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(key)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        //로그인 버튼
        binding.loginLoginBtn.setOnClickListener {
            Login(binding.loginIDEt.text.toString(), binding.loginPWEt.text.toString())
        }

        //비밀번호찾기 다이얼로그 버튼
        binding.loginLostBtn.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.findpw_dialog, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            val email = dialogView.findViewById<EditText>(R.id.findpw_id_et).text
            val button = dialogView.findViewById<Button>(R.id.findpw_findpw_btn)

            // 비밀번호찾기 이메일 전송 버튼
            button.setOnClickListener {
                if (email.isNotEmpty()) {
                    auth?.sendPasswordResetEmail(email.toString())
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this,
                                    email.toString() + R.string.login_lostpw_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(this, R.string.login_lostpw_fail, Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(this, R.string.login_lostpw_null, Toast.LENGTH_SHORT).show()
                }
            }
            alertDialog.show()
        }

        //회원가입 버튼
        binding.loginSignupBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        //구글로그인 다이얼로그 버튼
//        binding.loginSnsLoginBtn.setOnClickListener {
//            val intent = mGoogleSignInClient!!.signInIntent
//            startActivityForResult(intent, 100)
//        }
    }

    // 일반 이메일 로그인
    private fun Login(email: String, pw: String) {
        if (email.isNotEmpty() && pw.isNotEmpty()) {
            auth?.signInWithEmailAndPassword(email, pw)?.addOnCompleteListener {
                if (it.isSuccessful) {
                    val docRef = db.collection(collection).document("$email")
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT)
                                    .show()
                                BoardSingletone.Login()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                            }
                        }
                        .addOnFailureListener { exception ->
                        }
                } else {
                    Toast.makeText(this, R.string.login_fail, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, R.string.login_fail_null, Toast.LENGTH_SHORT).show()
        }
    }

    /////
    // 구글 로그인
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val google = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(google.idToken, null)
            auth?.signInWithCredential(credential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        GoogleLogin()
                    } else {
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    @SuppressLint("MissingInflatedId")
    fun GoogleLogin() {
        val intent = Intent(this, MainActivity::class.java)
        var email = auth!!.currentUser?.email
        var pw = auth?.currentUser?.providerData?.get(0)?.providerId
        var profile = auth!!.currentUser?.photoUrl.toString()

        email?.let { GoogleLoginCheck(it) }
        if (emailCheck) {
            startActivity(intent)
        } else {
            val googleNicknameDialogView =
                layoutInflater.inflate(R.layout.login_google_nickname_dialog, null)
            val googleNicknameDialog = AlertDialog.Builder(this)
                .setView(googleNicknameDialogView)
                .create()

            val nickName =
                googleNicknameDialogView.findViewById<EditText>(R.id.googlelogin_nickname_et)
            val loginBtn =
                googleNicknameDialogView.findViewById<Button>(R.id.googlelogin_nickname_btn)
            val checkBtn =
                googleNicknameDialogView.findViewById<Button>(R.id.googlelogin_btn_nickname_check)

            checkBtn.setOnClickListener {
                NickNameCheck(nickName.text.toString())
            }
            loginBtn.setOnClickListener {
                auth?.uid?.let {
                    db.collection(collection).document(it).set(
                        SignUpData(
                            email = email.toString(),
                            pw = pw.toString(),
                            nickName = signupNickname,
                            character = "",
                            profile = profile
                        )
                    )
                }
                auth?.uid?.let {
                    database.child("user").child(it)
                        .setValue(
                            ERModel(
                                profilePicture = profile,
                                email = email,
                                password = pw.toString(),
                                name = signupNickname,
                                uid = auth?.uid
                            )
                        )
                }
                googleNicknameDialog.dismiss()
                startActivity(intent)
            }
            googleNicknameDialog.show()
        }
    }

    fun GoogleLoginCheck(email: String) {
        emailCheck = false
        db.collection(collection)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (email == document.data["email"].toString()) {
                        emailCheck = true
                    }
                }
            }
    }

    fun NickNameCheck(nickname: String) {
        nickNameCheck = false
        db.collection(collection)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (nickname == document.data["nickName"]) {
                        nickNameCheck = true
                    }
                }
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val nickname = googleNicknameBinding.googleloginNicknameEt.text.toString()

                        //수정 : 로그인한 사람 닉네임 가져오기
                        val userID_call = RetrofitInstance.search_userID_api.getUserByNickname(
                            Constants.MAIN_APIKEY,
                            nickname
                        )
                        val userID_response = userID_call.execute()
                        if (userID_response.isSuccessful) {
                            val gameResponse = userID_response.body()
                            withContext(Dispatchers.Main) {
                                if (gameResponse?.user == null) {
                                    googleNicknameBinding.googleloginBtnNicknameCheck.visibility =
                                        View.VISIBLE
                                    googleNicknameBinding.googleloginBtnNicknameCheck.setTextColor(
                                        ContextCompat.getColor(
                                            this@LoginActivity,
                                            R.color.highlight_color2
                                        )
                                    )
                                    googleNicknameBinding.googleloginBtnNicknameCheck.text =
                                        "닉네임이 존재하지 않습니다."
                                    signupNickname = ""
                                } else if (nickNameCheck) {
                                    googleNicknameBinding.googleloginBtnNicknameCheck.visibility =
                                        View.VISIBLE
                                    googleNicknameBinding.googleloginBtnNicknameCheck.setTextColor(
                                        ContextCompat.getColor(
                                            this@LoginActivity,
                                            R.color.highlight_color2
                                        )
                                    )
                                    googleNicknameBinding.googleloginBtnNicknameCheck.text =
                                        "중복된 닉네임입니다."
                                    signupNickname = ""
                                } else {
                                    googleNicknameBinding.googleloginBtnNicknameCheck.visibility =
                                        View.VISIBLE
                                    googleNicknameBinding.googleloginBtnNicknameCheck.setTextColor(
                                        ContextCompat.getColor(
                                            this@LoginActivity,
                                            R.color.highlight_color
                                        )
                                    )
                                    googleNicknameBinding.googleloginBtnNicknameCheck.text =
                                        "사용가능한 닉네임입니다."
                                    signupNickname = nickname
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // 오류 처리
                        e.printStackTrace()
                    }
                }
            }
            .addOnFailureListener { exception ->
            }
    }
}
