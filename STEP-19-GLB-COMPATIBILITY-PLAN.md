# STEP 19 — GLB Runtime Compatibility (started)

## Goal
Make the supplied Earth GLB visible in the Home hero on the supported Android range without breaking the existing dark dashboard.

## Current verified constraints
- The project uses SceneView 2.3.0.
- Java/Kotlin target is 17.
- SceneView 2.3.0 Compose `Scene` does not accept `surfaceType`.
- The previous TextureSurface patch therefore caused the current Kotlin compile failure.

## Planned implementation gate
1. First confirm STEP 18 Build Green with the corrected 2.3.0 API call.
2. Then evaluate a SceneView upgrade using its published compatibility requirements (Kotlin/JVM/compileSdk), rather than changing only the dependency version.
3. Keep a non-GLB fallback UI for devices where Filament/GPU initialization fails; label it as fallback, not as a rendered Earth model.
4. Validate on the user's actual Android version and device GPU. Source compilation alone cannot prove GLB rendering.

## Not claimed
No claim is made that GLB now renders on the phone. No device runtime test was available during this step.
