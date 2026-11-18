package com.example.crudprodutos.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crudprodutos.R
import com.example.crudprodutos.data.model.ProdutoApi
import com.example.crudprodutos.data.session.SessionManager
import com.example.crudprodutos.databinding.ActivityMainBinding
import com.example.crudprodutos.ui.adapter.ProdutoAdapter
import com.example.crudprodutos.ui.viewmodel.ProdutoViewModel
import com.example.crudprodutos.ui.viewmodel.UiState

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var session: SessionManager
    private val viewModel: ProdutoViewModel by viewModels()
    private lateinit var adapter: ProdutoAdapter
    private var todosProdutos: List<ProdutoApi> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = session.getRestauranteNome() ?: "Produtos"

        setupRecyclerView()
        setupObservers()
        setupSearch()
        setupFab()

        carregarProdutos()
    }

    override fun onResume() {
        super.onResume()
        carregarProdutos()
    }

    private fun carregarProdutos() {
        viewModel.carregarProdutos(session.getBearerToken(), session.getRestauranteId())
    }

    private fun setupRecyclerView() {
        adapter = ProdutoAdapter(
            onEditar = { produto -> abrirFormulario(produto) },
            onDeletar = { produto -> confirmarExclusao(produto) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.produtos.observe(this) { lista ->
            todosProdutos = lista
            adapter.submitList(lista)
            binding.tvEmpty.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> { /* pode adicionar shimmer/progress se quiser */ }
                is UiState.Error -> Toast.makeText(this, state.mensagem, Toast.LENGTH_LONG).show()
                is UiState.Success -> { }
            }
        }
    }

    private fun setupSearch() {
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                val filtrado = if (query.isEmpty()) {
                    todosProdutos
                } else {
                    todosProdutos.filter {
                        it.nome.lowercase().contains(query) ||
                        it.descricao.lowercase().contains(query)
                    }
                }
                adapter.submitList(filtrado)
                binding.tvEmpty.visibility = if (filtrado.isEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFab() {
        binding.fabAdicionar.setOnClickListener { abrirFormulario(null) }
    }

    private fun abrirFormulario(produto: ProdutoApi?) {
        val intent = Intent(this, AddEditProdutoActivity::class.java)
        produto?.let {
            intent.putExtra(AddEditProdutoActivity.EXTRA_PRODUTO_ID, it.id)
            intent.putExtra(AddEditProdutoActivity.EXTRA_PRODUTO_NOME, it.nome)
            intent.putExtra(AddEditProdutoActivity.EXTRA_PRODUTO_DESCRICAO, it.descricao)
            intent.putExtra(AddEditProdutoActivity.EXTRA_PRODUTO_PRECO, it.preco)
            intent.putExtra(AddEditProdutoActivity.EXTRA_PRODUTO_DISPONIVEL, it.disponivel)
        }
        startActivity(intent)
    }

    private fun confirmarExclusao(produto: ProdutoApi) {
        AlertDialog.Builder(this)
            .setTitle("Excluir produto")
            .setMessage("Deseja excluir \"${produto.nome}\"?")
            .setPositiveButton("Sim") { _, _ ->
                viewModel.deletarProduto(
                    session.getBearerToken(),
                    produto.id,
                    session.getRestauranteId()
                )
            }
            .setNegativeButton("Não", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_qr -> {
                startActivity(Intent(this, QrScannerActivity::class.java))
                true
            }
            R.id.action_logout -> {
                AlertDialog.Builder(this)
                    .setTitle("Sair")
                    .setMessage("Deseja sair da sua conta?")
                    .setPositiveButton("Sim") { _, _ ->
                        session.logout()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .setNegativeButton("Não", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
