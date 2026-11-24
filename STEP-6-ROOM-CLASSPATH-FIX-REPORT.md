# LIFEEASY — STEP 6 Room Classpath Fix

## Confirmed CI compiler error
`app/src/main/kotlin/com/lifeeasy/agent/MainActivity.kt:16:67` reported:

`Cannot access androidx.room.RoomDatabase which is a supertype of LifeEasyDatabase. Check your module classpath for missing or conflicting dependencies.`

## Root cause
`:core:data` exposes `LifeEasyDatabase` through `DatabaseFactory.get(...)`. `LifeEasyDatabase` extends `androidx.room.RoomDatabase`. The Room runtime dependency was declared as `implementation` in `core/data/build.gradle.kts`, which keeps it off downstream compile classpaths. The `:app` module therefore could not resolve the public supertype.

## Change made
Changed only the Room runtime dependency in `core/data/build.gradle.kts` from:

```kotlin
implementation(libs.androidx.room.runtime)
```

to:

```kotlin
// RoomDatabase is a public supertype of LifeEasyDatabase, which is exposed to :app.
// Use api so downstream modules can resolve that public type on their compile classpath.
api(libs.androidx.room.runtime)
```

`room-ktx`, Room compiler/KSP, and other dependencies remain unchanged. This is the Gradle module API/implementation visibility correction corresponding to the exact compiler diagnostic.

## Validation status
- Confirmed the failing source line and error from the uploaded GitHub Actions log.
- Confirmed `LifeEasyDatabase : RoomDatabase` and `DatabaseFactory.get()` returns `LifeEasyDatabase`.
- Confirmed the old `:core:data` dependency used `implementation(libs.androidx.room.runtime)`.
- ZIP integrity and project configuration text checks are run before packaging.
- **Not verified here:** Gradle compilation, unit tests, APK assembly, emulator/device launch. The GitHub Actions rerun is required to confirm the fix end-to-end.
