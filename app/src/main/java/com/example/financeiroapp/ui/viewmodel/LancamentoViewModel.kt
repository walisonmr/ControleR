package com.example.financeiroapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeiroapp.data.dao.LancamentoDao
import com.example.financeiroapp.data.model.Lancamento
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class LancamentoViewModel(
    private val lancamentoDao: LancamentoDao,
    private val activeProfileId: StateFlow<Long>
) : ViewModel() {

    private val _mesAtual = MutableStateFlow(String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1))
    private val _anoAtual = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR).toString())

    @OptIn(ExperimentalCoroutinesApi::class)
    val lancamentos = combine(activeProfileId, _mesAtual, _anoAtual) { id, mes, ano -> 
        Triple(id, mes, ano)
    }.flatMapLatest { (id, mes, ano) ->
        lancamentoDao.getByMonth(id, mes, ano)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalEntradas = lancamentos.map { list ->
        list.filter { it.isEntrada }.sumOf { it.valor }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSaidas = lancamentos.map { list ->
        list.filter { !it.isEntrada }.sumOf { it.valor }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun adicionarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            // Garante que o ID do perfil esteja correto
            lancamentoDao.insert(lancamento.copy(perfilId = activeProfileId.value))
        }
    }

    fun atualizarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            lancamentoDao.update(lancamento)
        }
    }

    fun deletarLancamento(lancamento: Lancamento) {
        viewModelScope.launch {
            lancamentoDao.delete(lancamento)
        }
    }

    fun filtrar(mes: String, ano: String) {
        _mesAtual.value = mes
        _anoAtual.value = ano
    }
}
