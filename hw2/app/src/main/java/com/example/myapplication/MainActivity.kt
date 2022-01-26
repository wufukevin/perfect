package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.myapplication.video.VideoDecodeActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        var videoButton = findViewById<Button>(R.id.videoButton)


        videoButton.setOnClickListener {
            startActivity(VideoDecodeActivity.newIntent(this))
        }
    }
}