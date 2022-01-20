package com.example.hw1_v2

import android.graphics.Bitmap

class DownloadingPicture {
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