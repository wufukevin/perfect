package com.example.myapplication.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.*
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.audio.AudioDecoderThread

import com.example.myapplication.AudioTime

class VideoDecodeActivity : AppCompatActivity(), SurfaceHolder.Callback {

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, VideoDecodeActivity::class.java)
    }

    private lateinit var videoDecoder: VideoDecodeThread
    private lateinit var audioDecoder: AudioDecoderThread
    private lateinit var surfaceView: SurfaceView
    private lateinit var pauseAndPlayButton: Button
    private lateinit var audioTime: AudioTime

    private val videoPath by lazy {
        resources.openRawResourceFd(R.raw.sample3)
    }
    private var playVideo = true


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_video)

        videoDecoder = VideoDecodeThread()
        audioDecoder = AudioDecoderThread()
        surfaceView = findViewById(R.id.surfaceView)
        surfaceView.holder.addCallback(this@VideoDecodeActivity)
        pauseAndPlayButton = findViewById(R.id.pauseButton)
        audioTime = AudioTime()

    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        pauseAndPlayButton.setOnClickListener {
            if(playVideo){
                videoDecoder.pause()
                audioDecoder.pause()
                playVideo = false
                pauseAndPlayButton.text = "Play"
            }
            else{
                videoDecoder.play()
                audioDecoder.play()
                playVideo = true
                pauseAndPlayButton.text = "Pause"
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        audioTime.setAudioTime(0)
        audioDecoder.setAudioTimeToAudioThread(audioTime)

        //start play video and audio
        videoDecoder.init(holder.surface, videoPath, audioTime)
        videoDecoder.start()
        audioDecoder.startPlay(videoPath)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        videoDecoder.over()
        videoDecoder.release()
        audioDecoder.over()
        audioDecoder.release()
    }

    override fun onPause() {
        super.onPause()
        videoDecoder.over()
        videoDecoder.release()
        audioDecoder.over()
        audioDecoder.release()
    }
}