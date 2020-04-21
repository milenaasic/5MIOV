package com.vertial.fivemiov.ui.fragment_main

import android.annotation.SuppressLint
import android.content.ContentUris
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.ContactsContract
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.vertial.fivemiov.R

private val MYTAG="MY_NOVIADAPTER"
class MainFragmentAdapter2(var filterCursor: Cursor?)  : RecyclerView.Adapter<MainFragmentAdapter2.ViewHolder?>() {


    var filterString: String? = null

    companion object {
        // konstante koje definišu Cursor kolone, ali koje mi mozda ovde ne trebaju, jer ovde ne vadim ništa iz kolona
        private val CURSOR_ID=0
        private val CURSOR_LOOKUP_KEY=1
        private val CURSOR_NAME=2
        private val CURSOSR_PHOTO_THUMBNAIL_URI=3

    }

    override fun getItemCount(): Int {
      return filterCursor?.count?:0
    }

    fun setFilterCursorAndFilterString(
        c: Cursor?,
        filter: String?
    ) {
        if (c != null) {
            if (c.moveToPosition(1)) {
                Log.i(MYTAG,"cursor poz 1, ${c.getString(CURSOR_NAME)}")
            }
            filterCursor = c

        }
        filterString = filter

        notifyDataSetChanged()

    }


    interface OnViewHolderClicked {
        fun viewHolderClicked(v: View?, position: Int)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val rootView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_main_rec_view_item_type1, parent, false)
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {


        if (filterCursor != null && filterCursor!!.count != 0) {

            if (filterCursor!!.moveToPosition(position)) {

                /*val contactId = filterCursor!!.getInt(CURSOR_COLUMN_ID)

                val contactUri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, contactId.toLong()
                )

                val photoThumbUri = Uri.withAppendedPath(
                    contactUri,
                    ContactsContract.Contacts.Photo.CONTENT_DIRECTORY
                )
                val photoUri = Uri.withAppendedPath(
                    contactUri,
                    ContactsContract.Contacts.Photo.DISPLAY_PHOTO
                )
                val hasThumbnail =
                    filterCursor!!.isNull(CURSOR_PHOTO_THUMBNAIL_URI)
                val hasPhoto =
                    filterCursor!!.isNull(CURSOR_PHOTO_URI)*/

                val newNameOriginal = filterCursor!!.getString(CURSOR_NAME)

                holder.mItemTextView.text=newNameOriginal
                holder.mletterInCircle.text="M"
                /*val newName: String
                val maxLength = 37
                newName = if (newNameOriginal.length > maxLength) {
                    newNameOriginal.substring(0, maxLength - 4) + "..."
                } else {
                    newNameOriginal
                }*/

                /*if (!hasThumbnail) {
                    GlideApp.with(mViewHolderClicked as Fragment)
                        .load(photoThumbUri)
                        .error(R.drawable.checkbox)
                        .circleCrop()
                        .into(holder.mItemImageView)
                    holder.mletterInCircle.text = ""
                } else {
                    if (!hasPhoto) {
                        GlideApp.with(mViewHolderClicked as Fragment)
                            .load(photoUri)
                            .error(R.drawable.checkbox)
                            .circleCrop()
                            .into(holder.mItemImageView)
                        holder.mletterInCircle.text = ""
                    } else {
                        GlideApp.with(mViewHolderClicked as Fragment)
                            .load(R.drawable.checkbox)
                            .error(R.color.colorPrimary)
                            .circleCrop()
                            .into(holder.mItemImageView)
                        val firstLetter = newName.trim { it <= ' ' }[0]
                        val data = charArrayOf(firstLetter)
                        holder.mletterInCircle.text = String(data).toUpperCase()
                    }
                }*/
                //oboj slova koja su u upitu
                /*if (filterString != null) {
                    val spannableString = SpannableString(newName)
                    val colorSpan =
                        ForegroundColorSpan(Color.parseColor("#ff4081"))
                    val startIndex =
                        newName.toLowerCase().indexOf(filterString!!.toLowerCase())
                    if (startIndex != -1) {
                        val lastIndex = startIndex + filterString!!.length
                        spannableString.setSpan(colorSpan, startIndex, lastIndex, 0)
                        holder.mItemTextView.text = spannableString
                    } else {
                        holder.mItemTextView.text = newName
                    }
                } else holder.mItemTextView.text = newName
                if (position == 0) {
                    val firstLetter = newName[0]
                    val data = charArrayOf(firstLetter)
                    holder.showSeparator.visibility = View.GONE
                    holder.firstLetter.text = String(data)
                } else {
                    if (filterCursor!!.moveToPosition(position - 1)) {
                        val lastName =
                            filterCursor!!.getString(CURSOR_DISPLAY_NAME_PRIMARY)
                        val lastLetter = lastName[0]
                        val newLetter = newName[0]
                        if (lastLetter != newLetter) {
                            val data = charArrayOf(newLetter)
                            holder.firstLetter.text = String(data)
                            holder.showSeparator.visibility = View.VISIBLE
                        } else {
                            holder.firstLetter.text = ""
                            holder.showSeparator.visibility = View.GONE
                        }
                        filterCursor!!.moveToPosition(position)
                    }
                }*/
            }
        }
    }

    /*val itemCount: Int
        get() = if (filterCursor == null) {
            0
        } else filterCursor!!.count*/
    //@SuppressLint("CutPasteId") ovo je nesto bilo bezveze

    inner class ViewHolder internal constructor(view: View) :
        RecyclerView.ViewHolder(view), View.OnClickListener {

         val mItemImageView: ImageView
        val mItemTextView: TextView
        val firstLetter: TextView

         val touchLayout: ConstraintLayout
        val mletterInCircle: TextView

        override fun onClick(v: View) {
            val position: Int = getAdapterPosition()
            //mViewHolderClicked.viewHolderClicked(v, position)
        }

        init {
            mItemImageView = view.findViewById(R.id.imageView_contact)
            mItemTextView = view.findViewById(R.id.textView_contact_name)
            firstLetter = view.findViewById(R.id.textView_capital_letter)
            touchLayout = view.findViewById(R.id.rec_view_item_constr_layout)
            touchLayout.setOnClickListener(this)
            mletterInCircle = view.findViewById(R.id.textView_capital_letter)
        }
    }



}