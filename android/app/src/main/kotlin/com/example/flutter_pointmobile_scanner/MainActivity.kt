package com.example.flutter_pointmobile_scanner

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private lateinit var channel: MethodChannel
    private lateinit var scannerHelper: ScannerManager

    private val SCANNER_CHANNEL = "scanner_channel"
    private val SCAN_BUTTON_KEY_CODE = 1011 // Substitui pelo teu valor real, se mudar

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, SCANNER_CHANNEL)
        scannerHelper = ScannerManager(this, channel)

        scannerHelper.initScanner() // agora separado

        scannerHelper.setTriggerMode(0) // Trigger por botão físico
        scannerHelper.setBeepEnabled(1) // Ativar beep após leitura

        Log.d("MainActivity", "Flutter Engine configurado e Scanner inicializado")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Log.d("ScannerManager", "onKeyDown - KeyCode: $keyCode")

        if (keyCode == SCAN_BUTTON_KEY_CODE) {
            Log.d("ScannerManager", "Botão físico do scanner pressionado — iniciar leitura")
            scannerHelper.startScanAndReturn()
            return true
        }

        return super.onKeyDown(keyCode, event)
    }
}
