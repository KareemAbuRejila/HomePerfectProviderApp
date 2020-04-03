package com.codeshot.home_perfect_provider.models

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.codeshot.home_perfect_provider.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import io.ghyeok.stickyswitch.widget.StickySwitch
import java.io.Serializable


class Provider : Serializable {
    lateinit var personalImageUri: String
    var userName: String? = ""
    var id: String? = null
    var phone: String? = ""
    var birthDay: String? = ""
    var gender: String? = ""
    var age: Int? = 0
    var serviceType: String? =null
    var serviceId: String?=null
    var rate: Double? = 0.0
    var perHour: Double? = 0.0
    var online: Boolean = false
    var requests= ArrayList<String>()
    var address= HashMap<String, String>()
    var additions= ArrayList<Addition>()

    constructor()
    constructor(
        personalImageUri: String,
        userName: String?,
        phone: String?,
        birthDay: String?,
        gender: String?,
        age: Int?,
        serviceId:String?,
        serviceType: String?,
        perHour: Double?,
        address: HashMap<String, String>,
        additions: ArrayList<Addition>
    ) {
        this.personalImageUri = personalImageUri
        this.userName = userName
        this.phone = phone
        this.birthDay = birthDay
        this.gender = gender
        this.age = age
        this.serviceId = serviceId
        this.serviceType = serviceType
        this.perHour = perHour
        this.address = address
        this.additions = additions
    }


    companion object {
        @JvmStatic
        @BindingAdapter("ImgVProvider")
        fun loadImageProvider(view: ImageView, url: String) {
            if (url != "")
                Picasso.get().load(url).placeholder(R.drawable.ic_person_black_24dp).into(view)
        }

        @JvmStatic
        @BindingAdapter("ImgCProvider")
        fun loadImage(view: CircleImageView?, url: String?) {
            if (url != null)
                Picasso.get().load(url).placeholder(R.drawable.ic_person_black_24dp)
                    .error(R.color.error_color).into(view)
        }
        @JvmStatic
        @BindingAdapter("status")
        fun loadProviderStatus(switcher: StickySwitch,online:Boolean){
            if (online)
                switcher.setDirection(StickySwitch.Direction.RIGHT)
            else
                switcher.setDirection(StickySwitch.Direction.LEFT)

        }
    }
}