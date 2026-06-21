package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AddTransactionScreen
import com.example.ui.screens.BillsScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SavingsScreen
import com.example.ui.screens.SettingsScreen

object Routes {
    const val LOCK = "lock"
    const val MAIN = "main"
    const val ADD_TRANSACTION = "add_transaction"
    const val SETTINGS = "settings"
}

enum class NavigationTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    BUDGET("Budget", Icons.Default.Wallet),
    SAVINGS("Savings", Icons.Default.Savings),
    BILLS("Bills", Icons.Default.ReceiptLong),
    REPORTS("Reports", Icons.Default.Assessment)
}

@Composable
fun AppNavigation(
    viewModel: FinancialViewModel,
    navController: NavHostController = rememberNavController()
) {
    val isUnlocked by viewModel.isUnlocked.collectAsState()

    // Dynamically lock the initial starting route based on security state
    val startRoute = if (isUnlocked) Routes.MAIN else Routes.LOCK

    NavHost(
        navController = navController,
        startDestination = startRoute,
        modifier = Modifier.fillMaxSize().testTag("app_navigation_host")
    ) {
        // 1. PIN / Biometric Security Screen
        composable(Routes.LOCK) {
            LockScreen(
                viewModel = viewModel,
                onSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOCK) { inclusive = true }
                    }
                }
            )
        }

        // 2. Main Tabbed Layout Container Block
        composable(Routes.MAIN) {
            MainContainerScreen(
                viewModel = viewModel,
                onNavigateToAddTransaction = {
                    navController.navigate(Routes.ADD_TRANSACTION)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        // 3. New Transaction Form Input Screen
        composable(Routes.ADD_TRANSACTION) {
            AddTransactionScreen(
                viewModel = viewModel,
                onBack = {
                    navController.navigateUp()
                }
            )
        }

        // 4. Settings View Controller Core
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}

@Composable
fun MainContainerScreen(
    viewModel: FinancialViewModel,
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
    val currentLang by viewModel.currentLanguage.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        bottomBar = {
            if (!isTablet) {
                NavigationBar(
                    modifier = Modifier.testTag("app_bottom_bar")
                ) {
                    NavigationTab.entries.forEach { tab ->
                        val titleText = when(tab) {
                            NavigationTab.HOME -> if (currentLang == AppLanguage.INDONESIAN) "Beranda" else "Home"
                            NavigationTab.BUDGET -> getTranslation("budget", currentLang)
                            NavigationTab.SAVINGS -> if (currentLang == AppLanguage.INDONESIAN) "Target" else "Savings"
                            NavigationTab.BILLS -> if (currentLang == AppLanguage.INDONESIAN) "Tagihan" else "Bills"
                            NavigationTab.REPORTS -> if (currentLang == AppLanguage.INDONESIAN) "Laporan" else "Reports"
                        }
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = titleText
                                )
                            },
                            label = {
                                Text(text = titleText, fontSize = 11.sp, maxLines = 1)
                            },
                            modifier = Modifier.testTag("tab_${tab.title.lowercase()}"),
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isTablet) {
                NavigationRail(
                    modifier = Modifier.testTag("app_navigation_rail")
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    NavigationTab.entries.forEach { tab ->
                        val titleText = when(tab) {
                            NavigationTab.HOME -> if (currentLang == AppLanguage.INDONESIAN) "Beranda" else "Home"
                            NavigationTab.BUDGET -> getTranslation("budget", currentLang)
                            NavigationTab.SAVINGS -> if (currentLang == AppLanguage.INDONESIAN) "Target" else "Savings"
                            NavigationTab.BILLS -> if (currentLang == AppLanguage.INDONESIAN) "Tagihan" else "Bills"
                            NavigationTab.REPORTS -> if (currentLang == AppLanguage.INDONESIAN) "Laporan" else "Reports"
                        }
                        NavigationRailItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = titleText
                                )
                            },
                            label = {
                                Text(text = titleText, fontSize = 11.sp, maxLines = 1)
                            },
                            modifier = Modifier.testTag("tab_${tab.title.lowercase()}"),
                            alwaysShowLabel = true
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (selectedTab) {
                    NavigationTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToAddTransaction = onNavigateToAddTransaction,
                        onNavigateToSettings = onNavigateToSettings
                    )
                    NavigationTab.BUDGET -> BudgetScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.SAVINGS -> SavingsScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.BILLS -> BillsScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.REPORTS -> ReportsScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
