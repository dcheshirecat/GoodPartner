# Good Partner - ProGuard Rules
# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes JavascriptInterface
-keepattributes *Annotation*

# Keep our app classes
-keep class com.goodpartner.app.** { *; }

# AndroidX
-keep class androidx.** { *; }
-dontwarn androidx.**
