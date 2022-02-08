package com.example.myapplication.video

import android.content.res.AssetFileDescriptor
import android.media.*
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer


class VideoDecodeThread : Thread() {

    companion object {
        private const val VIDEO = "video/"
        const val TAG = "VideoDecoder"
    }

    private lateinit var extractor: MediaExtractor
    private lateinit var decoder: MediaCodec



    private var isStop = false
    private var isOver = false

    //用extractor取得video type
    //建立對應的 decoder
    //start decoder
    @RequiresApi(Build.VERSION_CODES.N)
    fun init(surface: Surface, file: AssetFileDescriptor): Boolean {
        isStop = false
        try {
            extractor = MediaExtractor()
            extractor.setDataSource(file)

            //K: trackCount : Count the number of tracks found in the data source
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
                        return false
                    }

                    decoder.start()
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    override fun run() {
        val newBufferInfo = MediaCodec.BufferInfo()
        val inputBuffers: Array<ByteBuffer> = decoder.inputBuffers

        //K: 為啥不用宣告 outputBuffers ??
//        val inputBuffers: Array<ByteBuffer> = decoder.getInputBuffer()
        val outputBuffers: Array<ByteBuffer> = decoder.outputBuffers


        var isInput = true
        var isFirst = false
        var startWhen = 0L

        while (isOver.not()) {
            while (isStop.not()) {
                //K: read sampleData and put it in the input queue
                decoder.dequeueInputBuffer(1000).takeIf { it >= 0 }?.let { index ->
                    // fill inputBuffers[inputBufferIndex] with valid data
                    val inputBuffer = inputBuffers[index]

                    //K: readSampleData : Retrieve the current encoded sample and store it in the byte buffer starting at the given offset
                    val sampleSize = extractor.readSampleData(inputBuffer, 0)

                    //K: advance : Advance to the next sample. Returns false if no more sample data is available
                    if (extractor.advance() && sampleSize > 0) {
                        //K: where is the inputBuffer ???
                        decoder.queueInputBuffer(index, 0, sampleSize, extractor.sampleTime, 0)
                    } else {
                        Log.d(TAG, "InputBuffer BUFFER_FLAG_END_OF_STREAM")
                        decoder.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        isInput = false
                    }
                }

                // for sleepRender
                val bufferInfo = MediaCodec.BufferInfo()
                startWhen = System.currentTimeMillis()

                when (val outIndex = decoder.dequeueOutputBuffer(newBufferInfo, 1000)) {
                    MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                        Log.d(TAG, "INFO_OUTPUT_BUFFERS_CHANGED")
                        decoder.outputBuffers
                    }
                    MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        Log.d(TAG, "INFO_OUTPUT_FORMAT_CHANGED format : " + decoder.outputFormat)
                    }
                    MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        Log.d(TAG, "INFO_TRY_AGAIN_LATER")
                    }
                    else -> {
//                    if (isFirst.not()) {
//                        startWhen = System.currentTimeMillis()
//                        isFirst = true
//                    }
//                    //K:  根據timestemp保持播放速度
//                    try {
//                        val sleepTime: Long =
//                            newBufferInfo.presentationTimeUs / 1000 - (System.currentTimeMillis() - startWhen)
//                        Log.d(
//                            TAG,
//                            "info.presentationTimeUs : " + (newBufferInfo.presentationTimeUs / 1000).toString() + " playTime: " + (System.currentTimeMillis() - startWhen).toString() + " sleepTime : " + sleepTime
//                        )
//                        if (sleepTime > 0) sleep(sleepTime)
//
//                    } catch (e: InterruptedException) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace()
//                    }


                        sleepRender(bufferInfo, startWhen)

                        //K: use this call to return the buffer to the codec or to render it on the output surface
                        decoder.releaseOutputBuffer(outIndex, true /* Surface init */)
                    }
                }

                // All decoded frames have been rendered, we can stop playing now
                // All decoded frames have been rendered, we can stop playing now
                if (newBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                    break
                }
            }
        }
//        release()

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

    //根據時間戳對齊數據
    private fun sleepRender(audioBufferInfo: MediaCodec.BufferInfo, startMs: Long) {
        // 这里的时间是 毫秒  presentationTimeUs 的时间是累加的 以微秒进行一帧一帧的累加
        val timeDifference = audioBufferInfo.presentationTimeUs / 1000 - (System.currentTimeMillis() - startMs)
        if (timeDifference > 0) {
            try {
                sleep(timeDifference)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
}