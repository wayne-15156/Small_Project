package com.example.project

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

data class Item(
    val name: String,
    val address: String
)

class MyAdapter(context: Context, private val data: ArrayList<Item>,
                        private val layout: Int): ArrayAdapter<Item>(context, R.layout.listview_item, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //依據傳入的Layout建立畫面
        val view = View.inflate(parent.context, layout, null)

        val item = getItem(position) ?:return view
        view.findViewById<TextView>(R.id.tv_name).text = item.name
        view.findViewById<TextView>(R.id.tv_address).text = item.address

        return view
    }

}