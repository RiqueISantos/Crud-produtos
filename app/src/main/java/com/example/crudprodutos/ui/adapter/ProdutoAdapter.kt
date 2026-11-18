package com.example.crudprodutos.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.crudprodutos.data.model.ProdutoApi
import com.example.crudprodutos.databinding.ItemProdutoBinding

class ProdutoAdapter(
    private val onEditar: (ProdutoApi) -> Unit,
    private val onDeletar: (ProdutoApi) -> Unit
) : ListAdapter<ProdutoApi, ProdutoAdapter.ProdutoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProdutoViewHolder(
        private val binding: ItemProdutoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: ProdutoApi) {
            binding.tvNome.text = produto.nome
            binding.tvDescricao.text = produto.descricao
            binding.tvPreco.text = "R$ %.2f".format(produto.preco)
            binding.tvCategoria.text = if (produto.disponivel) "Disponível" else "Indisponível"
            binding.tvQuantidade.text = "ID: ${produto.id}"

            binding.btnEditar.setOnClickListener { onEditar(produto) }
            binding.btnDeletar.setOnClickListener { onDeletar(produto) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProdutoApi>() {
            override fun areItemsTheSame(oldItem: ProdutoApi, newItem: ProdutoApi) =
                oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: ProdutoApi, newItem: ProdutoApi) =
                oldItem == newItem
        }
    }
}
