# Nothing Launcher — Pixel / Android 17 port

This branch is for the real APK-based port, using the version that already launches on the target Pixel as the base and the newer version as a reference.

## Source APKs required

Place these two files in `originals/`:

- `nothing-launcher-1.0.2.apk` — working base from the user's APKM package (extract its `base.apk` and rename it).
- `nothing-launcher-1.2.4.apk` — newer reference APK.

The source binaries are intentionally not fabricated or substituted with the separate Compose re-creation. They must be the actual supplied APKs.

## Current automation

The GitHub Actions workflow decodes both versions with Apktool and publishes the decoded trees as an artifact for comparison. It deliberately does **not** claim to produce a modified installable APK yet: compatibility changes must be identified and tested before rebuilding and signing.

## Target

- Device: Google Pixel 8
- Android: Android 17
- Baseline: Nothing Launcher 1.0.2 (reported to launch on the target device)
- Reference: Nothing Launcher 1.2.4

After adding the source APKs under `originals/`, push the branch or run the workflow manually from **Actions → Nothing Launcher APK analysis → Run workflow**.
