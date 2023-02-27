package com.rehman.wasaver.Activities

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.palette.graphics.Palette
import com.rehman.wasaver.R
import com.rehman.wasaver.databinding.ActivityViewVideoBinding

class ViewVideoActivity : AppCompatActivity() {

    lateinit var binding: ActivityViewVideoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_video)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_video)
        binding.back.setOnClickListener {
            onBackPressed()
        }

        val fileUri = intent.getStringExtra("fileUri")
        val video = Uri.parse(fileUri)

        try {
            val mediaController = MediaController(this)
            mediaController.setAnchorView(binding.videoView)

            binding.videoView.setVideoURI(video)
            binding.videoView.setOnPreparedListener {
                binding.videoView.start()
            }
            binding.videoView.setOnCompletionListener {
                binding.videoView.start()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.videoView.setOnClickListener(View.OnClickListener {

            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.wpPlay.visibility = View.VISIBLE
            } else {
                binding.videoView.start()
                binding.wpPlay.visibility = View.GONE
            }
        })


        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, video)
        val bitmap = retriever.getFrameAtTime(0)


        Palette.Builder(bitmap!!).maximumColorCount(15).generate { palette ->
            val mutedColor = palette!!.getMutedColor(0)
            val lightVibrantColor = palette.getLightVibrantColor(0)
            val vibrantColor = palette.getLightMutedColor(0);


            val gradientDrawable = GradientDrawable(
                GradientDrawable.Orientation.BOTTOM_TOP,
                intArrayOf(mutedColor, lightVibrantColor, vibrantColor)
            )

            binding.root.background = gradientDrawable

            val colorAnimation =
                ValueAnimator.ofArgb(mutedColor, lightVibrantColor, vibrantColor)
            colorAnimation.duration = 10000
            colorAnimation.repeatCount = ValueAnimator.INFINITE
            colorAnimation.repeatMode = ValueAnimator.REVERSE
            colorAnimation.addUpdateListener { animator ->
                val animatedColor = animator.animatedValue as Int
                gradientDrawable.colors = intArrayOf(lightVibrantColor, animatedColor)
                binding.root.background = gradientDrawable
            }
            colorAnimation.start()
        }


    }
}
