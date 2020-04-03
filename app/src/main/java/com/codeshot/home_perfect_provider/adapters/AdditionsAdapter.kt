package com.codeshot.home_perfect_provider.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codeshot.home_perfect_provider.databinding.ItemAdditionBinding
import com.codeshot.home_perfect_provider.databinding.ItemRequestBinding
import com.codeshot.home_perfect_provider.models.Addition
import com.codeshot.home_perfect_provider.models.Request

class AdditionsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var itemAdditionBinding: ItemAdditionBinding
    private lateinit var itemRequestBinding: ItemRequestBinding
    private var typeView:Int?=null

    private var additions=ArrayList<Addition>()
    private var requests:List<Request>?=ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        return  when (typeView) {
            0 -> {
                itemAdditionBinding = ItemAdditionBinding.inflate(inflater, parent, false)
                AdditionItem(itemAdditionBinding)
            }
            else -> {
                itemRequestBinding=ItemRequestBinding.inflate(inflater,parent,false)
                RequestItem(itemRequestBinding)
            }
        }
    }


    override fun getItemCount(): Int {
        return when {
            additions.isNotEmpty() -> {
                additions.size
            }
            requests!!.isNotEmpty() -> {
                requests!!.size
            }
            else -> 0
        }
    }
    fun setType(typeView:Int){
        this.typeView=typeView
        notifyDataSetChanged()
    }
//    fun addAddition(addition:Addition){
//        additions.add(addition)
//        notifyDataSetChanged()
//    }
    fun setList(additions:ArrayList<Addition>){
        this.additions=additions
        notifyDataSetChanged()
    }
    fun setListRequests(requests:List<Request>){
        this.requests=requests
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(typeView){
            0->(holder as AdditionItem).bindItem(addition = additions[position])
            1->(holder as RequestItem).bindItem(request = requests!![position])
        }
    }

    inner class AdditionItem(private val itemAdditionBinding: ItemAdditionBinding):
        RecyclerView.ViewHolder(itemAdditionBinding.root) {
        fun bindItem(addition: Addition){
            itemAdditionBinding.addition=addition
            itemAdditionBinding.executePendingBindings()
        }
    }
    inner class RequestItem(private val itemRequestBinding: ItemRequestBinding):
            RecyclerView.ViewHolder(itemRequestBinding.root){
        fun bindItem(request:Request){
            itemRequestBinding.request=request
            itemRequestBinding.executePendingBindings()
        }
    }
}