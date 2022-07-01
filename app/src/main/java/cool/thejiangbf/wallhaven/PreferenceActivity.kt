package cool.thejiangbf.wallhaven

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_preference.*

class PreferenceActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        title = "偏好设置"

        listener()

    }

    private fun listener() {
        for(i in 0 until linearColors.childCount){
            val line = linearColors.getChildAt(i) as LinearLayout
            for (j in 0 until line.childCount){
                val textView = line.getChildAt(j) as TextView
                textView.setOnClickListener(ColorListener(linearColors,tvColors))
            }
        }

    }


    class ColorListener(private val layout:LinearLayout,private val tv:TextView) : View.OnClickListener{
        override fun onClick(v: View?) {
            if (v is TextView){
                val text = v.text
                tv.text = "Colors: #$text"
                tv.tag = text
                layout.setBackgroundColor(Color.parseColor("#$text"))
            }
        }

    }
}