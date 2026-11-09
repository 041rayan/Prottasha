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
| Room | 2.7.2 | Accepted with SQLCipher; Room 2.8.x is blocked by upstream `SupportOpenHelperFactory` transaction/connection incompatibility |
| SQLCipher Android | 4.11.0 | Accepted on BG7; 4.20.0 loaded but hung in uninterruptible native state during direct Room-free open |

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

## Task 4 encrypted database gate

| Gate | Result |
|---|---|
| Direct SQLCipher 4.11 open/write/read/close | PASS on TECNO BG7 |
| SQLCipher + Room v1 combined instrumentation | PASS, 8/8 on TECNO BG7 |
| Encrypted database contract | PASS: ciphertext header, CRUD/reopen, foreign keys, missing/corrupt/wrong key, A/B isolation, background owner, quarantine/restore |
| Session lifecycle unit contract | PASS: A→B→A, close-before-switch/sign-out, failed switch, observer disposal, UI-lock policy |
| `testDebugUnitTest assembleDebug lintDebug compileDebugAndroidTestJavaWithJavac` | PASS; lint 0 errors, 235 inventoried warnings |

The `NewerVersionAvailable` warning for SQLCipher is intentionally retained: upgrading requires the same direct-native and Room lifecycle proof on the BG7.

## Task 5 account and legacy boundary gate

| Gate | Result |
|---|---|
| Account deletion coordinator unit contract | PASS: reauth, ordered cloud/Auth deletion, rollback, interruption recovery, post-Auth retry |
| Legacy import framework on TECNO BG7/API 33 | PASS, 4/4 tests |
| Encrypted local + legacy device group | PASS, 18/18 tests |
| Auth/account/bootstrap device group | PASS, 9/9 tests |
| Legacy source retention | PASS: read-only inspection and byte-for-byte source preservation |
| Prototype seed boundary | PASS: existing seed signatures are flagged; newly created legacy databases remain empty |

Task 5 performs no live account deletion and no legacy-source contraction. The cloud gateway deletes only the current UID profile until Task 13 adds bounded Circle-owned records.
