# Room
-keep class com.naviapp.data.local.entity.** { *; }

# Gson / Retrofit модели (данные из OSRM, Nominatim, Photon)
-keep class com.naviapp.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# MapLibre
-keep class org.maplibre.android.** { *; }
-dontwarn org.maplibre.android.**
