# :feature:voice — STEP 7 foundation

Implemented:
- `EagleEnrollment`: validates 3–5 enrollment phrase samples and encrypts an externally generated on-device voiceprint with Android Keystore AES-GCM.
- `SecretCodeManager`: validates a four-digit code, derives a salted PBKDF2-HMAC-SHA256 verifier, and persists only an AES-GCM encrypted verifier.
- `VoiceGuard`: fail-closed gate; sensitive commands require an enrolled profile, a successful speaker-match result, and the four-digit code.
- `AndroidVoiceProfileStore` and `AndroidSecretCodeStore`: persist ciphertext only.
- Unit tests cover voice mismatch, missing enrollment, wrong code, successful dual verification, and non-sensitive commands.

Explicit boundary: this step does not include microphone capture, a speaker-embedding model, or a production `VoiceMatchVerifier`. No fake matcher is shipped. Sensitive-command authorization must remain disabled until a real, offline, on-device speaker verification engine is integrated and validated. No microphone permission is requested in this step.
