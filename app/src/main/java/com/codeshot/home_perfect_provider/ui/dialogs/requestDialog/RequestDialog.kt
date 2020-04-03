package com.codeshot.home_perfect_provider.ui.dialogs.requestDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.codeshot.home_perfect_provider.common.Common
import com.codeshot.home_perfect_provider.common.Common.USERS_REF
import com.codeshot.home_perfect_provider.R
import com.codeshot.home_perfect_provider.databinding.DialogRequestBinding
import com.codeshot.home_perfect_provider.models.User
import com.squareup.picasso.Picasso

class RequestDialog(val requestId:String) : DialogFragment() {

    private lateinit var dialogRequestBinding:DialogRequestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialogRequestBinding=DialogRequestBinding.inflate(inflater,container,false)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialogRequestBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

//        USER_REF.document(userId.toString()).get()
//            .addOnSuccessListener {documentSnapshot ->
//                if (documentSnapshot.exists()){
//                    val user=documentSnapshot.toObject(User::class.java)
//                    dialogRequestBinding.user=user
//                    Picasso.get().load(user!!.personalImageUri).placeholder(R.drawable.ic_person_black_24dp).into(dialogRequestBinding.userImage)
//                }
//            }

        Common.REQUESTS_REF.document(requestId)
            .get().addOnSuccessListener {document ->
//                val requset=document.toObject(Request::class.java)
//                val from=requset!!.from
                val from=document["from"].toString()
                USERS_REF.document(from).get()
                    .addOnSuccessListener {documentSnapshot ->
                        if (documentSnapshot.exists()){
                            val user=documentSnapshot.toObject(User::class.java)
                            dialogRequestBinding.user=user
                            Picasso.get().load(user!!.personalImageUri).placeholder(R.drawable.ic_person_black_24dp).into(dialogRequestBinding.userImage)
                        }
                    }
            }

    }
}