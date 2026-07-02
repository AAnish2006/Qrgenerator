# Build

All commands are run from the project root. On Windows use `gradlew.bat` instead of `./gradlew`.

## Debug build

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`. Debug builds are signed with the auto-generated `debug.keystore` config (see `app/build.gradle.kts` → `signingConfigs["debugConfig"]`) so they can be installed directly.

Install straight to a connected device/emulator:

```bash
./gradlew installDebug
```

## Release APK

```bash
./gradlew assembleRelease
```

Requires signing environment variables — see `DEPLOY.md` for generating a keystore and setting `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`. Output: `app/build/outputs/apk/release/app-release.apk`.

## Release App Bundle (for Play Store upload)

```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`.

## Run tests

```bash
# Static analysis
./gradlew lintDebug

# JVM unit tests (repository, view model, parsers) — includes Robolectric tests
./gradlew testDebugUnitTest

# Compose screenshot tests (Roborazzi)
./gradlew recordRoborazziDebug   # capture/update golden images
./gradlew verifyRoborazziDebug   # verify against golden images

# Instrumented tests on a connected device/emulator (requires Hilt test runner, see below)
./gradlew connectedDebugAndroidTest
```

## Full "does everything still build" check (mirrors CI)

```bash
./gradlew lintDebug testDebugUnitTest assembleDebug
```

## Notes on this project's build setup

- **Kotlin/Java target:** 17, set via `compileOptions` + `kotlin { jvmToolchain(17) }` in `app/build.gradle.kts`.
- **Dependency injection:** Hilt. `QrApplication` is annotated `@HiltAndroidApp`; `MainActivity` is `@AndroidEntryPoint`; `QrViewModel` is `@HiltViewModel`. DI modules live in `app/src/main/java/com/example/di/`.
- **KSP** (not kapt) is used for Room, Moshi, and Hilt annotation processing — faster and is what the version catalog is already wired for.
- **Instrumented tests use a custom runner** (`com.example.HiltTestRunner`, set via `testInstrumentationRunner` in `app/build.gradle.kts`) so `@HiltAndroidTest` classes get a swappable Hilt test component. If you add plain (non-Hilt) instrumented tests, they still work fine under this runner.
- If Android Studio suggests a Gradle/AGP upgrade after you open the project, **decline it** unless you've read `gradle/wrapper/gradle-wrapper.properties` and `gradle/libs.versions.toml` and intentionally want to move versions — this project pins specific, tested versions.

## Troubleshooting

| Symptom | Likely fix |
|---|---|
| `Could not resolve com.google.dagger:hilt-android:...` | Check internet access to `google()`/`mavenCentral()`; corporate proxies sometimes block these. |
| `Unsupported class file major version` | Your `JAVA_HOME` / Gradle JDK is older than 17. Fix in Android Studio's Gradle JDK setting or `export JAVA_HOME=<path-to-jdk-17>`. |
| `SDK location not found` | Create `local.properties` (gitignored) with `sdk.dir=/path/to/Android/sdk`, or let Android Studio create it automatically on first sync. |
| Hilt: `[Dagger/MissingBinding]` | You added an `@Inject` constructor or a new dependency without a matching `@Provides`/`@Binds` in a module under `di/`. |
| `google-services.json` warnings | Expected — this project sets `missingGoogleServicesStrategy = WARN` since there's no Firebase project wired up by default. Safe to ignore unless you add real Firebase features. |
