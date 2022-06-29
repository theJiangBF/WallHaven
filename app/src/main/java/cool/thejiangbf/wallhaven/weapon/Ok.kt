package cool.thejiangbf.wallhaven.weapon

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.*
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

/**
 * OKHttp工具
 * @author jiangbaofu
 */

object Ok {
    const val TAG = "Ok棒棒棒"

    private val map = mutableMapOf<String,String>()

    fun post(url:String,json:String, resultCallback: Callback){
        Log.i(TAG, "POST方法, url=$url, paramJson=$json")
        map[url] = json
        val rb = json.toRequestBody("application/json;charset=utf-8".toMediaType())
        val request = Request.Builder().url(url).post(rb).build()
        OkHttpClient().newCall(request).enqueue(resultCallback)

    }

    fun get(url:String, resultCallback: Callback){
        Log.i(TAG, "GET方法, url=$url")
        val request = Request.Builder().url(url).get().build()
        OkHttpClient().newCall(request).enqueue(resultCallback)
    }

    fun getAndroidId(context: Context):String{
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /**
     * 下载文件
     */
    fun downFile(url:String, file: File, listener: OnDownloadListener){
        Log.i(TAG, "下载文件: url=$url, file=${file.absolutePath}")

        val request = Request.Builder().url(url).build()
        OkHttpClient().newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                listener.onFail(e)
            }

            override fun onResponse(call: Call, response: Response) {

                try {
                    val buf = ByteArray(2048)
                    var len:Int
                    val inStream: InputStream = response.body?.byteStream()!!
                    val total = response.body?.contentLength() ?: 1
                    if (file.exists()){
                        file.delete()
                        file.createNewFile()
                    }
                    val fos = FileOutputStream(file)
                    var sum = 0
                    while ((inStream.read(buf).also { len = it }) != -1) {
                        fos.write(buf,0,len)
                        sum += len
                        val progress = (sum * 1f / total * 100).toInt()
                        listener.onProgress(progress)
                    }
                    fos.flush()
                    // 下载完成
                    listener.onSuccess(file)
                }catch (e:Exception){
                    Log.e(TAG, "onResponse: 保存失败", e)
                    listener.onFail(e)
                }

            }
        })

    }


    interface OnDownloadListener{
        fun onFail(e:Exception)
        fun onProgress(progress: Int)
        fun onSuccess(savedFile: File)
    }

    interface OkResponse:Callback{
        override fun onFailure(call: Call, e: IOException) {
            val url:String = call.request().url.toString()
            val param:String? = map.remove(url)

            Log.e(TAG, "\n====================")
            Log.e(TAG, "==== \t\t\t\t 请求失败 \t\t\t\t ====")
            Log.e(TAG, "地址 : $url")
            Log.e(TAG, "参数*: $param")
            Log.e(TAG, "方法 : ${call.request().method}")
            Log.e(TAG, "原因 : ",e)
            Log.e(TAG, "====================\n")
        }

        override fun onResponse(call: Call, response: Response){
            val sJson = response.body?.string()
            val url:String = call.request().url.toString()
            val param:String? = map.remove(url)

            Log.i(TAG, "╔====================")
            Log.i(TAG, "╠====  请求成功${response.code}  ====")
            Log.i(TAG, "╠-地址 : $url")
            Log.i(TAG, "╠-参数*: $param")
            Log.i(TAG, "╠-方法 : ${call.request().method}")
            Log.i(TAG, "╠-返回 : $sJson")
            Log.i(TAG, "╚====================")

            sJson?.let { sj ->
                val json = JSONObject(sj)

                val result = json.opt("result")
                result?.let {
                    Log.i(TAG, "OkResponse.onResponse: 成功")
                    result(it)
                }?:{
                    Log.w(TAG, "OkResponse.onResponse: 失败")
                    error(json)
                }
            }

        }

        fun result(resultAny :Any)
        fun error(json:JSONObject){
            Log.w(TAG, "error: JSON结果异常! $json")
        }

    }
}
