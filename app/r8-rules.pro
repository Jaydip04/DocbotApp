# Keep rules for R8

# Keep the Error Prone annotations
-keep class com.google.errorprone.** { *; }
-keep @com.google.errorprone.annotations.** class * {*;}
-keepclassmembers class * {
    @com.google.errorprone.annotations.** *;
}

# Keep the javax.lang.model classes and its implementations
-keep class javax.lang.model.** { *; }
-keep class com.sun.** { *; }
-keep class javax.annotation.** { *; }

# Handle duplicate class issues
-dontnote javax.annotation.**

# Keep AAR resources and native libraries
-keep class **.R
-keep class **.R$* {
    public static <fields>;
}

# Keep native methods and their classes
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep PyTorch related classes
-keep class org.pytorch.** { *; }

# Keep all your model classes
-keep class com.docbot.docbotkt.** { *; }

# Keep MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# iText PDF
-keep class com.itextpdf.** { *; }

# Don't warn about jsr305 or other common annotations
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
-dontwarn com.google.errorprone.**
-dontwarn org.glassfish.**