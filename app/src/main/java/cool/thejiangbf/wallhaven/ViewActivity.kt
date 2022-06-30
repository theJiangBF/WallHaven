package cool.thejiangbf.wallhaven

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import cool.thejiangbf.wallhaven.weapon.Bmp
import cool.thejiangbf.wallhaven.weapon.browser
import cool.thejiangbf.wallhaven.weapon.document
import kotlinx.android.synthetic.main.activity_view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.nodes.Document
import java.io.File
import java.util.*

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
    private lateinit var futureBitmap:FutureTarget<Bitmap>




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        url = intent.getStringExtra("url").toString()
        val snap = intent.getByteArrayExtra("snap")
        val bmp = snap?.let { Bmp.byteArray2Bmp(it) }
        Log.i(TAG, "onCreate: 收到url:$url")

        if (url != null) {

            GlobalScope.launch {
                doc = browser.connect(url)
                val src = document.getElementById(doc.html(),"wallpaper").attr("src")

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
                true
            }else {
                Log.i(TAG, "onCreate: 显示!!")
                linearProperties.startAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_prop_show))
                false
            }

        }

        linearProperties.startAnimation(AnimationUtils.loadAnimation(this,R.anim.anim_prop_hide))

    }

    private fun save(){
        val check = ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (check == PackageManager.PERMISSION_GRANTED){
            val wallHaven = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"WallHaven")
            if (!wallHaven.exists()){
                wallHaven.mkdir()
            }
            val name = "${uploader}_${category}_${purity}_${System.currentTimeMillis()}.png"


            futureBitmap = Glide.with(this).asBitmap().listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    Toast.makeText(this@ViewActivity, "图片保存失败,网络错误!", Toast.LENGTH_SHORT).show()
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    if (this@ViewActivity::futureBitmap.isInitialized){
                        val exp = Bmp.save(futureBitmap.get() ,File(wallHaven,name))
                        if (exp == null){
                            Toast.makeText(this@ViewActivity, "图片保存成功!", Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(this@ViewActivity, "保存失败:${exp.message}", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(this@ViewActivity, "请稍后重试!", Toast.LENGTH_SHORT).show()
                    }
                    return false
                }

            }) .load(url).submit()


        }else{
            Toast.makeText(this, "保存失败,没有权限!", Toast.LENGTH_LONG).show()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.save -> {
                Log.i(TAG, "onOptionsItemSelected: 保存图片")
                save()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.big,menu)
        return super.onCreateOptionsMenu(menu)
    }

}