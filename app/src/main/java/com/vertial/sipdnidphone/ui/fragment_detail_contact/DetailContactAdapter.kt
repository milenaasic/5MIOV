package com.vertial.sipdnidphone.ui.fragment_detail_contact

import com.vertial.sipdnidphone.databinding.DetailContactRecViewPhoneBinding

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


private val MYTAG="MY_DetailContactAdapter"

class DetailContactAdapter(val clickListenerSIP: SipItemClickListener,val clickListenerPrenumber:PrenumberItemClickListener)
    : RecyclerView.Adapter<DetailContactAdapter.MyViewHolder>() {


    var dataList= listOf<PhoneItem>()
        set(value) {
            Log.i(MYTAG,"setvalue data list")
            Log.i(MYTAG,"setvalue $value")
            field=value
            notifyDataSetChanged()

        }

    override fun getItemCount(): Int {
        Log.i(MYTAG,"data list size ${dataList.size}")
        return dataList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):MyViewHolder {
        Log.i("MYTAG","onCreateViewHolder funkcija")
        return MyViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.bind(clickListenerSIP,clickListenerPrenumber,dataList[position])
    }



    class MyViewHolder private constructor(val binding: DetailContactRecViewPhoneBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListenerSip: SipItemClickListener,clickListenerPrenumber:PrenumberItemClickListener,item:PhoneItem){
            Log.i("MYTAG","phone item je $item")
            binding.phoneItem=item
            binding.sipClick=clickListenerSip
            binding.prenumberClick=clickListenerPrenumber

            binding.executePendingBindings()
        }


        companion object{
            fun from(parent: ViewGroup): MyViewHolder {
                val inflater= LayoutInflater.from(parent.context)
                val binding = DetailContactRecViewPhoneBinding.inflate(inflater, parent, false)
                return MyViewHolder(binding)
            }
        }
    }

}

class SipItemClickListener(val clickListener:(key:String)->Unit ){
    fun onClick(phone:String)=clickListener(phone)
}

class PrenumberItemClickListener(val clickListener:(key:String)->Unit ){
    fun onClick(phone:String)=clickListener(phone)
}