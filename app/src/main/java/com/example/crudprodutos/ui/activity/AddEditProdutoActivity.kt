package com.example.crudprodutos.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.crudprodutos.data.model.ProdutoRequest
import com.example.crudprodutos.data.session.SessionManager
import com.example.crudprodutos.databinding.ActivityAddEditProdutoBinding
import com.example.crudprodutos.ui.viewmodel.ProdutoViewModel
import com.example.crudprodutos.ui.viewmodel.UiState

class AddEditProdutoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditProdutoBinding
    private lateinit var session: SessionManager
    private val viewModel: ProdutoViewModel by viewModels()

    private val produtoId: Long get() = intent.getLongExtra(EXTRA_PRODUTO_ID, -1L)
    private val isEdicao get() = produtoId != -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditProdutoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setSupportActionBar(null)
        supportActionBar?.title = if (isEdicao) "Editar Produto" else "Adicionar Produto"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (isEdicao) preencherCampos()

        observarViewModel()

        binding.btnSalvar.setOnClickListener { salvar() }
        binding.btnCancelar.setOnClickListener { finish() }
    }

    private fun preencherCampos() {
        binding.etNome.setText(intent.getStringExtra(EXTRA_PRODUTO_NOME))
        binding.etDescricao.setText(intent.getStringExtra(EXTRA_PRODUTO_DESCRICAO))
        binding.etPreco.setText(intent.getDoubleExtra(EXTRA_PRODUTO_PRECO, 0.0).toString())
        // campo disponivel não tem na tela, usa true por padrão
    }

    private fun observarViewModel() {
        viewModel.operacaoConcluida.observe(this) { concluida ->
            if (concluida) {
                val msg = if (isEdicao) "Produto atualizado!" else "Produto criado!"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Error -> Toast.makeText(this, state.mensagem, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    private fun salvar() {
        val nome = binding.etNome.text.toString().trim()
        val descricao = binding.etDescricao.text.toString().trim()
        val precoStr = binding.etPreco.text.toString().trim()

        if (nome.isEmpty() || precoStr.isEmpty()) {
            Toast.makeText(this, "Preencha nome e preço.", Toast.LENGTH_SHORT).show()
            return
        }

        val preco = precoStr.toDoubleOrNull()
        if (preco == null || preco <= 0) {
            Toast.makeText(this, "Informe um preço válido.", Toast.LENGTH_SHORT).show()
            return
        }

        val request = ProdutoRequest(
            nome = nome,
            descricao = descricao,
            preco = preco,
            disponivel = true
        )

        val token = session.getBearerToken()

        if (isEdicao) {
            viewModel.atualizarProduto(token, produtoId, request)
        } else {
            viewModel.criarProduto(token, request)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_PRODUTO_ID = "extra_produto_id"
        const val EXTRA_PRODUTO_NOME = "extra_produto_nome"
        const val EXTRA_PRODUTO_DESCRICAO = "extra_produto_descricao"
        const val EXTRA_PRODUTO_PRECO = "extra_produto_preco"
        const val EXTRA_PRODUTO_DISPONIVEL = "extra_produto_disponivel"
    }
}
