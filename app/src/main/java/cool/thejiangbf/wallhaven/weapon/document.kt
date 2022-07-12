package cool.thejiangbf.wallhaven.weapon

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

object document {
    fun getElementsByAttributeValue(html: String, attribute: String, name: String): Elements {
        val document = Jsoup.parse(html)
        return document.html(html).getElementsByAttributeValue(attribute, name)
    }

    fun getElementsByClass(html: String, className: String): Elements {
        val document = Jsoup.parse(html)
        return document.html(html).getElementsByClass(className)
    }

    fun getElementsByTag(html: String, tagName: String): Elements {
        val document = Jsoup.parse(html)
        return document.html(html).getElementsByTag(tagName)
    }

    fun getElementById(html: String, id: String): Element {
        val document = Jsoup.parse(html)
        return document.html(html).getElementById(id)
    }
}