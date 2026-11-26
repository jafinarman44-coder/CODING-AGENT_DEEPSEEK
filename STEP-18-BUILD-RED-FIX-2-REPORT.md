# STEP 18 — Build Red Fix 2

## Evidence
The supplied GitHub Actions log (`Pasted text(8).txt`) reached `:app:compileDebugKotlin` and failed in `MainActivity.kt` with:
- line 54:28 — unresolved reference `SurfaceType`
- line 357:13 — no `Scene(...)` overload matched
- line 359:31 — unresolved reference `SurfaceType`

## Root cause
The project pins `io.github.sceneview:sceneview:2.3.0`. That version's `Scene` Compose overload does not expose the `surfaceType` argument and does not provide the imported `io.github.sceneview.SurfaceType` symbol. The previous patch incorrectly copied a newer SceneView API into the 2.3.0 dependency.

## Fix applied
- Removed `import io.github.sceneview.SurfaceType`.
- Removed `surfaceType = SurfaceType.TextureSurface` from the `Scene(...)` call.
- Kept the dependency at 2.3.0 to avoid an unplanned Kotlin/JVM toolchain migration.
- Kept `isOpaque = false`; this parameter is present in the 2.3.0 Scene overload.

## Next-step work started (STEP 19 readiness)
- Audited the current GLB rendering integration against the pinned SceneView API.
- Recorded that TextureView compositing cannot be enabled through this 2.3.0 Compose `Scene` call.
- The next GLB implementation decision is a controlled SceneView upgrade/migration or a separate compatible rendering host, after the current build is green. No unsupported API has been left in source.

## Validation
- Source inspection confirms the invalid import and argument are removed.
- Confirmed app dependency remains SceneView 2.3.0 and Java/Kotlin target 17.
- ZIP integrity will be checked after packaging.

## Not performed
- Gradle compilation, unit tests, APK assembly, GitHub Actions, and device rendering were not run in this environment. Do not treat this archive as build-green until CI confirms it.
