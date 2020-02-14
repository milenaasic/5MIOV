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
        var shouldShow=true

        if(position!=0){
                val sameFirstLetter=dataList[position-1].name.first().equals(dataList[position].name.first(),true)
                /*Log.i(MYTAG,"prvo slovo prethodnog je ${dataList[position-1].name.first()} ")
            Log.i(MYTAG,"prvo slovo trenutnog je ${dataList[position].name.first()} ")
            Log.i(MYTAG,"poredjenje je $sameFirstLetter")*/
                if(sameFirstLetter) shouldShow=false
        }

        holder.bind(clickListener,dataList[position],shouldShow)
    }

    class MyViewHolder private constructor(val binding: FragmentMainRecViewItemType1Binding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ContactItemClickListener,item:ContactItem,shouldShow: Boolean){
           binding.clickListener=clickListener
            binding.contactItem=item
            binding.shouldShow=shouldShow
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

class ContactItemClickListener(val clickListener:(id:String)->Unit ){
    fun onClick(item:ContactItem)=clickListener(item.lookUpKey)
}