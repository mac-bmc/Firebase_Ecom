package com.example.firebaseecom.homeUI

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebaseecom.detailsUI.ProductDetailsActivity
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.ProductListViewBinding
import com.example.firebaseecom.model.ProductModel

class ProductListAdapter(private val context: Context) :
    RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {
    private var productDetails = emptyList<ProductModel>()

    private lateinit var productListViewBinding: ProductListViewBinding

    class ProductViewHolder(private val productListViewBinding: ProductListViewBinding) :
        RecyclerView.ViewHolder(productListViewBinding.root) {
        fun bind(productModel: ProductModel) {
            productListViewBinding.productDetails = productModel

        }

    }


    fun setProduct(productDetails: List<ProductModel>) {
        this.productDetails = productDetails
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        productListViewBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.product_list_view, parent, false)
        return ProductViewHolder(productListViewBinding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productDetails[position]
        holder.bind(product)
        Glide.with(context)
            .load(R.drawable.placeholder_image)
            .into(holder.itemView.findViewById(R.id.homeProductView))
        holder.itemView.setOnClickListener{
            context.startActivity(Intent(context, ProductDetailsActivity::class.java))
        }

    }

    override fun getItemCount(): Int {
        return productDetails.count()
    }
}