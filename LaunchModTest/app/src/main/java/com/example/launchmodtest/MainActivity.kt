package com.example.launchmodtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        val button1 = findViewById<Button>(R.id.button11)
        val button2 = findViewById<Button>(R.id.button12)
        val button3 = findViewById<Button>(R.id.button13)

        button1.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {  }
            startActivity(intent)
        }
        button2.setOnClickListener {
            val intent = Intent(this, SecondActivity::class.java).apply {  }
            startActivity(intent)
        }
        button3.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java).apply {  }
            startActivity(intent)
        }
    }
}