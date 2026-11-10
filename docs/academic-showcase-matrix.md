# Prottasha Academic Showcase Evidence Matrix

Each row must link a rubric-visible capability to real implementation, automated proof, and a repeatable TECNO demonstration step. `PENDING` is never presented as passed.

| Capability | Implementation evidence | Automated proof | TECNO proof | Status |
|---|---|---|---|---|
| Application subclass | `ProttashaApp` + stable `AppGraph`/executors | `ApplicationBootstrapTest` 2/2 on API 33 | BG7 instrumentation initializes app and Firebase | PASS |
| View Binding | Auth, onboarding, account, shell, Today Activities/Fragments | Compile + focused UI suites | BG7 onboarding and five-tab shell 4/4 | PARTIAL |
| Semantic dark visual system | Obsidian Hope tokens; dark `color_on_hope` | Resource bootstrap assertion + lint 0 errors | BG7 resource assertion; full 200% walkthrough remains Task 14 | PARTIAL |
| Result/time primitives | Explicit `AppResult`; DST-safe `DayBoundary` | Core unit suite covers UTC, Dhaka, DST gap/overlap, timezone, invalid cutoffs | Feature integration remains Tasks 7–12 | PASS |
| Opaque UID namespace | SHA-256 database/key/file names contain no raw UID/email | `UserDatabaseIdentityTest` + encrypted A/B suite | BG7 A→B→A namespaces remain isolated | PASS |
| Multiple Activities | Auth, onboarding, main shell, account, support, settings | Manifest/UI tests | Verified-session onboarding → shell path passes | PARTIAL |
| Room CRUD and migration | UID-scoped SQLCipher Room v1, exported schema, FK enforcement, HMAC key preflight | Session unit suite + SQLCipher/Room instrumentation 8/8 | BG7 CRUD/reopen/A–B/quarantine/restore pass; first real migration arrives with v2 | PARTIAL |
| Encrypted session lifecycle | Stable `AppGraph` provider/manager; close-before-switch/sign-out; verified background owner only | `UserSessionManagerTest`, `SessionAccessPolicyTest`, bootstrap regression | BG7 missing/corrupt/wrong-key fail closed without replacing ciphertext | PASS |
| Account deletion safety | Password reauth → local quarantine → own cloud profile → Auth last → key destruction | Coordinator unit contract covers rollback, recent-login failure, interruption, and cleanup retry | Account UI 2/2; no live account was deleted during Task 5 | PARTIAL |
| Legacy compatibility boundary | Read-only source snapshot with SHA-256, counts, prototype flags, idempotent encrypted import journal | `LegacyImportFrameworkTest` 4/4 | BG7 source remains byte-for-byte unchanged; feature mappers remain deferred | PASS |
| Versioned onboarding | Saved purpose/identity draft; persistence only on finish; honest skip | `OnboardingAndShellTest` 4/4 | BG7 recreation, skip, finish, and version gate pass | PASS |
| Five-tab main shell | Today, Habits, Guard, Insights, Circle with Lifeline/settings | Navigation UI/device tests | BG7 shows honest empty Today and predictable Back | PASS |
| Event-ledger habits | UID-encrypted Room v3 habits/events/daily aggregates for Build, Avoid, Amount | Habit engine unit suite + habits instrumentation 3/3 | BG7 creates all three types, preserves account isolation, rejects duplicate nonces | PARTIAL |
| Habit depth and reminders | Room v4 notes/images/reminders, calendar history, one-shot alarms, neutral widget | Reminder unit suite, reminders 3/3, concrete legacy import 1/1 | BG7 malformed actions fail closed; source bytes preserved | PARTIAL |
| Guard rule configuration | Per-target Pause/Budget/Block, essential-app exclusion, launcher-only picker | Guard engine unit suite + Guard instrumentation 4/4 | BG7 stores independent modes and shows honest grayscale capability | PASS |
| Private usage observation | COMPLETE/PARTIAL/UNAVAILABLE aggregation, selected-package filter, host-only browser normalization | Usage/domain unit suite + disclosure device test | BG7 consent is separate/versioned; service retrieves no window content | PARTIAL |
| Guard intervention and focus | Accessibility overlay, exactly-once state machine, persisted user-started focus service | Intervention/focus unit suite; BG7 modes 3/3 and focus 1/1 | Pause has immediate Step Back; Block has no continue; service is non-sticky | PASS |
| Insights/reflection/support | Truthful follow-through snapshots, private fact/interpretation/lesson, sourced dial directory | Insights/support unit suite + BG7 flow 2/2 | Zero data is explicit; rapid reopen excluded; 999 entry shows source/date/hours | PASS |
| BroadcastReceiver | Reminder and network/power state receivers | Receiver tests | Reboot/time/airplane scenario | PENDING |
| Foreground Service | Active focus session only | Service tests | Pause/resume/end notification | PENDING |
| Content Provider | User-selected accountability contact | Permission/picker tests | Select or manually enter partner | PENDING |
| HTTP and JSON | Sourced support/quote repository | Parser/fallback tests | Online then offline result | PENDING |
| Firebase Authentication | Password-policy-matched state machine + Sign In/Create/Verify/Reset account gate | Auth repository/profile unit suite; Auth instrumentation package 5/5; rules 12/12 | BG7 signed-out routing/validation/rotation/Back pass; disposable live lifecycle remains Task 15 | PARTIAL |
| UID cloud profile | Minimal own profile with separate generated pseudonym | Profile contract tests + Firestore rules 12/12 | Live bootstrap with disposable account remains final Firebase flow | PARTIAL |
| Firestore | Membership-scoped closed pod with immutable roles/messages/reports | Emulator attack suite 16/16 overall; remote hash exact | UI 2/2; rules deployed, two-user membership remains unprovisioned | PARTIAL |
| Realtime Database | Mirrored-membership presence with own-write-only authority | Emulator membership/cross-write tests; remote semantic hash exact | Rules deployed; live disconnect presence awaits membership | PARTIAL |
| Local encrypted image | Bounded metadata-scrubbed JPEG + UID AES-GCM store | `EncryptedPrivateFileStoreTest` 5/5 on API 33 | BG7 key-loss, tamper, A/B, reopen, quarantine/restore proof; feature UI integration remains | PARTIAL |
| Rotation and saved state | Forms, onboarding, editors, timers | Recreation tests | Rotate during each critical draft | PENDING |
| Privacy/export | Salted PIN primitive, honest UI-lock model, PBKDF2 + AES-GCM archive, backup exclusion | Privacy unit suite + quality device tests 2/2 | Export picker writes ciphertext only; app backup disabled | PARTIAL |
| Accessibility and dynamic UI | Labels, 48dp targets, scalable scroll layouts, chart alternatives, reduced animation | Quality tests + lint | Checklist complete except final manual TalkBack/200% capture | PARTIAL |
