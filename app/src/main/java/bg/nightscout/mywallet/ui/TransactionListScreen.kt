package bg.nightscout.mywallet.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import bg.nightscout.mywallet.data.Transaction
import bg.nightscout.mywallet.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: WalletViewModel,
    onAddTransaction: (TransactionType?) -> Unit,
    onEditTransaction: (Transaction) -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()

    var showQuickAdd by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val dragThreshold = 60f

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Wallet") }) },
        floatingActionButton = {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Quick Add Options
                if (showQuickAdd) {
                    // Income option (ABOVE)
                    val incomeSelected = dragOffset.y < -dragThreshold
                    val incomeScale by animateFloatAsState(if (incomeSelected) 1.2f else 1f)
                    
                    Surface(
                        shape = CircleShape,
                        color = if (incomeSelected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(y = (-80).dp)
                            .scale(incomeScale)
                    ) {
                        Text("Income", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }

                    // Expense option (LEFT)
                    val expenseSelected = dragOffset.x < -dragThreshold
                    val expenseScale by animateFloatAsState(if (expenseSelected) 1.2f else 1f)

                    Surface(
                        shape = CircleShape,
                        color = if (expenseSelected) Color(0xFFF44336) else MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-80).dp)
                            .scale(expenseScale)
                    ) {
                        Text("Expense", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = { },
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (!showQuickAdd) onAddTransaction(null)
                            }
                        )
                    }.pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                showQuickAdd = true
                                dragOffset = Offset.Zero
                            },
                            onDragEnd = {
                                if (showQuickAdd) {
                                    if (dragOffset.y < -dragThreshold) {
                                        onAddTransaction(TransactionType.INCOME)
                                    } else if (dragOffset.x < -dragThreshold) {
                                        onAddTransaction(TransactionType.EXPENSE)
                                    }
                                }
                                showQuickAdd = false
                                dragOffset = Offset.Zero
                            },
                            onDragCancel = {
                                showQuickAdd = false
                                dragOffset = Offset.Zero
                            },
                            onDrag = { change, dragAmount ->
                                dragOffset += dragAmount
                                change.consume()
                            }
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }
    ) { padding ->
        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No transactions yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(transactions) { transaction ->
                    val category = categories.find { it.id == transaction.categoryId }
                    val account = accounts.find { it.id == transaction.accountId }
                    TransactionItem(
                        transaction = transaction,
                        categoryName = category?.name ?: "Unknown",
                        accountName = account?.name ?: "Unknown",
                        onEdit = { onEditTransaction(transaction) },
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    categoryName: String,
    accountName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = categoryName, style = MaterialTheme.typography.titleMedium)
                Text(text = accountName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall
                )
                if (transaction.note.isNotEmpty()) {
                    Text(text = transaction.note, style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "%s €%.2f", if (transaction.type == TransactionType.INCOME) "+" else "-", transaction.amount),
                    color = if (transaction.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color(0xFFF44336),
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
