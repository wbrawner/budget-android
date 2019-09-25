package com.wbrawner.budget.ui.transactions

import androidx.lifecycle.ViewModel
import com.wbrawner.budget.common.transaction.TransactionRepository
import com.wbrawner.budget.di.ViewModelKey
import com.wbrawner.budget.ui.base.LoadingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject

class TransactionListViewModel @Inject constructor(private val transactionRepo: TransactionRepository) :
        LoadingViewModel() {
    suspend fun getTransactions(budgetId: Long? = null, categoryId: Long? = null) = showLoader {
        transactionRepo.findAll(budgetId = budgetId, categoryId = categoryId)
    }
}

@Module
abstract class TransactionListViewModelMapper {
    @Binds
    @IntoMap
    @ViewModelKey(TransactionListViewModel::class)
    abstract fun bindTransactionListViewModel(transactionListViewModel:
                                              TransactionListViewModel): ViewModel
}