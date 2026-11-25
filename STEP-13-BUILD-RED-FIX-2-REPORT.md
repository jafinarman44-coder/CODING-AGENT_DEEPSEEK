# LIFEEASY — STEP 13 Build RED Fix 2

## Evidence
The latest GitHub Actions log reports exactly:

`MainActivity.kt:259:31 Null cannot be a value of a non-null type com.google.android.filament.utils.Manipulator`

The failing call used `manipulator = null` on SceneView `2.3.0`'s deprecated `Scene(...)` composable. The tagged SceneView v2.3.0 source declares this parameter as a required non-null `com.google.android.filament.utils.Manipulator` and documents the migration to `Manipulator.Builder().build(...)`.

## Change
- Added `import com.google.android.filament.utils.Manipulator`.
- Replaced `manipulator = null` with a remembered orbital manipulator:
  `manipulator = remember { Manipulator.Builder().build(Manipulator.Mode.ORBIT) }`
- Kept SceneView dependency pinned to `2.3.0` and did not reintroduce the unsupported `SurfaceType` / `surfaceType` arguments from the prior failed attempt.

## Validation
- Confirmed the reported `manipulator = null` no longer exists.
- Confirmed the new import and non-null builder expression are present exactly once.
- Confirmed the GLB asset remains in the project.
- ZIP integrity check performed.
- Full Android Gradle compilation and device rendering were not run in this environment; do not treat this source correction as a verified green build or GLB runtime fix.
