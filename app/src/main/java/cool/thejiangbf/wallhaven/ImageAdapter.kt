package cool.thejiangbf.wallhaven

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.util.Pair as P
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import cool.thejiangbf.wallhaven.weapon.Bmp


class ImageAdapter(val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var loadStat = 0

    private var list = mutableListOf<Wallpaper>()

    fun update(new:MutableList<Wallpaper>){
        list.clear()
        list.addAll(new)
        notifyDataSetChanged()
    }

    fun clear(){
        list.clear()
        notifyDataSetChanged()
    }

    fun insert(new:MutableList<Wallpaper>){
        list.addAll(new)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ITEM){
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false))
        }else{
            FootVH(LayoutInflater.from(parent.context).inflate(R.layout.item_foot, parent, false))
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VH){
            val wallpaper = list[position]
            with(holder){
                Glide.with(context).load(wallpaper.snapshot).placeholder(R.drawable.bad).diskCacheStrategy(DiskCacheStrategy.ALL).into(ivSnapshot)
                tvSize.text = wallpaper.size
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context,ViewActivity::class.java)

                val pair1 = P<View, String>(holder.ivSnapshot, "big")
                val options = ActivityOptions.makeSceneTransitionAnimation(context as Activity, pair1)

                intent.putExtra("url",wallpaper.photo)
                if (holder.ivSnapshot.drawable!=null){
                    intent.putExtra("snap",Bmp.bmp2Bytes(holder.ivSnapshot.drawable.toBitmap()))
                }
                context.startActivity(intent,options.toBundle())
                context.overridePendingTransition(0,0)
            }

        }else if (holder is FootVH){
            when(loadStat){
                LOADING -> {
                    holder.tvLoading.visibility = View.VISIBLE
                    holder.tvLoading.text = "加载中,请耐心等待..."
                }
                LOAD_COMPLETE -> {
                    holder.tvLoading.visibility = View.VISIBLE
                    holder.tvLoading.text = "加载完成"
                }
                LOAD_END -> {
                    holder.tvLoading.visibility = View.VISIBLE
                    holder.tvLoading.text = "亲！到底线了~~~"
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return if (list.isEmpty()) 0 else list.size+1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position+1 == itemCount){
            TYPE_FOOTER
        }else{
            TYPE_ITEM
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val manager = recyclerView.layoutManager
        if (manager is GridLayoutManager) {
            manager.spanSizeLookup = object : SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    // 如果当前是footer的位置，那么该item占据2个单元格，正常情况下占据1个单元格
                    return if (getItemViewType(position) == TYPE_FOOTER) manager.spanCount else 1
                }
            }
        }
    }


    fun setLoadState(state:Int){
        this.loadStat = state
        notifyDataSetChanged()
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivSnapshot: ImageView = itemView.findViewById(R.id.ivSnapshot)
        val tvSize: TextView = itemView.findViewById(R.id.tvSize)
    }

    class FootVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLoading: TextView = itemView.findViewById(R.id.tvLoading)
    }

    companion object{
        public const val TYPE_FOOTER = 2
        public const val TYPE_ITEM = 1
        public const val LOADING = 10
        public const val LOAD_COMPLETE = 11
        public const val LOAD_END = 12
    }

}