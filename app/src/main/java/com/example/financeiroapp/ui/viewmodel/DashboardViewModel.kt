package com.example.financeiroapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeiroapp.data.dao.ConfiguracaoDao
import com.example.financeiroapp.data.dao.DividaDao
import com.example.financeiroapp.data.dao.LancamentoDao
import com.example.financeiroapp.data.model.StatusDivida
import kotlinx.coroutines.flow.*
import java.util.Calendar

data class DashboardState(
    val saldoAtual: Double = 0.0,
    val totalDividasPendente: Double = 0.0,
    val reservaValor: Double = 0.0,
    val reservaPorcentagem: Double = 0.0,
    val investimentoValor: Double = 0.0,
    val investimentoPorcentagem: Double = 0.0,
    val dividasProximas: List<com.example.financeiroapp.data.model.Divida> = emptyList()
)

class DashboardViewModel(
    private val lancamentoDao: LancamentoDao,
    private val dividaDao: DividaDao,
    private val configuracaoDao: ConfiguracaoDao,
    private val activeProfileId: StateFlow<Long>
) : ViewModel() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardState> = activeProfileId.flatMapLatest { id ->
        val calendar = Calendar.getInstance()
        val mesAtual = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        val anoAtual = calendar.get(Calendar.YEAR).toString()

        combine(
            configuracaoDao.getConfig(id),
            dividaDao.getByMonth(id, mesAtual, anoAtual),
            dividaDao.getAll(id) // Para as dívidas próximas
        ) { config, dividasMes, todasDividas ->
            val seteDiasEmMs = 7 * 24 * 60 * 60 * 1000L
            val hoje = Calendar.getInstance().timeInMillis
            
            DashboardState(
                saldoAtual = config?.saldoAtual ?: 0.0,
                totalDividasPendente = dividasMes.filter { it.status == StatusDivida.NaoPago }.sumOf { it.valorParcela },
                reservaValor = config?.valorReservaAtual ?: 0.0,
                reservaPorcentagem = if (config != null && config.metaReserva > 0) (config.valorReservaAtual / config.metaReserva) * 100.0 else 0.0,
                investimentoValor = config?.valorInvestimentoAtual ?: 0.0,
                investimentoPorcentagem = if (config != null && config.metaInvestimento > 0) (config.valorInvestimentoAtual / config.metaInvestimento) * 100.0 else 0.0,
                dividasProximas = todasDividas.filter { 
                    it.status == StatusDivida.NaoPago && 
                    it.dataVencimento in hoje..(hoje + seteDiasEmMs)
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())
}
