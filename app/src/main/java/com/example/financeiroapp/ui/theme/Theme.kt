package com.example.financeiroapp.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ControleRColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = BackgroundBlack,
    primaryContainer = ElevatedBlack,
    onPrimaryContainer = GoldPrimary,
    secondary = IncomeNeon,
    onSecondary = BackgroundBlack,
    tertiary = DebtGolden,
    onTertiary = BackgroundBlack,
    background = BackgroundBlack,
    surface = CardBlack,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = ElevatedBlack,
    onSurfaceVariant = TextSecondary,
    error = ExpenseNeon,
    onError = TextPrimary
)

@Composable
fun FinanceiroAppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ControleRColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
