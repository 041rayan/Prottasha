# Prottasha Quiet Precision UI/UX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current cluttered presentation with the approved premium Quiet Precision design while preserving every existing feature, data path, service, and security boundary.

**Architecture:** Implement a resource-first visual system, then migrate one reachable screen family at a time. Preserve existing view IDs and ViewModel/repository interfaces where possible; Java changes are limited to UI state mapping, progressive disclosure, adapters, and the event-derived consistency heatmap. Every slice runs its existing behavior tests before proceeding.

**Tech Stack:** Java 17, Android XML Views, View Binding, Material Components, Navigation, RecyclerView, custom Android Views, JUnit 4, Espresso, TECNO BG7/API 33.

**Spec:** `docs/superpowers/specs/2026-11-10-prottasha-quiet-precision-ui-redesign.md`

## Global constraints

- Work directly on `main` as requested; preserve unrelated user files.
- Product source remains Java/XML; no Kotlin or Compose.
- Do not change Room schemas, SQLCipher/Keystore behavior, Firebase models/rules, Guard decisions, reminder semantics, export encryption, or account-deletion stages.
- Keep stable view IDs and navigation destinations unless an approved layout requires a UI-only replacement.
- Bundle Newsreader, IBM Plex Sans, and IBM Plex Mono locally under their OFL licenses; no runtime font request.
- Use `prottasha.png` as the approved source mark; do not redesign it.
- Keep the app dark-only for this academic release.
- No glassmorphism, blur, decorative gradient blobs, neon glow, emoji icons, generic bento wallpaper, or excessive pill controls.
- Product copy must be plain, specific, and human; preserve required legal, evidence, privacy, and safety meaning.
- Reference acceptance device remains TECNO BG7, Android 13, 720×1612, 4 GB RAM.

## Review focus

1. **Behavior preservation:** existing Auth, profile photo, habits, Guard, focus, Circle, privacy, and account actions must invoke the same ViewModel methods after layout replacement.
2. **Dynamic content:** long names, habit titles, domains, errors, and translated/system text must wrap without hiding primary actions.
3. **Accessibility:** 200% font, TalkBack order, 48dp targets, chart alternatives, and color-independent state must remain usable.
4. **Lifecycle:** rotation/process recreation must not lose drafts, selected tabs, current filters, focus state, or photo-picker state.
5. **Performance:** fonts, logo assets, RecyclerViews, custom heatmap drawing, and transitions must remain smooth on the BG7 with animations disabled or enabled.

---

### Task 1: Brand Assets, Fonts, and Semantic Tokens

**Files:**
- Create: `app/src/main/res/font/newsreader_variable.ttf` plus medium/semibold XML weight aliases
- Create: `app/src/main/res/font/ibm_plex_sans_variable.ttf` plus regular/medium/semibold/bold XML weight aliases
- Create: `app/src/main/res/font/ibm_plex_mono_regular.ttf`
- Create: `app/src/main/res/font/ibm_plex_mono_medium.ttf`
- Create: `docs/licenses/NEWSREADER-OFL.txt`
- Create: `docs/licenses/IBM-PLEX-OFL.txt`
- Create: `app/src/main/res/drawable-nodpi/prottasha_logo.webp`
- Create/modify: launcher foreground/background, monochrome icon, splash icon, and notification silhouette resources
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/dimens.xml`
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `app/src/main/res/values/design_tokens.xml`
- Create: `app/src/androidTest/java/com/prottasha/quality/BrandResourceTest.java`

**Interfaces:**
- Produces semantic colors, typography styles, spacing/radius tokens, motion durations, and reusable brand drawables consumed by all later tasks.
- Does not alter application/domain interfaces.

- [ ] **Step 1: Write failing resource tests**

Add `BrandResourceTest` assertions that the three font families, logo drawable, adaptive icon resources, monochrome icon, notification icon, and Quiet Precision colors resolve; assert `color_on_hope` remains dark and normal text colors meet the chosen contrast pairs.

- [ ] **Step 2: Run RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.quality.BrandResourceTest`

Expected: FAIL because new fonts/logo/tokens do not exist.

- [ ] **Step 3: Add approved assets and tokens**

Source fonts from their official OFL distributions, retain licenses, convert the approved root PNG losslessly to a bounded WebP Android resource, and define exact spec colors/type/spacing. Keep raster sizes bounded and avoid runtime font loading.

- [ ] **Step 4: Verify GREEN and build**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.quality.BrandResourceTest assembleDebug lintDebug`

Expected: PASS, zero lint errors.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res app/src/androidTest/java/com/prottasha/quality/BrandResourceTest.java docs/licenses
git add -f docs/superpowers/specs/2026-11-10-prottasha-quiet-precision-ui-redesign.md docs/superpowers/plans/2026-11-10-prottasha-quiet-precision-ui-redesign.md
git commit -m "style(brand): add Quiet Precision assets and tokens"
```

### Task 2: Reusable Components and Main Shell

**Files:**
- Create: `app/src/main/res/values/quiet_precision_styles.xml`
- Create: state-list drawables/selectors for tonal surfaces, rows, primary/secondary/text/destructive buttons, segmented controls, chips, and icon buttons
- Create: consistent vector icons for top app bar and bottom navigation
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/res/menu/bottom_nav_menu.xml`
- Modify: `app/src/main/res/color/nav_item_tint.xml`
- Modify: `app/src/main/java/com/prottasha/ui/main/MainActivity.java` only as needed to bind icon controls/insets
- Modify: `app/src/androidTest/java/com/prottasha/quality/WholeAppQualityTest.java`

**Interfaces:**
- Produces component styles used by every screen family.
- Preserves `main_nav_host`, `main_bottom_navigation`, `main_lifeline_button`, and `main_settings_button` IDs and existing click behavior.

- [ ] **Step 1: Write failing shell/component tests**

Extend `WholeAppQualityTest` to assert five labelled destinations, 48dp header actions, destination title accessibility, stable navigation IDs, no text-only Lifeline/Settings controls, and no content overlap with system bars/bottom navigation.

- [ ] **Step 2: Run RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.quality.WholeAppQualityTest`

Expected: FAIL on the new icon/header expectations.

- [ ] **Step 3: Implement component system and shell**

Create flat tonal component styles, replace repeated local card/button styling, use accessible vector icon actions, and retain predictable back/navigation behavior. No blur or floating glass navigation.

- [ ] **Step 4: Run GREEN**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.quality.WholeAppQualityTest lintDebug`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res app/src/main/java/com/prottasha/ui/main/MainActivity.java app/src/androidTest/java/com/prottasha/quality/WholeAppQualityTest.java
git commit -m "style(shell): establish premium navigation and components"
```

### Task 3: Premium Authentication, Onboarding, and Profile

**Files:**
- Modify: `app/src/main/res/layout/activity_auth.xml`
- Modify: `fragment_sign_in.xml`, `fragment_create_account.xml`, `fragment_verify_email.xml`, `fragment_reset_password.xml`, `activity_legal.xml`
- Modify: onboarding Activity/Fragment layouts
- Modify: `app/src/main/res/layout/activity_profile.xml`
- Modify: Auth/onboarding/profile strings
- Modify: Auth/onboarding/profile Java classes only for UI binding, focus, progress, and disclosure state
- Test: existing Auth, onboarding, account/profile instrumentation suites
- Create: `app/src/androidTest/java/com/prottasha/quality/PremiumAuthProfileUiTest.java`

**Interfaces:**
- Preserves all current AuthViewModel, OnboardingViewModel, ProfileViewModel methods and view IDs used by existing tests.
- Preserves encrypted local photo selection/removal and public generated-avatar fields.

- [ ] **Step 1: Write failing premium UI tests**

Assert logo visibility, Newsreader display title, single dominant action, autofill/password-toggle support, visible requirements, factual privacy copy, 48dp controls, profile photo edit action, private/Circle section labels, loading/error visibility, and 200% font scrolling.

- [ ] **Step 2: Run RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.quality.PremiumAuthProfileUiTest`

Expected: FAIL against current layouts.

- [ ] **Step 3: Redesign Auth and Profile XML**

Apply approved mockups: logo-led Auth, direct copy, clean form hierarchy, scannable password requirements, prominent profile photo, and clear private/public identity sections. Do not alter Firebase or encrypted-file behavior.

- [ ] **Step 4: Run affected behavior suites**

Run:

```bash
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.package=com.prottasha.auth
./gradlew connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.onboarding.OnboardingAndShellTest,com.prottasha.auth.AccountFlowTest,com.prottasha.quality.PremiumAuthProfileUiTest
./gradlew lintDebug
```

Expected: all existing behavior plus visual assertions pass.

- [ ] **Step 5: BG7 manual check**

Verify Sign In/Create/Verify/Reset, photo choose/remove, keyboard, password manager/paste, rotation, 200% font, and back behavior.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/layout app/src/main/res/values app/src/main/java/com/prottasha/auth app/src/main/java/com/prottasha/onboarding app/src/main/java/com/prottasha/settings/account app/src/androidTest
git commit -m "style(auth): redesign account and onboarding experience"
```

### Task 4: Today Dashboard and Event-Derived Heatmap

**Files:**
- Create: `app/src/main/java/com/prottasha/habits/ui/HabitConsistencyGridBuilder.java`
- Create: `app/src/main/java/com/prottasha/habits/ui/HabitConsistencyGridState.java`
- Create: `app/src/main/java/com/prottasha/habits/ui/HabitConsistencyHeatmapView.java`
- Modify: `app/src/main/java/com/prottasha/today/TodayViewModel.java`
- Modify: `app/src/main/java/com/prottasha/today/TodayFragment.java`
- Replace: `app/src/main/res/layout/fragment_today.xml`
- Create: dedicated Today habit/boundary row layouts and adapters if needed
- Test: `app/src/test/java/com/prottasha/habits/ui/HabitConsistencyGridBuilderTest.java`
- Modify: Today/onboarding/quality instrumentation tests

**Interfaces:**
- `HabitConsistencyGridBuilder.build(List<HabitEntity>, List<HabitDayAggregateEntity>, LocalDate, int weeks) -> HabitConsistencyGridState` returns immutable cells plus active-day/current-run/best-run/consistency summaries.
- It consumes existing schedules and aggregates; it does not change DAO, repository, or domain semantics.
- `HabitConsistencyHeatmapView.setState(HabitConsistencyGridState)` renders only; TalkBack summary comes from state.
- `TodayViewModel.consistency() -> LiveData<HabitConsistencyGridState>` exposes the UI-ready state.

- [ ] **Step 1: Write failing heatmap semantics tests**

Cover empty data, scheduled vs unscheduled days, Build/Amount completion, Avoid lapse/clean semantics, multiple habits per day, current-day marker, 12-week bounds, current/best run, and no-data summary.

- [ ] **Step 2: Run RED**

Run: `./gradlew testDebugUnitTest --tests 'com.prottasha.habits.ui.HabitConsistencyGridBuilderTest'`

Expected: FAIL because builder/state do not exist.

- [ ] **Step 3: Implement grid state and custom View**

Use existing `HabitScheduleEvaluator` and `HabitDao.listAllAggregatesBetween`; compute off-main in TodayViewModel. Draw a 7×12 grid with neutral/4 intensity levels, periwinkle current-day outline, visible legend, and text/TalkBack summary.

- [ ] **Step 4: Redesign Today**

Implement greeting/avatar, identity hero with one horizon motif, visible dashboard metrics, heatmap, due-habit rows, compact boundary summary, health repair state, and contextual Focus action. Remove equal-weight stacked cards while preserving all actions.

- [ ] **Step 5: Verify tests and BG7**

Run:

```bash
./gradlew testDebugUnitTest --tests 'com.prottasha.habits.ui.*'
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.onboarding.OnboardingAndShellTest,com.prottasha.quality.WholeAppQualityTest
./gradlew lintDebug
```

Check empty/populated heatmap, long identity text, no scheduled days, 200% font, landscape, and disabled animation on BG7.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/prottasha/habits/ui app/src/main/java/com/prottasha/today app/src/main/res/layout app/src/main/res/values app/src/test app/src/androidTest
git commit -m "style(today): add premium dashboard and consistency heatmap"
```

### Task 5: Habits and Guard Screen Families

**Files:**
- Modify: Habits Fragment/ViewModel/adapters, habit item/editor/detail/history layouts
- Modify: Guard Fragment/ViewModel/adapters, app picker, rule editor, item layouts
- Modify: habit/guard strings and vector icons
- Test: existing habit and Guard unit/instrumentation suites
- Create: `app/src/androidTest/java/com/prottasha/quality/HabitsGuardVisualStructureTest.java`

**Interfaces:**
- Preserve all existing habit/Guard repository calls, editor result handling, filters, search, health repair actions, enabled state, and accessibility disclosure.
- UI may add tabs/sheets/overflow menus but cannot change rule/habit semantics.

- [ ] **Step 1: Write failing visual-structure tests**

Assert RecyclerView usage, compact row action IDs, contextual add actions, filter/schedule/health access, empty/search-no-match states, 48dp targets, long title/domain wrapping, and retention of every existing action.

- [ ] **Step 2: Run RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.quality.HabitsGuardVisualStructureTest`

- [ ] **Step 3: Redesign Habits**

Use premium rows, compact progress/schedule metadata, one completion control, overflow actions, contextual add, compact filters, heatmap-led detail, grouped editor sections, and sticky save. Preserve draft/state behavior.

- [ ] **Step 4: Redesign Guard**

Use compact protection status, Rules/Schedule/Health tabs, premium target rows, contextual search/filter, mode labels, and add-boundary action. Keep detailed repair instructions inside Health/disclosure surfaces.

- [ ] **Step 5: Run affected suites**

Run:

```bash
./gradlew testDebugUnitTest --tests 'com.prottasha.habits.*' --tests 'com.prottasha.guard.*'
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.prottasha.habits
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.prottasha.guard
./gradlew lintDebug
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/prottasha/habits app/src/main/java/com/prottasha/guard app/src/main/res app/src/androidTest
git commit -m "style(core): redesign habits and Guard workflows"
```

### Task 6: Insights and Circle Screen Families

**Files:**
- Modify: Insights Fragment/adapters/custom trend View and layouts
- Modify: Circle Fragment/ViewModel, pod chat/accountability Activities, adapters, and layouts
- Modify: support-directory UI presentation only
- Modify: insights/circle/support strings and icons
- Test: existing insights, Circle, support, and RTDB presence/typing suites
- Create: `app/src/androidTest/java/com/prottasha/quality/InsightsCircleVisualStructureTest.java`

**Interfaces:**
- Preserve existing range selection, evidence calculations, CRUD actions, Firestore messages/moderation, RTDB presence/typing, contact picker, and support intents.
- “Journal & tools” is presentation grouping, not a new data model.

- [ ] **Step 1: Write failing structure tests**

Assert one primary Insights metric, visible useful dashboard information, heatmap/trend summary, Journal & tools access, real Circle presence, closed-pod entry, accountability/support/emergency destinations, and no fake values.

- [ ] **Step 2: Run RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.quality.InsightsCircleVisualStructureTest`

- [ ] **Step 3: Redesign Insights**

Keep range selector, one dominant metric, contribution/trend visual, concise observations, and more information upfront through structured sections. Move reflection/trigger/memory/rehearsal management behind Journal & tools navigation/sheets without removing operations.

- [ ] **Step 4: Redesign Circle**

Lead with real presence and pod action; use compact rows for accountability, support directory, and immediate help; move privacy explanation to a dedicated detail/sheet. Redesign chat composer/message rows without changing Firebase behavior.

- [ ] **Step 5: Run affected suites**

Run:

```bash
./gradlew testDebugUnitTest --tests 'com.prottasha.insights.*' --tests 'com.prottasha.circle.*' --tests 'com.prottasha.support.*'
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.insights.InsightsAndSupportTest
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.prottasha.circle
./gradlew lintDebug
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/prottasha/insights app/src/main/java/com/prottasha/circle app/src/main/java/com/prottasha/support app/src/main/res app/src/androidTest
git commit -m "style(insights): redesign evidence and Circle surfaces"
```

### Task 7: Settings, Privacy, Focus, Intervention, and Remaining Screens

**Files:**
- Modify: settings/account/privacy/app-lock layouts and UI binding
- Modify: focus/rehearsal/reflection/methodology/support/legal layouts
- Modify: intervention overlay/manual preview layouts and UI binding
- Modify: widget layout and notification-facing visual assets where applicable
- Modify: remaining active item/dialog/editor/detail layouts
- Test: existing account, privacy, focus, intervention, reminders, and quality suites
- Create: `app/src/androidTest/java/com/prottasha/quality/RemainingScreensVisualStructureTest.java`

**Interfaces:**
- Preserve every existing action ID/callback and all security/privacy copy semantics.
- Intervention state machine, focus service, app lock, archive, reminders, widget actions, and account deletion remain unchanged.

- [ ] **Step 1: Write failing screen-family tests**

Assert grouped settings rows, current status text, destructive separation, timer hierarchy, immediate Step Back prominence, sticky save where needed, scrollability at 200% font, and reachability of every existing operation.

- [ ] **Step 2: Run RED**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.prottasha.quality.RemainingScreensVisualStructureTest`

- [ ] **Step 3: Apply Quiet Precision to remaining screens**

Use shared styles rather than screen-local colors/radii. Keep focus/intervention visually sparse, settings row-based, editors grouped, and destructive actions explicit.

- [ ] **Step 4: Run affected suites**

Run:

```bash
./gradlew testDebugUnitTest --tests 'com.prottasha.settings.*' --tests 'com.prottasha.focus.*' --tests 'com.prottasha.intervention.*' --tests 'com.prottasha.reminders.*'
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.prottasha.focus
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.prottasha.intervention
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=com.prottasha.quality
./gradlew lintDebug
```

- [ ] **Step 5: Commit**

```bash
git add app/src/main app/src/androidTest app/src/test
git commit -m "style(ui): finish Quiet Precision screen migration"
```

### Task 8: Plain-Language Copy Audit

**Files:**
- Modify: all active `app/src/main/res/values/*_strings.xml` and `strings.xml`
- Modify: Java literals only by moving user-visible copy to resources; do not change control flow
- Create: `app/src/test/java/com/prottasha/quality/ProductCopyTest.java`

**Interfaces:**
- Produces approved plain-language copy across active screens.
- Preserves legal, methodology, privacy, emergency, and evidence meanings.

- [ ] **Step 1: Write failing copy test**

Scan compiled/source string resources for banned filler phrases, unexplained protocol IDs, hardcoded user-facing Java literals, fake urgency, unsupported claims, and obsolete theme language. Allowlist required legal citations and methodology terms.

- [ ] **Step 2: Run RED**

Run: `./gradlew testDebugUnitTest --tests 'com.prottasha.quality.ProductCopyTest'`

- [ ] **Step 3: Rewrite active copy**

Use direct labels, specific errors, factual empty states, concise help text, and normal sentence case. Keep disclosure detail only where it changes a user decision.

- [ ] **Step 4: Run GREEN and behavior suites**

Run: `./gradlew testDebugUnitTest lintDebug assembleDebug`

Expected: PASS; no copy test or lint failures.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/values app/src/main/java app/src/test/java/com/prottasha/quality/ProductCopyTest.java
git commit -m "style(copy): replace generated-sounding product text"
```

### Task 9: Whole-App Visual, Accessibility, and Regression Gate

**Files:**
- Modify: `docs/accessibility-checklist.md`
- Create: `docs/quiet-precision-ui-verification.md`
- Modify: quality instrumentation tests only where final proof needs coverage

**Interfaces:**
- Consumes the complete redesigned app.
- Produces device evidence and a limitations record; no feature behavior changes.

- [ ] **Step 1: Run complete automated gates**

Run:

```bash
./gradlew clean testDebugUnitTest assembleDebug lintDebug compileDebugAndroidTestJavaWithJavac
npm run test:rules
```

Expected: zero failures and zero lint errors.

- [ ] **Step 2: Run focused BG7 suites**

Run Auth/Profile, onboarding/shell, habits, Guard, intervention/focus, insights/support, Circle, reminders, and quality instrumentation in separate filtered runs to avoid the documented HiOS headless freeze.

- [ ] **Step 3: Perform visual acceptance walkthrough**

Capture Sign In, Create Account, Profile with/without photo, empty/populated Today, heatmap, Habits, Guard healthy/denied, Insights, Circle online/offline, Settings, Focus, intervention, and destructive confirmation states.

- [ ] **Step 4: Perform accessibility/performance checks**

Verify TalkBack order, 200% font, landscape, animations disabled, keyboard/autofill, touch targets, contrast, system bars, cold start, frame stats, and memory on the BG7.

- [ ] **Step 5: Confirm core diff boundary**

Review the final diff and document every changed Java file. Any non-UI/core change must have explicit justification and focused regression proof; otherwise revert it before completion.

- [ ] **Step 6: Commit evidence**

```bash
git add docs app/src/androidTest app/src/test
git commit -m "test(ui): verify Quiet Precision redesign on BG7"
```

## Final acceptance

- [ ] Approved logo and font system appear consistently without visual overuse.
- [ ] Auth/Profile match the approved premium mockups and profile photo still works.
- [ ] Today contains a truthful event-derived contribution heatmap and useful dashboard information.
- [ ] Guard, Insights, and Circle preserve depth without equal-weight card clutter.
- [ ] Every active screen follows the shared component system and plain-language copy rules.
- [ ] No database, cloud authority, encryption, service, scheduling, or business behavior regresses.
- [ ] Complete build/lint/unit/rules and focused BG7 instrumentation pass.
