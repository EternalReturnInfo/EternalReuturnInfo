package com.erionna.eternalreturninfo.ui.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.erionna.eternalreturninfo.databinding.SignupImageActivityBinding

class SignUpImagePage : AppCompatActivity() {
    private lateinit var binding:SignupImageActivityBinding
    private lateinit var selectedImageURI : Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SignupImageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupimgNextBtn.setOnClickListener {
            val intent = Intent(this,SignUpPage::class.java)
            intent.putExtra("uri",selectedImageURI)
            startActivity(intent)
            finish()
        }
    }



}