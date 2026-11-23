package com.example.crudprodutos.data.model

import com.google.gson.annotations.SerializedName

data class ProdutoApi(
    val id: Long,
    @SerializedName("restaurante_id") val restauranteId: Long,
    val nome: String,
    val descricao: String,
    val preco: Double,
    val disponivel: Boolean,
    @SerializedName("criado_em") val criadoEm: String?
)

data class ProdutoRequest(
    val nome: String,
    val descricao: String,
    val preco: Double,
    val disponivel: Boolean = true
)

data class LoginSolicitarRequest(
    val email: String
)

data class LoginRequest(
    val email: String,
    val codigo: String
)

data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    val restaurante: RestauranteInfo
)

data class RestauranteInfo(
    val id: Long,
    val nome: String,
    val email: String
)
