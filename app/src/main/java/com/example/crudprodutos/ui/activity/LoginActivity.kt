package com.example.crudprodutos.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.crudprodutos.data.model.LoginSolicitarRequest
import com.example.crudprodutos.data.remote.RetrofitClient
import com.example.crudprodutos.data.session.SessionManager
import com.example.crudprodutos.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        if (session.isLogado()) {
            irParaMain()
            return
        }

        binding.layoutCodigo.visibility = View.GONE
        binding.btnEnviarCodigo.text = "Entrar"
        binding.btnEnviarCodigo.setOnClickListener { login() }
    }

    private fun login() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.etEmail.error = "Informe o e-mail do restaurante"
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.loginDevBypass(LoginSolicitarRequest(email))
                setLoading(false)
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    session.salvarSessao(
                        token = body.accessToken,
                        restauranteId = body.restaurante.id,
                        nome = body.restaurante.nome,
                        email = body.restaurante.email
                    )
                    irParaMain()
                } else {
                    toast("Restaurante não encontrado. Verifique o e-mail.")
                }
            } catch (e: Exception) {
                setLoading(false)
                toast("Erro de conexão. Verifique se o backend está rodando.")
            }
        }
    }

    private fun irParaMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnEnviarCodigo.isEnabled = !loading
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
