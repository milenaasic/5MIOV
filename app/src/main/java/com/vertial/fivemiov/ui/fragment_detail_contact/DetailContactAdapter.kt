package com.vertial.fivemiov.ui.fragment_detail_contact

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.DetailContactRecViewPhoneBinding
import com.vertial.fivemiov.utils.isOnline


private val MYTAG="MY_DetailContactAdapter"

class DetailContactAdapter(val clickListenerNumber:PhoneNumberClickListener,
                            val clickListenerSIP: SipItemClickListener,
                            val clickListenerPrenumber:PrenumberItemClickListener,
                            val app: Application
                            )
    : RecyclerView.Adapter<DetailContactAdapter.MyViewHolder>() {


    var dataList= listOf<PhoneItem>()
        set(value) {
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

        holder.bind(clickListenerNumber,clickListenerSIP,clickListenerPrenumber,dataList[position],
            isOnline(app))
    }



    class MyViewHolder private constructor(val binding: DetailContactRecViewPhoneBinding):
        RecyclerView.ViewHolder(binding.root) {

        fun bind(clickListenerNumber: PhoneNumberClickListener,
                clickListenerSip: SipItemClickListener,
                clickListenerPrenumber:PrenumberItemClickListener,
                item:PhoneItem,
                isOnline:Boolean){

            Log.i("MYTAG","phone item je $item")
            if(!isOnline) binding.sipCallButton.isEnabled=false
            else binding.sipCallButton.isEnabled=true
            binding.phoneItem=item
            binding.phoneClick=clickListenerNumber
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

class PhoneNumberClickListener(val activity: Activity,val density: Float){
    fun onClick(view:View){

        if(shouldViewExpand(view)) {
            animationExpandView(view)
            shrinkOtherViews(view)
        }
        else animationShrinkView(view.parent as ConstraintLayout)

    }

    private fun shrinkOtherViews(currentView: View) {

        val currentViewParent=currentView.parent as ConstraintLayout
        val recView=currentViewParent.parent as RecyclerView

        when (recView.childCount){
            0,1->{}
            else->{
                for(n in 0..recView.childCount-1){
                     val view=recView.getChildAt(n)
                     if(!currentViewParent.equals(view) && view.height.div(density)>=60f) animationShrinkView(view)
                }
            }
        }
    }

    private fun shouldViewExpand(view: View): Boolean {
        Log.i("MYTAG","density je $density")
        val parent=view.parent as ConstraintLayout
        val parentHeight=parent.height.toFloat()
        Log.i("MYTAG","parent kroz density je ${parentHeight.div(density)}")

        if(parentHeight.div(density)>=60f) return false
        else return true
    }


    private fun animationExpandView(view: View) {

        val parent=view.parent as ConstraintLayout

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            parent.background= activity.resources.getDrawable(R.drawable.dummy_background,null)
        }else{
            parent.background= activity.resources.getDrawable(R.drawable.dummy_background)
        }*/

       // parent.elevation=2f*density
       parent.elevation=4f

        val currentHeight=parent.height

        val extendedHeight=currentHeight.times(2)

        val animator = ValueAnimator.ofInt(currentHeight,extendedHeight)

        animator.addUpdateListener {
            val value = it.getAnimatedValue() as Int
            parent.layoutParams.height = value
            parent.requestLayout()
         }

        animator.start()

    }



    private fun animationShrinkView(parent: View){

       // parent.setBackgroundColor(Color.WHITE)
        parent.elevation=0f
        val currentHeight=parent.height
        Log.i("MYTAG","view current height je $currentHeight")
        val shrinkHeight=currentHeight.div(2)
        Log.i("MYTAG","view shrink height je $shrinkHeight")

        val animator = ValueAnimator.ofInt(currentHeight,shrinkHeight)
        animator.addUpdateListener {
            val value = it.getAnimatedValue() as Int
            parent.layoutParams.height = value
            parent.requestLayout()
        }

        animator.start()

    }


}