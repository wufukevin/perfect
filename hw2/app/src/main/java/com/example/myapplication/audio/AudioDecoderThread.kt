package com.example.myapplication.audio

import android.content.res.AssetFileDescriptor
import android.media.*
import android.media.MediaCodec.BufferInfo
import android.media.MediaCodec.createDecoderByType
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myapplication.AudioTime

import android.media.AudioTrack


class AACAudioDecoderThread {

    companion object {
        private const val TIMEOUT_US = 1_000L
    }

    private lateinit var extractor: MediaExtractor
    private lateinit var decoder: MediaCodec
    private lateinit var audioTime: AudioTime

    private var isStop = false
    private var isOver = false
    private var sampleRate = 44100
    private var audioTrack: AudioTrack? = null

    @RequiresApi(Build.VERSION_CODES.N)
    fun startPlay(file: AssetFileDescriptor) {
        isStop = false
        extractor = MediaExtractor()
        try {
            extractor.setDataSource(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //create audio decoder
        (0 until extractor.trackCount).forEach { trackNumber ->
            val format = extractor.getTrackFormat(trackNumber)
            val mime = format.getString(MediaFormat.KEY_MIME)

            if(mime?.startsWith("audio/") == true){
                extractor.selectTrack(trackNumber)

                decoder = createDecoderByType(mime)
                decoder.configure(format, null, null, 0)
                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                decoder.start()

                Thread(audioDecoderAndPlayRunnable).start()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private var audioDecoderAndPlayRunnable = Runnable { audioDecoderAndPlay() }

    @RequiresApi(Build.VERSION_CODES.M)
    fun audioDecoderAndPlay() {

        val info = BufferInfo()

        //setting audioTrack and play it
        val buffsize: Int = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioTrack= AudioTrack(
            AudioManager.STREAM_MUSIC, sampleRate,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT,
            buffsize,
            AudioTrack.MODE_STREAM
        )
        audioTrack!!.play()

        while(!isOver){
            while (!isStop) {
                val inIndex: Int = decoder.dequeueInputBuffer(TIMEOUT_US)

                if (inIndex >= 0) {
                    //queue input buffer
                    val buffer = decoder.getInputBuffer(inIndex)
                    val sampleSize: Int = extractor.readSampleData(buffer!!, 0)
                    if (sampleSize < 0) {
                        Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM")
                        decoder.queueInputBuffer(
                            inIndex,
                            0,
                            0,
                            0,
                            MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                    } else {
                        decoder.queueInputBuffer(
                            inIndex,
                            0,
                            sampleSize,
                            extractor.sampleTime,
                            0
                        )
                        extractor.advance()
                    }

                    //dequeue output buffer
                    when (val outIndex: Int = decoder.dequeueOutputBuffer(info, TIMEOUT_US)) {
                        MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                            val format: MediaFormat = decoder.outputFormat
                            Log.d("DecodeActivity", "New format $format")
                            audioTrack!!.playbackRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                        }
                        MediaCodec.INFO_TRY_AGAIN_LATER -> Log.d(
                            "DecodeActivity",
                            "dequeueOutputBuffer timed out!"
                        )
                        else -> {
                            val outBuffer = decoder.getOutputBuffer(outIndex)
                            val chunk = ByteArray(info.size)
                            outBuffer?.get(chunk)
                            outBuffer!!.clear()
                            audioTrack!!.write(
                                chunk,
                                info.offset,
                                info.offset + info.size
                            )
                            decoder.releaseOutputBuffer(outIndex, false)
                            //update current audio time
                            setTimeValue()
                        }
                    }
                    // All decoded frames have been rendered, we can stop playing now
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM")
                        break
                    }
                }
            }
        }
        release()
    }

    fun release() {
        decoder.stop()
        decoder.release()
        extractor.release()
        audioTrack!!.stop()
        audioTrack!!.release()
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

//    根據時間戳對齊數據
//    private fun sleepRender(audioBufferInfo: MediaCodec.BufferInfo, startMs: Long) {
//        // 这里的时间是 毫秒  presentationTimeUs 的时间是累加的 以微秒进行一帧一帧的累加
//        val timeDifference = audioBufferInfo.presentationTimeUs / 1000 - (System.currentTimeMillis() - startMs)
//        if (timeDifference > 0) {
//            try {
//                Thread.sleep(timeDifference)
//            } catch (e: InterruptedException) {
//                e.printStackTrace()
//            }
//        }
//    }

//    fun setMediaSync(mMediaSync: MediaSync){
//        mediaSync = mMediaSync
//    }

    fun setAudioTimeToAudioThread(aAudioTime: AudioTime){
        audioTime = aAudioTime
    }

    private fun setTimeValue() {
        val numFramesPlayed = audioTrack!!.playbackHeadPosition
        audioTime.setAudioTime((numFramesPlayed * 1000000L) / sampleRate)
    }
}