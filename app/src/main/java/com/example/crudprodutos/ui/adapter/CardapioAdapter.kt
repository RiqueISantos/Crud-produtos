package com.example.crudprodutos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.crudprodutos.data.model.ProdutoApi
import com.example.crudprodutos.databinding.ItemCardapioBinding

class CardapioAdapter(
    private val produtos: List<ProdutoApi>
) : RecyclerView.Adapter<CardapioAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardapioBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(produtos[position])
    }

    override fun getItemCount() = produtos.size

    inner class ViewHolder(private val binding: ItemCardapioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: ProdutoApi) {
            binding.tvNome.text = produto.nome
            binding.tvDescricao.text = produto.descricao
            binding.tvPreco.text = "R$ %.2f".format(produto.preco)
            binding.tvDisponivel.text = if (produto.disponivel) "Disponível" else "Indisponível"
            binding.tvDisponivel.setBackgroundColor(
                if (produto.disponivel) 0xFF4CAF50.toInt() else 0xFFBDBDBD.toInt()
            )
        }
    }
}
