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
import com.example.financeiroapp.ui.viewmodel.LancamentoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// --- HELPERS ---

@Composable
fun getCategoryIcon(lancamento: Lancamento): ImageVector {
    if (lancamento.isEntrada) return Icons.Default.Star
    
    return when (lancamento.categoriaSaida) {
        CategoriaSaida.Alimentacao -> Icons.Default.List
        CategoriaSaida.Transporte -> Icons.Default.Home
        CategoriaSaida.Moradia -> Icons.Default.Home
        CategoriaSaida.Saude -> Icons.Default.Info
        CategoriaSaida.Educacao -> Icons.Default.Build
        CategoriaSaida.Lazer -> Icons.Default.Favorite
        CategoriaSaida.Vestuario -> Icons.Default.Check
        CategoriaSaida.Higiene -> Icons.Default.Star
        CategoriaSaida.Assinaturas -> Icons.Default.List
        CategoriaSaida.Combustivel -> Icons.Default.Home
        CategoriaSaida.Mercado -> Icons.Default.List
        CategoriaSaida.Farmacia -> Icons.Default.Info
        CategoriaSaida.Restaurante -> Icons.Default.Menu
        CategoriaSaida.Academia -> Icons.Default.Star
        CategoriaSaida.Pets -> Icons.Default.Favorite
        CategoriaSaida.Tecnologia -> Icons.Default.Build
        CategoriaSaida.Presentes -> Icons.Default.Favorite
        CategoriaSaida.Impostos -> Icons.Default.List
        else -> Icons.Default.List
    }
}

@Composable
fun getTipoTagColors(tipo: TipoLancamento): Pair<Color, Color> {
    return when (tipo) {
        TipoLancamento.Essencial -> IncomeNeon to Color.Black
        TipoLancamento.Necessario -> GoldPrimary to Color.Black
        TipoLancamento.Desnecessario -> DebtGolden to Color.Black
        TipoLancamento.Vontade -> Color(0xFF8E44AD) to Color.White
    }
}

// --- SCREEN ---

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LancamentosScreen(viewModel: LancamentoViewModel) {
    val lancamentos by viewModel.lancamentos.collectAsState()
    val totalEntradas by viewModel.totalEntradas.collectAsState()
    val totalSaidas by viewModel.totalSaidas.collectAsState()
    
    var showSheet by remember { mutableStateOf(false) }
    var lancamentoParaEditar by remember { mutableStateOf<Lancamento?>(null) }
    var itemToDelete by remember { mutableStateOf<Lancamento?>(null) }

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    var isMensal by remember { mutableStateOf(true) }
    val calendar = remember { Calendar.getInstance() }
    var mes by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var ano by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    LaunchedEffect(mes, ano, isMensal) {
        viewModel.filtrar(String.format("%02d", mes), ano.toString())
    }

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
                    lancamentoParaEditar = null
                    showSheet = true 
                },
                containerColor = GoldPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryMiniCard(Modifier.weight(1f), "Receitas", currencyFormatter.format(totalEntradas), IncomeNeon)
                SummaryMiniCard(Modifier.weight(1f), "Despesas", currencyFormatter.format(totalSaidas), ExpenseNeon)
            }

            if (lancamentos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyStateList("Nenhuma movimentação.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lancamentos, key = { it.id }) { lancamento ->
                        LancamentoItemM3(
                            lancamento = lancamento,
                            dateFormatter = dateFormatter,
                            currencyFormatter = currencyFormatter,
                            onClick = {
                                lancamentoParaEditar = lancamento
                                showSheet = true
                            },
                            onLongClick = { itemToDelete = lancamento }
                        )
                    }
                }
            }
        }

        if (showSheet) {
            AddLancamentoSheet(
                lancamentoParaEditar = lancamentoParaEditar,
                onDismiss = { showSheet = false },
                onSave = { novo ->
                    if (lancamentoParaEditar == null) {
                        viewModel.adicionarLancamento(novo)
                    } else {
                        viewModel.atualizarLancamento(novo)
                    }
                    showSheet = false
                }
            )
        }

        itemToDelete?.let { lancamento ->
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                title = { Text("Excluir?", color = GoldPrimary) },
                text = { Text("Deseja apagar este registro?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deletarLancamento(lancamento)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LancamentoItemM3(
    lancamento: Lancamento, 
    dateFormatter: SimpleDateFormat, 
    currencyFormatter: NumberFormat, 
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val (tagBg, tagText) = getTipoTagColors(lancamento.tipo)
    val categoriaNome = if (lancamento.isEntrada) lancamento.categoriaEntrada?.name else lancamento.categoriaSaida?.name

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = (if (lancamento.isEntrada) IncomeNeon else ExpenseNeon).copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        getCategoryIcon(lancamento), 
                        null, 
                        tint = if (lancamento.isEntrada) IncomeNeon else ExpenseNeon
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(lancamento.descricao, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(categoriaNome ?: "Outros", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9E9E9E))
                    if (!lancamento.isEntrada) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = CircleShape, color = tagBg) {
                            Text(
                                lancamento.tipo.name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                fontSize = 8.sp,
                                color = tagText,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (lancamento.isEntrada) "+" else "-") + currencyFormatter.format(lancamento.valor),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = if (lancamento.isEntrada) IncomeNeon else ExpenseNeon
                )
                Text(dateFormatter.format(Date(lancamento.data)), style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
            }
        }
    }
}
