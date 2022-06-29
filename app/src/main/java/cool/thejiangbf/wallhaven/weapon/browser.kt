package cool.thejiangbf.wallhaven.weapon

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object browser {
    fun connect(url:String):Document{
        return Jsoup.connect(url).get()
    }
}