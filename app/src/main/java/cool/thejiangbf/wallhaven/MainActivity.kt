package cool.thejiangbf.wallhaven

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility.MODE_IN
import androidx.transition.Visibility.MODE_OUT
import com.bumptech.glide.Glide
import cool.thejiangbf.wallhaven.weapon.Meta
import cool.thejiangbf.wallhaven.weapon.Bom
import cool.thejiangbf.wallhaven.weapon.document
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val TAG = "壁纸天堂 * 独家珍藏"
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
        setContentView(R.layout.activity_main)

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

    }

    private fun initView() {
        hideNavigation()
        adapter = ImageAdapter(this)
        rvMain.adapter = adapter
        rvMain.layoutManager = GridLayoutManager(this,2)
    }

    private fun initData() {
        val spp = getSharedPreferences("splash", MODE_PRIVATE)
        val src = spp.getString("url","https://w.wallhaven.cc/full/l3/wallhaven-l36mpy.jpg")
        purity = spp.getString("purity","111").toString()

        val sp = getSharedPreferences("prefs", MODE_PRIVATE)
        cate = sp.getString("Categories","100").toString()
        purity = sp.getString("Purity","100").toString()
        sort = sp.getString("Sorting","relevance").toString()
        order = sp.getString("Order","desc").toString()
        topRange = sp.getString("TopRange","1M").toString()
        color = sp.getString("Color","").toString()
        apikey = sp.getString("apikey","").toString()


        Glide.with(this).load(src).into(ivSplash)

        requestHot(1)

        GlobalScope.launch {
            delay(3000)
            runOnUiThread {
                createCircularRevealAnim(relativeSplash, MODE_OUT)
                showNavigation()
            }
        }

    }


    /**
     * 创建圆形揭示层动画
     */
    private fun createCircularRevealAnim(view:View, mode: Int) {
        //设置圆心坐标和半径
        val mCx: Int = (view.left + view.right) / 2 //获取x坐标
        val mCy: Int = (view.top + view.bottom) / 2 //获取y坐标
        //设置圆角半径
        val mRadius: Int = (view.width / 2).coerceAtLeast(view.height / 2)
        val anim: Animator = if (mode == MODE_IN) {
            //揭露动画创建，五个参数
            //param1:执行动画的视图；param2:动画开始的x坐标；param3:动画开始的y坐标；param4:动画开始的圆角半径；param5：动画结束的圆角半径
            ViewAnimationUtils.createCircularReveal(view, mCx, mCy, 0f, mRadius.toFloat())
        } else {
            ViewAnimationUtils.createCircularReveal(view, mCx, mCy, mRadius.toFloat(), 0f)
        }


        //添加监听器来保证开始动画之前，布局不会显示，也可以添加动画退出监听，让布局隐藏
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                super.onAnimationStart(animation)
                Log.d(TAG, "动画开始")
                view.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                view.visibility = View.INVISIBLE
            }
        })
        anim.duration = 500 //设置动画时长
        anim.start() //开启动画
    }

    private fun loadingMore() {
        adapter.setLoadState(ImageAdapter.LOADING) //FootView显示状态
        if (pageIndex < maxIndex) {
            pageIndex++
            requestHot(pageIndex)
            adapter.setLoadState(ImageAdapter.LOAD_COMPLETE)
            adapter.notifyDataSetChanged()
        }

    }

    private fun requestHot(page:Int){
        Log.i(TAG, "requestHot: $page")
        GlobalScope.launch {
            val doc = Bom.connect("https://wallhaven.cc/hot?page=$page&purity=$purity&apikey=$apikey&categories=$cate")
            if (doc==null){
                Toast.makeText(this@MainActivity, "服务器异常，请稍后重试！", Toast.LENGTH_SHORT).show()
                return@launch
            }
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

    private fun hideNavigation(){
        this.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        supportActionBar?.hide()


    }

    private fun showNavigation(){
        this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        supportActionBar?.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.hot -> {
                Log.i(TAG, "onOptionsItemSelected: 热门!!")
                requestHot(1)
            }
            R.id.about -> {
                startActivity(Intent(this,AboutActivity::class.java))
            }
            R.id.preference -> {
                startActivity(Intent(this,PreferenceActivity::class.java))
            }
            R.id.search -> {
                startActivity(Intent(this,SearchActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main,menu)
        return super.onCreateOptionsMenu(menu)
    }
}