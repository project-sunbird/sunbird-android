# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/amankasliwal/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepparameternames
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod


-keepnames class ** {  }

-keep class in.juspay.mystique.JsInterface {
    public *;
}

-keepclassmembers class in.juspay.mystique.JsInterface {
   public *;
}

-keepclassmembernames class * {
    java.lang.Class class$(java.lang.String);
    java.lang.Class class$(java.lang.String, boolean);
}

-keepclasseswithmembernames class * {
    native <methods>;
}


-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class in.juspay.mystique.DefaultLogger {
    public *;
}

-keep public class in.juspay.mystique.DuiLogger {
    public *;
}

-keep public class in.juspay.mystique.DynamicUI {
    public *;
}

-keep public interface in.juspay.mystique.ErrorCallback {
    public *;
}

-keep public class in.juspay.mystique.DuiInvocationHandler {
    public *;
}

-keep public class in.juspay.mystique.InflateView {
    public static void convertAndStoreArray (java.util.ArrayList, java.lang.Class, java.lang.String);
}

-keep class in.juspay.mystique.Renderer{
    public android.view.View createView(org.json.JSONObject);
}



