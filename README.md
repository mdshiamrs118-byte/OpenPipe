# OpenPipe

Tiny file-mover for Android 10+. Create rules (source folders + file extensions -> destination folder), tap **Run now**, done.

- Pure Android framework: no AndroidX, no libraries, no internet permission. Release APK is only a few dozen KB.
- Multiple rules, shown as cards on the home screen. Pin any rule to your launcher home screen from its Edit page.
- Move or copy, optional subfolder scan, Preview, History with Undo.
- Themes: System / Light / Dark / Black + 6 colour presets + custom hue slider.

## Build on GitHub
1. Push this folder to a GitHub repo (branch `main`).
2. Open the **Actions** tab -> *Build OpenPipe APK* runs automatically (or press *Run workflow*).
3. Download the `OpenPipe-apk` artifact -> `app-release.apk`.

Local build: `gradle assembleRelease` (Gradle 8.9, JDK 17, Android SDK 34).

## Permissions
- Android 11+: "All files access" (needed to move files between any folders).
- Android 10: classic storage permission.
