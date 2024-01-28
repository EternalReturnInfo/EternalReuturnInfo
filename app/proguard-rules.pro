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

-dontwarn okhttp3.internal.platform.** -dontwarn org.conscrypt.** -dontwarn org.bouncycastle.** -dontwarn org.openjsse.**

-dontwarn com.google.protobuf.java_com_google_android_gmscore_sdk_target_granule__proguard_group_gtm_N1281923064GeneratedExtensionRegistryLite**

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation



# Add this global rule
-keepattributes Signature

# This rule will properly ProGuard all the model classes in
# the package com.irionna.eternalreturninfo.models.
-keepclassmembers class com.irionna.eternalreturninfo.data.models.** {
  *;
}
-keepclassmembers class com.irionna.eternalreturninfo.models.** {
  *;
}
-keepclassmembers class com.irionna.data.models.** {
  *;
}
-keepclassmembers class com.irionna.models.** {
  *;
}


-keep class com.irionna.eternalreturninfo.model.** { *; }

-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**