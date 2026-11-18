package com.example.crudprodutos.data.repository

import com.example.crudprodutos.data.model.ProdutoApi
import com.example.crudprodutos.data.model.ProdutoRequest
import com.example.crudprodutos.data.remote.RetrofitClient

class ProdutoRepository {

    private val api = RetrofitClient.api

    suspend fun listar(restauranteId: Long): Result<List<ProdutoApi>> {
        return try {
            val response = api.listarProdutos(restauranteId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun criar(token: String, request: ProdutoRequest): Result<ProdutoApi> {
        return try {
            val response = api.criarProduto(token, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao criar produto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun atualizar(token: String, id: Long, request: ProdutoRequest): Result<ProdutoApi> {
        return try {
            val response = api.atualizarProduto(token, id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao atualizar produto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletar(token: String, id: Long): Result<Unit> {
        return try {
            val response = api.deletarProduto(token, id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao deletar produto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
