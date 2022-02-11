package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import com.example.myapplication.video.VideoDecodeActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        request read outer file right >> override "onRequestPermissionsResult"
        if (Build.VERSION.SDK_INT >= 23) {
            val REQUEST_CODE_PERMISSION_STORAGE = 100
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            for (str in permissions) {
                if (this.checkSelfPermission(str) !== PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(permissions, REQUEST_CODE_PERMISSION_STORAGE)
                    return
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        super.onResume()

        val videoButton = findViewById<Button>(R.id.videoButton)

        videoButton.setOnClickListener {
            startActivity(VideoDecodeActivity.newIntent(this))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}