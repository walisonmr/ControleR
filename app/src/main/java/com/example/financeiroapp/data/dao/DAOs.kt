package com.example.financeiroapp.data.dao

import androidx.room.*
import com.example.financeiroapp.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfilDao {
    @Insert
    suspend fun insert(perfil: Perfil): Long

    @Update
    suspend fun update(perfil: Perfil)

    @Delete
    suspend fun delete(perfil: Perfil)

    @Query("SELECT * FROM perfis")
    fun getAll(): Flow<List<Perfil>>

    @Query("SELECT * FROM perfis WHERE id = :id")
    suspend fun getById(id: Long): Perfil?

    @Query("DELETE FROM perfis WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface LancamentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(lancamento: Lancamento)

    @Update
    suspend fun update(lancamento: Lancamento)

    @Delete
    suspend fun delete(lancamento: Lancamento)

    @Query("DELETE FROM lancamentos WHERE perfilId = :perfilId AND descricao = :descricao")
    suspend fun deleteExactLancamento(perfilId: Long, descricao: String)

    @Query("DELETE FROM lancamentos WHERE perfilId = :perfilId")
    suspend fun deleteAllByPerfil(perfilId: Long)

    @Query("SELECT * FROM lancamentos WHERE perfilId = :perfilId ORDER BY data DESC")
    fun getAll(perfilId: Long): Flow<List<Lancamento>>

    @Query("""
        SELECT * FROM lancamentos 
        WHERE perfilId = :perfilId 
        AND strftime('%m', datetime(data / 1000, 'unixepoch')) = :mes 
        AND strftime('%Y', datetime(data / 1000, 'unixepoch')) = :ano
    """)
    fun getByMonth(perfilId: Long, mes: String, ano: String): Flow<List<Lancamento>>

    @Query("""
        SELECT * FROM lancamentos 
        WHERE perfilId = :perfilId 
        AND strftime('%Y', datetime(data / 1000, 'unixepoch')) = :ano
    """)
    fun getByYear(perfilId: Long, ano: String): Flow<List<Lancamento>>
}

@Dao
interface DividaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(divida: Divida)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(dividas: List<Divida>)

    @Update
    suspend fun update(divida: Divida)

    @Query("UPDATE dividas SET descricao = :descricao, categoria = :categoria, valorParcela = :valorParcela, valorTotal = :valorTotal WHERE grupoId = :grupoId AND parcelaAtual >= :parcelaInicio")
    suspend fun updateFutureInstallments(grupoId: String, parcelaInicio: Int, descricao: String, categoria: CategoriaDivida, valorParcela: Double, valorTotal: Double)

    @Delete
    suspend fun delete(divida: Divida)

    @Query("DELETE FROM dividas WHERE grupoId = :grupoId")
    suspend fun deleteByGrupoId(grupoId: String)

    @Query("DELETE FROM dividas WHERE perfilId = :perfilId")
    suspend fun deleteAllByPerfil(perfilId: Long)

    @Query("SELECT * FROM dividas WHERE perfilId = :perfilId ORDER BY dataVencimento ASC")
    fun getAll(perfilId: Long): Flow<List<Divida>>

    @Query("SELECT * FROM dividas WHERE perfilId = :perfilId AND status = :status")
    fun getByStatus(perfilId: Long, status: StatusDivida): Flow<List<Divida>>

    @Query("""
        SELECT * FROM dividas 
        WHERE perfilId = :perfilId 
        AND strftime('%m', datetime(dataVencimento / 1000, 'unixepoch')) = :mes 
        AND strftime('%Y', datetime(dataVencimento / 1000, 'unixepoch')) = :ano
    """)
    fun getByMonth(perfilId: Long, mes: String, ano: String): Flow<List<Divida>>

    @Query("""
        SELECT * FROM dividas 
        WHERE perfilId = :perfilId 
        AND strftime('%Y', datetime(dataVencimento / 1000, 'unixepoch')) = :ano
    """)
    fun getByYear(perfilId: Long, ano: String): Flow<List<Divida>>
}

@Dao
interface ConfiguracaoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(config: Configuracao)

    @Query("DELETE FROM configuracao WHERE perfilId = :perfilId")
    suspend fun deleteByPerfil(perfilId: Long)

    @Query("SELECT * FROM configuracao WHERE perfilId = :perfilId LIMIT 1")
    fun getConfig(perfilId: Long): Flow<Configuracao?>

    @Update
    suspend fun update(config: Configuracao)
}
