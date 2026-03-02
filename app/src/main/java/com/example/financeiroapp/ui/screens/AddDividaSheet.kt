package com.example.financeiroapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeiroapp.data.model.*
import com.example.financeiroapp.ui.theme.GoldPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDividaSheet(
    dividaParaEditar: Divida? = null,
    onDismiss: () -> Unit, 
    onSave: (Divida, Boolean) -> Unit
) {
    var descricao by remember { mutableStateOf(dividaParaEditar?.descricao ?: "") }
    var valorParcela by remember { mutableStateOf(dividaParaEditar?.valorParcela?.toString() ?: "") }
    var parcelasTotal by remember { mutableStateOf(dividaParaEditar?.parcelasTotal?.toString() ?: "1") }
    var parcelaAtual by remember { mutableStateOf(dividaParaEditar?.parcelaAtual?.toString() ?: "1") }
    var categoria by remember { mutableStateOf(dividaParaEditar?.categoria ?: CategoriaDivida.Outros) }
    
    var dataVencimento by remember { mutableLongStateOf(dividaParaEditar?.dataVencimento ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dataVencimento)

    var atualizarFuturas by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf(dividaParaEditar?.status ?: StatusDivida.NaoPago) }

    val pTotal = parcelasTotal.toIntOrNull() ?: 1
    val pAtual = parcelaAtual.toIntOrNull() ?: 1
    val erroParcela = pAtual > pTotal

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        dragHandle = { BottomSheetDefaults.DragHandle(color = GoldPrimary.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (dividaParaEditar == null) "Novo Compromisso" else "Editar Parcela ${dividaParaEditar.parcelaAtual}", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )

            if (dividaParaEditar != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Situação:", modifier = Modifier.weight(1f), color = Color.White)
                    Switch(
                        checked = status == StatusDivida.Pago, 
                        onCheckedChange = { status = if (it) StatusDivida.Pago else StatusDivida.NaoPago },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (status == StatusDivida.Pago) "QUITADA" else "PENDENTE", 
                        color = if (status == StatusDivida.Pago) Color(0xFF00E676) else Color(0xFFFF9100),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }

            TextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("O que é?") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedLabelColor = GoldPrimary, focusedIndicatorColor = GoldPrimary)
            )

            TextField(
                value = valorParcela,
                onValueChange = { valorParcela = it },
                label = { Text("Valor da Parcela (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedLabelColor = GoldPrimary, focusedIndicatorColor = GoldPrimary)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = parcelasTotal,
                    onValueChange = { parcelasTotal = it },
                    label = { Text("Total") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(focusedLabelColor = GoldPrimary, focusedIndicatorColor = GoldPrimary)
                )
                Column(modifier = Modifier.weight(1f)) {
                    TextField(
                        value = parcelaAtual,
                        onValueChange = { parcelaAtual = it },
                        label = { Text("Atual") },
                        isError = erroParcela,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedLabelColor = GoldPrimary, focusedIndicatorColor = GoldPrimary)
                    )
                }
            }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.3f)))
            ) {
                Icon(Icons.Default.Create, null, tint = GoldPrimary)
                Spacer(Modifier.width(8.dp))
                val df = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                Text("Primeiro Vencimento: ${df.format(Date(dataVencimento))}", color = Color.White)
            }

            EnumDropdown("Categoria", CategoriaDivida.values().toList(), categoria) { categoria = it }

            if (dividaParaEditar != null && dividaParaEditar.parcelasTotal > 1) {
                Surface(
                    color = Color(0xFF242424),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = atualizarFuturas, 
                            onCheckedChange = { atualizarFuturas = it },
                            colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                        )
                        Text("Replicar alteração para parcelas futuras?", style = MaterialTheme.typography.bodySmall, color = Color.White)
                    }
                }
            }

            Button(
                onClick = {
                    val vParcela = valorParcela.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (descricao.isNotBlank() && vParcela > 0 && !erroParcela) {
                        val divida = (dividaParaEditar ?: Divida(
                            grupoId = "", 
                            descricao = descricao, 
                            categoria = categoria, 
                            valorParcela = vParcela,
                            valorTotal = vParcela * pTotal,
                            parcelasTotal = pTotal,
                            parcelaAtual = pAtual,
                            dataVencimento = dataVencimento,
                            status = status
                        )).copy(
                            descricao = descricao,
                            categoria = categoria,
                            valorParcela = vParcela,
                            valorTotal = vParcela * pTotal,
                            parcelasTotal = pTotal,
                            parcelaAtual = pAtual,
                            dataVencimento = dataVencimento,
                            status = status
                        )
                        onSave(divida, atualizarFuturas)
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                enabled = descricao.isNotBlank() && valorParcela.isNotBlank() && !erroParcela
            ) {
                Text(if (dividaParaEditar == null) "LANÇAR DÍVIDA" else "ATUALIZAR", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dataVencimento = datePickerState.selectedDateMillis ?: dataVencimento
                    showDatePicker = false
                }) { Text("OK", color = GoldPrimary) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
