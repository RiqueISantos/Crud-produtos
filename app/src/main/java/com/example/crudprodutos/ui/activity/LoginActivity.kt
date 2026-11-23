package com.example.crudprodutos.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.crudprodutos.data.model.LoginRequest
import com.example.crudprodutos.data.model.LoginSolicitarRequest
import com.example.crudprodutos.data.remote.RetrofitClient
import com.example.crudprodutos.data.session.SessionManager
import com.example.crudprodutos.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private var emailAtual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        if (session.isLogado()) {
            irParaMain()
            return
        }

        // Etapa 1: Solicitar envio do código OTP
        binding.btnEnviarCodigo.setOnClickListener { solicitarCodigo() }

        // Etapa 2: Validar código OTP e entrar
        binding.btnEntrar.setOnClickListener { validarCodigo() }

        // Voltar para trocar o e-mail
        binding.btnVoltarEmail.setOnClickListener { voltarParaEmail() }

        // Acesso direto do cliente ao leitor de QR Code
        binding.btnSouCliente.setOnClickListener {
            startActivity(Intent(this, QrScannerActivity::class.java))
        }
    }

    private fun solicitarCodigo() {
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.etEmail.error = "Informe o e-mail do restaurante"
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.loginSolicitar(LoginSolicitarRequest(email))
                setLoading(false)

                if (response.isSuccessful) {
                    emailAtual = email
                    binding.tvCodigoInfo.text = "Código enviado para $email. Verifique sua caixa de entrada."
                    binding.layoutEmail.visibility = View.GONE
                    binding.layoutCodigo.visibility = View.VISIBLE
                } else {
                    toast("Restaurante não encontrado. Verifique o e-mail.")
                }
            } catch (e: Exception) {
                setLoading(false)
                toast("Erro de conexão. Verifique se o backend está rodando.")
            }
        }
    }

    private fun validarCodigo() {
        val codigo = binding.etCodigo.text.toString().trim()
        if (codigo.length < 6) {
            binding.etCodigo.error = "Informe o código de 6 dígitos"
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.login(LoginRequest(emailAtual, codigo))
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
                    toast("Código inválido ou expirado.")
                }
            } catch (e: Exception) {
                setLoading(false)
                toast("Erro de conexão ao validar o código.")
            }
        }
    }

    private fun voltarParaEmail() {
        binding.etCodigo.text?.clear()
        binding.layoutCodigo.visibility = View.GONE
        binding.layoutEmail.visibility = View.VISIBLE
    }

    private fun irParaMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnEnviarCodigo.isEnabled = !loading
        binding.btnEntrar.isEnabled = !loading
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}