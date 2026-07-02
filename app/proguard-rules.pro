# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Security hardening for release builds (now that isMinifyEnabled = true) ---

# Strip verbose/debug logging so purchase/auth flow details aren't left in
# the shipped binary's logcat output.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Google Play Billing library ships its own consumer rules, but keep the
# public API surface explicitly to avoid obfuscation breaking the purchase
# callback contract.
-keep class com.android.billingclient.api.** { *; }

# Moshi / Retrofit models used for any future backend calls (auth refresh,
# dynamic-QR redirect service) — keep field names so JSON (de)serialization
# isn't broken by obfuscation.
-keepclassmembers class com.example.** {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
