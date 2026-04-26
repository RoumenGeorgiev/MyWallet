package bg.nightscout.mywallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Category::class, Account::class, Transaction::class], version = 1, exportSchema = false)
abstract class WalletDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao

    companion object {
        @Volatile
        private var INSTANCE: WalletDatabase? = null

        fun getDatabase(context: Context): WalletDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WalletDatabase::class.java,
                    "wallet_database"
                )
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.walletDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: WalletDao) {
                // Default accounts
                dao.insertAccount(Account(name = "Cash"))
                dao.insertAccount(Account(name = "Bank Account"))
                dao.insertAccount(Account(name = "Credit Card"))

                // Default categories
                val incomeCategories = listOf("Salary", "Gift", "Investment")
                val expenseCategories = listOf("Food", "Transport", "Rent", "Entertainment", "Health")

                incomeCategories.forEach { dao.insertCategory(Category(name = it, type = TransactionType.INCOME)) }
                expenseCategories.forEach { dao.insertCategory(Category(name = it, type = TransactionType.EXPENSE)) }
            }
        }
    }
}
