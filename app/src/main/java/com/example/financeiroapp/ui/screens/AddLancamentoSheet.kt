package com.example.financeiroapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.financeiroapp.data.model.*
import com.example.financeiroapp.ui.theme.GoldPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLancamentoSheet(
    lancamentoParaEditar: Lancamento? = null,
    onDismiss: () -> Unit, 
    onSave: (Lancamento) -> Unit
) {
    var descricao by remember { mutableStateOf(lancamentoParaEditar?.descricao ?: "") }
    var valor by remember { mutableStateOf(lancamentoParaEditar?.valor?.toString() ?: "") }
    var isEntrada by remember { mutableStateOf(lancamentoParaEditar?.isEntrada ?: false) }
    
    var categoriaEntrada by remember { mutableStateOf(lancamentoParaEditar?.categoriaEntrada ?: CategoriaEntrada.Outros) }
    var categoriaSaida by remember { mutableStateOf(lancamentoParaEditar?.categoriaSaida ?: CategoriaSaida.Outros) }
    
    var tipo by remember { mutableStateOf(lancamentoParaEditar?.tipo ?: TipoLancamento.Necessario) }
    var formaPagamento by remember { mutableStateOf(lancamentoParaEditar?.formaPagamento ?: FormaPagamento.Debito) }
    
    var dataSelecionada by remember { mutableLongStateOf(lancamentoParaEditar?.data ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dataSelecionada)

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
                if (lancamentoParaEditar == null) "Novo Registro" else "Editar Registro", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isEntrada) "ENTRADA (+)" else "SAÍDA (-)", 
                    modifier = Modifier.weight(1f),
                    color = if (isEntrada) Color(0xFF00E676) else Color(0xFFFF5252),
                    fontWeight = FontWeight.Black
                )
                Switch(
                    checked = isEntrada, 
                    onCheckedChange = { isEntrada = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676), uncheckedThumbColor = Color(0xFFFF5252))
                )
            }

            TextField(
                value = descricao,
                onValueChange = { descricao = it },
                label = { Text("Descrição") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedLabelColor = GoldPrimary, focusedIndicatorColor = GoldPrimary)
            )

            TextField(
                value = valor,
                onValueChange = { valor = it },
                label = { Text("Valor (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedLabelColor = GoldPrimary, focusedIndicatorColor = GoldPrimary)
            )

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.3f)))
            ) {
                Icon(Icons.Default.Create, null, tint = GoldPrimary)
                Spacer(Modifier.width(8.dp))
                val df = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                Text("Data: ${df.format(Date(dataSelecionada))}", color = Color.White)
            }

            if (isEntrada) {
                EnumDropdown("Categoria", CategoriaEntrada.values().toList(), categoriaEntrada) { categoriaEntrada = it }
            } else {
                EnumDropdown("Categoria", CategoriaSaida.values().toList(), categoriaSaida) { categoriaSaida = it }
                EnumDropdown("Tipo", TipoLancamento.values().toList(), tipo) { tipo = it }
                EnumDropdown("Pagamento", FormaPagamento.values().toList(), formaPagamento) { formaPagamento = it }
            }

            Button(
                onClick = {
                    val valorDouble = valor.replace(",", ".").toDoubleOrNull()
                    if (descricao.isNotBlank() && valorDouble != null) {
                        onSave(
                            (lancamentoParaEditar ?: Lancamento(
                                data = dataSelecionada, 
                                descricao = descricao, 
                                tipo = if (isEntrada) TipoLancamento.Essencial else tipo, 
                                formaPagamento = if (isEntrada) FormaPagamento.Pix else formaPagamento, 
                                valor = valorDouble, 
                                isEntrada = isEntrada
                            )).copy(
                                data = dataSelecionada,
                                descricao = descricao,
                                categoriaEntrada = if (isEntrada) categoriaEntrada else null,
                                categoriaSaida = if (!isEntrada) categoriaSaida else null,
                                tipo = if (isEntrada) TipoLancamento.Essencial else tipo,
                                formaPagamento = if (isEntrada) FormaPagamento.Pix else formaPagamento,
                                valor = valorDouble,
                                isEntrada = isEntrada
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                enabled = descricao.isNotBlank() && valor.isNotBlank()
            ) {
                Text(if (lancamentoParaEditar == null) "SALVAR" else "ATUALIZAR", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dataSelecionada = datePickerState.selectedDateMillis ?: dataSelecionada
                    showDatePicker = false
                }) { Text("OK", color = GoldPrimary) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Enum<T>> EnumDropdown(label: String, options: List<T>, selected: T, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = selected.name,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = TextFieldDefaults.colors(focusedLabelColor = GoldPrimary, focusedIndicatorColor = GoldPrimary),
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFF242424))
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name, color = Color.White) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
