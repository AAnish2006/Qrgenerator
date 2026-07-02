# Deploy

## 1. Generate an upload keystore (one-time, per app)

```bash
keytool -genkeypair -v \
  -keystore my-upload-key.jks \
  -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
```

You'll be prompted for a store password, key password, and identity details. **Store this file and both passwords somewhere safe (e.g. a password manager) — if you lose them, you cannot update your app on Play Store under the same listing without going through Google's account-recovery process.**

`my-upload-key.jks` and `debug.keystore` are already covered by `.gitignore` — **never commit a keystore to git.**

## 2. Local release builds

`app/build.gradle.kts` reads signing config from environment variables, defaulting `KEYSTORE_PATH` to `${rootDir}/my-upload-key.jks` if unset:

```bash
export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
export STORE_PASSWORD="<your store password>"
export KEY_PASSWORD="<your key password>"

./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`.

## 3. GitHub Actions secrets (for CI-signed builds)

The workflow at `.github/workflows/android.yml` only builds & signs a release APK/AAB if these repo secrets are present (**Settings > Secrets and variables > Actions**):

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | `base64 -i my-upload-key.jks \| pbcopy` (macOS) or `base64 -w0 my-upload-key.jks` (Linux) — paste the output |
| `STORE_PASSWORD` | Your keystore password |
| `KEY_PASSWORD` | Your key password |

Without these secrets, CI still runs lint/unit tests and builds an **unsigned debug APK** — it just skips the release APK/AAB steps (with a warning) rather than failing the whole workflow.

## 4. Play Console setup (first release only)

1. Create the app in [Play Console](https://play.google.com/console).
2. **App content** → complete the required declarations: Privacy Policy (host `PRIVACY_POLICY_TEMPLATE.md`'s content somewhere public and link it), Data safety (`DATA_SAFETY_CHECKLIST.md` maps directly to the form's sections), Ads (this app shows none), Content rating questionnaire, Target audience.
3. **Monetize > Products**: create the three products referenced in `BillingManager.kt` with matching IDs:
   - `qr_pro_monthly` (subscription)
   - `qr_pro_yearly` (subscription)
   - `qr_pro_lifetime` (one-time product)
4. **Testing > Internal testing**: upload your first `.aab`, add testers, roll out. Play Billing purchases only work through a Play-installed build (internal testing track or higher) — sideloaded debug builds cannot complete real purchases.
5. Once verified in internal testing, promote through Closed → Open → Production tracks per Play Console's review flow.

## 5. Versioning

Bump both fields in `app/build.gradle.kts` → `defaultConfig` before every Play Store upload:

```kotlin
versionCode = 2      // must strictly increase on every upload, integer
versionName = "1.1"  // human-readable, shown to users
```

## 6. Release notes

Use `RELEASE_NOTES_TEMPLATE.md` as a starting point for each release's "What's new" text in Play Console.
