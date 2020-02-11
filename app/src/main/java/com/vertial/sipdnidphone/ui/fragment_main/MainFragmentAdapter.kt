package com.vertial.sipdnidphone.ui.fragment_main

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.databinding.FragmentMainRecViewItemType1Binding



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
        holder.bind(clickListener,dataList[position])
    }

    class MyViewHolder private constructor(val binding: FragmentMainRecViewItemType1Binding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ContactItemClickListener,item:ContactItem){
           binding.clickListener=clickListener
            binding.contactItem=item
            /*Glide.with(binding.imageViewContact)
                    .load(item.photoThumbUri)
                    .apply(RequestOptions().circleCrop())
                    .into(binding.imageViewContact)*/
            //binding.imageViewContact.setImageURI(Ur
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