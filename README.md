# LIFEEASY

Android application foundation for the LIFEEASY AI companion.

- Package: `com.lifeeasy.agent`
- Minimum SDK: 26
- Compile/Target SDK: 36
- Kotlin: 2.0.21
- Android Gradle Plugin: 8.10.1
- JVM target: 17
- UI: Jetpack Compose / Material 3
- 3D rendering: SceneView 2.3.0 / Filament

## Implemented project areas

- STEP 1–2: project and module scaffolding
- STEP 3: shared Compose design system
- STEP 4: security core
- STEP 5–6: Room/DataStore data layer and encrypted local memory foundation
- STEP 7: voice security foundation (not a complete live voice assistant)
- STEP 8: deterministic offline agent orchestration core (not an LLM)
- STEP 9: PDF/DOCX/PPTX/XLSX builder code and Android Print Framework gate (UI export workflow still pending)
- STEP 10: holographic UI/navigation integration
- STEP 11: user-supplied GLB asset integration (initial device rendering failed; repaired in STEP 12)
- STEP 12: transparent GLB rendering configuration, shared Home/Chat GLB component, and Monitor module foundation

## Explicit capability boundaries

- The local agent uses deterministic workflow logic; no cloud LLM is connected.
- Microphone capture, speech recognition, ElevenLabs output, web search, and image generation are not active.
- Document builders exist, but the editable document UI/export workflow is not fully connected.
- Monitor data is local and in-memory; it is not remote telemetry or persistent crash analytics.
- Device-level GLB rendering and animation still require validation on a physical Android device after the next GitHub Actions build.

See the STEP-specific reports for exact scope and verification limits.
