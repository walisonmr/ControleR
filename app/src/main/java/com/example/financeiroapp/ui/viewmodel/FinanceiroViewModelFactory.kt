package com.example.financeiroapp.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financeiroapp.data.database.FinanceiroDatabase
import kotlinx.coroutines.flow.StateFlow

class FinanceiroViewModelFactory(
    private val db: FinanceiroDatabase,
    private val sharedPreferences: SharedPreferences? = null,
    private val activeProfileId: StateFlow<Long>? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PerfilViewModel::class.java) -> {
                if (sharedPreferences == null) throw IllegalArgumentException("SharedPreferences required for PerfilViewModel")
                PerfilViewModel(db.perfilDao(), sharedPreferences) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                if (activeProfileId == null) throw IllegalArgumentException("activeProfileId required for DashboardViewModel")
                DashboardViewModel(db.lancamentoDao(), db.dividaDao(), db.configuracaoDao(), activeProfileId) as T
            }
            modelClass.isAssignableFrom(LancamentoViewModel::class.java) -> {
                if (activeProfileId == null) throw IllegalArgumentException("activeProfileId required for LancamentoViewModel")
                LancamentoViewModel(db.lancamentoDao(), activeProfileId) as T
            }
            modelClass.isAssignableFrom(DividaViewModel::class.java) -> {
                if (activeProfileId == null) throw IllegalArgumentException("activeProfileId required for DividaViewModel")
                DividaViewModel(db.dividaDao(), db.lancamentoDao(), activeProfileId) as T
            }
            modelClass.isAssignableFrom(ConfiguracaoViewModel::class.java) -> {
                if (activeProfileId == null) throw IllegalArgumentException("activeProfileId required for ConfiguracaoViewModel")
                ConfiguracaoViewModel(db.configuracaoDao(), db.lancamentoDao(), db.dividaDao(), activeProfileId) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
