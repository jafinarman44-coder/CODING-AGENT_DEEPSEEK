# LIFEEASY — STEP 13: GLB Render Repair + Real AI Provider Connection

Date: 2026-11-26

## User-reported symptoms
- The supplied GLB was still a black circle on Home and invisible/black in Chat.
- No animation was visible.
- Chat answered `I received your message: hi` and labeled the result `deterministic local workflow`.
- The user asked for the GLB in both Home and Chat and to start the next step.

## Source-level findings
1. `EarthGlobeScene` used SceneView 2.3.0's default SurfaceView. Setting `isOpaque = false` alone does not make a SurfaceView compose inline with sibling Compose content. The 3D scene must use `SurfaceType.TextureSurface` for inline alpha compositing, with `isOpaque = false`.
2. The scene did not explicitly provide a non-opaque environment/IBL for the supplied PBR materials. This can leave the model dark even when its geometry is loaded. The scene now supplies a remembered environment with opacity disabled, plus a main light.
3. `ConversationNode` is a deterministic template, not an LLM. The Chat screen previously surfaced that template as a completed response. The new Chat path calls an actual OpenAI-compatible chat-completions endpoint when configured. If no provider is configured, generic conversation is rejected with a setup instruction rather than echoed as AI.

## Changes
### GLB
- Set `surfaceType = SurfaceType.TextureSurface` and `isOpaque = false`.
- Supply `rememberEnvironment(environmentLoader, isOpaque = false)` to the Scene.
- Keep the user's GLB bytes unchanged in `app/src/main/assets/models/earth_globe_sci-fi_look.glb`.
- Keep `ModelNode(autoAnimate = true)` so the GLB's embedded `rotation` animation is requested.
- Keep the same EarthGlobeScene component in Home hero and Chat empty state.
- Camera explicitly looks at origin; key parameters remain compact for mobile.

### AI provider / Chat
- Added OpenAI-compatible `/chat/completions` client using OkHttp.
- Added Settings fields for provider label, API base URL, model ID and API key.
- API key is encrypted at rest using the project's Android Keystore AES-GCM cipher under a dedicated alias.
- Added INTERNET permission.
- HTTPS is accepted for remote endpoints; HTTP is accepted only for localhost/emulator or RFC1918 private IPv4 hosts to support local Ollama. App manifest permits cleartext at platform level because Android requires that for LAN Ollama; the app client independently validates the configured endpoint.
- Chat sends recent conversation context (last 20 messages) to the configured provider.
- With no provider configured, supported deterministic offline workflows remain available; generic conversation no longer returns the fake-looking `I received your message` template.
- Provider errors are shown to the user and recorded in local Monitor events. API keys are not logged.

### Monitor continuation
- Monitor module from STEP 12 is retained: runtime/device metrics and bounded recent agent/chat events.

## Local Ollama setup
For a PC-hosted Ollama server on the same Wi-Fi as the Android phone:
- Base URL: `http://<PC-LAN-IP>:11434/v1`
- Model: `llama3.1` (or an installed Ollama model name)
- API key: leave blank
- On Windows, Ollama must listen on `0.0.0.0:11434` and Windows Firewall must allow inbound TCP 11434 from the private LAN.
- Do not use `localhost` on the phone; it points to the phone itself.

## Static checks performed
- GLB asset exists and byte size remains 5,241,872 bytes.
- Home and Chat both call the same EarthGlobeScene.
- Scene source includes TextureSurface + isOpaque=false + explicit environment + main light.
- ModelNode still sets autoAnimate=true.
- Generic conversation path no longer exposes ConversationNode's echo as an assistant answer when no provider exists.
- Provider request path targets `/chat/completions`, supports bearer key, limits history to 20 messages, and rejects non-HTTPS public HTTP endpoints.
- Manifest includes INTERNET permission.
- ZIP integrity and source-level assertions are run by the packaging script.

## Not verified
- Android Gradle compilation, unit tests, APK assembly: not run here (project has no Gradle wrapper and no system Gradle executable).
- Device-side Filament render, TextureView compositing, GLB material/texture appearance and animation playback: require a new APK and phone test.
- Actual LLM response: requires the user to configure a valid reachable endpoint/model and, for paid cloud services, a valid API key/account.
- PC-hosted Ollama LAN connectivity: requires PC network/firewall setup and a same-network phone test.
