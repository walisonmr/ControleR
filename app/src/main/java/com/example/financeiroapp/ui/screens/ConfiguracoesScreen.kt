package com.example.financeiroapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financeiroapp.data.model.Configuracao
import com.example.financeiroapp.ui.theme.GoldPrimary
import com.example.financeiroapp.ui.viewmodel.ConfiguracaoViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    viewModel: ConfiguracaoViewModel,
    onBack: () -> Unit
) {
    val configState by viewModel.configuracao.collectAsState()
    
    var saldo by remember { mutableStateOf("") }
    var metaReserva by remember { mutableStateOf("") }
    var reservaAtual by remember { mutableStateOf("") }
    var metaInvest by remember { mutableStateOf("") }
    var investAtual by remember { mutableStateOf("") }
    
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentConfig = viewModel.configuracao.filterNotNull().first()
        saldo = currentConfig.saldoAtual.toString()
        metaReserva = currentConfig.metaReserva.toString()
        reservaAtual = currentConfig.valorReservaAtual.toString()
        metaInvest = currentConfig.metaInvestimento.toString()
        investAtual = currentConfig.valorInvestimentoAtual.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes ControleR", color = GoldPrimary, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0A0A0A))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Gestão de Saldo Global", color = GoldPrimary, fontWeight = FontWeight.Bold)
            ConfigField("Saldo Atual (R$)", saldo) { saldo = it }
            
            Divider(color = Color(0xFF242424))
            Text("Metas de Reserva", color = GoldPrimary, fontWeight = FontWeight.Bold)
            ConfigField("Valor da Meta", metaReserva) { metaReserva = it }
            ConfigField("Acumulado", reservaAtual) { reservaAtual = it }

            Divider(color = Color(0xFF242424))
            Text("Metas de Investimento", color = GoldPrimary, fontWeight = FontWeight.Bold)
            ConfigField("Valor da Meta", metaInvest) { metaInvest = it }
            ConfigField("Acumulado", investAtual) { investAtual = it }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val configId = configState?.id ?: 0
                    val perfilId = configState?.perfilId ?: 1
                    
                    val newConfig = Configuracao(
                        id = configId,
                        perfilId = perfilId,
                        saldoAtual = saldo.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        metaReserva = metaReserva.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        valorReservaAtual = reservaAtual.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        metaInvestimento = metaInvest.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        valorInvestimentoAtual = investAtual.replace(",", ".").toDoubleOrNull() ?: 0.0
                    )
                    viewModel.salvarConfiguracao(newConfig)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                enabled = listOf(saldo, metaReserva, reservaAtual, metaInvest, investAtual).all { 
                    (it.replace(",", ".").toDoubleOrNull() ?: -1.0) >= 0.0 
                }
            ) {
                Text("CONFIRMAR AJUSTES", fontWeight = FontWeight.Black)
            }
            
            TextButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RECOMEÇAR TUDO", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
            }
        }
    }
    
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Zerar Dados?", color = GoldPrimary) },
            text = { Text("Isso apagará permanentemente todos os registros deste perfil. Confirmar?", color = Color.White) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetarApp()
                        showResetDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                ) {
                    Text("SIM, APAGAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("CANCELAR", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun ConfigField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GoldPrimary,
            unfocusedBorderColor = Color(0xFF242424),
            focusedLabelColor = GoldPrimary
        )
    )
}
