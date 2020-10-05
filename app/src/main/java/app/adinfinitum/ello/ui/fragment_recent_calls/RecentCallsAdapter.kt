package app.adinfinitum.ello.ui.fragment_recent_calls


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.adinfinitum.ello.databinding.FragmentRecentCallRecviewItemBinding
import app.adinfinitum.ello.model.RecentCall

class RecentCallsAdapter(
    val clickListener: RecentCallClickListener
) : RecyclerView.Adapter<RecentCallsAdapter.MyViewHolder>() {

    var dataList= listOf<RecentCall>()
        set(value) {
            field=value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):MyViewHolder {
        return MyViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(clickListener,dataList[position])
    }



    class MyViewHolder private constructor(val binding:FragmentRecentCallRecviewItemBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind( clickListener: RecentCallClickListener,
                  item: RecentCall){
            binding.recentCallItem=item
            binding.clickListener=clickListener
            binding.executePendingBindings()
        }


        companion object{
            fun from(parent: ViewGroup): MyViewHolder {
                val inflater= LayoutInflater.from(parent.context)
                val binding = FragmentRecentCallRecviewItemBinding.inflate(inflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

}

class RecentCallClickListener(val clickListener:(key:String)->Unit ){
    fun onClick(phone:String)=clickListener(phone)
}

