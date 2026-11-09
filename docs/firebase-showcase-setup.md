# Firebase Academic Showcase Setup

**Project:** `prottasha-cse30`
**Plan:** Spark, billing disabled
**Debug package:** `com.prottasha.debug`

## Authentication

- Email/Password: enabled; password required.
- Anonymous and phone providers: disabled.
- Improved email privacy / enumeration protection: enabled.
- Password policy: `ENFORCE`, minimum 8, maximum 4096, uppercase + lowercase + numeric required, non-alphanumeric optional.
- Existing accounts are not forced to upgrade their password during sign-in.
- Verification/reset sender display name: Prottasha; final showcase wording review remains pending.

The Create Account UI must mirror these values exactly. If the live policy changes, update `PasswordPolicy`, its tests, and this document together.

## Databases

- Firestore `(default)`: Standard / Native / `nam5`.
- Firestore live rules: UID-owned Email/Password profile boundary; undefined/Circle paths denied until Task 13.
- Realtime Database: `prottasha-cse30-default-rtdb`; live rules remain deny-all until membership-scoped presence is implemented.
- Cloud Storage: not used.

## Local emulators

```bash
npm run test:rules
```

The script uses demo project `demo-prottasha` and starts Auth, Firestore, and RTDB emulators. Production builds must never call emulator endpoints.

## Deferred release registration

`com.prottasha` and `app/src/release/google-services.json` are created only during the signed-release gate with explicit confirmation. The current debug config exactly matches the registered debug app and includes the RTDB URL.
