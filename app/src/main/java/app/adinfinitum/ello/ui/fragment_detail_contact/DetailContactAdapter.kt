package app.adinfinitum.ello.ui.fragment_detail_contact

import android.app.Activity
import android.app.Application
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.adinfinitum.ello.databinding.DetailContactRecViewPhoneBinding
import app.adinfinitum.ello.model.PhoneItem
import app.adinfinitum.ello.utils.isOnline


private val MYTAG="MY_DetailContactAdapter"

class DetailContactAdapter(
                            val clickListenerSIP: SipItemClickListener,
                            val clickListenerPrenumber:PrenumberItemClickListener,
                            val app: Application
                            )
    : RecyclerView.Adapter<DetailContactAdapter.MyViewHolder>() {


    var dataList= listOf<PhoneItem>()
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

        holder.bind(clickListenerSIP,clickListenerPrenumber,dataList[position],
            isOnline(app))
    }



    class MyViewHolder private constructor(val binding: DetailContactRecViewPhoneBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind( clickListenerSip: SipItemClickListener,
                 clickListenerPrenumber:PrenumberItemClickListener,
                 item: PhoneItem,
                 isOnline:Boolean){


            if(!isOnline) binding.sipCallButton.isEnabled=false
            else binding.sipCallButton.isEnabled=true
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

class PrenumberItemClickListener(val activity: Activity,val clickListener:(activity:Activity,phone:String)->Unit ){
    fun onClick(phone:String)=clickListener(activity,phone)
}

