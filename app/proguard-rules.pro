# Hilt
-keep class com.repasdelaflemme.app.MainApplication
-keep class dagger.hilt.internal.aggregatedroot.codegen.*
-keep class hilt_aggregated_deps.*
-keep class com.repasdelaflemme.app.HiltTestRunner

# Room
-keep class androidx.room.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static final java.lang.String NAME;
    public static final java.util.Map<java.lang.Class<? extends androidx.room.migration.Migration>, java.lang.Integer> MIGRATIONS;
}
-keepclassmembers class com.repasdelaflemme.app.data.local.entity.** { *; }

# Gson
-keep class com.repasdelaflemme.app.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$ImageType
