# JARVIS Phase 3 — LiteRT-LM Local AI

Status: implementation design recorded; application source changes not yet committed.

## Source baseline inspected

The uploaded `JARVIS-security-hardened-working-copy.zip` contains a Flutter application with a native Android Kotlin host. It is not a fully native Compose app.

Relevant integration points:
- Android host: `android/app/src/main/kotlin/com/privateagent/private_agent/MainActivity.kt`
- Existing Flutter method channels: speech channel and accessibility/system channel
- Android Gradle module: `android/app/build.gradle.kts`
- Flutter AI gateway: `lib/services/ai_service.dart`
- Settings persistence: `lib/services/settings_service.dart`

Baseline Android configuration:
- Java 17
- Kotlin Gradle plugin 2.3.20
- Android Gradle Plugin 9.0.1
- minSdk 26

## Runtime and model selection

- Runtime: LiteRT-LM Android Kotlin API
- Initial pinned dependency candidate: `com.google.ai.edge.litertlm:litertlm-android:0.18.0`
- Initial model candidate: `litert-community/gemma-4-E2B-it-litert-lm`
- Model file: `gemma-4-E2B-it.litertlm`
- Model artifact size reported by the model card: approximately 2,583 MB (about 2.58 GB).
- Model repository declares Apache-2.0 for this LiteRT package; the upstream Gemma terms and usage policy must also be reviewed before distribution.

Official references:
- https://developers.google.com/edge/litert-lm/android
- https://github.com/google-ai-edge/LiteRT-LM/blob/main/docs/api/kotlin/getting_started.md
- https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm

## Planned implementation sequence

1. Add the pinned LiteRT-LM Android dependency and confirm Gradle resolution.
2. Add an isolated Kotlin local inference engine with explicit initialize, generate, unload, and status operations. Run initialization and inference off the UI thread; close Conversation and Engine resources deterministically.
3. Add a dedicated Flutter MethodChannel for local model operations. Do not overload the existing speech or accessibility channels.
4. Add foreground-only model download to app-private files storage. Use a temporary partial file, progress reporting, content-length validation where available, SHA-256 verification against a trusted manifest, and atomic rename after validation. No WorkManager, DownloadManager, or background service is to be introduced for model downloads.
5. Add cancellation and resumable transfer only after validating HTTP Range behavior and partial-file integrity. A pause/resume control must not report success unless the transfer actually pauses or resumes.
6. Add a Dart service abstraction and a small settings surface for model status, download, load/unload, and local inference.
7. Integrate local inference into the existing provider policy without bypassing manual provider selection or the user's cloud-spend protections. Local fallback must be explicit and must never trigger paid cloud calls.
8. Add tests for missing/corrupt model, download interruption, checksum mismatch, repeated load/unload, offline generation, and provider-routing policy.
9. Build and device-test the APK before declaring Phase 3 complete.

## Safety and compatibility constraints

- Preserve the existing Flutter architecture, features, credentials, permissions, and Phase 1/2 behavior.
- Keep model weights outside the APK and outside assets.
- Store model weights under app-private storage and exclude them from Android backup.
- Load at most one local model at a time.
- Treat model size as a disk requirement, not a promise of runtime memory use.
- Do not assume Gemma 4 E2B performance on the target OnePlus device from benchmarks for other phones.
- Do not claim successful compilation until CI or a local Flutter/Gradle build has completed.

## Current limitations

The GitHub repository stores project ZIP archives at its root rather than the extracted source tree. The uploaded ZIP is the inspected baseline. This document is committed to the dedicated Phase 3 branch as a design record; application source files have not yet been modified. The current environment does not have the Flutter SDK available for a verified APK build.
