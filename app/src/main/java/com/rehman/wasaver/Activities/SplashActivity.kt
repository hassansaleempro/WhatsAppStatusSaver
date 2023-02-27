package com.rehman.wasaver.Activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.rehman.wasaver.R

class SplashActivity : AppCompatActivity() {

    private lateinit var logo: RelativeLayout
    private lateinit var bottom: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val topAnim = AnimationUtils.loadAnimation(this, R.anim.top_anim)
        val bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim)
        logo = findViewById(R.id.logo)
        bottom = findViewById(R.id.progressBar)
        logo.startAnimation(topAnim)
        bottom.startAnimation(bottomAnim)


        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, DashboardActivity::class.java))
            overridePendingTransition(R.anim.fade_in_anim, R.anim.fade_out_anim)
            finish()
        }, 3000)


    }
}