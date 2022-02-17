package com.example.hw3

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.lang.Exception

class MainActivity: FlutterActivity() {
    private val CHANNEL = "VideoCall"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        Log.d("kevin_hw3", "android in onfigureFlutterEngine")

        val videoPath = resources.openRawResourceFd(R.raw.sample3)
        val videoDecoder = VideoDecodeThread()
        val audioDecoder = AudioDecodeThread()
        val audioTime = AudioTime()

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler{ call, result ->
            Log.d("kevin_hw3", "android in methodHandler")
//            val mediaPlayer = MediaPlayer.create(context, R.raw.sample3)
            // why texture need to announce in methodChannel or app will crash cause lose connect with phone ????????
            val surfaceTextureEntry = flutterEngine.renderer.createSurfaceTexture()
            val surface = Surface(surfaceTextureEntry?.surfaceTexture())
            val textureID = surfaceTextureEntry.id()

            when (call.method) {
                "initial Video" -> {
                    Log.d("kevin_hw3", "initial video in textureID: $textureID")
                    audioTime.setAudioTime(0)
                    audioDecoder.setAudioTimeToAudioThread(audioTime)
                    videoDecoder.init(surface, videoPath, audioTime)
                    videoDecoder.start()
                    audioDecoder.startPlay(videoPath)
                }
                "play Video" -> {
                    Log.d("kevin_hw3", "play video in textureID: $textureID")
                    videoDecoder.play()
                    audioDecoder.play()
                }
                "stop Video" -> {
                    Log.d("kevin_hw3", "stop video in textureID: $textureID")
                    videoDecoder.pause()
                    audioDecoder.pause()
                }
            }

//            Thread{
//                mediaPlayer.setSurface(surface)
//                mediaPlayer.start()
//            }.start()

            result.success(textureID)
        }
    }
}


