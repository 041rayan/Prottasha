# Prottasha Academic Limitations & Design Boundaries

This document transparently specifies the design trade-offs, operational boundaries, and system constraints of the Prottasha platform.

---

## 1. Firebase Spark Tier Constraints
- **Zero Cloud Functions / Cloud Storage:** The application is architected to operate strictly within the free Firebase Spark tier.
- **Client-Driven Deletion:** Because Cloud Functions are unavailable on Spark, cloud record deletion (pod messages, reactions, reports, RTDB mirrors) is performed in client-driven, paginated batches with strict security rule enforcement prior to Firebase Auth user deletion.
- **Encrypted Local Media:** User private images (notes, vision boards, reflection photos) are processed, metadata-scrubbed, and stored in AES-256-GCM encrypted local storage rather than cloud storage buckets.

---

## 2. Technical Stack Constraints
- **Pure Java 17 & View Binding:** The codebase is exclusively written in Java 17 using standard Android XML Views and View Binding. Kotlin and Jetpack Compose were intentionally excluded to guarantee deterministic execution, minimal heap overhead, and smooth 60fps rendering on entry-level Android devices like the TECNO Spark 20C.
- **Room with SQLCipher 4.11.0:** Upgrading beyond Room 2.7.2 or SQLCipher 4.11.0 is constrained by native connection compatibility with `SupportOpenHelperFactory` on Android 13 devices.

---

## 3. Privacy, Security & Anti-Surveillance Boundaries
- **No `QUERY_ALL_PACKAGES`:** The Guard module queries only launcher activities (`Intent.CATEGORY_LAUNCHER`), respecting Google Play privacy policies and avoiding intrusive package scanning.
- **Zero Node Scraping:** `ProttashaAccessibilityService` only inspects window state change events (`TYPE_WINDOW_STATE_CHANGED`) to identify foreground package names and browser URL domains. It strictly avoids reading on-screen text, form fields, passwords, or personal messages.
- **Opaque UID Namespacing:** Database names, keystore aliases, and internal storage folders use SHA-256 hashes of user UIDs to prevent leakage of user identities to unauthorized processes.

---

## 4. Truthful Metrics & Ethical Commitments
- **No Fabricated Data:** Prottasha never displays fabricated "hours saved", simulated active members, fake cloud messages, or arbitrary "wellness scores".
- **Zero-Data Honesty:** If no usage or habit events are recorded for a period, the UI renders an explicit, honest empty state rather than filling charts with speculative estimates.
- **Bangladesh Lifelines Verification:** Crisis helplines (999, 109, 16263, Kaan Pete Roi) are verified and bundled locally. Prottasha does not claim to replace professional psychiatric or medical care.
