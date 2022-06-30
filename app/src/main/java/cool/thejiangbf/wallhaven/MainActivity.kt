package cool.thejiangbf.wallhaven

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cool.thejiangbf.wallhaven.weapon.browser
import cool.thejiangbf.wallhaven.weapon.document
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val TAG = "壁纸天堂 * 独家珍藏"
    private lateinit var adapter : ImageAdapter
    private var pageIndex = 1
    private val maxIndex = 100

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

                val manager = recyclerView.layoutManager as LinearLayoutManager?
                //当不滑动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //获取最后一个完全显示的itemPosition
                    val lastItemPosition = manager!!.findLastCompletelyVisibleItemPosition()
                    val itemCount = manager.itemCount
                    // 判断是否滑动到了最后一个item，并且是向上滑动
                    if (lastItemPosition == itemCount - 1 && isSlidingUpward) {
                        loadingMore() //加载第二页
                    }
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isSlidingUpward = dy>0
            }

        })

        requestHot(1)


    }

    private fun loadingMore() {
        adapter.setLoadState(ImageAdapter.LOADING) //FootView显示状态
        if (pageIndex < maxIndex) {
            pageIndex++
            requestHot(pageIndex)
            adapter.setLoadState(ImageAdapter.LOAD_COMPLETE)
            adapter.notifyDataSetChanged()
        }
//        if (searchList.size() < count) {
//            adapter.setLoadState(ImageAdapter.LOADING)
//        } else {
//            adapter.setLoadState(ImageAdapter.LOAD_END)
//        }
    }




    private fun requestHot(page:Int){
        Log.i(TAG, "requestHot: $page")
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
                adapter.insert(walls)
                relativeEmpty.visibility = View.GONE
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