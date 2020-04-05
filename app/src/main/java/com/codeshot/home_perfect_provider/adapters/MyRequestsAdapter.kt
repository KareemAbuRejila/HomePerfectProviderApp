package com.codeshot.home_perfect_provider.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codeshot.home_perfect_provider.databinding.ItemRequestBinding
import com.codeshot.home_perfect_provider.models.Request
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MyRequestsAdapter(options: FirestoreRecyclerOptions<Request>) :
    FirestoreRecyclerAdapter<Request, MyRequestsAdapter.RequestItem>(options) {
    private var onRequestListener: OnRequestListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestItem {
        val inflater = LayoutInflater.from(parent.context)
        val itemRequestBinding = ItemRequestBinding.inflate(inflater, parent, false)
        return RequestItem(itemRequestBinding)
    }

    override fun onBindViewHolder(holder: RequestItem, position: Int, model: Request) {
        holder.bindItem(model)
    }

    fun setListener(onRequestListener: OnRequestListener) {
        this.onRequestListener = onRequestListener
    }

    interface OnRequestListener {
        fun onRequestClick(requestId: String)
    }

    inner class RequestItem(private val itemRequestBinding: ItemRequestBinding) :
        RecyclerView.ViewHolder(itemRequestBinding.root) {
        fun bindItem(request: Request) {
            itemRequestBinding.request = request
            itemRequestBinding.executePendingBindings()
            if (adapterPosition != RecyclerView.NO_POSITION && onRequestListener != null) {
                onRequestListener!!.onRequestClick(requestId = snapshots.getSnapshot(adapterPosition).id)
            }
        }
    }
}