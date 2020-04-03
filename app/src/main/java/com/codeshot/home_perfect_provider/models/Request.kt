package com.codeshot.home_perfect_provider.models

import android.annotation.SuppressLint
import android.location.Location
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.codeshot.home_perfect_provider.R
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Request: Serializable {
    var id:String?=null
    var from:String?=null
    var to:String?=null
    var time: Date? = null

    var status:String?=null
    val address=HashMap<String,String>()
    var requestLocation: Location?=null
    var additions=ArrayList<Addition>()
    var perHour:Double?=0.0
    var totalPrice:Double?=0.0

    var providerUserName: String? = null
    var providerUserImage: String? = null

    var customerUserName: String? = null
    var customerUserImage: String? = null

    constructor()

    companion object {
        @JvmStatic
        @BindingAdapter("ImgCRequest")
        fun loadCRequestImage(view: CircleImageView?, url: String?) {
            if (url != null)
                Picasso.get().load(url).placeholder(R.drawable.ic_person_black_24dp)
                    .error(android.R.color.holo_red_dark).into(view)
        }

        @JvmStatic
        @BindingAdapter("additions")
        fun loadAddictionsRequest(list: ListView, additions: ArrayList<Addition>) {
            if (additions.size!=0){
                val listArray=ArrayList<String>()
                additions.forEach {
                    listArray.add(it.name.toString())
                }
                val adapter=ArrayAdapter(list.context,android.R.layout.simple_list_item_1,listArray)
                list.adapter=adapter
            }
        }

        @SuppressLint("ResourceAsColor")
        @JvmStatic
        @BindingAdapter("statusRequest")
        fun loadRequestStatus(textView: TextView?, status: String?) {
            when (status) {
                "accepted" ->{
                    textView!!.setTextColor(textView.context.resources.getColor(android.R.color.holo_green_light))
                    textView.text=textView.context.resources.getString(R.string.accepted)
                }
                "waiting" -> {
                    textView!!.setTextColor(textView.context.resources.getColor(android.R.color.holo_blue_dark))
                    textView.text=textView.context.resources.getString(R.string.waiting)

                }
                "in progress" -> {
                    textView!!.setTextColor(textView.context.resources.getColor(android.R.color.holo_blue_bright))
                    textView.text=textView.context.resources.getString(R.string.in_progress)

                }
                "done" -> {
                    textView!!.setTextColor(textView.context.resources.getColor(android.R.color.holo_green_dark))
                    textView.text=textView.context.resources.getString(R.string.done)

                }
                "rating" -> {
                    textView!!.setTextColor(textView.context.resources.getColor(android.R.color.black))
                    textView.text=textView.context.resources.getString(R.string.rating)

                }
                "canceled"-> {
                    textView!!.setTextColor(textView.context.resources.getColor(android.R.color.holo_red_light))
                    textView.text=textView.context.resources.getString(R.string.canceled)

                }
                else -> return

            }
        }

    }

}