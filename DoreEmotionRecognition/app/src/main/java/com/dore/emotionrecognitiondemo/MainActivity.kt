package com.dore.emotionrecognitiondemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView


class MainActivity : AppCompatActivity() {

    val list_col = arrayOf<String>("Real time Emotion","Without Face Detect","Emotion From Image")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView:ListView = findViewById(R.id.mlistView)
        val myListAdapter = TableListAdapter(this,list_col)
        listView.adapter = myListAdapter

        listView.setOnItemClickListener(){adapterView, view, position, id ->
            val itemAtPos = adapterView.getItemAtPosition(position)
            val itemIdAtPos = adapterView.getItemIdAtPosition(position)
            if(itemIdAtPos.toInt() == 0) {
                startActivity(Intent(this, MaskCameraView::class.java))
            }
            if(itemIdAtPos.toInt() == 1) {
                startActivity(Intent(this, WithoutFaceDetectView::class.java))
            }
            if(itemIdAtPos.toInt() == 2) {
                startActivity(Intent(this, EmotionImageView::class.java))
            }

        }
    }
}
