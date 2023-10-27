package com.example.firebaseecom.homeUI

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.AdCorousalViewBinding

class CarousalAdapter(private val context: Context):RecyclerView.Adapter<CarousalAdapter.CarousalViewHolder>() {

    var imageList= emptyList<String?>()
    private lateinit var adCorousalViewBinding: AdCorousalViewBinding

    fun setAd(imageList: List<String>)
    {
        this.imageList=imageList
        notifyDataSetChanged()
    }
    class CarousalViewHolder(adCorousalViewBinding: AdCorousalViewBinding):RecyclerView.ViewHolder(adCorousalViewBinding.root) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CarousalViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        adCorousalViewBinding = DataBindingUtil.inflate(layoutInflater,
            R.layout.ad_corousal_view,parent
            ,false)
        return CarousalViewHolder(adCorousalViewBinding)

    }

    override fun onBindViewHolder(holder: CarousalViewHolder, position: Int) {
        val imageUrl=imageList[position]
        Log.d("carousal",imageUrl.toString())
        Glide.with(context)
            .load(imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .into(holder.itemView.findViewById(R.id.adView))

    }

    override fun getItemCount(): Int {
       return imageList.count()
    }
}