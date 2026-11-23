# LIFEEASY Security Core — Step 4

This module provides a small, explicit security foundation:

- Deterministic consent/authentication/block policy evaluation.
- AES-256-GCM encryption using Android Keystore; there is deliberately no plaintext fallback.
- Cryptographically strong URL-safe token generation.
- Redaction of common credentials, email addresses, and phone-like strings before diagnostics are logged.

## Important boundaries

- The policy engine is not a biometric authenticator. The app must obtain an authenticated state from a trusted authentication flow.
- Keystore encryption protects bytes at rest on the device. It does not secure data after the app decrypts it in memory.
- Redaction is best-effort and must not be treated as a guarantee that arbitrary user content contains no secrets.
- Root/emulator heuristics are not treated as proof of compromise and are not used to block installation or launch.
- Keystore keys can become unavailable after device reset, lock-screen changes, or OS/security events. Callers must handle decryption failure without falling back to plaintext.

## Tests

Unit tests cover policy ordering, credential/contact redaction, and token generation. Android Keystore encryption/decryption requires an Android runtime/device and is not claimed as host-unit-tested here.
