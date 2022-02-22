package com.example.hw3

class AudioTime {
    private var currentTime: Long = 0
    fun setAudioTime(time: Long) {
        currentTime = time
    }

    fun getAudioTime(): Long {
        return currentTime
    }
}