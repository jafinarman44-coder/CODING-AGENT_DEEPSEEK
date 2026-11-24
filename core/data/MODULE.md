# Core Data

Provides Room persistence and DataStore preferences. Personal memories are encrypted with the Android Keystore-backed AES-GCM cipher before being written to Room. The domain repository contract is implemented here; plaintext is only materialized in process memory when a consumer observes memories. Preferences are limited to non-secret presentation settings.
