# STEP 12 — GLB Rendering Repair + Monitor Foundation

Date: 2026-11-26

## User-reported failure

The STEP 11 APK built and opened, but the GLB was not visible, the SceneView area appeared as an opaque black rectangle that overlapped nearby UI, and the model animation did not appear. The user also requested the same GLB above the “What would you like to work on?” empty-chat prompt, and asked to start the next step.

## Root cause confirmed from the pinned SceneView 2.3.0 source

- SceneView 2.3.0's `Scene` composable hosts a native `SurfaceView`.
- Its `isOpaque` argument defaults to `true`; the native view sets Filament's blend mode to OPAQUE and configures the surface as opaque. This can produce the black opaque rectangle observed by the user.
- The GLB asset path used by the project is valid for SceneView 2.3.0's `ModelLoader.createModelInstance(assetFileLocation)` API. The asset is bundled under the correct `app/src/main/assets/models/` path.

Source references used for the diagnosis are listed in the delivery response: SceneView 2.3.0 `Scene.kt` and `SceneView.kt`, and the 2.3.0 `ModelLoader.kt` source.

## Changes made

1. Explicitly set `isOpaque = false` on the SceneView scene so the native render target clears with transparency rather than painting an opaque black rectangle.
2. Kept the SceneView 2.3.0 API and current Kotlin 2.0.21 / AGP 8.10.1 toolchain; no speculative major dependency migration.
3. Kept the user's original GLB binary unchanged and bundled at `app/src/main/assets/models/earth_globe_sci-fi_look.glb`.
4. Kept `ModelNode(autoAnimate = true)`; the supplied GLB contains one animation named `rotation` with six channels.
5. Reduced the Home hero model viewport width to keep it left of “Hello, Welcome” on narrow screens.
6. Replaced the decorative Canvas orb above “What would you like to work on?” in Chat with the same `EarthGlobeScene` composable, using a smaller framing scale.
7. Preserved an explicit failure label with the thrown loader message if model creation fails; no fake globe is substituted.

## STEP 12 — Monitor module foundation

- Implemented `feature:monitor/MonitorEventStore.kt` as an observable, bounded (30 entries) in-memory record of local agent executions.
- Connected Chat success and failure outcomes to the monitor event store.
- Replaced the previous static Monitor status-only page with live Android values: app version, Android version/API, available/total RAM, app heap usage/max, battery percentage, connected local modules, and recent agent execution outcomes.
- Added unit tests for newest-first ordering and the 30-event retention limit.
- The monitor is local and in-memory only. It is not persistent crash reporting, remote telemetry, or a full OS diagnostics suite.

## Asset inspection

- GLB 2.0; 5,241,872 bytes; header-declared size matches actual file size.
- 17 nodes, 6 meshes, 5 materials, 8 embedded images/textures.
- One embedded animation: `rotation` (6 channels / 6 samplers).
- No external image URI dependency detected.

## Static checks performed

- Asset SHA-256 matches the original user-uploaded GLB.
- Source explicitly passes `isOpaque = false`.
- Home hero uses the shared GLB composable.
- Empty Chat state uses the same GLB composable; the old Canvas HologramOrb function is removed.
- `autoAnimate = true` remains configured.
- Chat records both success and failure events in the feature monitor module.
- Monitor module exposes a StateFlow and has bounded-retention unit tests.
- ZIP archive integrity check passes.

## Verification boundary

Not run in this environment:
- Android Gradle compilation / unit test execution / APK assembly (no Gradle executable and no Gradle wrapper are present in the extracted project).
- Actual Filament rendering, alpha compositing, GLB texture display, and animation playback on the user's handset.

The prior user-reported GitHub Build Green confirms the previous STEP 11 source compiled, but does not validate this new ZIP. A fresh GitHub Actions run is required. Even a green build will not prove device rendering; the next phone check must confirm both Home and Chat GLB views and confirm the black rectangle is gone.
