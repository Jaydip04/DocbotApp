# ---------------------------
# Android WebView JS Interface
# ---------------------------
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# ---------------------------
# Useful Debugging Options
# ---------------------------
-keepattributes SourceFile, LineNumberTable
-keepattributes Exceptions, Signature, InnerClasses
#-renamesourcefileattribute SourceFile

# ---------------------------
# Suppress Warnings from Libraries
# ---------------------------
-dontwarn com.healthcubed.**
-dontwarn org.tukaani.xz.**
-dontwarn org.apache.commons.compress.**
-dontwarn com.itextpdf.**
-dontwarn org.pytorch.**
-dontwarn com.facebook.**
-dontwarn com.scichart.**

# ---------------------------
# Keep Core Library Classes
# ---------------------------
-keep class com.healthcubed.** { *; }
-keep class org.pytorch.Module { *; }
-keep class org.pytorch.Module.mNativePeer** { *; }
-keep class org.pytorch.** { *; }
-keep class com.facebook.jni.** { *; }
-keep class com.facebook.soloader.** { *; }
-keep class com.facebook.** { *; }
-keep class com.scichart.** { *; }

# iTextPDF - If you're using this for PDF generation
-keep class com.itextpdf.** { *; }

# Commons Compress (zip/xz support)
-keep class org.tukaani.xz.** { *; }
-keep class org.apache.commons.compress.** { *; }

# ---------------------------
# AWT (required by iText)
# ---------------------------
-keep class java.awt.** { *; }
-keep class java.awt.font.** { *; }
-keep class java.awt.geom.** { *; }
-keep class java.awt.image.** { *; }

# ---------------------------
# Optional R8 Tweaks
# ---------------------------
# Disable shrinking only if you want to be very safe
-dontshrink

# If using native libraries (JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep ErrorProne Annotations
-keep class javax.lang.model.** { *; }
-keep class com.google.errorprone.annotations.** { *; }
