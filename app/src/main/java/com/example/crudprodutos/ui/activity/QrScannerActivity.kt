package com.example.crudprodutos.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.crudprodutos.databinding.ActivityQrScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var jaLeu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnVoltar.setOnClickListener { finish() }
        binding.btnTestarManual.setOnClickListener { mostrarDialogTeste() }

        if (temPermissaoCamera()) {
            iniciarCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    private fun temPermissaoCamera() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            iniciarCamera()
        } else {
            Toast.makeText(this, "Permissão de câmera negada.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun iniciarCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(cameraExecutor, ::analisarFrame) }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Erro ao iniciar câmera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun analisarFrame(imageProxy: ImageProxy) {
        if (jaLeu) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.format == Barcode.FORMAT_QR_CODE) {
                        val valor = barcode.rawValue ?: continue
                        jaLeu = true
                        processarQrCode(valor)
                        break
                    }
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    private fun processarQrCode(valor: String) {
        // Aceita dois formatos:
        // 1. Só o ID numérico:  "1"
        // 2. URL com parâmetro: "http://...?restaurante_id=1"
        val restauranteId = extrairRestauranteId(valor)

        runOnUiThread {
            if (restauranteId != null) {
                val intent = Intent(this, CardapioActivity::class.java)
                intent.putExtra(CardapioActivity.EXTRA_RESTAURANTE_ID, restauranteId)
                intent.putExtra(CardapioActivity.EXTRA_QR_VALOR, valor)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "QR code inválido. Use um QR do restaurante.",
                    Toast.LENGTH_LONG
                ).show()
                jaLeu = false // permite tentar outro QR
            }
        }
    }

    private fun extrairRestauranteId(valor: String): Long? {
        // Formato 1: só número — "1"
        valor.trim().toLongOrNull()?.let { return it }

        // Formato 2: deep link — "fiood://restaurante/1"
        if (valor.startsWith("fiood://restaurante/")) {
            return valor.removePrefix("fiood://restaurante/").trim().toLongOrNull()
        }

        // Formato 3: URL com query param — "http://...?restaurante_id=1"
        return try {
            val uri = android.net.Uri.parse(valor)
            uri.getQueryParameter("restaurante_id")?.toLongOrNull()
                ?: uri.lastPathSegment?.toLongOrNull()
        } catch (e: Exception) {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun mostrarDialogTeste() {
        val input = android.widget.EditText(this).apply {
            hint = "ID do restaurante (ex: 1)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(48, 32, 48, 32)
        }
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Simular leitura de QR")
            .setMessage("Digite o ID do restaurante:")
            .setView(input)
            .setPositiveButton("Abrir cardápio") { _, _ ->
                val id = input.text.toString().trim()
                if (id.isNotEmpty()) processarQrCode(id)
                else Toast.makeText(this, "Digite um ID válido", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
