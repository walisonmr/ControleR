package com.example.financeiroapp.data.database

import android.content.Context
import androidx.room.*
import com.example.financeiroapp.data.dao.*
import com.example.financeiroapp.data.model.*

// --- CONVERTERS ---

class FinanceiroConverters {
    @TypeConverter
    fun fromCategoriaEntrada(value: CategoriaEntrada?) = value?.name
    @TypeConverter
    fun toCategoriaEntrada(value: String?) = value?.let { CategoriaEntrada.valueOf(it) }

    @TypeConverter
    fun fromCategoriaSaida(value: CategoriaSaida?) = value?.name
    @TypeConverter
    fun toCategoriaSaida(value: String?) = value?.let { CategoriaSaida.valueOf(it) }

    @TypeConverter
    fun fromTipoLancamento(value: TipoLancamento) = value.name
    @TypeConverter
    fun toTipoLancamento(value: String) = TipoLancamento.valueOf(value)

    @TypeConverter
    fun fromFormaPagamento(value: FormaPagamento) = value.name
    @TypeConverter
    fun toFormaPagamento(value: String) = FormaPagamento.valueOf(value)

    @TypeConverter
    fun fromCategoriaDivida(value: CategoriaDivida) = value.name
    @TypeConverter
    fun toCategoriaDivida(value: String) = try { CategoriaDivida.valueOf(value) } catch(e: Exception) { CategoriaDivida.Outros }

    @TypeConverter
    fun fromStatusDivida(value: StatusDivida) = value.name
    @TypeConverter
    fun toStatusDivida(value: String) = StatusDivida.valueOf(value)
}

// --- DATABASE ---

@Database(
    entities = [Lancamento::class, Divida::class, Configuracao::class, Perfil::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(FinanceiroConverters::class)
abstract class FinanceiroDatabase : RoomDatabase() {

    abstract fun lancamentoDao(): LancamentoDao
    abstract fun dividaDao(): DividaDao
    abstract fun configuracaoDao(): ConfiguracaoDao
    abstract fun perfilDao(): PerfilDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceiroDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `perfis` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `fotoPerfil` TEXT NOT NULL, `corPerfil` TEXT NOT NULL, `dataCriacao` INTEGER NOT NULL)")
                database.execSQL("INSERT INTO perfis (id, nome, fotoPerfil, corPerfil, dataCriacao) VALUES (1, 'Principal', 'P', '#2E7D32', ${System.currentTimeMillis()})")
                database.execSQL("ALTER TABLE lancamentos ADD COLUMN perfilId INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE dividas ADD COLUMN perfilId INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE configuracao ADD COLUMN perfilId INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE lancamentos_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        perfilId INTEGER NOT NULL,
                        data INTEGER NOT NULL,
                        descricao TEXT NOT NULL,
                        categoriaEntrada TEXT,
                        categoriaSaida TEXT,
                        tipo TEXT NOT NULL,
                        formaPagamento TEXT NOT NULL,
                        valor REAL NOT NULL,
                        isEntrada INTEGER NOT NULL
                    )
                """)
                database.execSQL("""
                    INSERT INTO lancamentos_new (id, perfilId, data, descricao, categoriaSaida, tipo, formaPagamento, valor, isEntrada)
                    SELECT id, perfilId, data, descricao, 'Outros', tipo, formaPagamento, valor, isEntrada FROM lancamentos
                """)
                database.execSQL("DROP TABLE lancamentos")
                database.execSQL("ALTER TABLE lancamentos_new RENAME TO lancamentos")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE dividas ADD COLUMN parcelasPagas INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE dividas_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        perfilId INTEGER NOT NULL,
                        grupoId TEXT NOT NULL,
                        descricao TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        valorParcela REAL NOT NULL,
                        valorTotal REAL NOT NULL,
                        parcelasTotal INTEGER NOT NULL,
                        parcelaAtual INTEGER NOT NULL,
                        parcelasPagas INTEGER NOT NULL,
                        dataVencimento INTEGER NOT NULL,
                        status TEXT NOT NULL
                    )
                """)
                // Migra os dados. Assume valorParcela = valorTotal / parcelasTotal para dados antigos
                database.execSQL("""
                    INSERT INTO dividas_new (id, perfilId, grupoId, descricao, categoria, valorParcela, valorTotal, parcelasTotal, parcelaAtual, parcelasPagas, dataVencimento, status)
                    SELECT id, perfilId, 'migrado_' || id, descricao, categoria, valorTotal / parcelasTotal, valorTotal, parcelasTotal, parcelaAtual, parcelasPagas, dataVencimento, status FROM dividas
                """)
                database.execSQL("DROP TABLE dividas")
                database.execSQL("ALTER TABLE dividas_new RENAME TO dividas")
            }
        }

        fun getDatabase(context: Context): FinanceiroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceiroDatabase::class.java,
                    "financeiro_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
