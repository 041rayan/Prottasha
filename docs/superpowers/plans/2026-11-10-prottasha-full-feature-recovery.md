# Prottasha Full-Feature Recovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the current foundation/demo skeleton into the complete, reachable academic product defined by the approved Prottasha specifications.

**Architecture:** Keep the existing Java/XML, UID-scoped SQLCipher, Firebase, repository, and domain-engine foundations. Finish the product through vertical milestones that connect storage → domain behavior → ViewModel state → reachable UI → TECNO BG7 proof. A task is not complete merely because its entities, helpers, or unit tests exist.

**Tech Stack:** Java 17, Android XML Views/View Binding, Material Components, Navigation, ViewModel/LiveData/SavedStateHandle, Room 2.7.2 + SQLCipher 4.11.0, Firebase Auth/Firestore/RTDB, OkHttp, JUnit 4, AndroidX Test/Espresso.

**Specs:**
- `docs/superpowers/specs/2026-11-09-prottasha-android13-java-redesign.md`
- `docs/superpowers/specs/2026-11-09-prottasha-academic-showcase-amendment.md`

## Chosen approach

- **Selected — vertical recovery:** finish one normal user flow at a time and prove it on the BG7.
- **Rejected — more isolated primitives:** this produced the current mismatch between class count and visible functionality.
- **Rejected — rewrite:** the account, encryption, event-ledger, rules, and domain foundations are reusable.

## Global constraints

- Product code remains Java 17 and Android XML Views; no Kotlin or Compose.
- Reference acceptance device: TECNO BG7, Android 13/API 33, 720×1612, 4 GB RAM.
- Verified Firebase Email/Password accounts remain mandatory; no guest/fake UID/data.
- One SQLCipher database, wrapped key, and encrypted private-file namespace per Firebase UID.
- Sensitive habits, usage, domains, reflections, and images remain local.
- Firebase remains Spark-only: Auth, Firestore, and RTDB; no Storage or Functions.
- Every metric comes from persisted events; no data never means perfect performance.
- Accessibility observes only user-selected targets and preserves essential/system escape paths.
- Commercial Play work remains deferred; the academic APK must still be signed and repeatable.
- Do not deploy cloud rules, provision users, delete live accounts, or destroy keys without explicit approval.

## Completion contract for every task

1. The feature is reachable from a normal signed-in user flow.
2. Empty, populated, denied, offline, and error states are visible and honest.
3. A focused test is watched RED before implementation and GREEN afterward.
4. The complete affected package suite and `lintDebug` pass.
5. The flow is exercised on the TECNO BG7 before the matrix row becomes `PASS`.
6. No matrix row is marked `PASS` for class existence or a manual-preview-only path.

## Review focus

1. **Account transition:** A→B→A must never display or mutate another UID’s state.
2. **Guard freshness:** adding/editing/disabling a rule while Accessibility is already connected must take effect immediately.
3. **Time truth:** midnight, timezone, DST, delayed actions, duplicate taps, and rapid reopen must preserve correct outcomes.
4. **Permission loss:** notification, usage, Accessibility, contact, Firebase, and battery restrictions must produce repairable degraded states.
5. **Process death:** onboarding drafts, active focus, reminders, current tab, and import/export transactions must restore safely.

---

### Task 1: Honest Baseline and Guard Picker Checkpoint

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/androidTest/java/com/prottasha/guard/ui/GuardRuleFlowTest.java`
- Modify: `app/src/androidTest/java/com/prottasha/test/ForegroundAndroidJUnitRunner.java`
- Modify: `docs/academic-showcase-matrix.md`

**Deliverable:** Preserve the launcher-query fix, make filtered BG7 test runs reliable, and downgrade every currently overstated matrix row.

- [ ] Add/retain `appPickerFindsInstalledChromeOnBg7`; verify it fails without the launcher `<queries>` declaration.
- [ ] Keep launcher intent visibility without `QUERY_ALL_PACKAGES`; deduplicate packages and exclude only proven essential targets.
- [ ] Change all unproven matrix rows to `PARTIAL`, `PENDING`, or `BLOCKED` with exact limitations.
- [ ] Run `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.prottasha.guard lintDebug`.
- [ ] Install and verify the BG7 picker shows the device’s launchable non-essential apps.
- [ ] Commit: `fix(guard): expose complete launcher picker honestly`.

### Task 2: Complete Account, Profile, Sign-Out, and Switching

**Files:**
- Modify: `app/src/main/java/com/prottasha/auth/data/FirebaseAuthGateway.java`
- Modify: `app/src/main/java/com/prottasha/auth/data/AuthRepository.java`
- Modify: `app/src/main/java/com/prottasha/auth/data/CloudUserProfile.java`
- Modify: `app/src/main/java/com/prottasha/auth/data/CloudUserProfileRepository.java`
- Create: `app/src/main/java/com/prottasha/settings/account/ProfileActivity.java`
- Create: `app/src/main/java/com/prottasha/settings/account/ProfileViewModel.java`
- Modify: `app/src/main/java/com/prottasha/settings/account/AccountActivity.java`
- Modify: `app/src/main/java/com/prottasha/ui/settings/SettingsActivity.java`
- Modify: account/profile layouts and strings
- Test: auth/account unit and instrumentation suites

**Deliverable:** A verified user can view/edit profile, change password/email with reauthentication, sign out, switch accounts, optionally remove local data, and delete the account with named progress.

- [ ] Write failing tests for profile CRUD, generated avatar fields, recent-login errors, sign-out-before-close, remove-local-data choice, A→B→A isolation, and offline/cloud-disabled states.
- [ ] Implement profile display name/pseudonym/avatar editing and local-only encrypted profile photo.
- [ ] Implement password/email change through Firebase reauthentication and verification.
- [ ] Add explicit Sign out and Switch account actions; close the database before Firebase sign-out.
- [ ] Add “remove this account’s local data” confirmation separate from normal sign-out.
- [ ] Run auth/account suites, SQLCipher A→B→A tests, rules tests, and BG7 account flow.
- [ ] Commit: `feat(account): complete profile and account switching`.

### Task 3: Finish Versioned Onboarding and the Today Core Loop

**Files:**
- Modify: `app/src/main/java/com/prottasha/onboarding/OnboardingViewModel.java`
- Modify: `app/src/main/res/navigation/onboarding_nav_graph.xml`
- Create: target selection, rule setup, access setup, and intervention-test onboarding Fragments
- Modify: `app/src/main/java/com/prottasha/today/TodayViewModel.java`
- Modify: `app/src/main/java/com/prottasha/today/TodayFragment.java`
- Modify: `app/src/main/res/layout/fragment_today.xml`
- Test: onboarding and Today UI suites

**Deliverable:** Purpose → identity/success memory → targets/domains → per-target modes → disclosure → contextual permissions → intervention preview, followed by a useful Today screen.

- [ ] Write failing tests for all seven onboarding stages, skip semantics, version upgrade, denied permissions, draft recreation, and intervention preview.
- [ ] Bump onboarding version and route completed v1 users only through missing steps.
- [ ] Add success-memory input and save it only when onboarding completes.
- [ ] Reuse the Guard picker/rule editor for target setup; do not create duplicate rule models.
- [ ] Request Accessibility, usage, and notifications one at a time after explanation; denial remains usable.
- [ ] Build Today state containing identity, due habits/actions, active boundaries, Hope Horizon, rebound/replacement, focus action, and actionable health banner.
- [ ] Run onboarding/Today tests, rotate at each draft stage, and complete the full first-run flow on BG7.
- [ ] Commit: `feat(onboarding): connect first run to the core loop`.

### Task 4: Complete Habit Planning, Management, and Statistics

**Files:**
- Modify: `app/src/main/java/com/prottasha/habits/data/*`
- Modify: `app/src/main/java/com/prottasha/habits/domain/*`
- Replace: `HabitEditActivity`, `HabitDetailActivity`, `HabitsFragment`, `HabitsViewModel`
- Create: habit editor/detail ViewModels and immutable UI-state models
- Create: RecyclerView adapters for habit list, substeps, history, and archived habits
- Modify: Room schema/migrations and habit layouts
- Test: habit unit, DAO, UI, rotation, and account-isolation suites

**Deliverable:** Complete Build/Avoid/Amount creation and management with truthful history/statistics.

- [ ] Write failing tests for edit/restore/reorder/filter/delete, undo, backfill, archive, rest/vacation, and unsaved-exit confirmation.
- [ ] Expose daily, weekdays, X/week, X/month, every-N day/week/month, from-last-done, and start date.
- [ ] Add Build checklist/focus link, Avoid lapse details/rebound, and Amount unit/increment/target/maximum.
- [ ] Add cue, two-minute action, reward, if-then/replacement, rebound, and optional accountability fields using progressive disclosure.
- [ ] Add week/month/year views plus current/best streak, completion rate, rolling consistency, and totals derived from events.
- [ ] Add explicit undo after quick actions and prevent duplicate nonce/day credit.
- [ ] Run all habit tests and demonstrate all three types plus edit/archive/restore on BG7.
- [ ] Commit: `feat(habits): complete planning history and statistics`.

### Task 5: Make Reminders, Widget, and Private Images Real

**Files:**
- Modify: habit editor/detail ViewModels and layouts
- Modify: `app/src/main/java/com/prottasha/reminders/*`
- Modify: `app/src/main/java/com/prottasha/widget/*`
- Modify: `app/src/main/java/com/prottasha/core/security/EncryptedPrivateFileStore.java`
- Modify: manifest receivers and notification channels
- Test: reminder, widget, image, reboot/timezone, and permission-denial suites

**Deliverable:** Users can configure reminders and private images; notifications and widget actions execute the same idempotent habit commands.

- [ ] Write failing tests for reminder CRUD, multiple times, quiet hours, snooze, timezone/reboot/package update, permission denial, and exact-alarm fallback.
- [ ] Add contextual Android 13 notification permission explanation/request/repair flow.
- [ ] Add Done/Add/Snooze/Open actions; Avoid Check-in stays neutral and requires unlock.
- [ ] Display scheduled reminders and inactive/denied status in habit details.
- [ ] Add bounded encrypted image import/display/delete for habits, reflections, success memories, and local profile photo.
- [ ] Upgrade the widget from static copy to private progress and validated Build/Amount actions; remain neutral while locked.
- [ ] Run reminder/image/widget suites and reboot/timezone/device checks on BG7.
- [ ] Commit: `feat(reminders): ship configurable reminders images and widget actions`.

### Task 6: Complete Guard Runtime, Budgets, Schedules, Domains, and Health

**Files:**
- Expand: `GuardRuleEntity`, `GuardScheduleEntity`, DAO/repository/domain models
- Modify: Guard Fragment/editor/picker and add RecyclerView/search/filter UI
- Modify: `ProttashaAccessibilityService`, `ServicePackageFilter`, `AccessibilityServiceHealth`
- Integrate: `UsageStatsDataSource`, browser adapters, `GuardDecisionEngine`, `BudgetCalculator`
- Modify: accessibility config/disclosure and Guard health layouts
- Test: Guard engine, service, browser privacy, permission-health, and device suites

**Deliverable:** Each selected app/domain independently enforces Pause, Budget, or Block with live rule refresh and honest health.

- [ ] Write failing tests proving saved/edited/disabled rules refresh while Accessibility remains connected.
- [ ] Add per-rule schedule, delay, cooldown, grace, escalation, mid-session interruption, enabled state, replacement action, and optional edit protection.
- [ ] Route every event through `GuardDecisionEngine`; remove mode-string-only behavior.
- [ ] Implement Budget using selected-target usage quality, warning threshold, reset boundary, and configured over-budget action.
- [ ] Implement Block only inside configured windows/focus sessions with reason, end time, and alternatives.
- [ ] Enable the Chrome adapter only for selected browsers; store canonical host/session only and show unsupported/private states.
- [ ] Render Accessibility, usage access, notification, battery/autostart, stale-service, and grayscale health with repair actions.
- [ ] Test every essential escape package and app/domain rule independently on BG7.
- [ ] Commit: `feat(guard): enforce complete app and domain rules`.

### Task 7: Complete Intervention Follow-Through and Focus

**Files:**
- Modify: intervention state machine, controller, overlay, events, repository, and preview Activity
- Expand: intervention and focus Room entities/DAOs/migrations
- Modify: Focus Activity/ViewModel/service/notification
- Test: intervention race, exact-once, process-death, notification-action, and focus-credit suites

**Deliverable:** Prompt shown → immediate Step Back or delayed intentional Continue → replacement/focus → rapid-reopen/follow-through classification.

- [ ] Write failing tests for delay, optional/required intention, exact-one terminal event, duplicate overlay, reconnect, rapid reopen, durable follow-through, grace, replacement, and session interruption.
- [ ] Persist rule ID, target, timestamps, delay, source, outcome, and optional intention for each event.
- [ ] Make Step Back immediate; unlock Continue only after configured delay.
- [ ] Add replacement habit/focus action and record completion without duplicate habit credit.
- [ ] Build a live countdown/Pomodoro Focus screen with restored state and completion summary.
- [ ] Add neutral notification progress with Pause/Resume/End actions and notification-denial repair.
- [ ] Run intervention/focus suites and execute Pause, Budget, Block, reopen, and process-death scenarios on BG7.
- [ ] Commit: `feat(intervention): complete follow through and focus`.

### Task 8: Complete Usage, Insights, Reflection, and Recovery Tools

**Files:**
- Modify: usage entities/DAO/repository/data source
- Expand: insights calculator/repository/Fragment/custom views
- Expand: journal, trigger plan, success memory, and rehearsal data/UI
- Create: list/detail ViewModels and RecyclerView adapters
- Test: boundary, source-quality, chart-text, journal CRUD, trigger/rebound, and account-isolation suites

**Deliverable:** Today/7/30-day local evidence connects usage, Guard outcomes, habits, focus, triggers, and recovery actions.

- [ ] Write failing tests for cutoff/timezone/DST, app/domain source quality, no-data semantics, and Today/7/30-day ranges.
- [ ] Persist selected-app opens/sessions/duration and selected-domain sessions off the main thread.
- [ ] Add accessible summaries for usage, interventions, habits, focus, rebound, and follow-through.
- [ ] Add per-target/per-habit drill-down and limited time-of-day/trigger patterns only when sample size is adequate.
- [ ] Add reflection history/edit/delete/images, trigger/replacement plans, success-memory bank, rehearsal, and chain review.
- [ ] Keep every chart paired with a textual equivalent and never show reclaimed-time claims.
- [ ] Run insight/usage/recovery suites and demonstrate empty plus populated periods on BG7.
- [ ] Commit: `feat(insights): connect truthful usage and recovery evidence`.

### Task 9: Complete Circle, Accountability, and Support

**Files:**
- Expand: Circle domain/data repository/data source/ViewModel/UI
- Modify: Firestore/RTDB rules and emulator tests
- Complete: Accountability Activity and local settings persistence
- Replace: support repository with validated HTTPS + bundled fallback
- Modify: Circle/support/methodology layouts and strings
- Test: emulator attacks, two-user UI, contact result, support cache/conflict/offline suites

**Deliverable:** A provisioned closed pod supports real membership, messages, reactions, moderation, blocking, leaving, presence, accountability sharing, and sourced support.

- [ ] Write failing rules/repository tests for pagination, edit/delete, reactions, report privacy/status, block filtering, leave, listener cleanup, avatars, and deletion enumeration.
- [ ] Implement bounded cursor pagination and all member-owned operations under immutable authority rules.
- [ ] Render real deterministic avatars and presence without fake members or fallback messages.
- [ ] Handle contact picker result, manual fallback, local partner persistence, exact share preview, and revocation.
- [ ] Add report/block/leave controls and explicit “what Circle can see” copy.
- [ ] Add HTTPS support-directory refresh with timeout, cancellation, schema/version/checksum validation, cache, stale/conflict/offline labels, dial, and validated website action.
- [ ] Provision two disposable verified UIDs only after approval; run two-user BG7/cloud proof and verify remote rule hashes.
- [ ] Commit: `feat(circle): complete closed pod accountability and support`.

### Task 10: Complete App Lock, Export/Import, and Deletion

**Files:**
- Integrate: `AppLockManager`, `PinCredentialStore`, `LocalDeletionService`
- Create: App Lock setup/unlock Activities and ViewModels
- Replace: manifest-only export with logical archive repository/manifest/import transaction
- Modify: Privacy and Account screens, widget/reminder lock policy, account deletion coordinator
- Test: lock lifecycle, biometric/PIN, wrong password/tamper, full round-trip, rollback, and deletion suites

**Deliverable:** Real UI privacy lock, complete encrypted export/import, and complete local/cloud deletion.

- [ ] Write failing tests for timeout, app background/foreground, account change, retry delay, PIN reset via Firebase reauth, BiometricPrompt cancellation, and sensitive action gating.
- [ ] Wire app lock into Launch/Main, reminders, widget, settings, and account transitions.
- [ ] Export every logical table plus encrypted-file manifest/content to PBKDF2/AES-GCM without plaintext temporary files.
- [ ] Validate schema, ownership, counts, references, archive limits, and password before transactional import; rollback on any failure.
- [ ] Add explicit export/import progress and result summaries.
- [ ] Wire full local deletion and verify account deletion paginates all owned Circle records before deleting Auth.
- [ ] Run lock/export/import/deletion suites and perform fresh-install export/import proof on BG7.
- [ ] Commit: `feat(privacy): ship real lock export import and deletion`.

### Task 11: Product UI, Accessibility, State, and BG7 Performance

**Files:**
- Replace dynamic LinearLayout lists with RecyclerView/ListAdapter where lists can grow
- Add immutable screen states/ViewModels to remaining Activities/Fragments
- Modify all active layouts, themes, insets, content descriptions, and state resources
- Update `docs/accessibility-checklist.md`
- Test: rotation, process recreation, large text, TalkBack, denied/offline/error, and performance checks

**Deliverable:** A coherent, polished product rather than a collection of basic forms.

- [ ] Define loading, empty, populated, denied, offline, stale, and error rendering for every top-level feature.
- [ ] Preserve drafts, filters, current tab, selected dates, and active timer across recreation/process death.
- [ ] Add consistent cards, icons, hierarchy, destructive confirmations, search/filter, and accessible alternatives.
- [ ] Verify 48dp targets, 4.5:1 text contrast, TalkBack order, 200% font, landscape, reduced animation, and system-bar insets.
- [ ] Measure cold start, memory, frame stats, service lifetime, package enumeration, and large lists on BG7; fix regressions against the approved targets.
- [ ] Run complete unit/UI/lint/rules gates and the entire manual device checklist.
- [ ] Commit: `refactor(ui): finish accessible product states and performance`.

### Task 12: Signed Academic Release and Evaluator Evidence

**Files:**
- Modify: `app/build.gradle`, `.gitignore`, Firebase release setup
- Create: `keystore.properties.example`
- Create/update: release verification, device checklist, showcase script, limitations, evidence matrix
- Create: architecture, session ownership, intervention, and Firebase rules diagrams

**Deliverable:** Reproducible signed academic APK and a five-to-eight-minute BG7 demonstration of every completed claim.

- [ ] With explicit approval, register `com.prottasha`, add release `google-services.json`, and configure private signing outside Git.
- [ ] Run clean unit, focused instrumentation, lint, assembleDebug, signed assembleRelease, and Firebase emulator gates.
- [ ] Fresh-install the signed APK and execute account → onboarding → habits/reminders/widget → Guard app/domain modes → intervention/focus → insights/reflection → Circle/support → lock/export/import/switch/deletion.
- [ ] Record APK SHA-256, certificate, version, cold start, frame stats, memory, services, permissions, remote rule hashes, and exact limitations.
- [ ] Link every evidence-matrix row to a real screen/class, automated result, and showcase timestamp.
- [ ] Do not claim 10/10 while any core-loop row is partial, pending, unavailable, or blocked.
- [ ] Commit: `chore: verify complete academic showcase release`.

## Final acceptance checklist

- [ ] Normal users can reach every retained feature without test-only entry points.
- [ ] Account creation, verification, sign-in/out, switching, profile changes, and deletion use real Firebase results.
- [ ] Today visibly connects boundaries, interventions, replacement habits/focus, and follow-through evidence.
- [ ] Full habits, reminders, widget, Guard, domain tracking, focus, insights, Circle, support, lock, and export/import work on BG7.
- [ ] All private data remains UID-isolated and encrypted; all cloud authority remains rule-enforced.
- [ ] No fake messages, users, presence, statistics, success, time saved, or unsupported health claims.
- [ ] The evidence matrix has no `PARTIAL`, `PENDING`, or `BLOCKED` row for the promised academic core loop.
