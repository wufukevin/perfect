package com.example.myapplication

class AudioTime {
    var currentTime: Long = 0
    fun setAudioTime(time: Long) {
        currentTime = time
    }

    fun getAudioTime(): Long {
        return currentTime
    }
}