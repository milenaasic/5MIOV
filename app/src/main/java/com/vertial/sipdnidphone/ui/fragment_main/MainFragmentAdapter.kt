package com.vertial.sipdnidphone.ui.fragment_main

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.databinding.FragmentMainRecViewItemType1Binding


private val MYTAG="MY_MainFragmentAdapter"
class MainFragmentAdapter(val clickListener: ContactItemClickListener)
    : RecyclerView.Adapter<MainFragmentAdapter.MyViewHolder>() {


    var dataList= listOf<ContactItem>()
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
        var viewType= MyViewHolderType(DEFAULT_LOOK)
        if(position==0)viewType= MyViewHolderType(POSITION_0_IN_LIST)
        if(position!=0){
                if(!dataList[position-1].name.first().equals(dataList[position].name.first(),true))
                viewType= MyViewHolderType(SHOW_FIRST_LETTER)
        }

        holder.bind(clickListener,dataList[position],viewType)
    }

    companion object{
        const val POSITION_0_IN_LIST=0
        const val SHOW_FIRST_LETTER=1
        const val DEFAULT_LOOK=2

    }

    class MyViewHolder private constructor(val binding: FragmentMainRecViewItemType1Binding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ContactItemClickListener,item:ContactItem,viewType:MyViewHolderType){
           binding.clickListener=clickListener
            binding.contactItem=item
            binding.viewType=viewType
            binding.executePendingBindings()
        }


        companion object{
            fun from(parent: ViewGroup): MyViewHolder {
                val inflater= LayoutInflater.from(parent.context)
                val binding = FragmentMainRecViewItemType1Binding.inflate(inflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

}

class ContactItemClickListener(val clickListener:(item:ContactItem)->Unit ){
    fun onClick(item:ContactItem)=clickListener(item)
}

data class MyViewHolderType(val type:Int)