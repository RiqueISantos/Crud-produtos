package com.example.crudprodutos.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.crudprodutos.data.model.ProdutoApi
import com.example.crudprodutos.data.model.ProdutoRequest
import com.example.crudprodutos.data.repository.ProdutoRepository
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    object Success : UiState()
    data class Error(val mensagem: String) : UiState()
}

class ProdutoViewModel : ViewModel() {

    private val repository = ProdutoRepository()

    private val _produtos = MutableLiveData<List<ProdutoApi>>()
    val produtos: LiveData<List<ProdutoApi>> = _produtos

    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    private val _operacaoConcluida = MutableLiveData<Boolean>()
    val operacaoConcluida: LiveData<Boolean> = _operacaoConcluida

    fun carregarProdutos(token: String, restauranteId: Long) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.listar(restauranteId)
            result.fold(
                onSuccess = {
                    _produtos.value = it
                    _uiState.value = UiState.Success
                },
                onFailure = {
                    _uiState.value = UiState.Error(it.message ?: "Erro ao carregar produtos")
                }
            )
        }
    }

    fun criarProduto(token: String, request: ProdutoRequest) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.criar(token, request)
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success
                    _operacaoConcluida.value = true
                },
                onFailure = {
                    _uiState.value = UiState.Error(it.message ?: "Erro ao criar produto")
                }
            )
        }
    }

    fun atualizarProduto(token: String, id: Long, request: ProdutoRequest) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.atualizar(token, id, request)
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success
                    _operacaoConcluida.value = true
                },
                onFailure = {
                    _uiState.value = UiState.Error(it.message ?: "Erro ao atualizar produto")
                }
            )
        }
    }

    fun deletarProduto(token: String, id: Long, restauranteId: Long) {
        viewModelScope.launch {
            val result = repository.deletar(token, id)
            result.fold(
                onSuccess = { carregarProdutos(token, restauranteId) },
                onFailure = {
                    _uiState.value = UiState.Error(it.message ?: "Erro ao deletar produto")
                }
            )
        }
    }
}
