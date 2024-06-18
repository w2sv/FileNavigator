# Prevent Proto DataStore fields from being deleted
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite {
    <fields>;
}