package com.xiaomi.aistudio.webview

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast

class WebAppInterface(private val context: Context) {

    @JavascriptInterface
    fun getVersion(): String = BuildConfig.VERSION_NAME

    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun isNativeApp(): Boolean = true

    @JavascriptInterface
    fun openSidebar() {
        // Called from JS to request sidebar opening
        // The web app should handle its own sidebar open logic
    }

    @JavascriptInterface
    fun closeSidebar() {
        // Called from JS to request sidebar closing
        // The web app should handle its own sidebar close logic
    }
}
