package com.example.crudprodutos.data.remote

import com.example.crudprodutos.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Autenticação ──────────────────────────────────────────────────────────

    @POST("restaurantes/login/solicitar")
    suspend fun loginSolicitar(
        @Body body: LoginSolicitarRequest
    ): Response<Map<String, String>>

    @POST("restaurantes/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<LoginResponse>

    @POST("restaurantes/login/dev-bypass")
    suspend fun loginDevBypass(
        @Body body: LoginSolicitarRequest
    ): Response<LoginResponse>

    // ── Produtos ──────────────────────────────────────────────────────────────

    @GET("produtos/restaurante/{restaurante_id}")
    suspend fun listarProdutos(
        @Path("restaurante_id") restauranteId: Long
    ): Response<List<ProdutoApi>>

    @POST("produtos/")
    suspend fun criarProduto(
        @Header("Authorization") token: String,
        @Body body: ProdutoRequest
    ): Response<ProdutoApi>

    @PUT("produtos/{id}")
    suspend fun atualizarProduto(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: ProdutoRequest
    ): Response<ProdutoApi>

    @DELETE("produtos/{id}")
    suspend fun deletarProduto(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Map<String, String>>
}
