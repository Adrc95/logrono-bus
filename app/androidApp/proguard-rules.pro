# ── Ofuscación agresiva ─────────────────────────────────────────────────────
# Everything that is not needed by reflection gets renamed and repackaged, which
# is what makes the APK hard to read. The keeps below are only the ones a library
# genuinely needs at runtime; anything broader only helps whoever tries to
# reverse the app.
-allowaccessmodification
-repackageclasses ''
-dontusemixedcaseclassnames
-keepattributes *Annotation*,AnnotationDefault,Signature,SourceFile,LineNumberTable

# ── kotlinx.serialization ───────────────────────────────────────────────────
# The operator's responses are deserialized through generated serializers that
# kotlinx.serialization looks up reflectively; these are the rules from its own
# documentation.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# ── Navigation keys ─────────────────────────────────────────────────────────
# These keys are serialized without @SerialName, so their property names are the
# contract used to restore the back stack after the process is killed.
-keepclassmembers @kotlinx.serialization.Serializable class *NavKey { <fields>; }
-keepclassmembers class dev.adrc95.logronobus.core.navigation.ShellRoute { <fields>; }

# ── Enums persisted by name (fares, theme) ──────────────────────────────────
# Room stores fare categories as their constant name and the theme preference is
# saved the same way; both are read back with valueOf(). Renaming the constants
# would turn a stored "JUBILADO" into an unknown value, so only the enum fields
# of this app's enums are kept — not the classes themselves.
-keepclassmembers enum dev.adrc95.logronobus.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Room ────────────────────────────────────────────────────────────────────
# Only the database class has to keep its name (its generated _Impl is looked up
# as "<name>_Impl"); entities and their properties are safe to rename because
# every column carries an explicit @ColumnInfo name.
-keep @androidx.room.Database class * { *; }

# ── Koin annotations ────────────────────────────────────────────────────────
# The generated module classes are referenced from code, but the annotations
# themselves are read while the modules are built.
-keepclassmembers class * {
    @org.koin.core.annotation.* <methods>;
}

# ── MapLibre (JNI) ──────────────────────────────────────────────────────────
# Native code calls back into these members by name.
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class org.maplibre.nativeffi.** { <methods>; }

# ── ML Kit text recognition (camera "Scan" tab) ─────────────────────────────
# ML Kit wires its components through a generated dependency graph that resolves
# classes by name at runtime. With anything less than a full keep, R8 renames or
# removes those classes and the app dies on start-up inside MlKitInitProvider
# ("Unsatisfied dependency for component ... vision.text.internal.zzo").
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text.** { *; }
-keep class com.google.android.gms.internal.mlkit_common.** { *; }
-dontwarn com.google.mlkit.**

# ── Optional transitive deps that are not shipped ───────────────────────────
-dontwarn org.slf4j.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.apache.**
-dontwarn org.eclipse.**
-dontwarn org.checkerframework.**
-dontwarn javax.**
-dontwarn java.lang.instrument.**
