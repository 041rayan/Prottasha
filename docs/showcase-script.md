# Prottasha Evaluator Showcase Script (5–8 Minutes)

This script provides an exact, step-by-step physical device walkthrough for evaluators and academic reviewers using the TECNO Spark 20C (BG7).

---

## Act 1: Identity, Account & Onboarding (Minutes 0:00 – 1:30)

1. **Launch & Authentication:**
   - Launch Prottasha from launcher. Observe instant splash screen (<950ms cold start).
   - Tap **Create Account** or **Sign In**.
   - Input verified evaluator credentials.
   - Point out: Database is dynamically opened in SQLCipher using an AES-256 key wrapped by Android Keystore under a SHA-256 opaque UID hash.
2. **First-Run Onboarding:**
   - The user is guided through the versioned Onboarding flow.
   - Input personal core purpose (e.g., *"Focus on graduation project and mental clarity"*).
   - Select identity anchors and primary goals.
   - Tap **Finish Onboarding**. Confirm persistence only occurs upon intentional completion.

---

## Act 2: Habits, Reminders & Neutral Widget (Minutes 1:30 – 2:45)

1. **Habit Engine:**
   - Navigate to the **Habits** tab. Note truthful empty state before creation.
   - Tap **Add Habit**. Create:
     - **Build Habit:** *"Read 10 pages"* (Daily target).
     - **Avoid Habit:** *"Avoid doomscrolling"* (Lapse logging with rebound prompt).
     - **Amount Habit:** *"Water intake"* (Increment: 250ml, Target: 2000ml).
   - Tap action buttons to record events into the immutable event ledger.
   - Tap filter chips (**Build**, **Avoid**, **Amount**, **All**) to demonstrate responsive filtering via `HabitsAdapter`.
2. **Home Screen Widget:**
   - Navigate to Android home screen.
   - Show the Prottasha Widget. Observe that widget text remains truthful and neutral without leaking sensitive habit titles when device is locked.

---

## Act 3: Guard Rules, Intervention & Focus Session (Minutes 2:45 – 4:15)

1. **Configuring Boundaries:**
   - Open the **Guard** tab. Observe the **Protection Health Card** indicating Accessibility and Usage stats status.
   - Tap **Add App**. The `AppPickerActivity` opens with instant RecyclerView search.
   - Select an app (e.g., YouTube) and configure **PAUSE** mode with a 15-second delay.
   - Tap **Add Website**. Enter `instagram.com` and configure **BLOCK** mode.
2. **Triggering Intervention:**
   - Switch to the selected target app.
   - The `ProttashaAccessibilityService` detects the window state and presents the **Pause Overlay** (`TYPE_ACCESSIBILITY_OVERLAY`).
   - Demonstrate the countdown timer with immediate **Step Back** option. No bypass is permitted.
3. **Deep Focus Session:**
   - Open Today tab and start a 25-minute **Focus Session**.
   - Notice the system foreground notification from `ProttashaFocusService`.
   - Pause, resume, and cleanly finish the session. Verify the service stops immediately and does not remain sticky.

---

## Act 4: Truthful Insights, Recovery & Closed Pod Circle (Minutes 4:15 – 6:00)

1. **Truthful Insights & Recovery:**
   - Open **Insights** tab.
   - Review Today, 7-Day, and 30-Day aggregate charts.
   - Emphasize: *Every chart is accompanied by an accessible text summary. No fabricated "time saved" metrics or fake streak claims.*
   - Add a private **Reflection** note (Fact, Interpretation, Action).
2. **Crisis Support Helplines:**
   - Tap the top-bar **Lifeline** button.
   - Show verified Bangladesh support services: **999 (National Emergency)**, **Kaan Pete Roi (Emotional Support)**, **109 (Women & Children Helpline)**, and **16263 (Health Help)**.
   - Tap **Call** to verify the system dialer opens with the correct number.
3. **Closed Circle Pod:**
   - Navigate to the **Circle** tab.
   - Show the pod member list with deterministic avatars.
   - Send a message in the pod chat. Observe real-time message sending and reaction chips.
   - Show typing indicator debounce behavior.
   - Show partner accountability settings (share note generation, contact picker with manual fallback).

---

## Act 5: App Lock, Secure Export/Import & Data Sovereignty (Minutes 6:00 – 7:30)

1. **App Lock & Gating:**
   - Navigate to **Settings** → **Security & App Lock**.
   - Enable App Lock and set a 6-digit PIN.
   - Minimize the app and reopen. The **AppLockActivity** appears immediately.
   - Enter wrong PIN 5 times: verify the 30-second lockout timer countdown activates.
   - Enter correct PIN to unlock.
2. **Encrypted Archive Export & Import:**
   - Navigate to **Settings** → **Privacy & Data**.
   - Tap **Export Encrypted Backup**. Enter an archive encryption password.
   - Confirm export completes via AES-256-GCM / PBKDF2 without writing any temporary plaintext files to disk.
   - Demonstrate **Import Backup**: validates schema version, HMAC signature, and decrypts in a single Room database transaction.
3. **Complete Account Deletion:**
   - Demonstrate the **Delete Account** option in Account settings.
   - Evaluator note: Deletion requires password re-authentication, deletes all user messages, reactions, reports, pod memberships, and RTDB mirrors in paginated cloud batches before deleting the Firebase Auth user and wiping local private keys.
