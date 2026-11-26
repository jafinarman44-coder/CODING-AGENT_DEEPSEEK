# LIFEEASY STEP 15 — Provider/model selector and GLB audit

## Changes
- Added provider presets for OpenAI, Gemini, DeepSeek, xAI Grok, Groq, Mistral, OpenRouter and local Ollama.
- Added provider and model dropdowns, with curated model IDs and tier/availability notes. Model IDs remain editable because catalogs and access change.
- Added `GET {baseUrl}/models` discovery for providers exposing the OpenAI-compatible model-list endpoint. The UI merges discovered IDs with curated suggestions.
- Improved HTTP error diagnostics to include the failing endpoint and a bounded response-body excerpt, making 404 causes diagnosable.
- Kept API key encryption through Android Keystore.
- Copilot is explicitly documented as not connectable through this generic public Chat Completions API-key adapter; a Copilot subscription is not treated as a general API key.
- Confirmed the project theme uses Material 3 `darkColorScheme`; no light-theme switch was introduced.

## GLB asset audit
- `app/src/main/assets/models/earth_globe_sci-fi_look.glb` is present.
- GLB header/version and declared total length are internally consistent (glTF 2.0, 5,241,872 bytes).
- Embedded glTF document declares 1 scene, 17 nodes, 6 meshes, 5 materials, 8 textures/images and 1 animation named `rotation`.
- Therefore a different file format is not currently required. The packaged asset is a valid GLB container with an animation track. This does not prove successful Filament rendering on the user's device.
- Black/overlapping viewport remains an unverified runtime rendering issue. No unsupported SceneView surface API changes were guessed in this step. Device Logcat around Filament/SceneView and a screenshot after this build are still needed to isolate the renderer/compositing failure.

## Validation limits
- ZIP integrity and source-level checks were run.
- Android Gradle compilation, unit tests, provider network calls, real API-key authentication, and device rendering were not run in this environment.
- Free/paid labels are indicative only. Providers can change model IDs, pricing, quota, regional availability and `/models` support. Copilot is informational only.
