package com.example.financeiroapp.ui.screens

import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.financeiroapp.data.model.Divida
import com.example.financeiroapp.data.model.TipoLancamento
import com.example.financeiroapp.ui.theme.GoldPrimary
import com.example.financeiroapp.ui.theme.IncomeNeon
import com.example.financeiroapp.ui.viewmodel.DashboardState
import com.example.financeiroapp.ui.viewmodel.DashboardViewModel
import com.example.financeiroapp.ui.viewmodel.LancamentoViewModel
import com.example.financeiroapp.ui.viewmodel.PerfilViewModel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    lancamentoViewModel: LancamentoViewModel,
    perfilViewModel: PerfilViewModel,
    onSettingsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val lancamentos by lancamentoViewModel.lancamentos.collectAsState()
    val activeProfile by perfilViewModel.activeProfile.collectAsState()
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "ControleR", 
                        style = MaterialTheme.typography.headlineMedium, 
                        fontWeight = FontWeight.Black,
                        color = GoldPrimary,
                        letterSpacing = 2.sp
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0A0A0A)
                ),
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Surface(
                            shape = CircleShape,
                            color = try { 
                                Color(AndroidColor.parseColor(activeProfile?.corPerfil ?: "#D4AF37")) 
                            } catch (e: Exception) { MaterialTheme.colorScheme.primary },
                            modifier = Modifier.size(36.dp),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = activeProfile?.fotoPerfil ?: "P",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = GoldPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (lancamentos.isEmpty() && uiState.totalDividasPendente == 0.0) {
            EmptyStateDisplay(paddingValues)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    BalanceCard(
                        value = currencyFormatter.format(uiState.saldoAtual),
                        isPositive = uiState.saldoAtual >= 0
                    )
                }

                item {
                    SummaryCard(
                        title = "Compromissos do Mês",
                        value = currencyFormatter.format(uiState.totalDividasPendente),
                        icon = Icons.Default.Warning,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ProgressCard(
                            modifier = Modifier.weight(1f),
                            title = "Reserva",
                            value = currencyFormatter.format(uiState.reservaValor),
                            progress = (uiState.reservaPorcentagem / 100).toFloat(),
                            color = GoldPrimary
                        )
                        ProgressCard(
                            modifier = Modifier.weight(1f),
                            title = "Investimento",
                            value = currencyFormatter.format(uiState.investimentoValor),
                            progress = (uiState.investimentoPorcentagem / 100).toFloat(),
                            color = GoldPrimary
                        )
                    }
                }

                item {
                    SectionTitle("Análise de Gastos")
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val gastosPorTipo = lancamentos.filter { !it.isEntrada }
                                .groupBy { it.tipo }
                                .mapValues { it.value.sumOf { l -> l.valor } }

                            if (gastosPorTipo.isNotEmpty()) {
                                DistributionPieChart(gastosPorTipo)
                            } else {
                                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                    Text("Sem movimentações", color = Color(0xFF9E9E9E))
                                }
                            }
                        }
                    }
                }

                if (uiState.dividasProximas.isNotEmpty()) {
                    item { SectionTitle("Próximos Vencimentos") }
                    items(uiState.dividasProximas) { divida ->
                        DashboardDividaItem(divida, dateFormatter, currencyFormatter)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateDisplay(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Star, null, modifier = Modifier.size(80.dp), tint = GoldPrimary.copy(alpha = 0.3f))
        Spacer(Modifier.height(24.dp))
        Text("Bem-vindo ao ControleR", color = GoldPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Comece a gerir sua vida financeira agora mesmo adicionando seu primeiro registro.",
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = Color(0xFF9E9E9E)
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = GoldPrimary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun BalanceCard(value: String, isPositive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Saldo Consolidado", style = MaterialTheme.typography.labelLarge, color = Color(0xFF9E9E9E))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = if (isPositive) GoldPrimary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, iconColor: Color, containerColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ProgressCard(modifier: Modifier, title: String, value: String, progress: Float, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000)
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Color(0xFF9E9E9E))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = GoldPrimary,
                trackColor = GoldPrimary.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.End), color = GoldPrimary)
        }
    }
}

@Composable
fun DashboardDividaItem(divida: Divida, dateFormatter: SimpleDateFormat, currencyFormatter: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(divida.descricao, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("Vence ${dateFormatter.format(Date(divida.dataVencimento))}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
            }
            Text(currencyFormatter.format(divida.valorParcela), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
fun DistributionPieChart(data: Map<TipoLancamento, Double>) {
    val colors = listOf(
        GoldPrimary.toArgb(),
        Color(0xFFF0C040).toArgb(),
        Color(0xFFB8860B).toArgb(),
        Color(0xFF8B4513).toArgb()
    )

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(AndroidColor.TRANSPARENT)
                setEntryLabelColor(AndroidColor.WHITE)
                setEntryLabelTextSize(10f)
                legend.isEnabled = true
                legend.textColor = AndroidColor.WHITE
                animateY(1000)
            }
        },
        modifier = Modifier.fillMaxWidth().height(200.dp),
        update = { chart ->
            val entries = data.map { PieEntry(it.value.toFloat(), it.key.name) }
            val dataSet = PieDataSet(entries, "").apply {
                setColors(colors)
                valueTextSize = 12f
                valueTextColor = AndroidColor.WHITE
                valueTypeface = Typeface.DEFAULT_BOLD
                valueFormatter = PercentFormatter(chart)
            }
            chart.data = PieData(dataSet)
            chart.setUsePercentValues(true)
            chart.invalidate()
        }
    )
}
