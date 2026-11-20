# Play Age Signals 0.0.6 reflection (whenMandatory). Without these, R8 may strip types and
# MandatorySharingProbe falls back to checkAgeSignals only.
-keep class com.google.android.play.agesignals.ApplicableAgeFeaturesRequest { *; }
-keep class com.google.android.play.agesignals.ApplicableAgeFeaturesRequest$Builder { *; }
-keep class com.google.android.play.agesignals.model.ApplicableAgeFeature { *; }