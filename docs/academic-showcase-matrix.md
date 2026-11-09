# Prottasha Academic Showcase Evidence Matrix

Each row must link a rubric-visible capability to real implementation, automated proof, and a repeatable TECNO demonstration step. `PENDING` is never presented as passed.

| Capability | Implementation evidence | Automated proof | TECNO proof | Status |
|---|---|---|---|---|
| Application subclass | `ProttashaApp` + stable `AppGraph`/executors | `ApplicationBootstrapTest` 2/2 on API 33 | BG7 instrumentation initializes app and Firebase | PASS |
| View Binding | Active Activities/Fragments/ViewHolders | Compile + UI tests | Navigate each active screen | PENDING |
| Semantic dark visual system | Obsidian Hope tokens; dark `color_on_hope` | Resource bootstrap assertion + lint 0 errors | BG7 resource assertion; full 200% walkthrough remains Task 14 | PARTIAL |
| Multiple Activities | Auth, onboarding, editor/detail, focus, support, pod, settings | Manifest/UI tests | Showcase navigation | PENDING |
| Room CRUD and migration | UID-scoped SQLCipher database | DAO/migration tests | Create/edit/restart/switch | PENDING |
| BroadcastReceiver | Reminder and network/power state receivers | Receiver tests | Reboot/time/airplane scenario | PENDING |
| Foreground Service | Active focus session only | Service tests | Pause/resume/end notification | PENDING |
| Content Provider | User-selected accountability contact | Permission/picker tests | Select or manually enter partner | PENDING |
| HTTP and JSON | Sourced support/quote repository | Parser/fallback tests | Online then offline result | PENDING |
| Firebase Authentication | Verified Email/Password lifecycle | Auth unit/UI tests | Register/verify/reset/switch/delete | PENDING |
| Firestore | Membership-scoped closed pod | Emulator attack tests | Two-user message/report/block flow | PENDING |
| Realtime Database | Membership-scoped presence | Emulator rules tests | Connect/disconnect presence | PENDING |
| Local encrypted image | UID AES-GCM private file store | Tamper/isolation tests | Import/view/delete after restart | PENDING |
| Rotation and saved state | Forms, onboarding, editors, timers | Recreation tests | Rotate during each critical draft | PENDING |
| Accessibility and dynamic UI | TalkBack, 48dp, 200% font, reduced motion | Quality tests + lint | Accessibility checklist | PENDING |
