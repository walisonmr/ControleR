package com.example.financeiroapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financeiroapp.ui.theme.GoldPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PeriodSelectorM3(
    isMensal: Boolean, 
    mes: Int, 
    ano: Int, 
    onPeriodToggle: () -> Unit, 
    onPrev: () -> Unit, 
    onNext: () -> Unit
) {
    val meses = listOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")
    
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        color = Color(0xFF1A1A1A),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrev) { Icon(Icons.Default.ArrowBack, null, tint = GoldPrimary) }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.combinedClickable(onClick = onPeriodToggle)
            ) {
                Text(
                    text = if (isMensal) meses[mes-1] else "Visão Anual",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldPrimary
                )
                Text(
                    text = ano.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            IconButton(onClick = onNext) { Icon(Icons.Default.ArrowForward, null, tint = GoldPrimary) }
        }
    }
}

@Composable
fun SummaryMiniCard(modifier: Modifier, title: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun EmptyStateList(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(Icons.Default.Info, null, modifier = Modifier.size(48.dp), tint = Color(0xFF242424))
        Spacer(Modifier.height(16.dp))
        Text(message, color = Color(0xFF9E9E9E), style = MaterialTheme.typography.bodyMedium)
    }
}
