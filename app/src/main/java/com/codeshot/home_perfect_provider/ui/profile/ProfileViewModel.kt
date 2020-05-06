package com.codeshot.home_perfect_provider.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.codeshot.home_perfect_provider.common.Common
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.MetadataChanges

class ProfileViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    val connected = MutableLiveData<Boolean>().apply {
        Common.PROVIDERS_REF
            .addSnapshotListener(
                MetadataChanges.INCLUDE,
                EventListener { t, firebaseFirestoreException ->
                    if (firebaseFirestoreException != null) return@EventListener
                    value = !t!!.metadata.isFromCache
                })
    }
}
