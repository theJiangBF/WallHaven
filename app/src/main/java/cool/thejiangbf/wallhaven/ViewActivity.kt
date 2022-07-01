package cool.thejiangbf.wallhaven

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import cool.thejiangbf.wallhaven.weapon.Bmp
import cool.thejiangbf.wallhaven.weapon.Bom
import cool.thejiangbf.wallhaven.weapon.document
import kotlinx.android.synthetic.main.activity_view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import java.io.File


class ViewActivity : AppCompatActivity() {
    private val TAG = "壁纸天堂 * 大图"
    private lateinit var doc:Document
    private var hiding = true

    private var uploader = ""
    private var category = ""
    private var purity = ""
    private var size = ""
    private var views = ""
    private var favs = ""

    private var url = ""
    private var src = ""
    private lateinit var futureBitmap:FutureTarget<Bitmap>




    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_view)

        url = intent.getStringExtra("url").toString()
        val snap = intent.getByteArrayExtra("snap")
        val bmp = snap?.let { Bmp.byteArray2Bmp(it) }
        Log.i(TAG, "onCreate: 收到url:$url")

        if (!TextUtils.isEmpty(url)) {

            GlobalScope.launch {
                doc = Bom.connect(url)
                src = document.getElementById(doc.html(),"wallpaper").attr("src")

                val sp = getSharedPreferences("splash", MODE_PRIVATE)
                val edit = sp.edit()
                edit.putString("url",src)
                edit.apply()

                val dl = document.getElementsByTag(doc.html(),"dl")[0]
                val dds = document.getElementsByTag(dl.html(),"dd")

                uploader = dds[0].child(1).text()
                category = dds[1].text()
                purity = dds[2].child(0).text()
                size = dds[3].text()
                views = dds[4].text()
                favs = dds[5].child(0).text()


                Log.i(TAG, "onCreate: wallpaper.src=$src")
                runOnUiThread {

                    tvUploader.text = uploader
                    tvCategory.text = category
                    tvPurity.text = purity
                    tvSize.text = size
                    tvViews.text = views
                    tvFavourites.text = favs

                    Glide.with(this@ViewActivity)
                        .load(src)
                        .placeholder(BitmapDrawable(resources,bmp))
                        .error(R.drawable.bad)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                Log.i(TAG, "onLoadFailed: 图片加载失败")
                                loading.hide()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.i(TAG, "onLoadFailed: 图片加载完成")
                                loading.hide()
                                linearProperties.startAnimation(AnimationUtils.loadAnimation(this@ViewActivity,R.anim.anim_prop_show))
                                releativeAction.startAnimation(AnimationUtils.loadAnimation(this@ViewActivity,R.anim.anim_prop_show_act))
                                hiding = false
                                return false
                            }

                        })
                        .into(ivBig)

                }
            }

        }

        ivBig.setOnClickListener {
            hiding = if (!hiding){
                Log.i(TAG, "onCreate: 隐藏!!")
                linearProperties.startAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_prop_hide))
                releativeAction.startAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_prop_hide_act))
                true
            }else {
                Log.i(TAG, "onCreate: 显示!!")
                linearProperties.startAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_prop_show))
                releativeAction.startAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_prop_show_act))
                false
            }

        }

        ivApply.setOnClickListener {
            wallpaper()
        }

        ivSave.setOnClickListener {
            loading.visibility = View.VISIBLE
            loading.show()
            GlobalScope.launch {
                save()
            }
        }

        linearProperties.startAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_prop_hide))
        releativeAction.startAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_prop_hide))

    }

    private fun save(){
        val check = ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (check == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "save: 有权限")
            val wallHaven = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"WallHaven")
            if (!wallHaven.exists()){
                val mk = wallHaven.mkdir()
                Log.i(TAG, "save: 文件夹不存在,正在创建,结果$mk")
            }
            val name = "${uploader}_${category}_${purity}_${System.currentTimeMillis()}.png"


            futureBitmap = Glide.with(this).asBitmap().listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    Log.w(TAG, "onLoadFailed: 网络异常,保存失败!")
                    runOnUiThread {
                        loading.hide()
                        Toast.makeText(this@ViewActivity, "图片保存失败,网络错误!", Toast.LENGTH_SHORT).show()
                    }
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    Log.i(TAG, "onResourceReady: ")
                    return false
                }


            }) .load(src).submit()


            if (this@ViewActivity::futureBitmap.isInitialized){
                val exp = Bmp.save(futureBitmap.get() ,File(wallHaven,name))

                runOnUiThread {
                    loading.hide()
                    if (exp == null){
                        Log.i(TAG, "onResourceReady: 保存成功")
                        Toast.makeText(this@ViewActivity, "图片保存成功!", Toast.LENGTH_SHORT).show()
                    }else {
                        Log.w(TAG, "onResourceReady: 保存失败啦")
                        Toast.makeText(this@ViewActivity, "保存失败:${exp.message}", Toast.LENGTH_SHORT).show()
                    }
                }

            }else{
                Log.w(TAG, "onResourceReady: futureBitmap没有初始化")
                runOnUiThread {
                    loading.hide()
                    Toast.makeText(this@ViewActivity, "请稍后重试!", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Log.w(TAG, "save: 没有权限")
            runOnUiThread {
                Toast.makeText(this, "保存失败,没有权限!", Toast.LENGTH_LONG).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun wallpaper(){
        Log.i(TAG, "wallpaper: 设置中...")
        Toast.makeText(this, "设置中...", Toast.LENGTH_SHORT).show()
        GlobalScope.launch {
            val wpm = getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager
            val mBitmap = ivBig.drawable.toBitmap() //path为绝对路径
            wpm.setBitmap(mBitmap, Rect(0, 0, mBitmap.width, mBitmap.height), true)
            Log.i(TAG, "wallpaper: 设置成功")

            runOnUiThread {
                Toast.makeText(this@ViewActivity, "设置成功!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }


    }

    override fun onResume() {
        super.onResume()
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),0x7f)
    }

    override fun onStop() {
        super.onStop()
        Glide.with(this).pauseAllRequests()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAfterTransition()
        overridePendingTransition(0,0)
    }

    private fun hideNavigation(){
        this.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)

        supportActionBar?.hide()
    }

//    @RequiresApi(Build.VERSION_CODES.N)
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            R.id.save -> {
//                Log.i(TAG, "onOptionsItemSelected: 保存图片")
//                loading.visibility = View.VISIBLE
//                loading.show()
//                GlobalScope.launch {
//                    save()
//                }
//            }
//            R.id.apply -> {
//                wallpaper()
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.big,menu)
//        return super.onCreateOptionsMenu(menu)
//    }

}