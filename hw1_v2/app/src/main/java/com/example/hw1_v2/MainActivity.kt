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
import android.view.View
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

const val DOWNLOAD_BUFFER_SIZE = 512


class MainActivity : AppCompatActivity() {
    private var testUrl = "https://images2.gamme.com.tw/news2/2017/77/08/qZqRoJ_WlKWXsKU.jpg"

    private lateinit var downloader: DownloadingPicture

    // binding name is equal to you layout name
    private lateinit var binding: ActivityMainBinding

    val downloadCoroutineScope = CoroutineScope(Dispatchers.Default)



    private suspend fun getUrlPic(url: String?) {
        lateinit var inputStream:InputStream
        lateinit var httpURLConnection: HttpURLConnection
        try {
            httpURLConnection =
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

            inputStream = httpURLConnection.inputStream
            val tmp = ByteArray(DOWNLOAD_BUFFER_SIZE)
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
        }
        catch (e: Exception) {
            Log.v("kevin IOException", e.toString())
        } finally {
            if (inputStream != null){
                inputStream.close()
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect()
            }

        }
    }

    private suspend fun updateProgressBar(readLen: Int) {
        // two function should put in different place
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

        downloader = DownloadingPicture()
    }

    override fun onResume() {
        super.onResume()

        binding.inputUrl.setText(testUrl)

        binding.startButton.setOnClickListener(onStartClick())

        binding.pauseButton.setOnClickListener(onPauseClick())

        binding.resumeButton.setOnClickListener(onResumeClick())

        binding.cancelButton.setOnClickListener(onCancelClick())
    }

    private fun onCancelClick(): (View) -> Unit = {
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

    private fun onResumeClick(): (View) -> Unit = {
        downloader.isPause = false
        downloadCoroutineScope.launch {
            getUrlPic(downloader.currentUrl)
            withContext(Dispatchers.Main) {
                binding.showDonwloadImag.setImageBitmap(downloader.img)
            }
            saveImage()
        }
    }

    private fun onPauseClick(): (View) -> Unit = {
        downloader.isPause = true
    }

    private fun onStartClick(): (View) -> Unit =
        {
            val usedUrl = binding.inputUrl.text.toString()
            downloadCoroutineScope.launch {
                downloader.cleanTempData()
                getUrlPic(usedUrl)
                withContext(Dispatchers.Main) {
                    binding.showDonwloadImag.setImageBitmap(downloader.img)
                }

                saveImage()
            }
        }

    private fun saveImage() {
        val imgName = UUID(3, 5).toString()
        if (downloader.progress == 100) {
            saveImage(downloader.img!!, imgName)
        }
    }
}