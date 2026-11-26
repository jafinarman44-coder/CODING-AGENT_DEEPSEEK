# LIFEEASY STEP 17 — One-key setup, chat reliability, mobile UX and GLB surface repair

## User-reported issues addressed
- Gemini chat returned HTTP 400 because no Authorization header was sent.
- Gemini returned temporary HTTP 503 overload responses.
- The app exposed confusing cloud/Ollama setup requirements.
- Monitor claimed document export was not connected even though the local PDF/DOCX/PPTX/XLSX builders are wired in Create Studio.
- The left navigation rail consumed too much width on a phone.
- The bundled GLB appeared black/hidden when composed alongside other Compose UI.

## Changes made
1. Remote AI is considered configured only when endpoint + model + non-empty API key are present. Local/private Ollama endpoints remain optional and do not require a key.
2. Default settings now point to Google Gemini OpenAI-compatible endpoint and `gemini-3.8-flash`.
3. API keys are trimmed and an accidentally pasted `Bearer ` prefix is normalized before Android Keystore encryption and request-header construction.
4. Chat now fails locally with a clear setup instruction instead of sending an unauthenticated request.
5. HTTP 429/5xx responses are retried up to three attempts with short backoff. 401/403 responses explain key/permission problems; other errors retain provider detail.
6. Settings simplified to a single Gemini key setup flow with official AI Studio key link, save, and `/models` connectivity check. Ollama is explicitly optional.
7. Phone navigation changed from a narrow side rail to a compact bottom navigation. Voice, Memory, Monitor and Library remain reachable from Explore.
8. Chat microphone now launches Android speech recognition in Bangla; Android TTS and local document export remain no-key features.
9. Monitor status corrected to report the connected local document builders and current AI-key configuration.
10. The Home hero was redesigned using the user's reference image as inspiration (dark indigo/purple/cyan, large central 3D hero, compact quick actions) without copying the reference layout exactly.
11. SceneView 2.3.0 `Scene` now explicitly uses `SurfaceType.TextureSurface` while retaining `isOpaque=false`, to composite the GLB inline with Compose instead of the default SurfaceView overlay behavior.

## Important capability boundaries
- One Google AI Studio key powers Gemini chat in this build.
- Android speech recognition/TTS and PDF/DOCX/PPTX/XLSX generation do not require another cloud API key (speech recognition availability/offline behavior depends on the Android speech service and installed language pack).
- Ollama is optional and is not required on the user's phone.
- Web search and image generation are not silently claimed as active; they require separate integration work.
- A Google API key must be created by the user; this project does not contain a shared key.

## Validation performed
- Static source checks: passed (single-key gating, auth-header normalization, retry branch, bottom navigation, microphone launcher, TextureSurface selection, monitor export status).
- GLB asset existence: checked in app assets.
- ZIP integrity: checked after packaging.
- Android Gradle compile / APK build: NOT run in this environment (project archive has no Gradle wrapper and no system Gradle executable available).
- Live Gemini request: NOT tested because no user API key was provided.
- Device GLB rendering: NOT tested on the user's handset. TextureSurface is the documented SceneView compositing mode, but the final device result still requires a new APK install.
