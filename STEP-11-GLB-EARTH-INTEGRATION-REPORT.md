# STEP 11 — User GLB Earth Model Integration

Date: 2026-11-25

## Requested change

Replace the custom-drawn sphere on the Home hero panel (left of “Hello, Welcome”) with the exact GLB file supplied by the user.

## Implemented

- Added the supplied model unchanged at `app/src/main/assets/models/earth_globe_sci-fi_look.glb`.
- Replaced the Home hero's `HologramOrb` Canvas call with `EarthGlobeScene`.
- Added SceneView + Filament model loading and rendering, using the model's embedded textures/materials and its embedded animation (`autoAnimate = true`).
- Kept the separate small decorative assistant orb in Chat; the Home hero no longer uses the Canvas-drawn globe.
- Added a visible load-failure message instead of silently drawing a substitute globe.
- Added third-party attribution in `THIRD-PARTY-ASSETS.md`.

## Compatibility decision

The existing project uses AGP 8.10.1, Kotlin 2.0.21, JVM target 17 and compileSdk 36. SceneView 2.3.0 is the SceneView release documented as using Kotlin stdlib 2.0.21 and JVM 17. Newer SceneView releases require a newer Kotlin/JVM/Android toolchain, so this step intentionally does not upgrade the build system just to add the GLB viewer.

Dependency: `io.github.sceneview:sceneview:2.3.0`.

## GLB file inspection

- Valid GLB 2.0 binary header and declared total length matches actual file size.
- File size: 5,241,872 bytes.
- One scene, 17 nodes, 6 meshes, 5 materials, 8 embedded textures/images.
- Embedded animations are present; the model node is configured with `autoAnimate = true`.
- No external image URI was found in the GLB JSON; model resources are self-contained.

## License attribution

The model metadata identifies “Earth Globe Sci-Fi Look” by spaceexpert, sourced from Sketchfab, licensed CC BY 4.0. Attribution and source/license URLs are recorded in `THIRD-PARTY-ASSETS.md`.

## Verification boundary

Passed:
- ZIP integrity check.
- GLB header/length and JSON structure checks.
- Static source checks: Home uses `EarthGlobeScene`; the Home hero no longer calls `HologramOrb`; asset path in code matches the bundled asset path; SceneView dependency and imports are present.

Not yet verified:
- Android Gradle compilation and APK assembly (this workspace has no Gradle executable or Gradle wrapper).
- Actual Filament rendering on a phone, animation playback, device GPU compatibility, and visual scale/camera framing.

The user must run GitHub Actions and install the resulting APK to confirm device rendering. A green build will verify compilation, not by itself confirm the 3D model is visible on the handset.
