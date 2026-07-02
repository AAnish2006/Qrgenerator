# Play Console — Data Safety Checklist

Use this as a worksheet before filling out **App content > Data safety** in
Play Console. It reflects this codebase as-is; re-check it whenever you add
a feature that touches data.

## Does your app collect or share any of the required user data types?

Based on this codebase today: **mostly No**, with two nuances to declare
honestly.

### Personal info

- [ ] Name — collected locally only (login screen), **not** transmitted off-device. Declare as "Collected, not shared, not required, data can be deleted" if you count local-only storage as collection (Play's definition is broad — when in doubt, declare it).
- [ ] Email address — same as above.

### Photos / camera

- [x] Photos or videos — the App uses the **camera** to scan QR codes. Frames are processed in memory and never stored or transmitted. Under Play's data safety definitions, real-time processing without storage/transmission generally does **not** count as "collection," but review Google's current camera-permission guidance before submitting, since policy wording changes.

### Financial info

- [ ] Purchase history — handled entirely by Google Play Billing; the App itself does not collect or transmit payment details. Google Play's own data safety declarations cover this at the platform level.

### App activity

- [ ] App interactions / in-app search history — the App's local scan/generation history stays on-device (Room DB), never transmitted. Not shared with third parties.

## Is all of the user data collected by your app encrypted in transit?

- N/A for most flows (nothing is transmitted). If you later add a backend (`NetworkConfig.kt`'s optional base URLs), you must answer **Yes** — `network_security_config.xml` already blocks cleartext HTTP app-wide, so any future traffic will be HTTPS.

## Do you provide a way for users to request that their data be deleted?

- **Yes.** In-app: delete individual history items or "Clear all" from the history screen; "Sign Out" clears the locally-stored profile. Uninstalling removes all local data. Purchases are managed via Google Play's subscription management, independent of the App.

## Third-party SDKs to declare

Check `app/build.gradle.kts` dependencies against this list before submitting — this list must be kept in sync as dependencies change:

- Google Play Billing Library (`billing-ktx`) — purchases
- Firebase BOM / `firebase-ai` / `firebase-appcheck-recaptcha` — **only if actually initialized and used**; if `google-services.json` is absent (default state, `missingGoogleServicesStrategy = WARN`), these are inert and may not need declaring. Confirm before submitting.
- ZXing (`core`) — on-device QR encode/decode, no data leaves the device
- Retrofit/OkHttp/Moshi — present as **infra for a possible future backend**; if no backend is wired up at release time, there is no actual network data collection to declare from these

## Before every submission

1. Re-read this file against the current `app/build.gradle.kts` dependency list — flag anything new.
2. Re-read `NetworkConfig.kt` / `.env` — if `DYNAMIC_QR_API_BASE_URL` or `AUTH_API_BASE_URL` are now set to a real backend, this checklist and the Privacy Policy both need a real update, not just a template.
3. Cross-check against Play Console's live Data Safety form fields directly — Google updates the form's categories periodically; this file is a starting worksheet, not a substitute for reading the current form.
