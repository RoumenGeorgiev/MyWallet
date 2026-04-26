package bg.nightscout.mywallet.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import bg.nightscout.mywallet.data.Account
import bg.nightscout.mywallet.data.Category
import bg.nightscout.mywallet.data.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.allCategories.collectAsState()
    val accounts by viewModel.allAccounts.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        val json = viewModel.exportToJson()
                        outputStream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Export successful", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                try {
                    context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                        val json = reader.readText()
                        viewModel.importFromJson(json)
                    }
                    Toast.makeText(context, "Import successful", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Data Management", style = MaterialTheme.typography.titleMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        exportLauncher.launch("my_wallet_backup.json")
                    }, modifier = Modifier.weight(1f)) {
                        Text("Export JSON")
                    }
                    Button(onClick = { 
                        importLauncher.launch("application/json")
                    }, modifier = Modifier.weight(1f)) {
                        Text("Import JSON")
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Categories", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                    }
                }
            }

            items(categories) { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    supportingContent = { Text(category.type.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteCategory(category) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Accounts", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showAddAccountDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Account")
                    }
                }
            }

            items(accounts) { account ->
                ListItem(
                    headlineContent = { Text(account.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.deleteAccount(account) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }
        }

        if (showAddCategoryDialog) {
            var name by remember { mutableStateOf("") }
            var type by remember { mutableStateOf(TransactionType.EXPENSE) }
            AlertDialog(
                onDismissRequest = { showAddCategoryDialog = false },
                title = { Text("Add Category") },
                text = {
                    Column {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                        Row {
                            RadioButton(selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE })
                            Text("Expense", modifier = Modifier.align(Alignment.CenterVertically))
                            RadioButton(selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME })
                            Text("Income", modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (name.isNotEmpty()) viewModel.addCategory(name, type)
                        showAddCategoryDialog = false
                    }) { Text("Add") }
                }
            )
        }

        if (showAddAccountDialog) {
            var name by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showAddAccountDialog = false },
                title = { Text("Add Account") },
                text = {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (name.isNotEmpty()) viewModel.addAccount(name)
                        showAddAccountDialog = false
                    }) { Text("Add") }
                }
            )
        }
    }
}
