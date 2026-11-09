# Firebase Closed Showcase Pod

**Client pod ID:** `academic-showcase`

Live provisioning is intentionally not performed by the Android client. After two disposable verified Firebase Auth users exist, an authorized evaluator/admin must create:

- `pods/academic-showcase` with `name` and server/admin `createdAt`.
- `pods/academic-showcase/members/{uid}` with immutable `role` (`OWNER`, `MODERATOR`, or `MEMBER`) and `joinedAt`.
- Matching RTDB booleans at `/podMembers/academic-showcase/{uid}=true`.
- Each user’s existing `/users/{uid}` profile with its rule-validated `podPseudonym`.

Clients cannot create membership, change roles, enumerate profiles, or write another UID’s presence/report/reaction. Firestore and RTDB emulator tests must pass before any live rule deployment. No secret credentials or disposable UID values belong in this document.

## Deployment gate

```bash
npm run test:rules
firebase deploy --only firestore:rules,database --dry-run --project prottasha-cse30
```

Live deployment requires explicit approval and remote ruleset hash verification.
