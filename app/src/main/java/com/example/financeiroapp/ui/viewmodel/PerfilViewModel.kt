package com.example.financeiroapp.ui.viewmodel

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeiroapp.data.dao.PerfilDao
import com.example.financeiroapp.data.model.Perfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class PerfilViewModel(
    private val perfilDao: PerfilDao,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val PREF_KEY_ACTIVE_PROFILE = "active_profile_id"

    // Carrega o ID salvo ou usa 1 (Principal) como padrão
    private val _activeProfileId = MutableStateFlow(sharedPreferences.getLong(PREF_KEY_ACTIVE_PROFILE, 1L))
    val activeProfileId = _activeProfileId.asStateFlow()

    val todosPerfis = perfilDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile = kotlinx.coroutines.flow.combine(todosPerfis, activeProfileId) { list, id ->
        list.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setActiveProfile(id: Long) {
        _activeProfileId.value = id
        sharedPreferences.edit { putLong(PREF_KEY_ACTIVE_PROFILE, id) }
    }

    fun criarPerfil(nome: String) {
        viewModelScope.launch {
            val iniciais = nome.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
            val cor = String.format("#%06X", 0xFFFFFF and Random.nextInt())
            
            val novoPerfil = Perfil(
                nome = nome,
                fotoPerfil = if (iniciais.isNotEmpty()) iniciais else "X",
                corPerfil = cor
            )
            val id = perfilDao.insert(novoPerfil)
            setActiveProfile(id)
        }
    }

    fun atualizarNomePerfil(id: Long, novoNome: String) {
        viewModelScope.launch {
            val perfilAtual = todosPerfis.value.find { it.id == id }
            perfilAtual?.let {
                val iniciais = novoNome.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
                val perfilAtualizado = it.copy(
                    nome = novoNome,
                    fotoPerfil = if (iniciais.isNotEmpty()) iniciais else "X"
                )
                perfilDao.update(perfilAtualizado)
            }
        }
    }

    fun deletarPerfil(perfil: Perfil) {
        viewModelScope.launch {
            // Não permite deletar o perfil principal (ID 1) se for regra de negócio, mas por enquanto permitiremos
            // exceto se for o único.
            if (todosPerfis.value.size > 1) {
                perfilDao.delete(perfil)
                if (activeProfileId.value == perfil.id) {
                    // Se deletou o ativo, volta para o ID 1 ou o primeiro disponível
                    val outro = todosPerfis.value.firstOrNull { it.id != perfil.id }
                    outro?.let { setActiveProfile(it.id) }
                }
            }
        }
    }
}
