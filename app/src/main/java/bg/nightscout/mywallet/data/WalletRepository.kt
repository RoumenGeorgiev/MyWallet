package bg.nightscout.mywallet.data

import kotlinx.coroutines.flow.Flow

class WalletRepository(private val walletDao: WalletDao) {
    val allTransactions: Flow<List<Transaction>> = walletDao.getAllTransactions()
    val allCategories: Flow<List<Category>> = walletDao.getAllCategories()
    val allAccounts: Flow<List<Account>> = walletDao.getAllAccounts()

    suspend fun insertTransaction(transaction: Transaction) = walletDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: Transaction) = walletDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = walletDao.deleteTransaction(transaction)

    suspend fun insertCategory(category: Category) = walletDao.insertCategory(category)
    suspend fun updateCategory(category: Category) = walletDao.updateCategory(category)
    suspend fun deleteCategory(category: Category) = walletDao.deleteCategory(category)

    suspend fun insertAccount(account: Account) = walletDao.insertAccount(account)
    suspend fun updateAccount(account: Account) = walletDao.updateAccount(account)
    suspend fun deleteAccount(account: Account) = walletDao.deleteAccount(account)
    
    suspend fun clearAllData() {
        walletDao.deleteAllTransactions()
        walletDao.deleteAllCategories()
        walletDao.deleteAllAccounts()
    }
}
