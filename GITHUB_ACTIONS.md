# GitHub Actions

`.github/workflows/android.yml` defines two jobs.

## `build` (runs on every push/PR to `main`)

1. Checkout, JDK 17, Android SDK, Gradle setup (with dependency caching via `gradle/actions/setup-gradle`).
2. `./gradlew lintDebug` — fails the job on lint errors (not just warnings, per default lint severity).
3. `./gradlew testDebugUnitTest` — all JVM/Robolectric unit tests (parser, repository, view model).
4. Uploads lint + unit test HTML reports as workflow artifacts regardless of pass/fail (`if: always()`), so you can download and inspect failures from the Actions UI.
5. `./gradlew assembleDebug` — always runs, uploaded as an artifact. Useful for manual QA on a PR without needing a local build.
6. **Conditionally** (only if the `KEYSTORE_BASE64` secret is set): decodes the keystore, runs `assembleRelease` and `bundleRelease`, uploads the signed APK and AAB as artifacts.
   - If the secret isn't set, the job logs a `::warning::` and skips those steps rather than failing — so the workflow is green on a fresh fork/clone before you've set up signing.

## `instrumentation-tests` (runs on every push/PR to `main`)

Boots a hardware-accelerated Android emulator (API 30, `google_apis`, x86_64) via `reactivecircus/android-emulator-runner` and runs `connectedDebugAndroidTest` — this includes the Hilt-powered Compose UI smoke test (`MainActivityUiTest`). Uploads the instrumentation HTML report as an artifact on failure or success.

This job is the most likely one to need tuning over time: emulator boot flakiness, API-level matrix expansion, or splitting it out to only run on `main` (not every PR) if it becomes slow — none of that is configured defensively here, so watch its run time as the test suite grows.

## Required repository secrets for full functionality

| Secret | Required for | If missing |
|---|---|---|
| `KEYSTORE_BASE64` | Signed release APK/AAB | Release build steps are skipped with a warning; rest of the workflow still passes |
| `STORE_PASSWORD` | Signed release APK/AAB | Same as above |
| `KEY_PASSWORD` | Signed release APK/AAB | Same as above |

See `DEPLOY.md` for how to generate and encode the keystore.

## Extending this workflow

- **Play Store auto-upload:** add a step using `r0adkll/upload-google-play` after `bundleRelease`, gated behind a tag push (e.g. `v*`) rather than every merge to `main`, and store the Play Developer API service-account JSON as another secret (never in the repo).
- **Static analysis beyond lint:** consider `detekt` or `ktlint` as additional steps if the team wants stricter Kotlin style enforcement than Android Lint covers.
