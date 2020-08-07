package com.wbrawner.budget.ui.categories

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.wbrawner.budget.AllowanceApplication
import com.wbrawner.budget.R
import com.wbrawner.budget.common.category.Category
import com.wbrawner.budget.ui.EXTRA_BUDGET_ID
import com.wbrawner.budget.ui.EXTRA_CATEGORY_ID
import com.wbrawner.budget.ui.base.BindableAdapter
import com.wbrawner.budget.ui.base.BindableData
import com.wbrawner.budget.ui.base.ListWithAddButtonFragment
import kotlinx.coroutines.launch

class CategoryListFragment : ListWithAddButtonFragment<Category, CategoryViewModel>() {
    override val noItemsStringRes: Int = R.string.categories_no_data
    override val viewModelClass: Class<CategoryViewModel> = CategoryViewModel::class.java

    override fun onCreate(savedInstanceState: Bundle?) {
        (requireActivity().application as AllowanceApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override suspend fun loadItems(): Pair<List<CategoryData>, Map<Int, (view: View) -> CategoryViewHolder>> {
        val budgetId = arguments?.getLong(EXTRA_BUDGET_ID)
        if (budgetId == null) {
            findNavController().navigateUp()
            return Pair(emptyList(), emptyMap())
        }
        val budget = viewModel.getBudget(budgetId)
        activity?.title = budget.name
        return Pair(
                viewModel.getCategories(budgetId).map { CategoryData(it) },
                mapOf(CATEGORY_VIEW to { v ->
                    CategoryViewHolder(v, viewModel, findNavController())
                })
        )
    }

    override fun addItem() {
        startActivity(Intent(activity, CategoryFormActivity::class.java))
    }

    companion object {
        const val TAG_FRAGMENT = "categories"
    }
}

const val CATEGORY_VIEW = R.layout.list_item_category

class CategoryData(val category: Category) : BindableData {
    override val viewType: Int = CATEGORY_VIEW
}

class CategoryViewHolder(
        itemView: View,
        private val viewModel: CategoryViewModel,
        private val navController: NavController
) : BindableAdapter.CoroutineViewHolder<CategoryData>(itemView) {
    private val name: TextView = itemView.findViewById(R.id.category_title)
    private val amount: TextView = itemView.findViewById(R.id.category_amount)
    private val progressBar: ProgressBar = itemView.findViewById(R.id.category_progress)

    @SuppressLint("NewApi")
    override fun onBind(item: CategoryData) {
        with(item) {
            name.text = category.title
            // TODO: Format according to budget's currency
            amount.text = String.format("${'$'}%.02f", category.amount / 100.0f)
            val tintColor = if (category.expense) R.color.colorTextRed else R.color.colorTextGreen
            val colorStateList = with(itemView.context) {
                android.content.res.ColorStateList.valueOf(getColor(tintColor))
            }
            progressBar.progressTintList = colorStateList
            progressBar.indeterminateTintList = colorStateList
            progressBar.max = category.amount.toInt()
            launch {
                val balance = viewModel.getBalance(category)
                progressBar.isIndeterminate = false
                progressBar.setProgress(
                        balance,
                        true
                )
                amount.text = itemView.context.getString(
                        R.string.balance_remaning,
                        (category.amount - balance) / 100.0f
                )
            }
            itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putLong(EXTRA_CATEGORY_ID, category.id ?: -1)
                }
                navController.navigate(R.id.categoryFragment, bundle)
            }
        }
    }
}