package com.example.hw1_v2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.MediaStore

import android.util.Log
import android.view.View
import android.widget.*
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.coroutines.CoroutineContext

import kotlinx.coroutines.*

class downloadWebPicture{
    var current_url:String? = null
    var img: Bitmap? = null
    var progress: Int = 0
    var tempDownloadImage = ByteArray(1)
    var length: Int = 0
    var desPos = 0
    var isPause:Boolean = false
    var isOver:Boolean = false

//    suspend fun handleWebPic(url: String?){
//        img = gerUrlPic(url)
//    }

    suspend fun getUrlPic(url: String?){
        try {
            Log.v("kevin test", "1")
            val imgUrl = URL(url)


            Log.v("kevin test", "2")
            val httpURLConnection: HttpURLConnection = imgUrl.openConnection() as HttpURLConnection
            httpURLConnection.setRequestProperty("Range", "bytes=" + this.desPos + "-")
            Log.v("kevin test", "3")
//            if (this.length == httpURLConnection.contentLength){
//                // 若不支援續傳 則重新開始下載
//                this.desPos = 0
//            }
            Log.v("kevin test", "start connect")
            httpURLConnection.connect()
            Log.v("kevin test", "4")

            val inputStream: InputStream = httpURLConnection.inputStream

            val tmpLength = 512
            val tmp = ByteArray(tmpLength)
            var readLen = 0

            if (url != current_url){
                this.current_url = url
                this.length = httpURLConnection.contentLength
                this.tempDownloadImage = ByteArray(this.length)
            }


            if (this.length != -1) {
                Log.v("kevin Debug", "start download")
                while (inputStream.read(tmp).also { readLen = it } > 0) {
                    try {
                        Thread.sleep(1)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    System.arraycopy(tmp, 0, this.tempDownloadImage, this.desPos, readLen)
                    Log.v("kevin test", "max: ${this.length} / desPos: ${this.desPos} / readLen: ${readLen}")
                    this.desPos += readLen

                    this.progress = (this.desPos * 100 / this.length)

                    val msg = Message()
                    msg.what = 2

                    if (isPause){
                        Log.v("kevin test", "download pause")
                        break
                    }
                }

                if (this.desPos == this.length){
                    Log.v("kevin Debug", "finish download and write the img file")
                    this.img = BitmapFactory.decodeByteArray(this.tempDownloadImage, 0, this.tempDownloadImage.size)
                    cleanTempData()
                }else if(isOver){
                    Log.v("kevin Debug", "delete the temp file")
                    cleanTempData()
                    val msg = Message()
                    msg.what = 3
//                    handler.sendMessage(msg)
                    this.isPause = true
                }else{
                    throw Exception("Only read " + this.desPos + " bytes")
                }
            }
            httpURLConnection.disconnect()
        } catch (e: Exception) {
            Log.v("kevin IOException", e.toString())
        }
    }

    suspend fun cleanTempData(){

    }
//    @Synchronized
}


class MainActivity : AppCompatActivity() {


    var test_url = "https://images2.gamme.com.tw/news2/2017/77/08/qZqRoJ_WlKWXsKU.jpg"

    private var picDownloadManager : downloadWebPicture? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        picDownloadManager = downloadWebPicture()
    }

    override fun onResume() {
        super.onResume()

        val inputUrl = findViewById<EditText>(R.id.inputUrl)
        val startButton = findViewById<Button>(R.id.startButton)
        val pauseButton = findViewById<Button>(R.id.pauseButton)
        val resumeButton = findViewById<Button>(R.id.resumeButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)
        val showUrl = findViewById<TextView>(R.id.showUrl)
        val showImage = findViewById<View>(R.id.showDonwloadImag) as ImageView
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)// as ProgressBar
        val progressNumb = findViewById<TextView>(R.id.progressNum)

        var usedUrl = test_url
        val downloadCoroutineScope = CoroutineScope(Dispatchers.Default)


        startButton.setOnClickListener {
            downloadCoroutineScope.launch{
                //update view
                //add suspend upon used fun
                Log.v("kevin test", "start coroutines")
                picDownloadManager!!.getUrlPic(usedUrl)
                withContext(Dispatchers.Main) {
                    showImage.setImageBitmap(picDownloadManager!!.img)
                }
            }
        }

    }
}