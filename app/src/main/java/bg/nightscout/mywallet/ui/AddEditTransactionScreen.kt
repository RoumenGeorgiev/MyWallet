package bg.nightscout.mywallet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bg.nightscout.mywallet.data.Transaction
import bg.nightscout.mywallet.data.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    viewModel: WalletViewModel,
    transactionId: Long? = null,
    initialType: TransactionType? = null,
    onNavigateBack: () -> Unit
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()

    val editingTransaction = transactions.find { it.id == transactionId }

    var amount by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(editingTransaction?.note ?: "") }
    var type by remember { mutableStateOf(editingTransaction?.type ?: initialType ?: TransactionType.EXPENSE) }
    var categoryId by remember { mutableStateOf(editingTransaction?.categoryId ?: (categories.firstOrNull { it.type == type }?.id ?: 0L)) }
    var accountId by remember { mutableStateOf(editingTransaction?.accountId ?: (accounts.firstOrNull()?.id ?: 0L)) }
    var date by remember { mutableStateOf(editingTransaction?.date ?: System.currentTimeMillis()) }

    var showDatePicker by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        if (transactionId == null) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (transactionId == null) "Add Transaction" else "Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                FilterChip(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { 
                        type = TransactionType.EXPENSE 
                        categoryId = categories.firstOrNull { it.type == TransactionType.EXPENSE }?.id ?: 0L
                    },
                    label = { Text("Expense") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = type == TransactionType.INCOME,
                    onClick = { 
                        type = TransactionType.INCOME 
                        categoryId = categories.firstOrNull { it.type == TransactionType.INCOME }?.id ?: 0L
                    },
                    label = { Text("Income") }
                )
            }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (€)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            // Category Dropdown
            var categoryExpanded by remember { mutableStateOf(false) }
            val filteredCategories = categories.filter { it.type == type }
            
            // Ensure categoryId is valid for the current type
            if (categoryId != 0L && categories.find { it.id == categoryId }?.type != type) {
                categoryId = filteredCategories.firstOrNull()?.id ?: 0L
            }

            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = filteredCategories.find { it.id == categoryId }?.name ?: "Select Category",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    filteredCategories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                categoryId = category.id
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Account Dropdown
            var accountExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = accountExpanded,
                onExpandedChange = { accountExpanded = !accountExpanded }
            ) {
                OutlinedTextField(
                    value = accounts.find { it.id == accountId }?.name ?: "Select Account",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = accountExpanded,
                    onDismissRequest = { accountExpanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
                            onClick = {
                                accountId = account.id
                                accountExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(date)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (transactionId == null) {
                        viewModel.addTransaction(amt, date, categoryId, accountId, type, note)
                    } else {
                        viewModel.updateTransaction(Transaction(id = transactionId, amount = amt, date = date, categoryId = categoryId, accountId = accountId, type = type, note = note))
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotEmpty() && categoryId != 0L && accountId != 0L
            ) {
                Text(if (transactionId == null) "Save" else "Update")
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date)
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        date = datePickerState.selectedDateMillis ?: date
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
