package com.example.myapplication

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import com.example.myapplication.video.VideoDecodeActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()

        val videoButton = findViewById<Button>(R.id.videoButton)

        videoButton.setOnClickListener {
            startActivity(VideoDecodeActivity.newIntent(this))
        }
    }
}