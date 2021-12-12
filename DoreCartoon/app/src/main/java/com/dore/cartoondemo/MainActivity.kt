package com.dore.cartoondemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView


class MainActivity : AppCompatActivity() {

    val list_col = arrayOf<String>("Picture To Cartoon")

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
                startActivity(Intent(this, ImageCartoonView::class.java))
            }

        }
    }
}
