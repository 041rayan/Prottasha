# Prottasha Architecture, Session, Intervention, and Security Diagrams

This document contains architectural diagrams documenting the security model, intervention state machine, and cloud rules.

---

## 1. High-Level Architecture Overview

```mermaid
flowchart TD
    subgraph UI["UI & Presentation Layer"]
        A["Main Shell & Navigation"]
        B["Habits Tab & Editor"]
        C["Guard Tab & App Picker"]
        D["Insights & Reflection"]
        E["Circle Pod & Accountability"]
        F["App Lock & Privacy Settings"]
    end

    subgraph Domain["Domain & Engine Layer"]
        G["Habit Engine & Ledger"]
        H["Guard Engine & Schedules"]
        I["Usage Stats Aggregator"]
        J["Insights Calculator"]
        K["App Lock Policy"]
        L["Archive Exporter & Importer"]
    end

    subgraph Data["Repository & Access Layer"]
        M["UserSessionManager"]
        N["HabitRepository"]
        O["GuardRuleRepository"]
        P["UsageRepository"]
        Q["PodRepository"]
        R["LogicalArchiveRepository"]
    end

    subgraph Platform["Storage & Platform Layer"]
        S["SQLCipher Room DB (Per-UID AES-256)"]
        T["Android Keystore (Key Wrapping)"]
        U["Accessibility Service & Overlays"]
        V["AlarmManager & Focus Service"]
        W["Firebase Spark (Auth, Firestore, RTDB)"]
    end

    UI --> Domain
    Domain --> Data
    Data --> Platform
```

---

## 2. Session Ownership and Cryptographic Isolation

```mermaid
flowchart LR
    A["User Signs In"] --> B["Firebase Auth Token"]
    B --> C["SessionUser (UID)"]
    C --> D["UserDatabaseIdentity (SHA-256 Hash)"]
    
    subgraph KeyMgmt["Keystore Wrapping"]
        E["Android Keystore System"]
        F["Master Key"]
        E --> F
    end

    D --> G["Database File: prottasha_{uidHash}.db"]
    F --> H["Wrapped Passphrase"]
    H --> G
    
    subgraph Storage["UID-Isolated Sandbox"]
        G --> I["Room Entities (Habits, Events, Rules, Notes)"]
        D --> J["Private Encrypted Images: files/{uidHash}/"]
    end

    K["Sign Out / Account Switch"] --> L["Close SQLCipher DB"]
    L --> M["Evict In-Memory Keys"]
    M --> N["Reset AppGraph Identity"]
```

---

## 3. Guard Intervention State Machine

```mermaid
stateDiagram-v2
    [*] --> TargetLaunched: User opens application
    TargetLaunched --> InspectingRule: AccessibilityService checks package/URL
    
    InspectingRule --> AllowedUsage: No active rule matches
    InspectingRule --> PauseActive: Match rule with PAUSE mode
    InspectingRule --> BlockActive: Match rule with BLOCK mode

    state PauseActive {
        [*] --> OverlayDisplayed
        OverlayDisplayed --> StepBackAction: User taps Step Back
        OverlayDisplayed --> WaitingDelay: Countdown running (e.g. 15s)
        WaitingDelay --> GracePeriodGranted: Delay expires & user continues
    }

    state BlockActive {
        [*] --> BlockOverlayDisplayed
        BlockOverlayDisplayed --> ImmediateExit: User taps Close / Back
    }

    StepBackAction --> ReturnToHome: Launch Home screen intent
    ImmediateExit --> ReturnToHome: Launch Home screen intent
    GracePeriodGranted --> AllowedUsage: Temporary access granted
    ReturnToHome --> [*]
    AllowedUsage --> [*]
```

---

## 4. Firebase Cloud Security & Authority Rules

```mermaid
flowchart TD
    subgraph Client["Android Client Request"]
        REQ["Firebase SDK Operation"]
    end

    subgraph Engine["Firebase Security Rules Engine"]
        AUTH_CHECK{"Is Request Authenticated?"}
        PATH_ROUTER{"Collection Path Router"}
        
        PROFILE_RULE{"/users/{uid}"}
        POD_MEMBER_RULE{"/pods/{podId}/members/{uid}"}
        POD_MSG_RULE{"/pods/{podId}/messages/{msgId}"}
        RTDB_PRESENCE{"/presence/{podId}/{uid}"}
        RTDB_TYPING{"/typing/{podId}/{uid}"}
    end

    REQ --> AUTH_CHECK
    AUTH_CHECK -- No --> DENY_1["DENY: Unauthenticated"]
    AUTH_CHECK -- Yes --> PATH_ROUTER

    PATH_ROUTER -->|User Profiles| PROFILE_RULE
    PROFILE_RULE -->|auth.uid == uid & immutable fields valid| ALLOW_PROFILE["ALLOW: Profile Access"]
    PROFILE_RULE -->|Cross-user or tamper| DENY_PROFILE["DENY: Permission Denied"]

    PATH_ROUTER -->|Pod Members| POD_MEMBER_RULE
    POD_MEMBER_RULE -->|Member exists in pod| ALLOW_POD["ALLOW: Read Pod State"]
    POD_MEMBER_RULE -->|Self-promotion / non-member| DENY_POD["DENY: Forbidden"]

    PATH_ROUTER -->|Pod Messages| POD_MSG_RULE
    POD_MSG_RULE -->|Sender matches auth.uid & content <= 1000 chars| ALLOW_MSG["ALLOW: Create Message"]
    POD_MSG_RULE -->|Impersonation / oversized| DENY_MSG["DENY: Invalid Payload"]

    PATH_ROUTER -->|Realtime Presence & Typing| RTDB_PRESENCE
    PATH_ROUTER -->|Realtime Presence & Typing| RTDB_TYPING
    RTDB_PRESENCE -->|auth.uid == uid| ALLOW_PRESENCE["ALLOW: Own Presence"]
    RTDB_TYPING -->|auth.uid == uid| ALLOW_TYPING["ALLOW: Own Typing Status"]
```
