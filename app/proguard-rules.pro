-keepattributes JavascriptInterface
-keepclassmembers class com.xiaomi.aistudio.webview.WebAppInterface {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class android.webkit.** { *; }
