package com.example.financeiroapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeiroapp.data.model.*
import com.example.financeiroapp.ui.theme.*
import com.example.financeiroapp.ui.components.*
import com.example.financeiroapp.ui.viewmodel.DividaViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// --- HELPERS ---

@Composable
fun getDividaCategoryIcon(categoria: CategoriaDivida): ImageVector {
    return when (categoria) {
        CategoriaDivida.CartaoCredito -> Icons.Default.Star
        CategoriaDivida.Financiamento -> Icons.Default.Home
        CategoriaDivida.Emprestimo -> Icons.Default.Info
        CategoriaDivida.Aluguel -> Icons.Default.Home
        CategoriaDivida.Agua -> Icons.Default.List
        CategoriaDivida.Luz -> Icons.Default.Info
        CategoriaDivida.Internet -> Icons.Default.List
        CategoriaDivida.Telefone -> Icons.Default.List
        CategoriaDivida.PlanoSaude -> Icons.Default.Star
        CategoriaDivida.IPVA -> Icons.Default.Home
        CategoriaDivida.IPTU -> Icons.Default.Home
        CategoriaDivida.Assinatura -> Icons.Default.List
        CategoriaDivida.Outros -> Icons.Default.MoreVert
    }
}

@Composable
fun getUrgencyColor(vencimento: Long, status: StatusDivida): Color {
    if (status == StatusDivida.Pago) return Color(0xFF9E9E9E)
    val hoje = Calendar.getInstance().timeInMillis
    val diff = vencimento - hoje
    val seteDias = 7 * 24 * 60 * 60 * 1000L
    val trintaDias = 30 * 24 * 60 * 60 * 1000L

    return when {
        diff < 0 -> ExpenseNeon
        diff < seteDias -> ExpenseNeon
        diff < trintaDias -> DebtGolden
        else -> IncomeNeon
    }
}

// --- SCREEN ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DividasScreen(viewModel: DividaViewModel) {
    val todasDividas by viewModel.todasDividas.collectAsState()
    
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    
    var showSheet by remember { mutableStateOf(false) }
    var dividaParaEditar by remember { mutableStateOf<Divida?>(null) }
    var itemToDelete by remember { mutableStateOf<Divida?>(null) }
    var statusFiltro by remember { mutableStateOf<StatusDivida?>(null) }

    var isMensal by remember { mutableStateOf(true) }
    val calendar = remember { Calendar.getInstance() }
    var mes by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var ano by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    LaunchedEffect(mes, ano, isMensal) {
        viewModel.filtrar(String.format("%02d", mes), ano.toString())
    }

    val totalAPagar = todasDividas.filter { it.status == StatusDivida.NaoPago }.sumOf { it.valorParcela }
    val totalPago = todasDividas.filter { it.status == StatusDivida.Pago }.sumOf { it.valorParcela }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ControleR", color = GoldPrimary, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    dividaParaEditar = null
                    showSheet = true 
                },
                containerColor = GoldPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0A0A0A))
        ) {
            PeriodSelectorM3(
                isMensal = isMensal,
                mes = mes,
                ano = ano,
                onPeriodToggle = { isMensal = !isMensal },
                onPrev = { if (isMensal) { if (mes == 1) { mes = 12; ano-- } else mes-- } else ano-- },
                onNext = { if (isMensal) { if (mes == 12) { mes = 1; ano++ } else mes++ } else ano++ }
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard(Modifier.weight(1f), "A Pagar", currencyFormatter.format(totalAPagar), ExpenseNeon)
                SummaryMiniCard(Modifier.weight(1f), "Quitado", currencyFormatter.format(totalPago), IncomeNeon)
            }

            StatusFilterTabsM3(
                selectedStatus = statusFiltro,
                onStatusSelected = { 
                    statusFiltro = it
                    viewModel.setFiltroStatus(it)
                }
            )

            if (todasDividas.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateList("Nenhum compromisso.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(todasDividas, key = { it.id }) { divida ->
                        DividaItemM3(
                            divida = divida,
                            dateFormatter = dateFormatter,
                            currencyFormatter = currencyFormatter,
                            onClick = {
                                dividaParaEditar = divida
                                showSheet = true
                            },
                            onStatusToggle = {
                                val novoStatus = if (divida.status == StatusDivida.Pago) StatusDivida.NaoPago else StatusDivida.Pago
                                viewModel.atualizarStatus(divida, novoStatus)
                            },
                            onLongClick = { itemToDelete = divida }
                        )
                    }
                }
            }
        }

        if (showSheet) {
            AddDividaSheet(
                dividaParaEditar = dividaParaEditar,
                onDismiss = { showSheet = false },
                onSave = { nova, atualizarFuturas ->
                    if (dividaParaEditar == null) {
                        viewModel.adicionarDivida(nova)
                    } else {
                        viewModel.atualizarDivida(nova, atualizarFuturas)
                    }
                    showSheet = false
                }
            )
        }

        itemToDelete?.let { divida ->
            var deletarTudo by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Remover?", color = GoldPrimary) },
                text = { 
                    Column {
                        Text("Deseja excluir este compromisso?")
                        if (divida.parcelasTotal > 1) {
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = deletarTudo, 
                                    onCheckedChange = { deletarTudo = it },
                                    colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                                )
                                Text("Excluir todo o grupo?", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletarDivida(divida, deletarTudo)
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseNeon)
                    ) { Text("Excluir", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) { Text("Cancelar", color = Color.Gray) }
                }
            )
        }
    }
}

@Composable
fun StatusFilterTabsM3(selectedStatus: StatusDivida?, onStatusSelected: (StatusDivida?) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedStatus == null,
            onClick = { onStatusSelected(null) },
            label = { Text("Todos") },
            shape = CircleShape,
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = Color.Black)
        )
        FilterChip(
            selected = selectedStatus == StatusDivida.NaoPago,
            onClick = { onStatusSelected(StatusDivida.NaoPago) },
            label = { Text("Pendentes") },
            shape = CircleShape,
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = Color.Black)
        )
        FilterChip(
            selected = selectedStatus == StatusDivida.Pago,
            onClick = { onStatusSelected(StatusDivida.Pago) },
            label = { Text("Pagos") },
            shape = CircleShape,
            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = Color.Black)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DividaItemM3(
    divida: Divida, 
    dateFormatter: SimpleDateFormat, 
    currencyFormatter: NumberFormat,
    onClick: () -> Unit,
    onStatusToggle: () -> Unit,
    onLongClick: () -> Unit
) {
    val urgenciaColor = getUrgencyColor(divida.dataVencimento, divida.status)
    val isQuitada = divida.status == StatusDivida.Pago && divida.parcelaAtual == divida.parcelasTotal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(
            containerColor = if (divida.status == StatusDivida.Pago) Color(0xFF121212) else Color(0xFF1A1A1A)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = urgenciaColor.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(getDividaCategoryIcon(divida.categoria), null, tint = urgenciaColor)
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(divida.descricao, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    if (isQuitada) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Check, null, tint = IncomeNeon, modifier = Modifier.size(14.dp))
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Parc. ${divida.parcelaAtual}/${divida.parcelasTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E9E9E)
                    )
                    if (divida.status == StatusDivida.NaoPago) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Vence ${dateFormatter.format(Date(divida.dataVencimento))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = urgenciaColor,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Spacer(Modifier.width(8.dp))
                        Text("PAGO", style = MaterialTheme.typography.labelSmall, color = IncomeNeon, fontWeight = FontWeight.Black)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    currencyFormatter.format(divida.valorParcela),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = if (divida.status == StatusDivida.Pago) Color(0xFF9E9E9E) else ExpenseNeon
                )
                
                IconButton(onClick = onStatusToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (divida.status == StatusDivida.Pago) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = "Pagar",
                        tint = if (divida.status == StatusDivida.Pago) IncomeNeon else Color.Gray.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
