package bg.nightscout.mywallet.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import bg.nightscout.mywallet.data.Transaction
import bg.nightscout.mywallet.data.TransactionType
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

private val ChartColors = listOf(
    Color(0xFF2196F3), Color(0xFFE91E63), Color(0xFFFFEB3B),
    Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0),
    Color(0xFF00BCD4), Color(0xFF8BC34A), Color(0xFFFF5722),
    Color(0xFF607D8B), Color(0xFF795548), Color(0xFFCDDC39)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    var selectedTimeframe by remember { mutableStateOf("Monthly") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
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
            }

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

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Summary", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Income:", color = Color(0xFF4CAF50))
                            Text("€${String.format("%.2f", totalIncome)}", color = Color(0xFF4CAF50))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Expense:", color = Color(0xFFF44336))
                            Text("€${String.format("%.2f", totalExpense)}", color = Color(0xFFF44336))
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance:", style = MaterialTheme.typography.titleLarge)
                            Text("€${String.format("%.2f", totalIncome - totalExpense)}", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TabRow(selectedTabIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1) {
                        Tab(
                            selected = selectedType == TransactionType.EXPENSE,
                            onClick = { selectedType = TransactionType.EXPENSE },
                            text = { Text("Expenses") }
                        )
                        Tab(
                            selected = selectedType == TransactionType.INCOME,
                            onClick = { selectedType = TransactionType.INCOME },
                            text = { Text("Income") }
                        )
                    }
                }
            }

            val typeTransactions = filteredTransactions.filter { it.type == selectedType }
            val totalForType = typeTransactions.sumOf { it.amount }

            if (totalForType > 0) {
                val categoryStats = typeTransactions.groupBy { it.categoryId }
                    .map { (catId, trans) ->
                        val category = categories.find { it.id == catId }
                        val amount = trans.sumOf { it.amount }
                        val percentage = (amount / totalForType) * 100
                        CategoryStat(category?.name ?: "Unknown", amount, percentage.toFloat())
                    }.sortedByDescending { it.amount }

                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CategoryPieChart(
                            stats = categoryStats,
                            onSliceClick = { stat ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "${stat.name}: €${String.format("%.2f", stat.amount)} (${String.format("%.1f", stat.percentage)}%)",
                                        duration = SnackbarDuration.Short
                                    )
                                    // Optionally scroll to the item in the list
                                    val index = categoryStats.indexOf(stat)
                                    if (index != -1) {
                                        listState.animateScrollToItem(index + 4) // +4 to account for header items
                                    }
                                }
                            }
                        )
                    }
                }

                itemsIndexed(categoryStats) { index, stat ->
                    val color = ChartColors[index % ChartColors.size]
                    ListItem(
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                        },
                        headlineContent = { Text(stat.name) },
                        supportingContent = { 
                            LinearProgressIndicator(
                                progress = { stat.percentage / 100f },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                color = color,
                                trackColor = color.copy(alpha = 0.2f)
                            )
                        },
                        trailingContent = {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("€${String.format("%.2f", stat.amount)}")
                                Text("${String.format("%.1f", stat.percentage)}%", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    )
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No ${selectedType.name.lowercase()} data for this period")
                    }
                }
            }
        }
    }
}

data class CategoryStat(val name: String, val amount: Double, val percentage: Float)

@Composable
fun CategoryPieChart(stats: List<CategoryStat>, onSliceClick: (CategoryStat) -> Unit) {
    Canvas(modifier = Modifier
        .size(200.dp)
        .padding(16.dp)
        .pointerInput(stats) {
            detectTapGestures { offset ->
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val dx = offset.x - centerX
                val dy = offset.y - centerY
                val distance = sqrt(dx * dx + dy * dy)
                
                // Only trigger if click is inside the pie circle
                if (distance <= size.width / 2f) {
                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    // Adjust angle to be [0, 360) and start from -90 degrees (top)
                    angle = (angle + 90f + 360f) % 360f
                    
                    var currentAngle = 0f
                    for (stat in stats) {
                        val sweepAngle = (stat.percentage / 100f) * 360f
                        if (angle >= currentAngle && angle <= (currentAngle + sweepAngle)) {
                            onSliceClick(stat)
                            break
                        }
                        currentAngle += sweepAngle
                    }
                }
            }
        }
    ) {
        var startAngle = -90f
        stats.forEachIndexed { index, stat ->
            val sweepAngle = (stat.percentage / 100f) * 360f
            drawArc(
                color = ChartColors[index % ChartColors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            startAngle += sweepAngle
        }
    }
}
