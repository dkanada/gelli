-dontwarn
-ignorewarnings

-keep class com.dkanada.gramophone.**.* { *; } # Keep all Gelli classes and attributes
-keepnames class **.* { *; } # Keep class and attribute names
-keepattributes SourceFile,LineNumberTable # Keep file names/line numbers

# Jellyfin API
-keepclasseswithmembers class org.jellyfin.apiclient.model.**.* { *; }

# Retrofit
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
