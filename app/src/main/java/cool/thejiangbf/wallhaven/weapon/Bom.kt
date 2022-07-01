package cool.thejiangbf.wallhaven.weapon

import android.util.Log
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Bom {
    private const val TAG = "壁纸天堂 * 浏览器"

    fun connect(url:String):Document?{
        return try {
            Log.i(TAG, "connect: url=$url")
            Jsoup.connect(url).get()
        }catch (hse : HttpStatusException){
            null
        }

    }
}