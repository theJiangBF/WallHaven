package cool.thejiangbf.wallhaven.weapon

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.*

object Bmp {
    fun bmp2Bytes(bmp: Bitmap):ByteArray{
        val os = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, os)
        return os.toByteArray()
    }

    fun byteArray2Bmp(bytes:ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes,0,bytes.size)
    }

    /**
     * 保存Bitmap
     */
    fun save(bm: Bitmap,file:File):Exception? {
        if (file.exists()) {
            file.delete()
        }
        return try {
            val out = FileOutputStream(file)
            bm.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
            null
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            e
        } catch (e: IOException) {
            e.printStackTrace()
            e
        }

    }
}