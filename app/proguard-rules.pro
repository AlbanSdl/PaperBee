# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class fr.asdl.minder.**$$serializer { *; }
#noinspection ShrinkerUnresolvedReference
-keepclassmembers class fr.asdl.minder.** {
    *** Companion;
}
#noinspection ShrinkerUnresolvedReference
-keepclasseswithmembers class fr.asdl.minder.** {
    kotlinx.serialization.KSerializer serializer(...);
}