package com.example.coroutinestest


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlin.coroutines.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val counter = findViewById<TextView>(R.id.textView)
        val button = findViewById<Button>(R.id.button)
        var x = 0

//        GlobalScope.launch(Dispatchers.Main) {
//            for (i in 10 downTo 1) {
//                counter.text = "count down $i ..." // update text
//                delay(1000)
//            }
//            counter.text = "Done!"
//        }

//        viewModel = ViewModelProviders.of(this).get(LiveDataViewModel::class.java)
//
//        viewModel.name.observe(this, object : Observer<String> {
//            override fun onChanged(name: String) {
//                userName.text = name
//            }
//        })

        val coroutineScope = CoroutineScope(Dispatchers.Main)

        val job = coroutineScope.launch {
            for (i in 10 downTo 1) { // countdown from 10 to 1
                counter.text = "count down $i ..." // update text
                delay(1000) // wait half a second
            }
            counter.text = "Done!"
        }

        button.setOnClickListener {
//            coroutineScope.launch{
//                x = addOne(x)
//                counter.setText(x.toString())
//            }
            job.cancel()
        }



    }

    suspend fun addOne(x: Int):Int{
        delay(3000)
        return x+1
    }
}