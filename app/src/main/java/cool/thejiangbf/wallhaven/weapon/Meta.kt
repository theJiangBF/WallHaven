package cool.thejiangbf.wallhaven.weapon

import android.content.Context
import android.content.pm.PackageManager

object Meta {
    var apikey = ""

    fun get(context: Context, name:String):String{
        val appInfo = context.packageManager.getApplicationInfo(context.packageName,PackageManager.GET_META_DATA)
        val ak = appInfo.metaData.getString("apikey")
        apikey = ak ?: ""
        return apikey
    }
}