package cool.thejiangbf.wallhaven.weapon

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Bom {
    private const val TAG = "壁纸天堂 * 浏览器"

    fun connect(url:String):Document{
        Log.i(TAG, "connect: url=$url")
        return Jsoup.connect(url).get()
    }
}