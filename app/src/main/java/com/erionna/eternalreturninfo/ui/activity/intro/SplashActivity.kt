package com.erionna.eternalreturninfo.ui.activity.intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.erionna.eternalreturninfo.R
import com.erionna.eternalreturninfo.ui.activity.LoginPage

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        Handler().postDelayed({
            val intent = Intent(this, LoginPage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        },2000)
    }
}