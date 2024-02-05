package com.example.project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.viewpager.widget.ViewPager

class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_pager)

        val tv_page = findViewById<TextView>(R.id.tv_page)
        val viewpager = findViewById<ViewPager>(R.id.viewPager)

        intent?.extras?.let {
            val num = it.size()-1
            val image_list = mutableListOf<String>()
            for (i in 0 until num) {
                image_list.add(it.getString("landscape$i")!!)
            }

            viewpager.adapter = ImageAdapter(this, image_list)
            viewpager.setCurrentItem(it.getInt("currentPos"), false)
            val mlistener = object: ViewPager.OnPageChangeListener{
                override fun onPageScrollStateChanged(position: Int) {
                    //頁面滑動狀態改變時
                }
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    //頁面滾動時觸發
                    tv_page.text = "${position+1}/$num"
                }
                override fun onPageSelected(position: Int) {
                    //當新的頁面被選擇時

                }
            }
            viewpager.addOnPageChangeListener(mlistener)
            tv_page.text = "${it.getInt("currentPos")+1}/$num"
        }
    }
}