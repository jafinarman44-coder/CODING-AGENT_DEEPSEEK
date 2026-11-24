# Feature: Memory

## Responsibility
Encrypted personal-memory management UI: observe, search, create, edit and delete saved memories.

## Dependencies
- `:core:domain` for the `MemoryRepository` contract and `MemoryItem` model.
- `:core:designsystem` for shared visual foundations.
- Jetpack Compose Material 3 for the screen and confirmation dialogs.

## Security boundary
The feature receives a domain repository and does not access Room or Android Keystore directly. Persistence encryption is implemented in `:core:data` using `:core:security`. Decrypted memory content exists in process memory while displayed or searched.

## Current limitations
- Memories are local to this installation; cloud sync and cross-device sync are not implemented.
- No automatic AI extraction or memory ranking is implemented in this stage.
