package com.example.myapplication.audio

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
//import com.example.myapplication.databinding.AudioActivityBinding


class AudioDecodeActivity : AppCompatActivity() {

    companion object {

        fun newIntent(context: Context): Intent =
            Intent(context, AudioDecodeActivity::class.java)
    }

    private val audioDecoder: AudioDecoderThread by lazy {
        AudioDecoderThread()
    }

    private val audioPathAAC by lazy {
        resources.openRawResourceFd(R.raw.sample_aac)
    }

//    @RequiresApi(Build.VERSION_CODES.N)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val binding = AudioActivityBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.playAac.setOnClickListener {
//            audioDecoder.startPlay(audioPathAAC)
//        }
//    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_audio)

        val playButton = findViewById<Button>(R.id.playButton)
        playButton.setOnClickListener {
            audioDecoder.startPlay(audioPathAAC)
        }
    }

    override fun onPause() {
        super.onPause()
        audioDecoder.pause()
    }
}