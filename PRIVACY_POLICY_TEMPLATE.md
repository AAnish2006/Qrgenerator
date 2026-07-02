# Privacy Policy — QR Generator & Reader

_Last updated: [DATE]_

**This is a starting template — read it, edit the bracketed parts, and have
it reviewed before publishing. It reflects what the app does as of this
codebase; if you add features (a real backend, ads, analytics), update this
document to match before shipping.**

## Summary

QR Generator & Reader ("the App") is developed by [YOUR NAME / COMPANY]. This
policy explains what data the App accesses and how it's used.

## Data we collect

| Data | Collected? | Where it's stored | Why |
|---|---|---|---|
| Camera feed | Yes, while scanning | Processed on-device only, never saved or transmitted | To detect and decode QR codes (ZXing library) |
| Scan/generation history | Yes | Locally on your device only (Room database) | So you can view/re-use past QR codes. Never uploaded anywhere. |
| Account name/email | Yes, if you sign in | Locally on your device only, in encrypted storage (`EncryptedSharedPreferences`, Android Keystore-backed) | To personalize the app. **Note:** login is currently a local/simulated profile — see "Accounts" below. |
| Purchase data (Premium) | Yes, if you buy Premium | Handled entirely by Google Play Billing; the App only receives a purchase confirmation | To unlock Premium features |
| Analytics / advertising identifiers | No | — | The App contains no ad SDKs and no analytics SDKs |

## Accounts

Signing in currently stores the name/email you type locally, on your device
only — it is **not** sent to any server, and there is no real authentication
behind it yet. If a future version adds real sign-in (e.g. Firebase Auth or
a custom backend), this policy must be updated to describe what the
authentication provider collects.

## Camera permission

The App requests camera access solely to scan QR codes in real time. Camera
frames are processed on-device and are never recorded, stored, or
transmitted.

## Payments

Premium purchases are processed by Google Play Billing. We do not receive or
store your payment card details — Google handles all payment information
per its own privacy policy: https://policies.google.com/privacy

## Data sharing

We do not sell, rent, or share your data with third parties. The App makes
no network calls that transmit your personal data (see the table above).

## Data deletion

- **Scan/generation history:** delete individual items or clear all history from within the App, or uninstall the App to remove all local data.
- **Account/profile:** tap "Sign Out" in the App to clear your locally-saved name/email.
- **Purchases:** manage or cancel subscriptions via Google Play > Subscriptions.

## Children's privacy

The App is not directed at children under 13 and does not knowingly collect
data from children.

## Changes to this policy

We may update this policy as the App changes. Continued use of the App after
changes means you accept the updated policy.

## Contact

[YOUR EMAIL / SUPPORT CONTACT]

---
_Before publishing: host this file's content at a public, stable URL (e.g. a
GitHub Pages page or your own website) — Play Console requires a live URL,
not a file upload._
