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

## STEP 13 — GLB rendering and AI provider setup

This build adds a real OpenAI-compatible chat endpoint. Configure it from **Settings → AI Provider**.

- Cloud endpoint: use an HTTPS base URL such as `https://api.openai.com/v1`, enter a model ID supported by your provider, and provide its API key.
- Local Ollama on a PC on the same Wi-Fi: use `http://<PC-LAN-IP>:11434/v1`, enter the installed Ollama model name (for example `llama3.1`), and leave API key blank. Ollama must listen on `0.0.0.0:11434`; allow inbound TCP 11434 in Windows Firewall for the private LAN. `localhost` on the phone is not the PC.
- API keys are encrypted at rest with Android Keystore AES-GCM. Remote HTTP endpoints are rejected; HTTP is restricted in app validation to local/private hosts.

The supplied GLB is rendered using SceneView's TextureSurface + transparent render target and an explicit environment for its PBR materials. Device rendering and animation still require handset verification after installing the new APK.
