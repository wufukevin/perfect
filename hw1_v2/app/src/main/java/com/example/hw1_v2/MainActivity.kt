package com.example.hw1_v2

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.hw1_v2.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

const val TEMP_LENGTH = 512

class DownloadWebPicture {
    var currentUrl: String? = null
    var img: Bitmap? = null
    var progress: Int = 0
    var tempDownloadImage = ByteArray(1)
    var length: Int = 0
    var desPos = 0
    var isPause: Boolean = false
    var isOver: Boolean = false

    fun cleanTempData() {
        this.currentUrl = null
        this.img = null
        this.progress = 0
        this.tempDownloadImage = ByteArray(1)
        this.desPos = 0
        this.isPause = false
        this.isOver = false
    }
}


class MainActivity : AppCompatActivity() {
    private var testUrl = "https://images2.gamme.com.tw/news2/2017/77/08/qZqRoJ_WlKWXsKU.jpg"

    private lateinit var downloader: DownloadWebPicture

    // binding name is equal to you layout name
    private lateinit var binding: ActivityMainBinding

    private suspend fun getUrlPic(url: String?) {
        try {
            val httpURLConnection: HttpURLConnection =
                URL(url).openConnection() as HttpURLConnection
            httpURLConnection.setRequestProperty(
                "Range",
                "bytes=" + downloader.desPos + "-"
            )

            // 若不支援續傳 則重新開始下載
            if (downloader.length == httpURLConnection.contentLength) {
                downloader.desPos = 0
            }

            httpURLConnection.connect()

            val inputStream: InputStream = httpURLConnection.inputStream
            val tmp = ByteArray(TEMP_LENGTH)
            var readLen: Int

            if (url != downloader.currentUrl) {
                downloader.currentUrl = url
                downloader.length = httpURLConnection.contentLength
                downloader.tempDownloadImage = ByteArray(downloader.length)
            }

//                begin download
            if (downloader.length != -1) {
                while (inputStream.read(tmp)
                        .also { readLen = it } > 0 && !downloader.isPause
                ) {
                    delay(1)
                    System.arraycopy(
                        tmp,
                        0,
                        downloader.tempDownloadImage,
                        downloader.desPos,
                        readLen
                    )
                    Log.v(
                        "kevin test",
                        "max: ${downloader.length} / desPos: ${downloader.desPos} / readLen: $readLen"
                    )

                    updateProgressBar(readLen)
                }

                when {
                    downloader.desPos == downloader.length -> {
                        Log.v("kevin Debug", "finish download and write the img file")
                        downloader.img = BitmapFactory.decodeByteArray(
                            downloader.tempDownloadImage,
                            0,
                            downloader.tempDownloadImage.size
                        )
                    }
                    downloader.isOver -> {
                        Log.v("kevin Debug", "delete the temp file")
                        downloader.isPause = true
                    }
                    else -> {
                        throw Exception("Only read " + downloader.desPos + " bytes")
                    }
                }
            }
            httpURLConnection.disconnect()
            inputStream.close()
        }
        catch (e: Exception) {
            Log.v("kevin IOException", e.toString())
        }
    }

    private suspend fun updateProgressBar(readLen: Int) {
        downloader.desPos += readLen
        downloader.progress =
            (downloader.desPos * 100 / downloader.length)

        // update progress bar
        withContext(Dispatchers.Main) {
            binding.progressBar.progress = downloader.progress
            binding.progressNum.text = downloader.progress.toString()
        }
    }

    private fun saveImage(inputImage: Bitmap, title: String) {
        var fos: OutputStream? = null

        // For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, title)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/ief")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                // Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
                Log.v("kevin test456", imageUri.toString())
            }
        } else {
            // These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, title)
            fos = FileOutputStream(image)
            Log.v("kevin test 123", imagesDir.path)
        }

        fos?.use {
            inputImage.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    //  use view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        downloader = DownloadWebPicture()
    }

    override fun onResume() {
        super.onResume()

        binding.inputUrl.setText(testUrl)
        var usedUrl = ""
        val downloadCoroutineScope = CoroutineScope(Dispatchers.Default)



        binding.startButton.setOnClickListener {
            usedUrl = binding.inputUrl.text.toString()
            downloadCoroutineScope.launch {
                downloader.cleanTempData()
                getUrlPic(usedUrl)
                withContext(Dispatchers.Main) {
                    binding.showDonwloadImag.setImageBitmap(downloader.img)
                }

                saveImage()
            }
        }

        binding.pauseButton.setOnClickListener {
            downloader.isPause = true
        }

        binding.resumeButton.setOnClickListener {
            downloader.isPause = false
            downloadCoroutineScope.launch {
                getUrlPic(usedUrl)
                withContext(Dispatchers.Main) {
                    binding.showDonwloadImag.setImageBitmap(downloader.img)
                }
                saveImage()
            }
        }

        binding.cancelButton.setOnClickListener {
            downloadCoroutineScope.launch {
                downloader.cleanTempData()
                downloader.isPause = true
                downloader.isOver = true
                withContext(Dispatchers.Main) {
                    binding.showDonwloadImag.setImageBitmap(downloader.img)
                    binding.progressBar.progress = downloader.progress
                    binding.progressNum.text = downloader.progress.toString()
                }

            }
        }
    }

    private fun saveImage() {
        val imgName = UUID(3, 5).toString()
        if (downloader.progress == 100) {
            saveImage(downloader.img!!, imgName)
        }
    }
}