package com.example.project

import android.app.Dialog
import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.squareup.picasso.Picasso //通過URL取得Image函式庫


class MainActivity2 : AppCompatActivity() {
    private lateinit var key: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sec_activity)

        intent?.extras?.let {
            val imageView = findViewById<ImageView>(R.id.imageview)
            Picasso.get()
                .load(it.getString("pic_url"))
                .resize(200,200)
                .into(imageView)
            findViewById<TextView>(R.id.tv_info_1).text = it.getString("name")
            findViewById<TextView>(R.id.tv_info_2).text = it.getString("address")
            findViewById<TextView>(R.id.tv_info_3).text = "景觀圖(${it.size()-4})"
            //動態產生星星
            val star_ctn = findViewById<LinearLayout>(R.id.Linear1)
            for (i in 0 until it.getInt("star")) {
                val img = ImageView(this)
                img.setImageResource(R.drawable.star)
                img.layoutParams = LinearLayout.LayoutParams(200, 200)
                img.setPadding(4,4,4,4)
                star_ctn.addView(img)
            }
            //動態產生Landscape
            val landscape1 = findViewById<LinearLayout>(R.id.Linear2)
            val landscape2 = findViewById<LinearLayout>(R.id.Linear3)

            //打包Bundle Address
            val bund_landscape = Bundle()
            for (i in 0 until it.size()-4)
                bund_landscape.putString("landscape$i", it.getString("landscape$i")!!)

            //Load所有Landscape圖片
            for (i in 0 until it.size()-4) {
                val img = ImageView(this)
                Picasso.get()
                    .load(it.getString("landscape$i"))
                    .resize(280,221)
                    .into(img)
                img.setPadding(4,4,4,4)

                //進入ImagePager
                img.setOnClickListener {
                    val currentPos = i    //紀錄點選的是第i張圖片
                    bund_landscape.putInt("currentPos", currentPos)
                    val intent = Intent(this, ImageActivity::class.java)
                    intent.putExtras(bund_landscape)
                    startActivity(intent)

                }

                if (i < 3)
                    landscape1.addView(img)
                else
                    landscape2.addView(img)
            }
        }
    }
}