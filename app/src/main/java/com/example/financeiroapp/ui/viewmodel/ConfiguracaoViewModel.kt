package com.example.financeiroapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeiroapp.data.dao.ConfiguracaoDao
import com.example.financeiroapp.data.dao.DividaDao
import com.example.financeiroapp.data.dao.LancamentoDao
import com.example.financeiroapp.data.model.Configuracao
import com.example.financeiroapp.data.model.Lancamento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConfiguracaoViewModel(
    private val configuracaoDao: ConfiguracaoDao,
    private val lancamentoDao: LancamentoDao,
    private val dividaDao: DividaDao,
    private val activeProfileId: StateFlow<Long>
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val configuracao = activeProfileId.flatMapLatest { id ->
        configuracaoDao.getConfig(id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        // Inicializa configuração para o perfil ativo se não existir
        viewModelScope.launch {
            activeProfileId.collect { perfilId ->
                configuracaoDao.getConfig(perfilId).first() ?: configuracaoDao.save(
                    Configuracao(perfilId = perfilId, saldoAtual = 0.0, metaReserva = 0.0, valorReservaAtual = 0.0, metaInvestimento = 0.0, valorInvestimentoAtual = 0.0)
                )
            }
        }
        
        observarLancamentos()
    }

    private fun observarLancamentos() {
        viewModelScope.launch {
            combine(
                activeProfileId.flatMapLatest { lancamentoDao.getAll(it) },
                configuracao.filterNotNull()
            ) { lista, config ->
                val saldoTotal = lista.sumOf { if (it.isEntrada) it.valor else -it.valor }
                if (config.saldoAtual != saldoTotal) {
                    configuracaoDao.update(config.copy(saldoAtual = saldoTotal))
                }
            }.collect()
        }
    }

    val porcReserva = configuracao.map { config ->
        if (config == null || config.metaReserva <= 0.0) 0.0 
        else (config.valorReservaAtual / config.metaReserva) * 100.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val porcInvestimento = configuracao.map { config ->
        if (config == null || config.metaInvestimento <= 0.0) 0.0 
        else (config.valorInvestimentoAtual / config.metaInvestimento) * 100.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun salvarConfiguracao(config: Configuracao) {
        viewModelScope.launch { configuracaoDao.update(config) }
    }

    fun resetarApp() {
        viewModelScope.launch {
            val perfilId = activeProfileId.value
            lancamentoDao.deleteAllByPerfil(perfilId)
            dividaDao.deleteAllByPerfil(perfilId)
            // Reseta configuração mas mantém metas (opcional, aqui vou resetar saldo e manter metas ou resetar tudo? O pedido diz "apaga todos os dados... e reinicia saldo para 0". Vou resetar saldo e manter o registro de configuração para não quebrar a UI, mas com valores zerados)
            configuracao.value?.let { config ->
                 configuracaoDao.update(config.copy(saldoAtual = 0.0, valorReservaAtual = 0.0, valorInvestimentoAtual = 0.0))
            }
        }
    }
}
