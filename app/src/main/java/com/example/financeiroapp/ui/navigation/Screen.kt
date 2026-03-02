package com.example.financeiroapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen("splash", "Splash", Icons.Default.Home)
    object Dashboard : Screen("dashboard", "Início", Icons.Default.Home)
    object Lancamentos : Screen("lancamentos", "Lançamentos", Icons.Default.List)
    object Dividas : Screen("dividas", "Dívidas", Icons.Default.Warning)
    object Configuracoes : Screen("configuracoes", "Configurações", Icons.Default.Build)
    object Perfil : Screen("perfil", "Perfil", Icons.Default.AccountCircle)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Lancamentos,
    Screen.Dividas
)
