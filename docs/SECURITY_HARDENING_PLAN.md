# JARVIS Android Security Hardening — Separate Branch

Branch: `security/jarvis-phase3-hardening`
Base: `feature/phase3-litertlm-local-ai`

## Objective

Harden the JARVIS Android application without changing or removing existing product features. This work is isolated from `main` and the Phase 3 feature branch. No existing branch is to be force-pushed, reset, or rewritten.

## Guardrails

- Preserve the existing JARVIS user experience, local LiteRT-LM integration, voice, vision, automation, and other implemented capabilities.
- Do not remove a permission or service until its call sites and user-visible feature dependency have been traced.
- Prefer runtime permission requests at the point of use, least-privilege alternatives, app-private storage, and explicit user consent.
- Do not disable accessibility or notification-listener functionality if it is an intentional, user-enabled feature. Document why each special access is required and how the user can revoke it.
- Never commit API keys, signing keys, keystores, model weights, user data, or generated secrets.
- Keep model files out of the APK and Android backup; verify checksums before loading.
- Require HTTPS for remote services. A local loopback HTTP endpoint, if needed for the on-device model bridge, must be narrowly scoped and documented.
- Keep debug and release signing separate. Release signing credentials must be supplied by the owner through a secure mechanism and must never be committed.

## Source archive received and inspected

Received `Jarvis-2.0-feature-phase3-litertlm-local-ai (1).zip`. It contains the nested `JARVIS-Phase3-LiteRTLM-corrected-review.zip` (143 entries), a prior security working-copy ZIP, a previously reviewed ZIP, and an outer GitHub Actions workflow file. The nested Phase 3 archive was extracted and source-inspected.

### Confirmed source-level observations

- `android/app/src/main/AndroidManifest.xml` declares sensitive capabilities including `MANAGE_EXTERNAL_STORAGE`, `QUERY_ALL_PACKAGES`, `READ_SMS`, contacts, camera, microphone, overlay, usage stats, accessibility, and notification-listener service access. These have not been removed because the source contains corresponding user-facing functions and dependencies that need individual runtime/device verification.
- The application globally sets `android:usesCleartextTraffic="true"`. This is not safe to switch off blindly: source references include loopback Ollama, configurable Home Assistant endpoints, and a non-HTTPS ISS API URL. A per-destination transport migration is required before tightening the global policy without breaking features.
- `FileProvider` is non-exported and grants URI permissions, but `res/xml/file_paths.xml` exposes the entire internal cache, external cache, and files directories to any recipient of a granted URI. The traced camera flow writes only to `cacheDir/camera_snapshots`. A minimal hardening change is to restrict the provider mapping to that camera snapshot subdirectory and remove the unused broad mappings; this should be tested with the camera capture feature.
- Release builds currently use the debug signing configuration in `android/app/build.gradle.kts`. This is not suitable for production distribution; owner-supplied release signing configuration is required.
- LiteRT-LM model loading resolves the requested filename under app-private `filesDir/local_models` and checks canonical parent containment, file readability, and nonzero length. The Phase 3 review status also records a content-length and SHA-256 check in the downloader; runtime/device verification remains outstanding.
- `settings_service.dart` uses `flutter_secure_storage` for AI, Telegram, and Home Assistant credentials and includes a verified legacy-value migration path. The migration fallback retains a legacy value in memory if encrypted storage fails, avoiding silent credential loss; this fallback should be revisited before production because the legacy persisted value may remain until migration succeeds.
- The archive's own review notes state that Flutter/Dart checks and APK/device tests have not yet been run. Source inspection is not a substitute for a verified build or runtime test.

## Audit sequence

1. Inventory the actual Android manifest, Gradle configuration, network security configuration, exported components, services, receivers, providers, permissions, and native method-channel handlers.
2. Trace every declared permission and special access to a feature and its call sites. Classify as required, optional, unused, or unresolved.
3. Review storage, backup, logging, WebView, intent handling, deep links, file-provider paths, and network traffic for data exposure.
4. Apply minimal source changes with tests; preserve existing feature behavior.
5. Run static checks and the Android/Flutter CI build. Review the merged manifest and APK permissions from the produced artifact.
6. Test the APK on-device, including the relevant permission-denial and feature-degradation paths.
7. Produce a release-signed APK only after the owner supplies release signing material securely and the build passes.

## Current implementation status and limitations

- The uploaded source ZIP has been inspected locally. It is a source archive nested inside a repository snapshot; the full source tree is not extracted as ordinary tracked files in the GitHub branch.
- The existing branch contains this security plan, but the uploaded binary archive itself has not been committed as source files. GitHub text-file operations cannot safely turn the 10 MB ZIP into a complete extracted project in one operation.
- No source code or permission has been changed in this commit. This is deliberate: adding only isolated manifest/Kotlin files to a repository whose checked-in application tree is incomplete could create a misleading or non-buildable partial project. The exact source tree must be imported as a coherent set before applying the narrowly scoped FileProvider fix.
- Flutter/Dart SDK and an Android device/emulator were not available for this source inspection. No build, test, merged-manifest, APK signature, or runtime result is claimed.
- The archive includes a GitHub Actions workflow at the outer snapshot level, but the nested Phase 3 source ZIP's own status document says it does not contain a workflow. The outer workflow must be checked against the extracted project layout before it can be relied on.

## Acceptance criteria

- Every retained sensitive permission has a documented feature justification and an appropriate consent/revocation path.
- No feature is silently removed or silently routed to a paid cloud provider.
- No secrets or private signing material appear in the repository or build logs.
- CI build succeeds and the APK's merged manifest, signing certificate, and network configuration are inspected from the produced artifact.
- Existing Phase 3 local-model behavior remains intact.
