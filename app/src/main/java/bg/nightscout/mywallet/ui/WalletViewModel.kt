package bg.nightscout.mywallet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bg.nightscout.mywallet.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WalletRepository
    val allTransactions: StateFlow<List<Transaction>>
    val allCategories: StateFlow<List<Category>>
    val allAccounts: StateFlow<List<Account>>

    init {
        val dao = WalletDatabase.getDatabase(application).walletDao()
        repository = WalletRepository(dao)
        allTransactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allCategories = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allAccounts = repository.allAccounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun addTransaction(amount: Double, date: Long, categoryId: Long, accountId: Long, type: TransactionType, note: String) {
        viewModelScope.launch {
            repository.insertTransaction(Transaction(amount = amount, date = date, categoryId = categoryId, accountId = accountId, type = type, note = note))
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, type = type))
        }
    }
    
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addAccount(name: String) {
        viewModelScope.launch {
            repository.insertAccount(Account(name = name))
        }
    }
    
    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    fun exportToJson(): String {
        val data = ExportData(
            categories = allCategories.value,
            accounts = allAccounts.value,
            transactions = allTransactions.value
        )
        return Json.encodeToString(data)
    }

    fun importFromJson(jsonString: String) {
        viewModelScope.launch {
            try {
                val data = Json.decodeFromString<ExportData>(jsonString)
                repository.clearAllData()
                data.accounts.forEach { repository.insertAccount(it) }
                data.categories.forEach { repository.insertCategory(it) }
                data.transactions.forEach { repository.insertTransaction(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
