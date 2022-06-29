package cool.thejiangbf.wallhaven

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cool.thejiangbf.wallhaven.weapon.Ok
import cool.thejiangbf.wallhaven.weapon.browser
import cool.thejiangbf.wallhaven.weapon.document
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val TAG = "壁纸天堂 * 独家珍藏"
    private lateinit var adapter : ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = ImageAdapter(this)
        rvMain.adapter = adapter
        rvMain.layoutManager = GridLayoutManager(this,2)

        rvMain.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var isSlidingUpward = false

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

//        val temp = mutableListOf<Wallpaper>()
//        repeat(10){
//            temp.add(Wallpaper(R.drawable.img,"","1234x00$it"))
//        }
//
//        adapter.update(temp)
    }

    private fun requestHot(page:Int){
        GlobalScope.launch {
            val doc = browser.connect("https://wallhaven.cc/hot?page=$page")
            val list = document.getElementsByTag(doc.html(),"figure")
            Log.i(TAG, "requestHot: 找到${list.size}条数据")

            val walls = mutableListOf<Wallpaper>()
            for (i in 0 until list.size){
                val figure = list[i]

                val img = figure.getElementsByTag("img")[0]
                val snapshot = img.attr("data-src")

                val a = figure.getElementsByTag("a")[0]
                val href = a.attr("href")

                val spanRes = figure.getElementsByClass("wall-res")[0]
                val size = spanRes.text()

                walls.add(Wallpaper(snapshot,href,size))
                Log.i(TAG, "requestHot: 第${i+1}条数据 -> href=$href")
            }

            runOnUiThread{
                adapter.update(walls)
            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.hot -> {
                Log.i(TAG, "onOptionsItemSelected: 热门!!")
                requestHot(1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main,menu)
        return super.onCreateOptionsMenu(menu)
    }
}