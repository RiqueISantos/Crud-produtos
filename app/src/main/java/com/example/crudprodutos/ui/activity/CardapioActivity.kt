package com.example.crudprodutos.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.crudprodutos.data.remote.RetrofitClient
import com.example.crudprodutos.databinding.ActivityCardapioBinding
import com.example.crudprodutos.ui.adapter.CardapioAdapter
import kotlinx.coroutines.launch

class CardapioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardapioBinding

    private val restauranteId: Long get() = intent.getLongExtra(EXTRA_RESTAURANTE_ID, -1L)
    private val qrValor: String get() = intent.getStringExtra(EXTRA_QR_VALOR) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardapioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Cardápio"

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        carregarCardapio()
    }

    private fun carregarCardapio() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.listarProdutos(restauranteId)
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val produtos = response.body()?.filter { it.disponivel } ?: emptyList()

                    if (produtos.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        // Atualiza o título com nome do restaurante se vier na URL
                        supportActionBar?.title = "Cardápio"
                        binding.recyclerView.adapter = CardapioAdapter(produtos)
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@CardapioActivity,
                        "Restaurante não encontrado.", Toast.LENGTH_LONG).show()
                    finish()
                }
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this@CardapioActivity,
                    "Erro de conexão.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_RESTAURANTE_ID = "extra_restaurante_id"
        const val EXTRA_QR_VALOR = "extra_qr_valor"
    }
}
