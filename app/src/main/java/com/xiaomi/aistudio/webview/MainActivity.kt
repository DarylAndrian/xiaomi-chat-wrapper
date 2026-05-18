package com.xiaomi.aistudio.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xiaomi.aistudio.webview.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private val targetUrl = "https://aistudio.xiaomimimo.com"
    private val baseUrl = "aistudio.xiaomimimo.com"

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                if (System.currentTimeMillis() - lastBackPressTime < 2000) {
                    finishAffinity()
                } else {
                    lastBackPressTime = System.currentTimeMillis()
                    Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private var lastBackPressTime = 0L

    // Swipe gesture detection for sidebar
    private lateinit var gestureDetector: GestureDetector
    private var isHorizontalSwipe = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply window insets for proper padding around system bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        webView = binding.webView
        progressBar = binding.progressBar
        swipeRefresh = binding.swipeRefresh

        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        setupSwipeGesture()
        setupWebView()
        setupSwipeRefresh()

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState)
        } else {
            loadUrl(targetUrl)
        }
    }

    private fun setupSwipeGesture() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y

                // Only detect horizontal swipes (ignore vertical)
                if (abs(diffX) > abs(diffY) &&
                    abs(diffX) > SWIPE_THRESHOLD &&
                    abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (diffX > 0) {
                        // Swipe Right - Open sidebar
                        webView.evaluateJavascript(
                            "javascript:try{document.querySelector('[data-testid=\"sidebar-toggle\"],.sidebar-toggle,#sidebar-toggle,.menu-toggle,[aria-label=\"Menu\"],[aria-label=\"Toggle Sidebar\"]')?.click()}catch(e){}",
                            null
                        )
                    } else {
                        // Swipe Left - Close sidebar
                        webView.evaluateJavascript(
                            "javascript:try{document.querySelector('[data-testid=\"sidebar-close\"],.sidebar-close,#sidebar-close,.menu-close')?.click();document.querySelector('.sidebar-overlay,.drawer-overlay,.backdrop')?.click()}catch(e){}",
                            null
                        )
                    }
                    return true
                }
                return false
            }
        })

        // Set touch listener on WebView to detect swipes
        webView.setOnTouchListener { view, event ->
            // Let gestureDetector handle touch events for swipe detection
            val handled = gestureDetector.onTouchEvent(event)

            // Track if horizontal swipe is happening to disable SwipeRefreshLayout
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isHorizontalSwipe = false
                    // Only allow SwipeRefreshLayout if touch starts in the top 20% of the screen
                    val topThreshold = view.height * 0.2f
                    swipeRefresh.isEnabled = event.y <= topThreshold
                }
                MotionEvent.ACTION_MOVE -> {
                    // If significant horizontal movement detected, disable pull-to-refresh
                    if (!isHorizontalSwipe && gestureDetector.onTouchEvent(event)) {
                        isHorizontalSwipe = true
                    }
                }
            }

            // If horizontal swipe detected, disable SwipeRefreshLayout
            if (isHorizontalSwipe) {
                swipeRefresh.isEnabled = false
            }

            // Re-enable SwipeRefreshLayout when touch ends
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                isHorizontalSwipe = false
                swipeRefresh.isEnabled = true
            }

            false // Let WebView still receive the event for normal scrolling
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            defaultTextEncodingName = "UTF-8"
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = true
            // Remove "; wv" to avoid being detected as a restricted WebView by some services
            // However, for Google Login, it's often better to use a standard mobile UA
            userAgentString = userAgentString.replace("; wv", "")
            // Ensure cookie manager is enabled for sessions
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        }

        webView.apply {
            scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
            isScrollbarFadingEnabled = true
            webViewClient = AiStudioWebViewClient()
            webChromeClient = AiStudioChromeClient()
            addJavascriptInterface(WebAppInterface(this@MainActivity), "AndroidBridge")
            isLongClickable = true
            setOnLongClickListener { true }
            setDownloadListener { url, _, _, _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.apply {
            setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light
            )
            setProgressBackgroundColorSchemeResource(android.R.color.white)
            setOnRefreshListener { webView.reload() }
        }
    }

    private fun loadUrl(url: String) {
        if (isNetworkAvailable()) {
            webView.loadUrl(url)
        } else {
            showOfflinePage()
        }
    }

    private fun showOfflinePage() {
        val html = """
            <html><head><meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
                body{font-family:sans-serif;display:flex;justify-content:center;align-items:center;
                min-height:100vh;background:#f5f5f5;margin:0}
                .c{text-align:center;padding:2rem}
                .i{font-size:4rem}h1{font-size:1.5rem;margin:.5rem 0}
                p{color:#666;margin-bottom:2rem}
                button{background:#FF6900;color:#fff;border:none;padding:12px 32px;
                border-radius:8px;font-size:1rem;cursor:pointer}
            </style></head><body><div class="c">
                <div class="i">&#128225;</div>
                <h1>No Internet Connection</h1>
                <p>Please check your network and try again.</p>
                <button onclick="location.reload()">Retry</button>
            </div></body></html>
        """.trimIndent()
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private inner class AiStudioWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            
            // Allow internal domains and authentication domains to load within the WebView
            if (url.contains(baseUrl) || 
                url.contains("xiaomimimo.com") || 
                url.contains("google.com/accounts") || 
                url.contains("accounts.google.com") ||
                url.contains("xiaomi.com") ||
                url.contains("mi.com")) {
                return false
            }
            
            try { 
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) 
            } catch (_: Exception) { 
                Toast.makeText(this@MainActivity, "Cannot open link", Toast.LENGTH_SHORT).show() 
            }
            return true
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (request?.isForMainFrame == true) showOfflinePage()
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: android.net.http.SslError?) {
            handler?.cancel()
            Toast.makeText(this@MainActivity, "SSL Error: connection not secure", Toast.LENGTH_SHORT).show()
        }
    }

    private inner class AiStudioChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar.progress = newProgress
            progressBar.visibility = if (newProgress == 100) View.GONE else View.VISIBLE
        }

        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?): Boolean {
            fileUploadCallback?.onReceiveValue(null)
            fileUploadCallback = filePathCallback
            try {
                startActivityForResult(Intent.createChooser(fileChooserParams?.createIntent(), "Choose file"), 1001)
            } catch (_: Exception) { fileUploadCallback = null; return false }
            return true
        }

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            val nw = WebView(this@MainActivity)
            nw.webViewClient = AiStudioWebViewClient()
            nw.webChromeClient = AiStudioChromeClient()
            (resultMsg?.obj as? WebView.WebViewTransport)?.webView = nw
            resultMsg?.sendToTarget()
            return true
        }
    }

    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001) {
            val result = if (resultCode == RESULT_OK && data != null) data.data?.let { arrayOf(it) } else null
            fileUploadCallback?.onReceiveValue(result)
            fileUploadCallback = null
        }
    }

    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); webView.saveState(outState) }
    override fun onResume() { super.onResume(); webView.onResume() }
    override fun onPause() { webView.onPause(); super.onPause() }

    override fun onDestroy() {
        webView.apply {
            stopLoading(); webViewClient = WebViewClient(); loadUrl("about:blank")
            removeAllViews(); (parent as? ViewGroup)?.removeView(this); destroy()
        }
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) { webView.reload(); return true }
        return super.onKeyDown(keyCode, event)
    }
}