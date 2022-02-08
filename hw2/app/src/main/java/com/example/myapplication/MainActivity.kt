package com.example.myapplication

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import com.example.myapplication.video.VideoDecodeActivity
import com.example.myapplication.audio.AACAudioDecoderThread


class MainActivity : AppCompatActivity() {
//    private val audioDecoder = AACAudioDecoderThread()
//    private val audioPathAAC by lazy {
//        resources.openRawResourceFd(R.raw.sample4)
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()

        val videoButton = findViewById<Button>(R.id.videoButton)
//        val audioButton = findViewById<Button>(R.id.audioButton)

        videoButton.setOnClickListener {
            startActivity(VideoDecodeActivity.newIntent(this))
        }

//        audioButton.setOnClickListener {
//            audioDecoder.startPlay(audioPathAAC)
//        }
    }
}