package com.example.project

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var ed_place: EditText
    private lateinit var btn_delete: Button
    private lateinit var btn_search: Button
    private lateinit var btn_history: Button
    private lateinit var myObject: MyObject
    private val resultList = ArrayList<Item>()
    private val historylist = ArrayList<Item>()
    private lateinit var adapter: MyAdapter
    private var marker = MarkerOptions()

    class MyObject {
        lateinit var results: Results

        class Results {
            lateinit var content: Array<Content>

            class Content {
                var lat = 0F
                var lng = 0F
                var name = ""
                var vicinity = ""
                var photo = ""
                var landscape = arrayListOf<String>()
                var star = 0
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && requestCode == 0) {
            for(result in grantResults)
                if(result != PackageManager.PERMISSION_GRANTED)
                    finish()
            loadMap()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ed_place = findViewById(R.id.ed_place)
        btn_delete = findViewById(R.id.btn_delete)
        btn_search = findViewById(R.id.btn_search)
        btn_history = findViewById(R.id.btn_history)
        
        loadMap()
    }

    override fun onMapReady(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("dbg", "定位未允許")
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
        } else {
            Log.e("dbg", "定位已授權")
            map.isMyLocationEnabled = true
            setListener(map)
            sendRequest(map)
        }
    }

    private fun sendRequest(map: GoogleMap) {
        val url = "https://android-quiz-29a4c.web.app/"

        Log.e("dbg", "進入sendRequest")
        val req = Request.Builder()
            .url(url)
            .build()

        OkHttpClient().newCall(req).enqueue(object : Callback {             //!!!enqueue運行API在background thread!!!
            override fun onResponse(call: Call, response: Response) {
                Log.e("dbg", "進入Response")
                val json = response.body?.string()

                myObject = Gson().fromJson(json, MyObject::class.java)
                pinPosition(map, myObject)

            }

            override fun onFailure(call: Call, e: IOException) {
                Toast.makeText(this@MainActivity,
                    "查詢失敗 $e", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun pinPosition(map: GoogleMap, myObject: MyObject) {

        runOnUiThread {
            myObject.results.content.forEach { pos ->

                marker.position(LatLng(pos.lat.toDouble(), pos.lng.toDouble()))
                marker.title(pos.name)
                //marker.draggable(true)
                map.addMarker(marker)

                //移動畫面
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                    LatLng(23.718, 120.992), 8f))
            }
        }

    }

    private fun loadMap() {
        val map = supportFragmentManager.findFragmentById(R.id.map)
                                                as SupportMapFragment
        map.getMapAsync(this)
    }

    private fun setListener(map: GoogleMap) {

        map.setOnMarkerClickListener {
            val title = it.title
            val latlng = it.position

            val info_dialog = Dialog(this)
            info_dialog.setContentView(R.layout.information)
            info_dialog.findViewById<TextView>(R.id.tv_title).text = title

            info_dialog.findViewById<Button>(R.id.btn_info).setOnClickListener {
                info_dialog.dismiss()
                //包資料
                val intent = Intent(this, MainActivity2::class.java)
                val bundle = Bundle()
                myObject.results.content.forEach {
                    if (it.name == title) {
                        bundle.putString("pic_url", it.photo)
                        bundle.putString("name", title)
                        bundle.putString("address", it.vicinity)
                        bundle.putInt("star", it.star)
                        for(i in 0 until  it.landscape.size)
                            bundle.putString("landscape$i", it.landscape[i])
                        return@forEach
                    }
                }
                intent.putExtras(bundle)
                startActivity(intent)

            }
            info_dialog.findViewById<Button>(R.id.btn_map).setOnClickListener {
                /* 直接開啟導航 程式碼
                val gmmIntentUri =
                    Uri.parse("google.navigation:q=${latlng.latitude},${latlng.longitude}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
                */

                val url = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&" +
                            "destination=${latlng.latitude},${latlng.longitude}"
                )
                val intent = Intent().apply {
                    action = "android.intent.action.VIEW"
                    data = url
                }
                info_dialog.dismiss()
                startActivity(intent)
            }

            info_dialog.show()
            true
        }

        btn_delete.setOnClickListener {
            if(ed_place.length() > 0)
                ed_place.setText("")
        }

        btn_search.setOnClickListener {
            val searchResult = mutableListOf<MyObject.Results.Content>()
            if (ed_place.length() > 0) {
                //搜尋景點名稱是否有包含關鍵字
                myObject.results.content.forEach { i ->
                    val regex = Regex(pattern = ed_place.text.toString())
                    if (regex.containsMatchIn(input = i.name))
                        searchResult.add(i)
                }
                //searchResult儲存查詢結果
                if (searchResult.size < 1)
                    Toast.makeText(this, "查無結果", Toast.LENGTH_SHORT).show()
                else {
                    //包裝查詢結果給ArrayAdapter
                    resultList.clear()
                    searchResult.forEach { j ->
                        resultList.add(Item(j.name, j.vicinity))
                    }

                    //剩下Dialog顯示"已做好的listview與ArrayApdater"
                    val search_dialog = Dialog(this)
                    search_dialog.setContentView(R.layout.search_result)
                    val listview = search_dialog.findViewById<ListView>(R.id.listview)
                    adapter = MyAdapter(this, resultList, R.layout.listview_item)
                    listview.adapter = adapter
                    //設定ListView點擊Item監聽器
                    listview.setOnItemClickListener {parent, view, position, id ->
                        var find_flag = false
                        //檢查是否有重複歷史紀錄
                        historylist.forEach {
                            if (it.name == resultList[position].name) {
                                find_flag = true
                                return@forEach
                            }
                        }
                        if (find_flag == false)
                            historylist.add(resultList[position])
                        //點擊按鈕後跳轉至該地點
                        val place = resultList[position].name
                        myObject.results.content.forEach {
                            if (it.name == place){
                                map.moveCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(it.lat.toDouble(), it.lng.toDouble()), 14f))

                                return@forEach
                            }
                        }
                        search_dialog.dismiss()
                        //--------------------
                    }
                    search_dialog.show()
                }
            }
        }

        btn_history.setOnClickListener {
            if (historylist.size < 1) {
                Toast.makeText(this, "無歷史紀錄", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Intent("aaa")
            val history_dialog = Dialog(this)
            history_dialog.setContentView(R.layout.search_history)

            val listview2 = history_dialog.findViewById<ListView>(R.id.listview2)
            adapter = MyAdapter(this, historylist, R.layout.listview_item)
            listview2.adapter = adapter
            //設定ListView點擊Item監聽器
            listview2.setOnItemClickListener {parent, view, position, id ->
                //點擊按鈕後事件~~~~~~~~
                val place = historylist[position].name
                myObject.results.content.forEach {
                    if (it.name == place){
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(it.lat.toDouble(), it.lng.toDouble()), 14f))
                        return@forEach
                    }
                }
                history_dialog.dismiss()
                //--------------------
            }
            history_dialog.show()

            history_dialog.findViewById<Button>(R.id.btn_listClr).setOnClickListener {
                historylist.clear()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "已清除紀錄", Toast.LENGTH_SHORT).show()
            }


        }


    }

}