package cool.thejiangbf.wallhaven

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility.MODE_IN
import androidx.transition.Visibility.MODE_OUT
import com.bumptech.glide.Glide
import cool.thejiangbf.wallhaven.weapon.Meta
import cool.thejiangbf.wallhaven.weapon.Bom
import cool.thejiangbf.wallhaven.weapon.Prefs
import cool.thejiangbf.wallhaven.weapon.Prefs.apikey
import cool.thejiangbf.wallhaven.weapon.Prefs.cate
import cool.thejiangbf.wallhaven.weapon.Prefs.order
import cool.thejiangbf.wallhaven.weapon.Prefs.purity
import cool.thejiangbf.wallhaven.weapon.Prefs.sort
import cool.thejiangbf.wallhaven.weapon.Prefs.topRange
import cool.thejiangbf.wallhaven.weapon.Prefs.color
import cool.thejiangbf.wallhaven.weapon.document
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_preference.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val TAG = "壁纸天堂 * 独家珍藏"
    private lateinit var adapter : ImageAdapter
    private var pageIndex = 1
    private val maxIndex = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        listener()
        prefListener()


        initData()
        initPrefData()

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


    private fun prefListener() {

        etApikey.addTextChangedListener {
            if (!TextUtils.isEmpty(it)){
                cbPurity3.isEnabled = true
            }else{
                cbPurity3.isEnabled = false
                cbPurity3.isChecked = false
            }
            apikey = it.toString().trim()
        }

        btnSave.setOnClickListener {
            cardLoading.visibility = View.VISIBLE

            val sp = getSharedPreferences("prefs", MODE_PRIVATE)
            val edit = sp.edit()

            val cate = Categories.tag as Array<Int>
            val pure = Purity.tag as Array<Int>

            edit.putString("Categories","${cate[0]}${cate[1]}${cate[2]}")
            edit.putString("Purity","${pure[0]}${pure[1]}${pure[2]}")
            edit.putString("Sorting",Sorting.tag as String)
            edit.putString("Order",Order.tag as String)
            edit.putString("TopRange",TopRange.tag as String)
            edit.putString("Color",tvColors.tag as String)
            edit.putString("apikey",etApikey.text.toString())

            edit.commit()



            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            GlobalScope.launch {
                delay(1000)
                runOnUiThread {
                    cardLoading.visibility = View.GONE
                }
                delay(300)
                runOnUiThread {
                    drawer.closeDrawers()
                }
            }
        }

        cbCate1.setOnCheckedChangeListener(CategoryListener(Categories))
        cbCate2.setOnCheckedChangeListener(CategoryListener(Categories))
        cbCate3.setOnCheckedChangeListener(CategoryListener(Categories))

        cbPurity1.setOnCheckedChangeListener(PurityListener(Purity))
        cbPurity2.setOnCheckedChangeListener(PurityListener(Purity))
        cbPurity3.setOnCheckedChangeListener(PurityListener(Purity))

        rgSort.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.rbRele -> {
                    Sorting.tag = "relevance"
                }
                R.id.rbRand -> {
                    Sorting.tag = "random"
                }
                R.id.rbViews -> {
                    Sorting.tag = "views"
                }
                R.id.rbFav -> {
                    Sorting.tag = "favorites"
                }
                R.id.rbTop -> {
                    Sorting.tag = "toplist"
                    rgTopRange.isEnabled = true
                }
            }
            sort = Sorting.tag.toString()
            Sorting.text = "Sorting: ${Sorting.tag}"
        }

        rgOrder.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.rbDesc -> {
                    Order.tag = "desc"
                    Order.text = "Order: desc"
                }
                R.id.rbAsc -> {
                    Order.tag = "asc"
                    Order.text = "Order: asc"
                }
            }
            order = Order.tag.toString()
        }

        rgTopRange.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.rbRange1 -> { TopRange.tag = "1d" }
                R.id.rbRange2 -> { TopRange.tag = "3d" }
                R.id.rbRange3 -> { TopRange.tag = "1w" }
                R.id.rbRange4 -> { TopRange.tag = "1M" }
                R.id.rbRange7 -> { TopRange.tag = "1y" }
            }
            TopRange.text = "Top Range: ${TopRange.tag}"
            topRange = TopRange.tag.toString()

        }



        for(i in 0 until linearColors.childCount){
            val line = linearColors.getChildAt(i) as LinearLayout
            for (j in 0 until line.childCount){
                val textView = line.getChildAt(j) as TextView
                textView.setOnClickListener(ColorListener(linearColors, tvColors))
            }
        }

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


    private fun initPrefData() {
        cardLoading.visibility = View.VISIBLE


        val arrCate = arrayOf(cate[0].toString().toInt(), cate[1].toString().toInt(), cate[2].toString().toInt())
        val arrPure = arrayOf(purity[0].toString().toInt(), purity[1].toString().toInt(), purity[2].toString().toInt())

        etApikey.setText(apikey)
        Categories.tag = arrCate
        Purity.tag = arrPure
        Sorting.tag = sort
        Order.tag = order
        TopRange.tag = topRange
        tvColors.tag = color

        rgTopRange.isEnabled = false


        if (arrCate[0] == 1){ cbCate1.isChecked = true }
        if (arrCate[1] == 1){ cbCate2.isChecked = true }
        if (arrCate[2] == 1){ cbCate3.isChecked = true }

        if (arrPure[0] == 1){ cbPurity1.isChecked = true }
        if (arrPure[1] == 1){ cbPurity2.isChecked = true }
        if (arrPure[2] == 1){ cbPurity3.isChecked = true }

        when(sort){
            "relevance" -> { rbRele.isChecked = true }
            "random" -> { rbRand.isChecked = true }
            "views" -> { rbViews.isChecked = true }
            "favorites" -> { rbFav.isChecked = true }
            "toplist" -> { rbTop.isChecked = true }
        }

        if (order == "desc"){
            rbDesc.isChecked = true
        }else{
            rbAsc.isChecked = true
        }

        when(topRange){
            "1d" -> {rbRange1.isChecked = true}
            "3d" -> {rbRange2.isChecked = true}
            "1w" -> {rbRange3.isChecked = true}
            "1M" -> {rbRange4.isChecked = true}
            "1y" -> {rbRange7.isChecked = true}
        }

        tvColors.text = "Colors: #$color"
        if (color?.length==6){
            linearColors.setBackgroundColor(Color.parseColor("#$color"))
        }

        GlobalScope.launch {
            delay(1000)
            runOnUiThread {
                cardLoading.visibility = View.GONE
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
            val doc = Bom.connect("https://wallhaven.cc/hot?page=$page&purity=$purity&apikey=$apikey&categories=$cate&topRange=$topRange&sorting=$sort&order=$order&colors=$color")
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
                adapter.clear()
                requestHot(1)
            }
            R.id.about -> {
                startActivity(Intent(this,AboutActivity::class.java))
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


    private class CategoryListener(private val textView: TextView): CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val arr:Array<Int> = textView.tag as Array<Int>
            when(buttonView?.id){
                R.id.cbCate1 -> { arr[0] = if (isChecked) 1 else 0 }
                R.id.cbCate2 -> { arr[1] = if (isChecked) 1 else 0 }
                R.id.cbCate3 -> { arr[2] = if (isChecked) 1 else 0 }
            }
            cate = "${arr[0]}${arr[1]}${arr[2]}"
            textView.text = "Categories: $cate"
        }
    }

    private class PurityListener(private val textView: TextView): CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val arr:Array<Int> = textView.tag as Array<Int>
            when(buttonView?.id){
                R.id.cbPurity1 -> { arr[0] = if (isChecked) 1 else 0 }
                R.id.cbPurity2 -> { arr[1] = if (isChecked) 1 else 0 }
                R.id.cbPurity3 -> {
                    arr[2] = if (isChecked) 1 else 0
                    if (isChecked){
                        Toast.makeText(buttonView.context, "You should konwn what you are doing !!!", Toast.LENGTH_SHORT).show()
                    }

                }
            }
            purity = "${arr[0]}${arr[1]}${arr[2]}"
            textView.text = "Purity: $purity"
        }
    }

    class ColorListener(private val layout:LinearLayout,private val tv:TextView) : View.OnClickListener{
        override fun onClick(v: View?) {
            if (v is TextView){
                val text = v.text.toString()
                if (text == "clear"){
                    tv.text = "Colors: 清除"
                    color = ""
                    layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }else{
                    tv.text = "Colors: #$text"
                    color = text
                    layout.setBackgroundColor(Color.parseColor("#$text"))
                }
            }
        }

    }


}