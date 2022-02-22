package com.example.hw3

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val TEXTURE_CHANNEL = "VideoCall"
    private val PROGRESS_CHANNEL = "VideoProgress"
    private val SEEK_CHANNEL = "Seek"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        val videoPath = resources.openRawResourceFd(R.raw.sample3)
        val videoDecoder = VideoDecodeThread()
        val audioDecoder = AudioDecodeThread()
        val audioTime = AudioTime()

        //use flutterEngine.getDartExecutor().getBinaryMessenger() as the binary messenger rather than getFlutterView()
        //https://github.com/flutter/flutter/wiki/Upgrading-pre-1.12-Android-projects
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, TEXTURE_CHANNEL).setMethodCallHandler{ call, result ->
            // why texture need to announce in methodChannel or app will crash cause lose connect do device ????????
            val surfaceTextureEntry = flutterEngine.renderer.createSurfaceTexture()
            val surface = Surface(surfaceTextureEntry?.surfaceTexture())
            val textureID = surfaceTextureEntry.id()

            when (call.method) {
                "initial Video" -> {
                    audioTime.setAudioTime(0)
                    audioDecoder.setAudioTimeToAudioThread(audioTime)
                    videoDecoder.init(surface, videoPath, audioTime)
                    videoDecoder.start()
                    audioDecoder.startPlay(videoPath)

                    val fileDurationInSecond = (videoDecoder.fileDuration()/1000000).toFloat()
                    val map = mapOf("textureID" to textureID, "fileDuration" to fileDurationInSecond )
                    result.success(map)
                }
                "play Video" -> {
                    videoDecoder.play()
                    audioDecoder.play()

                    result.success(textureID)
                }
                "stop Video" -> {
                    videoDecoder.pause()
                    audioDecoder.pause()

                    result.success(textureID)
                }
            }
        }

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, PROGRESS_CHANNEL).setStreamHandler(
            object : EventChannel.StreamHandler {
                private var sink: EventChannel.EventSink? = null
                private var handler = Handler(Looper.getMainLooper())

                val runnable = Runnable {
                    sendCurrentProgress()
                }
                fun sendCurrentProgress(){
                    val currentTimeInSecond = (videoDecoder.currentTime()/1000000).toFloat()
                    sink?.success(currentTimeInSecond)
                    handler.postDelayed(runnable, 500)
                }
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    sink = events
                    handler.post(runnable)
                }
                override fun onCancel(arguments: Any?) {
                    sink = null
                    handler.removeCallbacks(runnable)
                }
            }
        )

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, SEEK_CHANNEL).setMethodCallHandler{ call, result ->
            when (call.method) {
                "seek video" -> {
                    val seekedTime = call.argument("time") as Double?
                    val seekedTimeInUS = seekedTime?.toLong()?.times(1000000)
                    videoDecoder.seekTo(seekedTimeInUS!!)
                    audioDecoder.seekTo(seekedTimeInUS)

                    result.success(seekedTime)
                }
            }
        }
    }
}


