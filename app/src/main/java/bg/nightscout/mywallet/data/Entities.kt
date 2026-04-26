package bg.nightscout.mywallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(tableName = "categories")
@Serializable
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: TransactionType
)

@Entity(tableName = "accounts")
@Serializable
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)

@Entity(tableName = "transactions")
@Serializable
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val date: Long, // Timestamp
    val categoryId: Long,
    val accountId: Long,
    val type: TransactionType,
    val note: String = ""
)

@Serializable
data class ExportData(
    val categories: List<Category>,
    val accounts: List<Account>,
    val transactions: List<Transaction>
)
