package com.example.financeiroapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- ENUMS ---

enum class CategoriaEntrada {
    Salario, Freelance, RendaExtra, Investimentos, AluguelRecebido, Presente, Reembolso, Outros
}

enum class CategoriaSaida {
    Alimentacao, Transporte, Moradia, Saude, Educacao, Lazer, Vestuario, Higiene, Assinaturas, 
    Combustivel, Farmacia, Restaurante, Mercado, Academia, Pets, Tecnologia, Presentes, Impostos, Outros
}

enum class TipoLancamento {
    Essencial, Necessario, Desnecessario, Vontade
}

enum class FormaPagamento {
    Dinheiro, Pix, Credito, Debito
}

enum class CategoriaDivida {
    CartaoCredito, Financiamento, Emprestimo, Aluguel, Agua, Luz, Internet, Telefone, 
    PlanoSaude, IPVA, IPTU, Assinatura, Outros
}

enum class StatusDivida {
    Pago, NaoPago
}

// --- ENTITIES ---

@Entity(tableName = "perfis")
data class Perfil(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nome: String,
    val fotoPerfil: String, // Iniciais, ex: "W"
    val corPerfil: String, // Hex code, ex: "#FF0000"
    val dataCriacao: Long = System.currentTimeMillis()
)

@Entity(tableName = "lancamentos")
data class Lancamento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long = 1,
    val data: Long,
    val descricao: String,
    val categoriaEntrada: CategoriaEntrada? = null,
    val categoriaSaida: CategoriaSaida? = null,
    val tipo: TipoLancamento,
    val formaPagamento: FormaPagamento,
    val valor: Double,
    val isEntrada: Boolean
)

@Entity(tableName = "dividas")
data class Divida(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val perfilId: Long = 1,
    val grupoId: String, // UUID para agrupar parcelas
    val descricao: String,
    val categoria: CategoriaDivida,
    val valorParcela: Double,
    val valorTotal: Double, // valorParcela * parcelasTotal
    val parcelasTotal: Int,
    val parcelaAtual: Int,
    val parcelasPagas: Int = 0,
    val dataVencimento: Long,
    val status: StatusDivida
)

@Entity(tableName = "configuracao")
data class Configuracao(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val perfilId: Long = 1,
    val saldoAtual: Double,
    val metaReserva: Double,
    val valorReservaAtual: Double,
    val metaInvestimento: Double,
    val valorInvestimentoAtual: Double
)
