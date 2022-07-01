package cool.thejiangbf.wallhaven

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cool.thejiangbf.wallhaven.weapon.Meta
import cool.thejiangbf.wallhaven.weapon.Bom
import cool.thejiangbf.wallhaven.weapon.document
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SearchActivity : AppCompatActivity() {
    private val TAG = "壁纸天堂 * 搜索"
    private lateinit var adapter : ImageAdapter
    private var pageIndex = 1
    private val maxIndex = 100
    private var cate = "111"
    private var purity = "100"
    private var sort = "toplist"
    private var order = "desc"
    private var topRange = "1M"
    private var color = ""
    private var apikey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideNavigation()
        setContentView(R.layout.activity_search)

        initView()

        listener()

        initData()

    }

    private fun listener() {

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

        ivSearch.setOnClickListener {
            pageIndex = 1
            search(etSearch.text.toString(),pageIndex)
            loading.show()
        }

    }

    private fun initView() {
        hideNavigation()
        adapter = ImageAdapter(this)
        rvMain.adapter = adapter
        rvMain.layoutManager = GridLayoutManager(this,2)

        loading.hide()

    }

    private fun initData() {
        val spp = getSharedPreferences("splash", MODE_PRIVATE)
        val src = spp.getString("url","https://w.wallhaven.cc/full/l3/wallhaven-l36mpy.jpg")
        purity = spp.getString("purity","111").toString()
        apikey = Meta.get(this,"apikey")

        val sp = getSharedPreferences("prefs", MODE_PRIVATE)
        cate = sp.getString("Categories","100").toString()
        purity = sp.getString("Purity","100").toString()
        sort = sp.getString("Sorting","relevance").toString()
        order = sp.getString("Order","desc").toString()
        topRange = sp.getString("TopRange","1M").toString()
        color = sp.getString("Color","").toString()


    }


    private fun loadingMore() {
        adapter.setLoadState(ImageAdapter.LOADING) //FootView显示状态
        if (pageIndex < maxIndex) {
            pageIndex++
            search(etSearch.text.toString(),  pageIndex)
            adapter.setLoadState(ImageAdapter.LOAD_COMPLETE)
            adapter.notifyDataSetChanged()
        }

    }

    private fun search(q:String, page:Int){
        Log.i(TAG, "requestHot: $page")
        GlobalScope.launch {
            val doc = Bom.connect("https://wallhaven.cc/search?q=$q&page=$page&purity=$purity&apikey=$apikey&categories=$cate&sorting=$sort&order=$order&topRange=$topRange&colors=$color")
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
                loading.hide()
            }

        }

    }

    private fun hideNavigation(){
        this.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        supportActionBar?.hide()


    }




}