package com.example.firebaseecom.payments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.CheckoutlistviewBinding
import com.example.firebaseecom.model.ProductHomeModel
import com.example.firebaseecom.model.asMap

class ProductCheckoutAdapter(
    private val activityFunctionClass: ProductCheckoutActivity.ActivityFunctionClass,
    val langId: String
) : RecyclerView.Adapter<ProductCheckoutAdapter.MyViewHolder>() {

    interface ActivityFunctionInterface {
        fun addTotalPrice(productList: List<ProductHomeModel?>)
    }


    var productList = mutableListOf<ProductHomeModel?>()
    private lateinit var checkOutListViewBinding: CheckoutlistviewBinding
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        checkOutListViewBinding =
            DataBindingUtil.inflate(inflater, R.layout.checkoutlistview, parent, false)
        return MyViewHolder(checkOutListViewBinding, langId)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val product = productList[position]
        holder.bind(product!!)

    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun setProducts(productList: List<ProductHomeModel?>) {
        this.productList = productList.toMutableList()
        activityFunctionClass.addTotalPrice(productList)

    }

    class MyViewHolder(
        private val checkOutListViewBinding: CheckoutlistviewBinding,
        val langId: String
    ) : RecyclerView.ViewHolder(checkOutListViewBinding.root) {
        fun bind(product: ProductHomeModel) {
            checkOutListViewBinding.apply {
                productOrders = product
                titleText.text = product.productTitle.asMap()[langId].toString()
            }
        }

    }
}


