# ProGuard/R8 rules for Global Audit app

# Keep annotations and signatures required by Retrofit/Moshi/AppAuth
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*

# Kotlin metadata
-keep class kotlin.Metadata { *; }

# ---- Retrofit / OkHttp / Okio ----
# Keep Retrofit core (interfaces and generated code)
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep OkHttp/Okio to avoid stripping required internals
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ---- Moshi JSON ----
# Keep Moshi core and adapters
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# If using Moshi code gen with @JsonClass(generateAdapter = true), keep those models
-keep @com.squareup.moshi.JsonClass class ** { *; }

# Keep fields annotated with @Json so Moshi can access them
-keepclassmembers class ** {
    @com.squareup.moshi.Json <fields>;
}

# ---- AppAuth (OIDC/OAuth2) ----
-keep class net.openid.appauth.** { *; }
-dontwarn net.openid.appauth.**

# ---- JWT decoder ----
-keep class com.auth0.android.jwt.** { *; }

# ---- AndroidX Security Crypto (EncryptedSharedPreferences) ----
-keep class androidx.security.** { *; }
-dontwarn androidx.security.**

# ---- General Android keeps (optional, safe) ----
# Keep enum values to avoid reflection issues
-keepclassmembers enum * { **[] values(); ** valueOf(java.lang.String); }

# You can uncomment for debugging ProGuard issues
# -whyareyoukeeping class com.yourpackage.**

# ---- Firebase Crashlytics ----
# Keep source file and line number info for better deobfuscation in Crashlytics
-keepattributes SourceFile,LineNumberTable

# Keep custom exception types (helps retain meaningful class names for exceptions)
-keep public class * extends java.lang.Exception
