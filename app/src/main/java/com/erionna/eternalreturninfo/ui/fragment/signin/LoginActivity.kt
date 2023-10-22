package com.erionna.eternalreturninfo.ui.fragment.signin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.databinding.LoginActivityBinding
import com.erionna.eternalreturninfo.ui.fragment.findduo.FindDuoFragment
import com.erionna.eternalreturninfo.ui.fragment.signup.SignUpActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginActivityBinding
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView() = with(binding) {

        mAuth = Firebase.auth


        //회원가입버튼
        signupBtn.setOnClickListener {

            var intent: Intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)

        }


        loginBtn.setOnClickListener {

            val email = logInEmail.text.toString().trim()
            val pw = logInPassword.text.toString().trim()

            login(email, pw)
        }

        logoutBtn.setOnClickListener {
            mAuth.signOut()
            Toast.makeText(
                this@LoginActivity,
                mAuth.currentUser?.uid.toString(),
                Toast.LENGTH_SHORT
            )
                .show()
        }

        checkBtn.setOnClickListener {
            Toast.makeText(
                this@LoginActivity,
                mAuth.currentUser?.uid.toString(),
                Toast.LENGTH_SHORT
            )
                .show()
        }

    }

    private fun login(email: String, pw: String) {

        mAuth.signInWithEmailAndPassword(email, pw)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    Toast.makeText(this@LoginActivity, "로그인 완료", Toast.LENGTH_SHORT).show()


                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                    Log.d("Login", "Error:${task.exception}")
                }
            }
    }


}