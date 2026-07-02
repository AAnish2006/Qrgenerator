<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# QR Generator & Reader

A Jetpack Compose Android app to scan, generate, customize, and manage QR
codes, with a free tier + Google Play Billing–powered Premium tier.

Originally scaffolded in Google AI Studio; this repo has since been made
into a standard, buildable Android Studio / Gradle project — see the docs
below for details.

## Requirements

- **Android Studio:** Otter (2025.2) or newer
- **JDK:** 17
- **Compile / target SDK:** 36
- **Min SDK:** 24 (Android 7.0)
- **Kotlin:** 2.2.10 · **AGP:** 9.1.1 · **Gradle:** 9.1.0 (via the included wrapper)

Full setup steps: **[INSTALL.md](INSTALL.md)**

## How to build

```bash
./gradlew assembleDebug     # debug APK
./gradlew assembleRelease   # release APK (needs signing config — see DEPLOY.md)
./gradlew bundleRelease     # release App Bundle (.aab), for Play Store upload
```

Full build/test commands and troubleshooting: **[BUILD.md](BUILD.md)**

## How to run

1. Open the project root in Android Studio and let it sync.
2. Copy `.env.example` → `.env`, fill in `GEMINI_API_KEY` if you use the Gemini-powered features (everything else can stay blank).
3. Run on an emulator or physical device (▶ in Android Studio, or `./gradlew installDebug`).
4. To test real Premium purchases, create the three products below in **Play Console > Monetize > Products** with matching IDs, then install via the **Internal testing** track (Play Billing does not work against real products on sideloaded debug builds):
   - `qr_pro_monthly` (subscription)
   - `qr_pro_yearly` (subscription)
   - `qr_pro_lifetime` (one-time product)

## How to sign & deploy

Keystore generation, GitHub Secrets for CI signing, and the Play Console
release flow: **[DEPLOY.md](DEPLOY.md)**

## CI

Every push/PR runs lint, unit tests, and an instrumented UI test in GitHub
Actions, and builds a debug APK (plus a signed release APK/AAB if signing
secrets are configured): **[.github/workflows/android.yml](.github/workflows/android.yml)**,
documented in **[GITHUB_ACTIONS.md](GITHUB_ACTIONS.md)**.

## Architecture

- **UI:** Jetpack Compose, Material 3
- **Pattern:** MVVM + Repository
- **DI:** Hilt (`QrApplication` → `@HiltAndroidApp`, `MainActivity` → `@AndroidEntryPoint`, `QrViewModel` → `@HiltViewModel`; modules in `app/src/main/java/com/example/di/`)
- **Local persistence:** Room (`data/AppDatabase.kt`) for scan/generation history; `EncryptedSharedPreferences` (Android Keystore-backed) for profile/theme/entitlement cache
- **Async:** Kotlin Coroutines + Flow
- **Networking libs present (Retrofit/OkHttp/Moshi):** wired for a *future* backend — see "Security & Backend" below; nothing calls them yet
- **QR encode/decode:** ZXing, entirely on-device
- **Payments:** Google Play Billing Library, entitlement re-verified against Play on every launch (never trusted from local cache alone)

## Testing

- `app/src/test/` — JVM unit tests: `QrCodeParserTest`, `QrRepositoryTest` (in-memory Room), `QrViewModelTest` (daily-limit/theme/session logic), plus a Roborazzi Compose screenshot test.
- `app/src/androidTest/` — instrumented Compose UI smoke test (`MainActivityUiTest`) running under a Hilt-aware test runner (`HiltTestRunner`).

Run them via `BUILD.md`'s test commands.

## Play Store prep

- **[PRIVACY_POLICY_TEMPLATE.md](PRIVACY_POLICY_TEMPLATE.md)** — starting point; host publicly and link it in Play Console.
- **[DATA_SAFETY_CHECKLIST.md](DATA_SAFETY_CHECKLIST.md)** — worksheet mapped to Play Console's Data Safety form.
- **[RELEASE_NOTES_TEMPLATE.md](RELEASE_NOTES_TEMPLATE.md)** — template for each release's "What's new" text.

## Security & Backend

This app currently has **no backend server** — it's fully self-contained:

| Feature | Implementation | Notes |
|---|---|---|
| Login | Simulated locally, stored in `EncryptedSharedPreferences` | No real OAuth/session. See "Making auth real" below if you need it. |
| Premium / Payments | **Google Play Billing** (`BillingManager.kt`) | Real purchases. Entitlement is re-verified against Play on every launch, never trusted from local cache alone. |
| QR generate/scan | On-device (ZXing) | No network call, nothing to secure server-side. |
| Secrets | `.env` (gitignored) → Secrets Gradle Plugin → build-time resources | `GEMINI_API_KEY` is the only secret in play today. Never hardcode API keys in Kotlin/XML. |
| Traffic | `network_security_config.xml` blocks all cleartext (HTTP) | Any future backend call must be HTTPS. |
| Release builds | R8 minify + resource shrink enabled | Reduces reverse-engineering surface; debug-only premium toggle is compiled out via `BuildConfig.ENABLE_DEBUG_PREMIUM_TOGGLE`. |

**Making auth real:** the login screen currently just saves whatever name/email you type. To replace it with real authentication, the two common options are (a) Firebase Authentication — this project already has the Firebase BOM and `google-services` plugin wired in, so it's a small lift — or (b) your own backend issuing JWTs. Either way, set the backend's base URL via `AUTH_API_BASE_URL` in `.env` (already scaffolded in `NetworkConfig.kt`) rather than hardcoding it, and never store long-lived tokens outside `EncryptedSharedPreferences` / the Android Keystore.

**Adding a real payments backend:** Play Billing above requires no server. If you need server-side receipt verification (recommended before shipping to real users, to prevent purchase spoofing), your backend calls the Play Developer API using a service-account key — that key **must live on the server only** and must never be added to this app's `.env`, since anything in `.env` ships inside the compiled app.

**Adding a QR generation/redirect backend:** if you add "dynamic" QR codes that redirect through your own server (already teased in the Premium feature list), point the client at it via `DYNAMIC_QR_API_BASE_URL` in `.env` and call it through `NetworkConfig.kt` — the Retrofit/OkHttp/Moshi dependencies and a shared `NetworkModule` (Hilt) are already present for this purpose.
