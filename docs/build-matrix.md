# Prottasha Academic Build Matrix

**Captured:** 2026-11-10
**Reference runtime:** TECNO BG7, Android 13/API 33

## Starting matrix

| Component | Starting value | Status |
|---|---:|---|
| Android Gradle Plugin | 8.11.1 | Builds; compatibility gate open |
| Gradle | 9.2.1 | Builds |
| Java source/target | 17 | Builds on host JDK 22 |
| compileSdk | 36 | Accepted |
| targetSdk | 35 | Accepted for side-loaded academic APK |
| minSdk | 26 | Accepted |
| Google Services plugin | 4.4.2 | Applied and processes debug config |
| Firebase BOM | 34.18.0 | Accepted: Auth 24.2.0, RTDB 22.0.1, Firestore 26.6.0; no Kotlin metadata diagnostic |
| Room | 2.8.5 | Accepted |
| SQLCipher Android | 4.20.0 | Packages and installs on BG7; encrypted DB lifecycle proof belongs to Task 4 |

## Fresh baseline

| Gate | Result |
|---|---|
| `./gradlew testDebugUnitTest assembleDebug` | PASS |
| `./gradlew lintDebug` | FAIL: 1 error, 238 warnings |
| First lint error | `ProttashaFocusService.java:49` missing notification permission handling |
| Firebase rules emulator | 12/12 PASS before Task 1 |
| Debug Google Services config | Exact remote match, including RTDB URL |

No lint baseline will be added. The accepted matrix above produces clean compilation and no dependency metadata diagnostics.

## Task 1 accepted gate

| Gate | Result |
|---|---|
| `compileDebugAndroidTestJavaWithJavac` | PASS |
| `testDebugUnitTest assembleDebug lintDebug` | PASS |
| Lint report | 0 errors, 237 inventoried warnings |
| `ApplicationBootstrapTest` on TECNO BG7/API 33 | PASS, 2/2 tests |

The warning inventory remains visible and must decrease as prototype screens are replaced; warnings are not hidden behind a baseline.

### Warning ownership

| Count | Lint ID | Planned owner |
|---:|---|---|
| 117 | `HardcodedText` | Tasks 3, 6–14 replace prototype layouts with string resources |
| 46 | `SetTextI18n` | Owning feature slice introduces formatted/plural resources |
| 44 | `UnusedResources` | Incremental replacement plus final Task 14 cleanup |
| 12 | `Overdraw` | Tasks 6–14 canonical layouts/theme surfaces |
| 2 | `NotifyDataSetChanged` | ListAdapter/DiffUtil replacements in feature slices |
| 1 each | `CustomSplashScreen`, `Autofill`, `TextFields`, `SmallSp` | Auth and visual shell Tasks 3/6 |
| remainder | SDK/version/typography/plural/data-extraction/default-locale | Task-specific replacement or Task 14 final quality pass |
