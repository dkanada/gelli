-dontwarn
-ignorewarnings

-keep class com.dkanada.gramophone.**.* { *; }
-keepnames class **.* { *; }
-keepattributes SourceFile
-keepattributes LineNumberTable

# Jellyfin API
-keepclasseswithmembers class org.jellyfin.apiclient.model.**.* { *; }

# RetroFit
-dontwarn retrofit.**
-keep class retrofit.**.* { *; }
-keepattributes Signature
-keepattributes Exceptions

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
