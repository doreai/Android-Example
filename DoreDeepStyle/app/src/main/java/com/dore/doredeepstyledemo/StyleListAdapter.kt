package com.dore.doredeepstyledemo

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class StyleListAdapter(private val context: Activity, private val title: Array<String>)
    : ArrayAdapter<String>(context, R.layout.style_listitem, title) {

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.style_listitem, null, true)

        val titleText = rowView.findViewById(R.id.lbstyleItem) as TextView


        titleText.text = title[position]


        return rowView
    }
}