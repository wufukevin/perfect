package com.example.myapplication.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.audio.AACAudioDecoderThread
import android.media.MediaCodec.CodecException
import android.media.MediaCodec.createDecoderByType
import android.os.Handler
import java.nio.ByteBuffer
import android.media.AudioTrack

import android.media.AudioManager
import android.view.Surface


class VideoDecodeActivity : AppCompatActivity(), SurfaceHolder.Callback {

    companion object {

        fun newIntent(context: Context): Intent =
            Intent(context, VideoDecodeActivity::class.java)
    }

    private var videoDecoder: VideoDecodeThread? = null
    private var audioDecoder: AACAudioDecoderThread? = null
    private val videoPath by lazy {
        resources.openRawResourceFd(R.raw.sample4)
    }

    private lateinit var surfaceView: SurfaceView
    private lateinit var pauseAndPlayButton: Button
    private var playVideo = true

//    private lateinit var sync: MediaSync
//-----------------------
    private lateinit var mExtractorVideo: MediaExtractor
    private lateinit var mExtractorAudio: MediaExtractor
    private lateinit var mVideoDecoder: MediaCodec
    private lateinit var mAudioDecoder: MediaCodec
//    private var miVideoTrack: Int = 1
//    private var miAudioTrack: Int = 1
    private var nFrameRate: Int = 1
    private var mnSampleRate: Int = 1
    private var mnChannel: Int = 1
    private lateinit var mMediaSync: MediaSync
    private lateinit var mediaSyncSurface: Surface


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.play_video)

        surfaceView = findViewById(R.id.surfaceView)
        pauseAndPlayButton = findViewById(R.id.pauseButton)
        surfaceView.holder.addCallback(this@VideoDecodeActivity)
        videoDecoder = VideoDecodeThread()
        audioDecoder = AACAudioDecoderThread()
//        sync = MediaSync()
//        sync.setSurface(surface)

    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        pauseAndPlayButton.setOnClickListener {
            if(playVideo){
                videoDecoder?.pause()
                audioDecoder?.pause()
                playVideo = false
                pauseAndPlayButton.text = "Play"
                Log.d("Kevin", "now stop")
            }
            else{
                videoDecoder?.play()
                audioDecoder?.play()
                playVideo = true
                pauseAndPlayButton.text = "Pause"
                Log.d("Kevin", "now play")
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d("kevin", "surfaceChanged")
        mMediaSync = MediaSync()
        mMediaSync.setSurface(holder.surface)
        mediaSyncSurface = mMediaSync.createInputSurface()
        mMediaSync.setCallback(object : MediaSync.Callback() {
            override fun onAudioBufferConsumed(p0: MediaSync, p1: ByteBuffer, p2: Int) {
                p1.clear()
//                        mAudioDecoder.releaseOutputBuffer(p2, false)
            }
        }, Handler())
        audioDecoder!!.setMediaSync(mMediaSync)


        if (videoDecoder?.init(mediaSyncSurface, videoPath) == true) {
            mMediaSync.playbackParams = PlaybackParams().setSpeed(1.0f)
            videoDecoder?.start()
            audioDecoder?.startPlay(videoPath)
        } else {
            videoDecoder = null
        }
////--------------------------
//        mExtractorVideo = MediaExtractor()
//        mExtractorVideo.setDataSource(videoPath)
//        mExtractorAudio = MediaExtractor()
//        mExtractorAudio.setDataSource(videoPath)
//
//        mMediaSync = MediaSync()
//        mMediaSync.setSurface(holder.surface)
//
//        Log.d("kevin", "begin create audio decoder")
////        (0..mExtractorAudio.trackCount).forEach { index ->
//        for (index in 0..mExtractorAudio.trackCount){
//            Log.d("kevin", "$index-1")
//            val mediaFormat = mExtractorAudio.getTrackFormat(index)
//            Log.d("kevin", "$index-2")
//            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
//
//            Log.d("kevin", "$index-3")
//            if (mime?.startsWith("audio/") == true) {
//                mExtractorAudio.selectTrack(index)
//                mAudioDecoder = createDecoderByType(mime)
//                mnSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
//                mnChannel = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
//                mAudioDecoder.configure(mediaFormat, null, null, 0)
//
////                Log.d("kevin", "begin apply audio decoder")
//                mAudioDecoder.apply {
//                    setCallback(object : MediaCodec.Callback() {
//                        override fun onInputBufferAvailable(mediaCodec: MediaCodec, i: Int) {
//                            val byteBuffer = mAudioDecoder.getInputBuffer(i)
//                            val nRead = mExtractorAudio.readSampleData(byteBuffer!!, 0)
//                            Log.d("Audio", "onInputBufferAvailable i $i nRead $nRead")
//                            if (nRead < 0) {
//                                mAudioDecoder.queueInputBuffer(
//                                    i,
//                                    0,
//                                    0,
//                                    0,
//                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
//                                )
//                            } else {
//                                mAudioDecoder.queueInputBuffer(
//                                    i,
//                                    0,
//                                    nRead,
//                                    mExtractorAudio.sampleTime,
//                                    0
//                                )
//                                mExtractorAudio.advance()
//                            }
//                        }
//
//                        override fun onOutputBufferAvailable(
//                            mediaCodec: MediaCodec,
//                            i: Int,
//                            bufferInfo: MediaCodec.BufferInfo
//                        ) {
//                            val decoderBuffer = mAudioDecoder.getOutputBuffer(i)
//                            val copyBuffer = ByteBuffer.allocate(decoderBuffer!!.remaining())
//                            copyBuffer.put(decoderBuffer)
//                            copyBuffer.flip()
//                            mAudioDecoder.releaseOutputBuffer(i, false)
//                            mMediaSync.queueAudio(copyBuffer, i, bufferInfo.presentationTimeUs)
//                            Log.d(
//                                "Audio",
//                                "onOutputBufferAvailable i " + i + " presentationTimeUs " + bufferInfo.presentationTimeUs
//                            )
//                        }
//
//                        override fun onError(mediaCodec: MediaCodec, e: CodecException) {
//                            Log.d("Audio", "onError")
//                            e.printStackTrace()
//                        }
//
//                        override fun onOutputFormatChanged(
//                            mediaCodec: MediaCodec,
//                            mediaFormat: MediaFormat
//                        ) {
//                            Log.d("Audio", "onOutputFormatChanged")
//                        }
//                    })
//                }
////                Log.d("kevin", "finish apply audio decoder")
//
//                val buffsize = AudioTrack.getMinBufferSize(
//                    mnSampleRate,
//                    AudioFormat.CHANNEL_OUT_STEREO,
//                    AudioFormat.ENCODING_PCM_16BIT
//                )
//                val audioTrack = AudioTrack(
//                    AudioManager.STREAM_MUSIC, mnSampleRate,
//                    AudioFormat.CHANNEL_OUT_STEREO,
//                    AudioFormat.ENCODING_PCM_16BIT,
//                    buffsize,
//                    AudioTrack.MODE_STREAM
//                )
//                mMediaSync.setAudioTrack(audioTrack)
//
//                mMediaSync.setCallback(object : MediaSync.Callback() {
//                    override fun onAudioBufferConsumed(p0: MediaSync, p1: ByteBuffer, p2: Int) {
//                        p1.clear()
////                        mAudioDecoder.releaseOutputBuffer(p2, false)
//                    }
//                }, Handler())
//                break
//            }
//            Log.d("kevin", "$index-4")
//        }
//
//        Log.d("kevin", "begin create video decoder")
////        (0..mExtractorVideo.trackCount).forEach { index ->
//        for (index in 0..mExtractorVideo.trackCount) {
//            val mediaFormat = mExtractorVideo.getTrackFormat(index)
//            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
//
//            Log.d("kevin", "$index-3")
//            if (mime?.startsWith("video/") == true) {
//                mExtractorVideo.selectTrack(index)
//                mVideoDecoder = createDecoderByType(mime)
//                nFrameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
//                mediaSyncSurface = mMediaSync.createInputSurface()
//
//                mVideoDecoder.configure(mediaFormat, mediaSyncSurface, null, 0 /* Decode */)
//
//                mVideoDecoder.apply {
//                    setCallback(object : MediaCodec.Callback() {
//                        override fun onInputBufferAvailable(mediaCodec: MediaCodec, i: Int) {
//                            val byteBuffer: ByteBuffer? = mVideoDecoder.getInputBuffer(i)
//                            val nRead = mExtractorVideo.readSampleData(byteBuffer!!, 0)
//                            Log.d("Video", "onInputBufferAvailable i $i nRead $nRead")
//                            if (nRead < 0) {
//                                mVideoDecoder.queueInputBuffer(
//                                    i,
//                                    0,
//                                    0,
//                                    0,
//                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
//                                )
//                            } else {
//                                mVideoDecoder.queueInputBuffer(
//                                    i,
//                                    0,
//                                    nRead,
//                                    mExtractorVideo.sampleTime,
//                                    0
//                                )
//                                mExtractorVideo.advance()
//                            }
//                        }
//                        override fun onOutputBufferAvailable(
//                            mediaCodec: MediaCodec,
//                            i: Int,
//                            bufferInfo: MediaCodec.BufferInfo
//                        ) {
////                            if (0 != MediaCodec.BUFFER_FLAG_END_OF_STREAM and bufferInfo.flags) {
////                                Log.d("Video", "onOutputBufferAvailable BUFFER_FLAG_END_OF_STREAM")
////                                //                            return;
////                            }
////                            mVideoDecoder.releaseOutputBuffer(
////                                i,
////                                bufferInfo.presentationTimeUs * 1000
////                            )
////                            Log.d(
////                                "Video",
////                                "onOutputBufferAvailable i " + i + " presentationTimeUs " + bufferInfo.presentationTimeUs
////                            )
//                            mediaCodec.releaseOutputBuffer(i, 1000 * bufferInfo.presentationTimeUs)
//                        }
//
//                        override fun onError(mediaCodec: MediaCodec, e: CodecException) {
//                            Log.d("Video", "onError")
//                            e.printStackTrace()
//                        }
//
//                        override fun onOutputFormatChanged(
//                            mediaCodec: MediaCodec,
//                            mediaFormat: MediaFormat
//                        ) {
//                            Log.d("Video", "onOutputFormatChanged")
//                        }
//                    })
//                }
//                break
//            }
//            Log.d("kevin", "$index-4")
//        }
//
//
////         miss setOnErrorListener
//        mMediaSync.playbackParams = PlaybackParams().setSpeed(1.0f)
//        Log.d("kevin", "MediaSync start")
//        mAudioDecoder.start()
//        Log.d("kevin", "mAudioDecoder start")/**/
//        mVideoDecoder.start()
//        Log.d("kevin", "mVideoDecoder start")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        videoDecoder?.over()
        videoDecoder?.release()
        audioDecoder?.over()
        audioDecoder?.release()
    }

    override fun onPause() {
        super.onPause()
        videoDecoder?.over()
        videoDecoder?.release()
        audioDecoder?.over()
        audioDecoder?.release()
    }
}