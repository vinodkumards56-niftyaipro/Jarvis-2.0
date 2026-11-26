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

## Audit sequence

1. Inventory the actual Android manifest, Gradle configuration, network security configuration, exported components, services, receivers, providers, permissions, and native method-channel handlers.
2. Trace every declared permission and special access to a feature and its call sites. Classify as required, optional, unused, or unresolved.
3. Review storage, backup, logging, WebView, intent handling, deep links, file-provider paths, and network traffic for data exposure.
4. Apply minimal source changes with tests; preserve existing feature behavior.
5. Run static checks and the Android/Flutter CI build. Review the merged manifest and APK permissions from the produced artifact.
6. Test the APK on-device, including the relevant permission-denial and feature-degradation paths.
7. Produce a release-signed APK only after the owner supplies release signing material securely and the build passes.

## Evidence and current limitations

- The selected base branch is `feature/phase3-litertlm-local-ai`.
- The repository root contains ZIP archives of project source; the checked-in extracted Android project tree is incomplete (the visible `Jarvis-master/android/app` tree currently exposes only a limited Kotlin source subtree).
- A Phase 3 implementation-plan document is present under `docs/`.
- The prior Play Protect screenshot says the app may request access to sensitive data. That warning alone does not establish that the APK is malware, and it does not identify which permissions caused the warning.
- Claims about the APK's exact manifest permissions, debug/release certificate, or cleartext-traffic setting are **not yet independently verified from the actual APK or its merged manifest** in this branch.
- No application source or permission has been changed in this initial commit. Source modifications are blocked until the exact ZIP/source tree used to produce the APK is available for extraction and inspection.

## Acceptance criteria

- Every retained sensitive permission has a documented feature justification and an appropriate consent/revocation path.
- No feature is silently removed or silently routed to a paid cloud provider.
- No secrets or private signing material appear in the repository or build logs.
- CI build succeeds and the APK's merged manifest, signing certificate, and network configuration are inspected from the produced artifact.
- Existing Phase 3 local-model behavior remains intact.
