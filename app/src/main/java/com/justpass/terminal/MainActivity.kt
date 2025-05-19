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
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var pendingRequest: PermissionRequest? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    
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
            override fun onPermissionRequest(request: PermissionRequest) {
                val wantsCamera = 
                    request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                    
                if (!wantsCamera) {
                    request.deny()
                    return
                }
                
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Grant immediately: lets getUserMedia() resolve inside JS
                    request.grant(request.resources)
                } else {
                    // Ask the Android-side runtime permission first
                    pendingRequest = request
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_REQUEST_CODE
                    )
                }
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
    
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            val ok = grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED
            pendingRequest?.let { req ->
                if (ok) req.grant(req.resources) else req.deny()
                pendingRequest = null
            }
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