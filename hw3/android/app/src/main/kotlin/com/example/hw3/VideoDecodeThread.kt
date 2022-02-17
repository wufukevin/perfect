package com.example.hw3

import android.annotation.SuppressLint
import android.content.res.AssetFileDescriptor
import android.media.*
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import com.example.hw3.AudioTime


class VideoDecodeThread : Thread() {

    companion object {
        private const val VIDEO = "video/"
        const val TAG = "VideoDecoder"
    }

    private lateinit var extractor: MediaExtractor
    private lateinit var decoder: MediaCodec
    private lateinit var audioTime: AudioTime

    private var isStop = false
    private var isOver = false

    @RequiresApi(Build.VERSION_CODES.N)
    fun init(surface: Surface, file: AssetFileDescriptor, aAudioTime: AudioTime){
        isStop = false
        audioTime = aAudioTime
        try {
            extractor = MediaExtractor()

            try {
//                var path = Environment.getExternalStorageDirectory().path +"/DCIM/sample4k.mp4"
//                extractor.setDataSource(path)
                extractor.setDataSource(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //create video decoder
            (0..extractor.trackCount).forEach { index ->
                val format = extractor.getTrackFormat(index)

                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith(VIDEO) == true) {
                    extractor.selectTrack(index)
                    decoder = MediaCodec.createDecoderByType(mime)
                    try {
                        Log.d(TAG, "format : $format")
                        decoder.configure(format, surface, null, 0 /* Decode */)
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "codec $mime failed configuration. $e")
                    }
                    decoder.start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongConstant")
    override fun run() {
        val newBufferInfo = MediaCodec.BufferInfo()
        var keepDequeOutputBuffer = true
        var outIndex = 1

        while (isOver.not()) {
            while (isStop.not()) {
                //dequeue input buffer
                decoder.dequeueInputBuffer(1000).takeIf { it >= 0 }?.let { index ->
                    val inputBuffer = decoder.getInputBuffer(index)
                    val sampleSize = extractor.readSampleData(inputBuffer!!, 0)
                    if (extractor.advance() && sampleSize > 0) {
                        decoder.queueInputBuffer(index, 0, sampleSize, extractor.sampleTime, 0)
                    } else {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM")
                        decoder.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    }
                }


                //queue output buffer
                if (keepDequeOutputBuffer)  outIndex = decoder.dequeueOutputBuffer(newBufferInfo, 1000)
                when (outIndex) {
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + decoder.outputFormat)
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        Log.d(TAG, "INFO_TRY_AGAIN_LATER")
                    }
                    else -> {
                        val videoCurrentTime = newBufferInfo.presentationTimeUs
                        val audioCurrentTime = audioTime.getAudioTime()
                        val sleepTime: Long = (videoCurrentTime - audioCurrentTime)/1000
                        var render  = true

//                        Log.d("kevin", "video: $videoCurrentTime // audio: $audioCurrentTime // sleep: $sleepTime")

                        if (sleepTime > 10 ){
                            // if video faster than audio, let video wait another loop
                            Log.d("kevin", "video: $videoCurrentTime // audio: $audioCurrentTime // sleep: $sleepTime  pause")
                            keepDequeOutputBuffer = false
                            break
                        }
                        else if (sleepTime < -60){
                            // if video slower than audio too much , don't show the view
                            Log.d("kevin", "video: $videoCurrentTime // audio: $audioCurrentTime // sleep: $sleepTime  drop")
                            render = false
                        }
                        else {
                            Log.d("kevin", "video: $videoCurrentTime // audio: $audioCurrentTime // sleep: $sleepTime")
                        }
                        keepDequeOutputBuffer = true
                        decoder.releaseOutputBuffer(outIndex, render)
                    }
                }
                // All decoded frames have been rendered, we can stop playing now
                if (newBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                    break
                }
            }
        }
        release()
    }

    fun release() {
        decoder.stop()
        decoder.release()
        extractor.release()
    }

    fun pause() {
        isStop = true
    }

    fun over() {
        isStop = true
        isOver = true
    }

    fun play() {
        isStop = false
    }

}