package com.codeshot.home_perfect_provider.models

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.codeshot.home_perfect_provider.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class User {
    var personalImageUri:String? = ""
    var userName:String? = ""
    var phone:String?=null
    var id: String? = null
    var requests:List<String>?=ArrayList()
    var email:String?=null
    var bod:String?=null
    var gender:String?=null
    var address=HashMap<String,String>()

    constructor(personalImageUri: String, userName: String) {
        this.personalImageUri = personalImageUri
        this.userName = userName
    }

    constructor()
    constructor(personalImageUri: String?) {
        this.personalImageUri = personalImageUri
    }


    companion object {
        @JvmStatic
        @BindingAdapter("ImgCUser")
        fun loadImageCUser(view: CircleImageView, url: String) {
            Picasso.get().load(url).placeholder(R.drawable.ic_person_black_24dp).error(R.color.error_color).into(view)
        }
        @JvmStatic
        @BindingAdapter("ImgVUser")
        fun loadImageVUser(view: ImageView?, url: String) {
            if (url!="")
                Picasso.get().load(url).placeholder(R.drawable.ic_person_black_24dp).into(view)
        }
    }



}