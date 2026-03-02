package com.example.financeiroapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeiroapp.data.model.Perfil
import com.example.financeiroapp.ui.theme.GoldPrimary
import com.example.financeiroapp.ui.viewmodel.PerfilViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel,
    onBack: () -> Unit
) {
    val perfis by viewModel.todosPerfis.collectAsState()
    val activeProfileId by viewModel.activeProfileId.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var perfilParaEditar by remember { mutableStateOf<Perfil?>(null) }
    var perfilToDelete by remember { mutableStateOf<Perfil?>(null) }
    var newProfileName by remember { mutableStateOf("") }
    var editProfileName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seletor de Perfil", color = GoldPrimary, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0A0A0A))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = GoldPrimary,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Perfil")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0A0A0A))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(perfis) { perfil ->
                PerfilItem(
                    perfil = perfil,
                    isActive = perfil.id == activeProfileId,
                    onSelect = { viewModel.setActiveProfile(perfil.id) },
                    onEdit = { 
                        perfilParaEditar = perfil
                        editProfileName = perfil.nome
                    },
                    onDelete = { perfilToDelete = perfil }
                )
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Criar Novo Perfil", color = GoldPrimary) },
            text = {
                OutlinedTextField(
                    value = newProfileName,
                    onValueChange = { newProfileName = it },
                    label = { Text("Nome do Proprietário") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedLabelColor = GoldPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProfileName.isNotBlank()) {
                            viewModel.criarPerfil(newProfileName)
                            newProfileName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("CRIAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }

    perfilParaEditar?.let { perfil ->
        AlertDialog(
            onDismissRequest = { perfilParaEditar = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Editar Nome do Perfil", color = GoldPrimary) },
            text = {
                OutlinedTextField(
                    value = editProfileName,
                    onValueChange = { editProfileName = it },
                    label = { Text("Nome do Proprietário") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary, focusedLabelColor = GoldPrimary)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editProfileName.isNotBlank()) {
                            viewModel.atualizarNomePerfil(perfil.id, editProfileName)
                            perfilParaEditar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("SALVAR", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { perfilParaEditar = null }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }

    perfilToDelete?.let { perfil ->
        AlertDialog(
            onDismissRequest = { perfilToDelete = null },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Deletar Perfil?", color = Color(0xFFFF5252)) },
            text = { Text("Esta ação removerá todos os dados vinculados a '${perfil.nome}'. Confirmar?", color = Color.White) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletarPerfil(perfil)
                    perfilToDelete = null
                }) { Text("EXCLUIR", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { perfilToDelete = null }) { Text("MANTER", color = Color.Gray) }
            }
        )
    }
}

@Composable
fun PerfilItem(
    perfil: Perfil,
    isActive: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(20.dp),
        border = if (isActive) BorderStroke(2.dp, GoldPrimary) else BorderStroke(1.dp, Color(0xFF242424)),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1A1A1A) else Color(0xFF121212)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        try { Color(android.graphics.Color.parseColor(perfil.corPerfil)) } catch (e: Exception) { GoldPrimary },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = perfil.fotoPerfil,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = perfil.nome,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) GoldPrimary else Color.White
                )
                if (isActive) {
                    Text(
                        text = "PERFIL ATIVO",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = GoldPrimary.copy(alpha = 0.7f))
                }
                if (!isActive) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = Color(0xFF444444))
                    }
                }
            }
        }
    }
}
