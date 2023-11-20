import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.firebaseecom.CartOrder.ProductListActivity
import com.example.firebaseecom.R
import com.example.firebaseecom.databinding.CartViewBinding
import com.example.firebaseecom.model.ProductHomeModel
import javax.inject.Inject

class ProductCartAdapter @Inject constructor(
    val activityFunctionClass: ProductListActivity.ActivityFunctionClass
) : RecyclerView.Adapter<ProductCartAdapter.MyViewHolder>() {
    interface ActivityFunctionInterface {

        fun navigateToDetails(productHomeModel: ProductHomeModel)
        fun addTotalPrice(productList: List<ProductHomeModel>)
        fun deleteFromCart(productHomeModel: ProductHomeModel, position: Int)

    }

    var productList: MutableList<ProductHomeModel> = mutableListOf()
    private lateinit var cartViewBinding: CartViewBinding
    override fun onCreateViewHolder(

        parent: ViewGroup,
        viewType: Int
    ): ProductCartAdapter.MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        cartViewBinding = DataBindingUtil.inflate(layoutInflater, R.layout.cart_view, parent, false)
        return MyViewHolder(cartViewBinding)

    }

    override fun onBindViewHolder(holder: ProductCartAdapter.MyViewHolder, position: Int) {
        val productHome = productList[position]
        holder.bind(productHome,position)
        Glide.with(holder.itemView)
            .load(productHome.productImage)
            .error(R.drawable.placeholder_image)
            .into(holder.itemView.findViewById(R.id.productImage))
        holder.itemView.setOnClickListener {
            activityFunctionClass.navigateToDetails(productHome)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun setProduct(productList: List<ProductHomeModel>) {
        this.productList = productList.toMutableList()
        notifyDataSetChanged()
        activityFunctionClass.addTotalPrice(productList)
    }

    inner class MyViewHolder(private val cartViewBinding: CartViewBinding) :
        RecyclerView.ViewHolder(cartViewBinding.root) {
        fun bind(productHomeModel: ProductHomeModel,position:Int) {
            cartViewBinding.productHome = productHomeModel
            cartViewBinding.deleteBtn.setOnClickListener {
                activityFunctionClass.deleteFromCart(productHomeModel,position)
                productList.removeAt(adapterPosition)
                notifyItemRemoved(adapterPosition)
                activityFunctionClass.addTotalPrice(productList)

            }
        }

    }

}


