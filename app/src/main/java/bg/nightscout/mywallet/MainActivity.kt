package bg.nightscout.mywallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import bg.nightscout.mywallet.data.TransactionType
import bg.nightscout.mywallet.ui.*
import bg.nightscout.mywallet.ui.theme.MyWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyWalletTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: WalletViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentDestination == "transactions" || currentDestination == "statistics" || currentDestination == "settings") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Transactions") },
                        label = { Text("Transactions") },
                        selected = currentDestination == "transactions",
                        onClick = { 
                            navController.navigate("transactions") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            } 
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Star, contentDescription = "Statistics") },
                        label = { Text("Stats") },
                        selected = currentDestination == "statistics",
                        onClick = { 
                            navController.navigate("statistics") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            } 
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentDestination == "settings",
                        onClick = { 
                            navController.navigate("settings") {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            } 
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "transactions",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("transactions") {
                TransactionListScreen(
                    viewModel = viewModel,
                    onAddTransaction = { type: TransactionType? ->
                        val route = if (type != null) "add_transaction?type=${type.name}" else "add_transaction"
                        navController.navigate(route)
                    },
                    onEditTransaction = { transaction ->
                        navController.navigate("edit_transaction/${transaction.id}")
                    }
                )
            }
            composable(
                route = "add_transaction?type={type}",
                arguments = listOf(navArgument("type") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val typeString = backStackEntry.arguments?.getString("type")
                val type = typeString?.let { TransactionType.valueOf(it) }
                AddEditTransactionScreen(
                    viewModel = viewModel,
                    initialType = type,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_transaction/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getLong("transactionId")
                AddEditTransactionScreen(
                    viewModel = viewModel,
                    transactionId = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("statistics") {
                StatisticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
