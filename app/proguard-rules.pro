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

# Keep line numbers so the (deobfuscated) crash reports in Play Console point to the right line,
# but hide the original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------------------------------------------------------------------------------------------
# GTFS-Realtime bindings (NewApiInterface/GTFSRProvider)
# The full protobuf runtime looks up the generated getters/builders by name via reflection
# (field accessor tables, descriptors), so the generated classes must keep their members.
-keep class com.google.transit.realtime.** { *; }
-dontwarn com.google.protobuf.**

# ---------------------------------------------------------------------------------------------
# Model classes passed between fragments as Serializable in Bundles
# (IncomingBusRespModel[], BusStops[], BusPlaces[] ...): keep the serialization hooks.
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ---------------------------------------------------------------------------------------------
# WorkManager 2.7 + Room 2.2.5 (pulled in by play-services-ads) instantiate these classes
# reflectively through their no-arg constructor. Their own rules keep only the class, and R8 full
# mode (default since AGP 8) then removes the constructor -> crash at startup:
# "Failed to create an instance of androidx.work.impl.WorkDatabase".
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class * extends androidx.work.InputMerger { <init>(); }
