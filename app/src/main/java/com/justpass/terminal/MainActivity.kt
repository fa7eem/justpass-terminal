package com.justpass.terminal

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.PermissionRequest

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    
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
        
        // Set up WebChromeClient to handle camera permissions
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                // Grant camera permissions when requested by the web page
                request?.grant(request.resources)
            }
        }
        
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.builtInZoomControls = true
        webSettings.displayZoomControls = false
        webSettings.setSupportZoom(true)
        webSettings.defaultTextEncodingName = "utf-8"
        webSettings.mediaPlaybackRequiresUserGesture = false
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