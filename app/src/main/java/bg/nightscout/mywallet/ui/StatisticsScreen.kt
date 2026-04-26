package bg.nightscout.mywallet.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import bg.nightscout.mywallet.data.Transaction
import bg.nightscout.mywallet.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState()
    var selectedTimeframe by remember { mutableStateOf("Monthly") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FilterChip(
                    selected = selectedTimeframe == "Monthly",
                    onClick = { selectedTimeframe = "Monthly" },
                    label = { Text("Monthly") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = selectedTimeframe == "Yearly",
                    onClick = { selectedTimeframe = "Yearly" },
                    label = { Text("Yearly") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val now = Calendar.getInstance()
            val filteredTransactions = transactions.filter {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                if (selectedTimeframe == "Monthly") {
                    cal.get(Calendar.MONTH) == now.get(Calendar.MONTH) && cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                } else {
                    cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                }
            }

            val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

            Text("Total Income: €${String.format("%.2f", totalIncome)}", color = Color.Green)
            Text("Total Expense: €${String.format("%.2f", totalExpense)}", color = Color.Red)
            Text("Balance: €${String.format("%.2f", totalIncome - totalExpense)}", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(32.dp))

            if (totalIncome > 0 || totalExpense > 0) {
                PieChart(income = totalIncome.toFloat(), expense = totalExpense.toFloat())
            } else {
                Text("No data for this period", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
fun PieChart(income: Float, expense: Float) {
    val total = income + expense
    val incomeAngle = (income / total) * 360f
    val expenseAngle = (expense / total) * 360f

    Canvas(modifier = Modifier.size(200.dp).padding(16.dp)) {
        drawArc(
            color = Color.Green,
            startAngle = 0f,
            sweepAngle = incomeAngle,
            useCenter = true,
            size = Size(size.width, size.height)
        )
        drawArc(
            color = Color.Red,
            startAngle = incomeAngle,
            sweepAngle = expenseAngle,
            useCenter = true,
            size = Size(size.width, size.height)
        )
    }
}
