package com.vertial.fivemiov.ui.fragment_main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.vertial.fivemiov.databinding.FragmentMainRecViewItemType1Binding
import com.vertial.fivemiov.model.ContactItem
import com.vertial.fivemiov.model.RawContactWithoutNumber
import com.vertial.fivemiov.utils.EMPTY_NAME
import java.util.*
import kotlin.collections.ArrayList


private val MYTAG="MY_MainFragmentAdapter"
class MainFragmentAdapter(val clickListener: ContactItemClickListener,val myColor: String)
    : RecyclerView.Adapter<MainFragmentAdapter.MyViewHolder>(){

    var filteredList=listOf<ContactItem>()

    var dataList= listOf<ContactItem>()
        set(value) {
            field=value
            notifyDataSetChanged()

        }

    var stringToColor:String?=null

    override fun getItemCount(): Int {
        return dataList.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):MyViewHolder {
        return MyViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var viewType= MyViewHolderType(DEFAULT_LOOK)

        when{
            dataList[position].name.equals(EMPTY_NAME)->viewType= MyViewHolderType(DEFAULT_LOOK)
            position==0->viewType= MyViewHolderType(POSITION_0_IN_LIST)
            position!=0->{
                if(!dataList[position-1].name.first().equals(dataList[position].name.first(),true))
                    viewType= MyViewHolderType(SHOW_FIRST_LETTER)
            }

        }

        holder.bind(clickListener,dataList[position],viewType,stringToColor,myColor)

    }

    companion object{
        const val POSITION_0_IN_LIST=0
        const val SHOW_FIRST_LETTER=1
        const val DEFAULT_LOOK=2

    }

    class MyViewHolder private constructor(val binding: FragmentMainRecViewItemType1Binding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListener: ContactItemClickListener, item: ContactItem, viewType:MyViewHolderType, stringToColor:String?, myColor: String){
           binding.clickListener=clickListener
            binding.contactItem=item
            binding.viewType=viewType
            binding.textToColor=stringToColor
            binding.myColor=myColor
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

    /*override fun getFilter(): Filter {
        Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val charSearch = constraint.toString()
                    if (charSearch.isEmpty()) {
                        filteredList=dataList
                    } else {
                        val resultList = ArrayList<String>()
                        for (item in dataList) {
                            if (item.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT))) {
                                resultList.add(item)
                            }
                        }
                        filteredList = resultList
                    }
                    val filterResults = FilterResults()
                    filterResults.values = countryFilterList
                    return filterResults
                }

                @Suppress("UNCHECKED_CAST")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    result = results?.values as List<ContactItem>
                    notifyDataSetChanged()
                }

            }
        }
    }*/
}

class ContactItemClickListener(val clickListener:(item: ContactItem)->Unit ){
    fun onClick(item: ContactItem)=clickListener(item)
}

data class MyViewHolderType(val type:Int)