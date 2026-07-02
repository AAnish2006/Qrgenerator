# Install / Prerequisites

## Requirements

| Tool | Version |
|---|---|
| Android Studio | Otter (2025.2) or newer — needed for AGP 9.x / built-in Kotlin support |
| JDK | 17 (Temurin recommended). Set in Android Studio via **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK** |
| Android SDK Platform | API 36 (compile/target SDK) |
| Android SDK Build-Tools | 36.0.0+ |
| Kotlin | 2.2.10 (bundled via the version catalog, no separate install needed) |
| Min supported device / emulator | API 24 (Android 7.0) |

Gradle itself does **not** need to be installed separately — this project ships the Gradle Wrapper (`gradlew` / `gradlew.bat` + `gradle/wrapper/gradle-wrapper.jar`), which downloads the exact Gradle version pinned in `gradle/wrapper/gradle-wrapper.properties` (Gradle 9.1.0) on first run.

## Get the code into Android Studio

1. **File > Open**, select the project's root folder (the one containing `settings.gradle.kts`).
2. Let Android Studio sync Gradle. First sync will download Gradle 9.1.0, AGP 9.1.1, and all dependencies — this can take several minutes depending on your connection.
3. If prompted to "Trust Gradle Project", accept it.

## Local secrets

1. Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
2. Fill in `GEMINI_API_KEY` if you use the Gemini-powered features. Everything else can stay blank (see `.env.example` comments — this app has no required backend).
3. `.env` is already in `.gitignore`. **Never commit it.**

## Verify the toolchain works

```bash
./gradlew --version
```

should print Gradle 9.1.0, and confirm it picked up a JDK 17 (or newer) daemon.

If `./gradlew` isn't executable on macOS/Linux:

```bash
chmod +x gradlew
```

See `BUILD.md` for how to actually build the app, and `DEPLOY.md` for signing/Play Store steps.
