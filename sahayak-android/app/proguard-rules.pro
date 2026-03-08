# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class com.sahayak.android.data.model.** { *; }
-keepclassmembers class com.sahayak.android.data.model.** { *; }
-dontwarn okhttp3.**
-dontwarn retrofit2.**
