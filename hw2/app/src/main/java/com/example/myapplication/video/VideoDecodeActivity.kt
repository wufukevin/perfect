package com.example.myapplication.video

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
//K: what is surfaceHolder??
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R


class VideoDecodeActivity : AppCompatActivity(), SurfaceHolder.Callback {

    companion object {

        fun newIntent(context: Context): Intent =
            //K :??
            Intent(context, VideoDecodeActivity::class.java)
    }

    //K: where to find VideoDecodeThread??
    private var videoDecode: VideoDecodeThread? = null

    //K: lazy : val 第一使用時才執行
    private val videoPath by lazy {
        resources.openRawResourceFd(R.raw.sample_video)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SurfaceView(this).apply {
            //K: ??
            holder.addCallback(this@VideoDecodeActivity)
            setContentView(this)

            videoDecode = VideoDecodeThread()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (videoDecode?.init(holder.surface, videoPath) == true) {
            videoDecode?.start()
        } else {
            videoDecode = null
        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        videoDecode?.close()
    }

    override fun onPause() {
        super.onPause()
        videoDecode?.close()
    }
}