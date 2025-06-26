package com.example.flutter_pointmobile_scanner

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import device.common.DecodeResult
import device.sdk.ScanManager
import io.flutter.plugin.common.MethodChannel

/**
 * Classe de apoio com explicações sobre os principais métodos do SDK PointMobile Scanner
 */
class ScannerManager(private val context: Context, private val methodChannel: MethodChannel) {

    private var scanManager: ScanManager? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun initScanner() {
        Thread {
            try {
                scanManager = ScanManager.getInstance()
                scanManager?.aDecodeAPIInit()
                Log.d("ScannerManager", "ScanManager inicializado com sucesso")
            } catch (e: Exception) {
                Log.e("ScannerManager", "Erro ao iniciar ScanManager", e)
            }
        }.start()
    }





    /**
     * Define o modo de trigger (botão físico, software, contínuo)
     * @param mode 0 = modo normal (hardware), 1 = modo contínuo, 2 = via software
     */
    fun setTriggerMode(mode: Int) {
        try {
            scanManager?.aDecodeSetTriggerMode(mode)
        } catch (e: Exception) {
            Log.e("ScannerHelper", "Erro ao definir trigger mode", e)
        }
    }

    /**
     * Ativa ou desativa o beep sonoro após leitura
     * @param enabled 1 para ativar, 0 para desativar
     */
    fun setBeepEnabled(enabled: Int) {
        try {
            scanManager?.aDecodeSetBeepEnable(enabled)
        } catch (e: Exception) {
            Log.e("ScannerHelper", "Erro ao definir beep", e)
        }
    }

    /**
     * Inicia uma leitura (modo software) e lê o resultado
     */
    fun startScanAndReturn() {
        try {
            scanManager?.aDecodeSetTriggerOn(1)
            scanManager?.aDecodeSetResultSymIdEnable(1)
            mainHandler.postDelayed({
                val result = getScanResult()
                result?.let {
                   

                    if (it.equals("READ_FAIL", ignoreCase = true)) {
                        Log.w("ScannerHelper", "Leitura falhou - a parar scanner.")
                        scanManager?.aDecodeSetTriggerOn(0) // Para a leitura ativa
                        return@postDelayed
                    }

                    Log.d("ScannerHelper", "Código lido: $it")
                    // Lê se a opção do SymId está ativa (só para debug/log)
                    val symIdStatus = scanManager?.aDecodeGetResultSymIdEnable()
                    Log.d("ScannerConfig", "Symbology ID Enable: $symIdStatus")

                    val symid = scanManager?.aDecodeSymGetSymId(1);
                    Log.d("ScannerConfig", "symid: $symid")
                    
                    methodChannel.invokeMethod("onBarcodeScanned", it)
                } ?: run {
                Log.w("ScannerHelper", "Leitura falhou ou sem dados")
                scanManager?.aDecodeSetTriggerOn(0) // Falhou, parar leitura
            }
            }, 500) // espera 500ms antes de tentar obter resultado
        } catch (e: Exception) {
            Log.e("ScannerHelper", "Erro ao iniciar leitura e devolver resultado", e)
        }
    }

    /**
     * Termina uma leitura ativa
     */
    fun stopScan() {
        try {
            scanManager?.aDecodeSetTriggerOn(0)
        } catch (e: Exception) {
            Log.e("ScannerHelper", "Erro ao parar scan", e)
        }
    }

    /**
     * Obtém o resultado da leitura do buffer
     * @return o código lido como String, ou null se falhar
     */
    fun getScanResult(): String? {
        return try {
            val result = DecodeResult()
            scanManager?.aDecodeGetResult(result)
            if (result.decodeLength > 0) {
                val decodedStr = result.toString("UTF-8")

                Log.d("ScanResult", "Código lido: $decodedStr")
                Log.d("ScanResult", "Symbologia (symName): ${result.symName}")
                Log.d("ScanResult", "SymID (symId): ${result.symId.toInt().toChar()}")
                Log.d("ScanResult", "SymType: ${result.symType}")
                Log.d("ScanResult", "Letter: ${result.letter.toInt().toChar()}")
                Log.d("ScanResult", "Modifier: ${result.modifier}")
                Log.d("ScanResult", "Tempo decodificação: ${result.decodeTimeMillisecond} ms")
            } else {
                Log.w("ScanResult", "Leitura sem dados")
            }
            result.toString("UTF-8")
        } catch (e: Exception) {
            Log.e("ScannerHelper", "Erro ao obter resultado do scan", e)
            null
        }
    }
    
    /**
     * Ativa ou desativa o trigger manualmente.
     * @param enabled true para trigger ON, false para trigger OFF
     */
    fun setTriggerEnabled(enabled: Boolean) {
        try {
            // aDecodeSetTriggerEnable espera 1 para ligar, 0 para desligar
            scanManager?.aDecodeSetTriggerEnable(if (enabled) 1 else 0)
            Log.d("ScannerHelper", "Trigger set to: ${if (enabled) "ON" else "OFF"}")
        } catch (e: Exception) {
            Log.e("ScannerHelper", "Erro ao definir trigger enable", e)
        }
    }

    /**
     * Retorna o estado atual do trigger
     * @return true se trigger estiver ativo, false caso contrário ou erro
     */
    fun isTriggerEnabled(): Boolean {
        return try {
            val status = scanManager?.aDecodeGetTriggerEnable() ?: 0
            Log.d("ScannerHelper", "Trigger status: $status")
            status == 1
        } catch (e: Exception) {
            Log.e("ScannerHelper", "Erro ao obter status do trigger", e)
            false
        }
    }
}

/**
 * Códigos de barras suportados (dependente da configuração do hardware):
 *  - EAN-8
 *  - EAN-13
 *  - GS1-128 (subset do Code128)
 *  - ITF (Interleaved 2 of 5)
 *  - Code39
 *  - QRCode
 *
 * Nota: Algumas simbologias podem requerer ativação via configuração adicional.
 */
