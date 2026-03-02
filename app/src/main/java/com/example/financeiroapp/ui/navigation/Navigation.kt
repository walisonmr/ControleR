package com.example.financeiroapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.financeiroapp.data.database.FinanceiroDatabase
import com.example.financeiroapp.ui.screens.*
import com.example.financeiroapp.ui.viewmodel.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    db: FinanceiroDatabase
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("financeiro_prefs", android.content.Context.MODE_PRIVATE) }
    
    // 1. Cria Factory e ViewModel de Perfil
    val perfilFactory = FinanceiroViewModelFactory(db, sharedPreferences = prefs)
    val perfilViewModel: PerfilViewModel = viewModel(factory = perfilFactory)
    
    // 2. Cria Factory para os outros ViewModels, dependendo do perfil ativo
    val appFactory = FinanceiroViewModelFactory(db, activeProfileId = perfilViewModel.activeProfileId)
    
    val dashboardViewModel: DashboardViewModel = viewModel(factory = appFactory)
    val lancamentoViewModel: LancamentoViewModel = viewModel(factory = appFactory)
    val dividaViewModel: DividaViewModel = viewModel(factory = appFactory)
    val configuracaoViewModel: ConfiguracaoViewModel = viewModel(factory = appFactory)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute != Screen.Splash.route) {
                FinanceiroBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onTimeout = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    dashboardViewModel, 
                    lancamentoViewModel,
                    perfilViewModel,
                    onSettingsClick = { navController.navigate(Screen.Configuracoes.route) },
                    onProfileClick = { navController.navigate(Screen.Perfil.route) }
                ) 
            }
            composable(Screen.Lancamentos.route) { 
                LancamentosScreen(lancamentoViewModel) 
            }
            composable(Screen.Dividas.route) { 
                DividasScreen(dividaViewModel) 
            }
            composable(Screen.Configuracoes.route) {
                ConfiguracoesScreen(
                    viewModel = configuracaoViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Perfil.route) {
                PerfilScreen(
                    viewModel = perfilViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun FinanceiroBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
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
