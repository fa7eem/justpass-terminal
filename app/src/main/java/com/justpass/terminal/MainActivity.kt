package com.justpass.terminal

import android.content.Intent
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.JavascriptInterface
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    
    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val qrCode = result.data?.getStringExtra(QRScannerActivity.RESULT_QR_CODE)
            qrCode?.let {
                // Send QR code back to the WebView
                webView.evaluateJavascript("handleQRCode('$it')", null)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        webView = findViewById(R.id.webView)
        setupWebView()
        
        // Load JustPass management login page
        webView.loadUrl("https://justpass.app/management/login")
    }
    
    private fun setupWebView() {
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                // Only allow navigation within justpass.app domain
                if (url != null && url.startsWith("https://justpass.app/")) {
                    view?.loadUrl(url)
                    return true
                }
                return true // Block navigation outside justpass.app
            }
            
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                // In production, you should handle SSL errors properly
                // For now, we'll proceed (not recommended for production)
                handler?.proceed()
            }
        }
        
        webView.webChromeClient = WebChromeClient()
        
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.setSupportZoom(true)
        webSettings.defaultTextEncodingName = "utf-8"
        
        // Add JavaScript interface for QR scanning
        webView.addJavascriptInterface(WebViewJavaScriptInterface(), "AndroidApp")
    }
    
    inner class WebViewJavaScriptInterface {
        @JavascriptInterface
        fun scanQRCode() {
            // Launch QR scanner activity
            val intent = Intent(this@MainActivity, QRScannerActivity::class.java)
            qrScannerLauncher.launch(intent)
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle back button for WebView navigation
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}