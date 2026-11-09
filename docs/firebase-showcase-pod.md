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

## Live rules deployment — 2026-11-10

- Explicit deployment approval received.
- Firestore ruleset: `8bb7963e-f078-43b1-8029-a0cfadd9496f`.
- Firestore local/remote SHA-256: `ba8e02dd8a9e32f03e7ffdc57450126219224fc79fdb8fdfde879b6cbd42dc3b` (exact match).
- RTDB semantic SHA-256: `26b0529da2839ceeb34eec591a26ccf25e9d9e832d612633b68ac4f4f32e8233` (exact semantic match).
- Membership remains unprovisioned; no users or pod data were created.
