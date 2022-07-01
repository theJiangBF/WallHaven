package cool.thejiangbf.wallhaven

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.activity_preference.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PreferenceActivity : AppCompatActivity() {
    private val TAG = "壁纸天堂 * 偏好设置"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference)

        title = "偏好设置"

        listener()

        initData()
    }

    private fun initData() {
        cardLoading.visibility = View.VISIBLE

        val sp = getSharedPreferences("prefs", MODE_PRIVATE)
        val ak = sp.getString("apikey","").toString()
        val cate = sp.getString("Categories","100").toString()
        val pure = sp.getString("Purity","100").toString()
        val sort = sp.getString("Sorting","relevance")
        val order = sp.getString("Order","desc")
        val top = sp.getString("TopRange","1M")
        val color = sp.getString("Color","")

        val arrCate = arrayOf(cate[0].toString().toInt(), cate[1].toString().toInt(), cate[2].toString().toInt())
        val arrPure = arrayOf(pure[0].toString().toInt(), pure[1].toString().toInt(), pure[2].toString().toInt())

        etApikey.setText(ak)
        Categories.tag = arrCate
        Purity.tag = arrPure
        Sorting.tag = sort
        Order.tag = order
        TopRange.tag = top
        tvColors.tag = color

        rgTopRange.isEnabled = false


        if (arrCate[0] == 1){ cbCate1.isChecked = true }
        if (arrCate[1] == 1){ cbCate2.isChecked = true }
        if (arrCate[2] == 1){ cbCate3.isChecked = true }

        if (arrPure[0] == 1){ cbPurity1.isChecked = true }
        if (arrPure[1] == 1){ cbPurity2.isChecked = true }
        if (arrPure[2] == 1){ cbPurity3.isChecked = true }

        when(sort){
            "relevance" -> { rbRele.isChecked = true }
            "random" -> { rbRand.isChecked = true }
            "views" -> { rbViews.isChecked = true }
            "favorites" -> { rbFav.isChecked = true }
            "toplist" -> { rbTop.isChecked = true }
        }

        if (order == "desc"){
            rbDesc.isChecked = true
        }else{
            rbAsc.isChecked = true
        }

        when(top){
            "1d" -> {rbRange1.isChecked = true}
            "3d" -> {rbRange2.isChecked = true}
            "1w" -> {rbRange3.isChecked = true}
            "1M" -> {rbRange4.isChecked = true}
            "1y" -> {rbRange7.isChecked = true}
        }

        tvColors.text = "Colors: #$color"
        if (color?.length==6){
            linearColors.setBackgroundColor(Color.parseColor("#$color"))
        }

        GlobalScope.launch {
            delay(1000)
            runOnUiThread {
                cardLoading.visibility = View.GONE
            }
        }

    }

    private fun listener() {

        etApikey.addTextChangedListener {
            if (!TextUtils.isEmpty(it)){
                cbPurity3.isEnabled = true
            }else{
                cbPurity3.isEnabled = false
                cbPurity3.isChecked = false
            }
        }

        btnSave.setOnClickListener {
            cardLoading.visibility = View.VISIBLE

            val sp = getSharedPreferences("prefs", MODE_PRIVATE)
            val edit = sp.edit()

            val cate = Categories.tag as Array<Int>
            val pure = Purity.tag as Array<Int>

            edit.putString("Categories","${cate[0]}${cate[1]}${cate[2]}")
            edit.putString("Purity","${pure[0]}${pure[1]}${pure[2]}")
            edit.putString("Sorting",Sorting.tag as String)
            edit.putString("Order",Order.tag as String)
            edit.putString("TopRange",TopRange.tag as String)
            edit.putString("Color",tvColors.tag as String)
            edit.putString("apikey",etApikey.text.toString())

            edit.commit()



            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
            GlobalScope.launch {
                delay(1000)
                runOnUiThread {
                    cardLoading.visibility = View.GONE
                }
                delay(300)
                runOnUiThread {
                    finish()
                }
            }
        }

        cbCate1.setOnCheckedChangeListener(CategoryListener(Categories))
        cbCate2.setOnCheckedChangeListener(CategoryListener(Categories))
        cbCate3.setOnCheckedChangeListener(CategoryListener(Categories))

        cbPurity1.setOnCheckedChangeListener(PurityListener(Purity))
        cbPurity2.setOnCheckedChangeListener(PurityListener(Purity))
        cbPurity3.setOnCheckedChangeListener(PurityListener(Purity))

        rgSort.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.rbRele -> {
                    Sorting.tag = "relevance"
                    Sorting.text = "Sorting: relevance"
                }
                R.id.rbRand -> {
                    Sorting.tag = "random"
                    Sorting.text = "Sorting: random"
                }
                R.id.rbViews -> {
                    Sorting.tag = "views"
                    Sorting.text = "Sorting: views"
                }
                R.id.rbFav -> {
                    Sorting.tag = "favorites"
                    Sorting.text = "Sorting: favorites"
                }
                R.id.rbTop -> {
                    Sorting.tag = "toplist"
                    Sorting.text = "Sorting: toplist"
                    rgTopRange.isEnabled = true
                }
            }
        }

        rgOrder.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.rbDesc -> {
                    Order.tag = "desc"
                    Order.text = "Order: desc"
                }
                R.id.rbAsc -> {
                    Order.tag = "asc"
                    Order.text = "Order: asc"
                }
            }
        }

        rgTopRange.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.rbRange1 -> { TopRange.tag = "1d" }
                R.id.rbRange2 -> { TopRange.tag = "3d" }
                R.id.rbRange3 -> { TopRange.tag = "1w" }
                R.id.rbRange4 -> { TopRange.tag = "1M" }
                R.id.rbRange7 -> { TopRange.tag = "1y" }
            }
            Order.text = "Top Range: ${Order.tag}"

        }



        for(i in 0 until linearColors.childCount){
            val line = linearColors.getChildAt(i) as LinearLayout
            for (j in 0 until line.childCount){
                val textView = line.getChildAt(j) as TextView
                textView.setOnClickListener(ColorListener(linearColors,tvColors))
            }
        }

    }

    private class CategoryListener(private val textView: TextView):CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val arr:Array<Int> = textView.tag as Array<Int>
            when(buttonView?.id){
                R.id.cbCate1 -> { arr[0] = if (isChecked) 1 else 0 }
                R.id.cbCate2 -> { arr[1] = if (isChecked) 1 else 0 }
                R.id.cbCate3 -> { arr[2] = if (isChecked) 1 else 0 }
            }
            textView.tag = arr
            textView.text = "Categories: ${arr[0]}${arr[1]}${arr[2]}"
        }
    }

    private class PurityListener(private val textView: TextView):CompoundButton.OnCheckedChangeListener{
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val arr:Array<Int> = textView.tag as Array<Int>
            when(buttonView?.id){
                R.id.cbPurity1 -> { arr[0] = if (isChecked) 1 else 0 }
                R.id.cbPurity2 -> { arr[1] = if (isChecked) 1 else 0 }
                R.id.cbPurity3 -> {
                    arr[2] = if (isChecked) 1 else 0
                    if (isChecked){
                        Toast.makeText(buttonView.context, "You should konwn what you are doing !!!", Toast.LENGTH_SHORT).show()
                    }

                }
            }
            textView.tag = arr
            textView.text = "Purity: ${arr[0]}${arr[1]}${arr[2]}"
        }
    }

    class ColorListener(private val layout:LinearLayout,private val tv:TextView) : View.OnClickListener{
        override fun onClick(v: View?) {
            if (v is TextView){
                val text = v.text
                if (text == "clear"){
                    tv.text = "Colors: 清除"
                    tv.tag = null
                    layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
                }else{
                    tv.text = "Colors: #$text"
                    tv.tag = text
                    layout.setBackgroundColor(Color.parseColor("#$text"))
                }
            }
        }

    }
}