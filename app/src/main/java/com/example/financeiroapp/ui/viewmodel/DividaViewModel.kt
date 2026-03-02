package com.example.financeiroapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeiroapp.data.dao.DividaDao
import com.example.financeiroapp.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class DividaViewModel(
    private val dividaDao: DividaDao,
    private val lancamentoDao: com.example.financeiroapp.data.dao.LancamentoDao,
    private val activeProfileId: StateFlow<Long>
) : ViewModel() {

    private val _statusFiltro = MutableStateFlow<StatusDivida?>(null)
    private val _mesAtual = MutableStateFlow(String.format("%02d", Calendar.getInstance().get(Calendar.MONTH) + 1))
    private val _anoAtual = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR).toString())

    val todasDividas = combine(_statusFiltro, _mesAtual, _anoAtual, activeProfileId) { status, mes, ano, perfilId ->
        Quadruple(status, mes, ano, perfilId)
    }.flatMapLatest { (status, mes, ano, perfilId) ->
        dividaDao.getByMonth(perfilId, mes, ano).map { lista ->
            when (status) {
                StatusDivida.NaoPago -> lista.filter { it.status == StatusDivida.NaoPago }
                StatusDivida.Pago -> lista.filter { it.status == StatusDivida.Pago || it.parcelasPagas > 0 }
                else -> lista
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPendente = todasDividas.map { list ->
        list.filter { it.status == StatusDivida.NaoPago }.sumOf { it.valorParcela }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // FUNÇÃO DE CRIAÇÃO: Cria da parcelaAtual até parcelasTotal
    fun adicionarDivida(dividaBase: Divida) {
        viewModelScope.launch {
            val grupoId = UUID.randomUUID().toString()
            val parcelas = mutableListOf<Divida>()
            val perfilId = activeProfileId.value

            for (i in dividaBase.parcelaAtual..dividaBase.parcelasTotal) {
                val vencimento = Calendar.getInstance().apply {
                    timeInMillis = dividaBase.dataVencimento
                    add(Calendar.MONTH, i - dividaBase.parcelaAtual)
                }.timeInMillis

                parcelas.add(
                    dividaBase.copy(
                        perfilId = perfilId,
                        grupoId = grupoId,
                        parcelaAtual = i,
                        dataVencimento = vencimento,
                        parcelasPagas = 0,
                        status = StatusDivida.NaoPago
                    )
                )
            }
            dividaDao.insertAll(parcelas)
        }
    }

    // FUNÇÃO DE EDIÇÃO: Lógica para sincronizar parcelas futuras
    fun atualizarDivida(dividaEditada: Divida, atualizarFuturas: Boolean) {
        viewModelScope.launch {
            // Sincroniza parcelasPagas com o status atual antes de salvar a parcela atual
            val dividaFinal = dividaEditada.copy(parcelasPagas = if (dividaEditada.status == StatusDivida.Pago) 1 else 0)
            
            // 1. Atualiza a parcela específica que foi aberta no formulário
            dividaDao.update(dividaFinal)

            // 2. Se mudou de Pago para NaoPago, remove o lançamento automático vinculado
            if (dividaFinal.status == StatusDivida.NaoPago) {
                val descricaoParcela = "Pagamento: ${dividaFinal.descricao} - Parcela ${dividaFinal.parcelaAtual} de ${dividaFinal.parcelasTotal}"
                lancamentoDao.deleteExactLancamento(activeProfileId.value, descricaoParcela)
            }

            // 3. Lógica de Replicar para o Grupo
            if (atualizarFuturas) {
                // Atualiza Descrição, Categoria e Valor em todas as parcelas futuras do grupo (parcelaAtual > editada)
                dividaDao.updateFutureInstallments(
                    grupoId = dividaFinal.grupoId,
                    parcelaInicio = dividaFinal.parcelaAtual + 1,
                    descricao = dividaFinal.descricao,
                    categoria = dividaFinal.categoria,
                    valorParcela = dividaFinal.valorParcela,
                    valorTotal = dividaFinal.valorTotal
                )
            }
        }
    }

    fun atualizarStatus(divida: Divida, novoStatus: StatusDivida) {
        viewModelScope.launch {
            val descricaoParcela = "Pagamento: ${divida.descricao} - Parcela ${divida.parcelaAtual} de ${divida.parcelasTotal}"
            
            if (novoStatus == StatusDivida.Pago) {
                dividaDao.update(divida.copy(status = StatusDivida.Pago, parcelasPagas = 1))
                val categoriaSaida = mapearCategoria(divida.categoria)
                lancamentoDao.insert(
                    Lancamento(
                        perfilId = activeProfileId.value,
                        data = System.currentTimeMillis(),
                        descricao = descricaoParcela,
                        categoriaSaida = categoriaSaida,
                        tipo = TipoLancamento.Essencial,
                        formaPagamento = FormaPagamento.Debito,
                        valor = divida.valorParcela,
                        isEntrada = false
                    )
                )
            } else {
                dividaDao.update(divida.copy(status = StatusDivida.NaoPago, parcelasPagas = 0))
                lancamentoDao.deleteExactLancamento(activeProfileId.value, descricaoParcela)
            }
        }
    }

    private fun mapearCategoria(cat: CategoriaDivida): CategoriaSaida {
        return when (cat) {
            CategoriaDivida.CartaoCredito -> CategoriaSaida.Outros
            CategoriaDivida.Financiamento -> CategoriaSaida.Moradia
            CategoriaDivida.Emprestimo -> CategoriaSaida.Outros
            CategoriaDivida.Aluguel -> CategoriaSaida.Moradia
            CategoriaDivida.Agua -> CategoriaSaida.Moradia
            CategoriaDivida.Luz -> CategoriaSaida.Moradia
            CategoriaDivida.Internet -> CategoriaSaida.Assinaturas
            CategoriaDivida.Telefone -> CategoriaSaida.Assinaturas
            CategoriaDivida.PlanoSaude -> CategoriaSaida.Saude
            CategoriaDivida.IPVA -> CategoriaSaida.Impostos
            CategoriaDivida.IPTU -> CategoriaSaida.Impostos
            CategoriaDivida.Assinatura -> CategoriaSaida.Assinaturas
            CategoriaDivida.Outros -> CategoriaSaida.Outros
        }
    }

    fun deletarDivida(divida: Divida, deletarTudo: Boolean) {
        viewModelScope.launch {
            if (deletarTudo) {
                dividaDao.deleteByGrupoId(divida.grupoId)
            } else {
                dividaDao.delete(divida)
            }
        }
    }

    fun setFiltroStatus(status: StatusDivida?) {
        _statusFiltro.value = status
    }

    fun filtrar(mes: String, ano: String) {
        _mesAtual.value = mes
        _anoAtual.value = ano
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
