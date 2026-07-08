<p align="center">
  <img src="docs/assets/icon.png" alt="Oar" width="96" height="96" />
</p>

<h1 align="center">Oar</h1>

<p align="center">
  <strong>Track and manage your expenses — calmly, and on your own terms.</strong>
</p>

<p align="center">
  Oar is a local-first Android app for tracking spending and managing budgets. Your financial data<br/>
  stays on your device by default, with optional end-to-end encrypted backup to <em>your own</em> Google Drive.
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white">
  <img alt="Min SDK" src="https://img.shields.io/badge/API-29%2B-brightgreen">
  <a href="https://github.com/remi-naz/Oar/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/remi-naz/Oar"></a>
  <a href="https://github.com/remi-naz/Oar/issues"><img alt="GitHub issues" src="https://img.shields.io/github/issues/remi-naz/Oar"></a>
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=dev.ridill.oar" target="_blank" rel="noopener noreferrer"><strong>Get it on Google Play</strong></a>
  &nbsp;·&nbsp;
  <a href="https://github.com/remi-naz/Oar/issues">Report a Bug</a>
  &nbsp;·&nbsp;
  <a href="https://github.com/remi-naz/Oar/issues">Request a Feature</a>
  &nbsp;·&nbsp;
  <a href="https://remi-naz.github.io/Oar/privacy-policy.html" target="_blank" rel="noopener noreferrer">Privacy Policy</a>
</p>

---

## Table of Contents

- [About](#about)
- [Screenshots](#screenshots)
- [Features](#features)
- [Built With](#built-with)
- [Getting Started](#getting-started)
- [Contributing](#contributing)
- [Authors](#authors)

## About

<p align="center">
  <img src="docs/assets/feature.png" alt="Oar feature preview" width="560">
</p>

Oar helps individuals manage their finances effectively. Log what you earn and spend, group and tag
it however makes sense to you, set a monthly budget, and let scheduled reminders keep you on track —
all in a clean, distraction-free interface.

The guiding principle is **local-first**: everything lives on your device unless *you* choose to
turn
on backup. No accounts required to get started, no data leaving your phone by default.

## Screenshots

<p align="center">
  <img src="docs/assets/screenshots/screenshot-1.jpg" width="19%" alt="Balance & recent spends">
  <img src="docs/assets/screenshots/screenshot-2.jpg" width="19%" alt="Manage spending">
  <img src="docs/assets/screenshots/screenshot-3.jpg" width="19%" alt="Schedule reminders">
  <img src="docs/assets/screenshots/screenshot-4.jpg" width="19%" alt="Group & tag transactions">
  <img src="docs/assets/screenshots/screenshot-5.jpg" width="19%" alt="Stay on top of finances">
</p>

## Features

|                            |                                                                                            |
|----------------------------|--------------------------------------------------------------------------------------------|
| **Transactions**           | Log income and expenses, then browse them in a paged, filterable list.                     |
| **Folders**                | Group related transactions (a trip, a project) and track their combined total.             |
| **Tags**                   | Categorize spending and filter your history by tag.                                        |
| **Budget Cycles**          | Set a monthly budget and track your balance against it across cycles.                      |
| **Schedules**              | Set up recurring transactions with reminder notifications so nothing slips.                |
| **Aggregations**           | See spending broken down and summarized over time.                                         |
| **Encrypted Drive Backup** | Optionally sign in and back up/restore to your own Google Drive — encrypted before upload. |
| **Biometric App Lock**     | Lock the app behind device biometrics for extra privacy.                                   |
| **Local-First Storage**    | All data lives on-device by default; nothing is sent anywhere unless you enable backup.    |

## Built With

- **Kotlin** & **Jetpack Compose** for the UI, with **Navigation 3** for app navigation
- **Room** (SQLite) for local persistence, with **Paging 3** for the transaction lists
- **Hilt** for dependency injection
- **WorkManager** for scheduled/background work (reminders, backups)
- **DataStore Preferences** for app settings
- **AndroidX Biometric & Security Crypto** for app lock and encrypted backups
- **jBCrypt** for password/PIN hashing in the encrypted backup flow
- **Firebase** (Auth, Crashlytics, Analytics, Remote Config) & the **Google Drive API** for optional
  sign-in and cloud backup
- **Retrofit & OkHttp** for networking with the Google APIs
- **Coil** for image loading
- **Lottie** for onboarding and empty-state animations
- **Timber** for logging
- **Keval** for expression evaluation in the amount input field

## Getting Started

**1. Clone the repo**

```bash
git clone https://github.com/remi-naz/Oar.git
```

**2. Build** — open the project in Android Studio, or from the CLI:

```bash
./gradlew assembleInternalDebug
```

**3. Install** on a connected device / emulator:

```bash
./gradlew installInternalDebug
```

> [!NOTE]
> Google sign-in and Drive backup require your own `google-services.json` under `app/`, since
> Firebase / Google credentials aren't checked into the repo. Everything else runs without it.

## Contributing

Contributions are welcome! To propose a change:

1. **Fork** the project
2. Create your feature branch — `git checkout -b feature/AmazingFeature`
3. Commit your changes — `git commit -m 'Add some AmazingFeature'`
4. Push to the branch — `git push origin feature/AmazingFeature`
5. Open a **Pull Request**

Found a bug or have an idea? [Open an issue](https://github.com/remi-naz/Oar/issues).

## Authors

- **Ridill** — *Android Developer* — <a href="https://github.com/remi-naz" target="_blank" rel="noopener noreferrer">@remi-naz</a>

---

<div align="center">

Built with care by <a href="https://github.com/remi-naz" target="_blank" rel="noopener noreferrer">Ridill</a> &nbsp;·&nbsp; <a href="https://remi-naz.github.io/Oar/privacy-policy.html" target="_blank" rel="noopener noreferrer">Privacy Policy</a> &nbsp;·&nbsp; <a href="https://remi-naz.github.io/Oar/data-deletion.html" target="_blank" rel="noopener noreferrer">Delete My Data</a>

</div>
