import { readFile } from 'node:fs/promises';
import { after, before, beforeEach, describe, test } from 'node:test';

import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import {
  collection,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  serverTimestamp,
  setDoc,
  Timestamp,
  updateDoc,
} from 'firebase/firestore';
import { get, ref, set } from 'firebase/database';

const PROJECT_ID = 'demo-prottasha';

const unverifiedClaims = {
  email: 'alice@example.test',
  email_verified: false,
  firebase: { sign_in_provider: 'password' },
};

const verifiedClaims = {
  ...unverifiedClaims,
  email_verified: true,
};

let testEnv;

function profile(overrides = {}) {
  return {
    displayName: 'Alice Example',
    podPseudonym: 'HopefulAlice',
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
    schemaVersion: 1,
    ...overrides,
  };
}

function firestoreFor(uid, claims) {
  return testEnv.authenticatedContext(uid, claims).firestore();
}

async function seedPod() {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    const now = Timestamp.now();
    await setDoc(doc(db, 'users/alice'), {
      displayName: 'Alice', podPseudonym: 'HopefulAlice',
      createdAt: now, updatedAt: now, schemaVersion: 1,
    });
    await setDoc(doc(db, 'users/bob'), {
      displayName: 'Bob', podPseudonym: 'SteadyBob',
      createdAt: now, updatedAt: now, schemaVersion: 1,
    });
    await setDoc(doc(db, 'pods/omega'), { name: 'Omega', createdAt: now });
    await setDoc(doc(db, 'pods/omega/members/alice'), { role: 'OWNER', joinedAt: now });
    await setDoc(doc(db, 'pods/omega/members/bob'), { role: 'MEMBER', joinedAt: now });
  });
}

before(async () => {
  const [firestoreRules, databaseRules] = await Promise.all([
    readFile('firestore.rules', 'utf8'),
    readFile('database.rules.json', 'utf8'),
  ]);

  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: { rules: firestoreRules },
    database: { rules: databaseRules },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
  await testEnv.clearDatabase();
});

after(async () => {
  await testEnv.cleanup();
});

describe('Firestore user profiles', () => {
  test('denies unauthenticated reads and creates', async () => {
    const db = testEnv.unauthenticatedContext().firestore();

    await assertFails(getDoc(doc(db, 'users/alice')));
    await assertFails(setDoc(doc(db, 'users/alice'), profile()));
  });

  test('denies anonymous accounts even if Authentication is enabled accidentally', async () => {
    const db = firestoreFor('anonymous-user', {
      email_verified: false,
      firebase: { sign_in_provider: 'anonymous' },
    });

    await assertFails(getDoc(doc(db, 'users/anonymous-user')));
    await assertFails(setDoc(doc(db, 'users/anonymous-user'), profile()));
  });

  test('lets an unverified account create and read only its valid profile', async () => {
    const db = firestoreFor('alice', unverifiedClaims);
    const ownProfile = doc(db, 'users/alice');

    await assertSucceeds(setDoc(ownProfile, profile()));
    await assertSucceeds(getDoc(ownProfile));
    await assertFails(updateDoc(ownProfile, {
      displayName: 'Changed before verification',
      updatedAt: serverTimestamp(),
    }));
  });

  test('lets an unverified account erase its own profile', async () => {
    const db = firestoreFor('alice', unverifiedClaims);
    const ownProfile = doc(db, 'users/alice');

    await assertSucceeds(setDoc(ownProfile, profile()));
    await assertSucceeds(deleteDoc(ownProfile));
  });

  test('lets a verified owner update allowed profile fields', async () => {
    const db = firestoreFor('alice', verifiedClaims);
    const ownProfile = doc(db, 'users/alice');

    await assertSucceeds(setDoc(ownProfile, profile()));
    await assertSucceeds(updateDoc(ownProfile, {
      displayName: 'Alice Updated',
      podPseudonym: 'HopefulAlice2',
      avatarSeed: 'alice-seed',
      avatarPalette: 'hope',
      avatarStyle: 'rings',
      updatedAt: serverTimestamp(),
    }));
  });

  test('denies cross-user reads, writes, and deletes', async () => {
    const aliceDb = firestoreFor('alice', verifiedClaims);
    await assertSucceeds(setDoc(doc(aliceDb, 'users/alice'), profile()));

    const bobDb = firestoreFor('bob', {
      ...verifiedClaims,
      email: 'bob@example.test',
    });
    const aliceProfile = doc(bobDb, 'users/alice');

    await assertFails(getDoc(aliceProfile));
    await assertFails(setDoc(aliceProfile, profile({ displayName: 'Impersonated' })));
    await assertFails(updateDoc(aliceProfile, {
      displayName: 'Impersonated',
      updatedAt: serverTimestamp(),
    }));
    await assertFails(deleteDoc(aliceProfile));
  });

  test('denies profile collection enumeration', async () => {
    const db = firestoreFor('alice', verifiedClaims);
    await assertSucceeds(setDoc(doc(db, 'users/alice'), profile()));

    await assertFails(getDocs(collection(db, 'users')));
  });

  test('denies schema pollution and self-assigned authority', async () => {
    const db = firestoreFor('alice', verifiedClaims);
    const ownProfile = doc(db, 'users/alice');

    await assertFails(setDoc(ownProfile, profile({
      role: 'admin',
      isModerator: true,
      ownerUid: 'alice',
    })));

    await assertSucceeds(setDoc(ownProfile, profile()));
    await assertFails(updateDoc(ownProfile, {
      role: 'admin',
      updatedAt: serverTimestamp(),
    }));
  });

  test('denies oversized fields, partial avatar data, and wrong types', async () => {
    const db = firestoreFor('alice', verifiedClaims);
    const ownProfile = doc(db, 'users/alice');

    await assertFails(setDoc(ownProfile, profile({
      displayName: 'x'.repeat(81),
    })));
    await assertFails(setDoc(ownProfile, profile({
      avatarSeed: 'missing-palette-and-style',
    })));
    await assertFails(setDoc(ownProfile, profile({
      schemaVersion: '1',
    })));

    await assertSucceeds(setDoc(ownProfile, profile()));
    await assertFails(updateDoc(ownProfile, {
      displayName: 'x'.repeat(81),
      updatedAt: serverTimestamp(),
    }));
  });

  test('denies client timestamps and immutable-field changes', async () => {
    const db = firestoreFor('alice', verifiedClaims);
    const ownProfile = doc(db, 'users/alice');

    await assertFails(setDoc(ownProfile, profile({
      createdAt: Timestamp.fromMillis(1),
      updatedAt: Timestamp.fromMillis(1),
    })));

    await assertSucceeds(setDoc(ownProfile, profile()));
    await assertFails(updateDoc(ownProfile, {
      schemaVersion: 2,
      updatedAt: serverTimestamp(),
    }));
    await assertFails(updateDoc(ownProfile, {
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    }));
  });

  test('denies every undefined Firestore path', async () => {
    const db = firestoreFor('alice', verifiedClaims);

    await assertFails(setDoc(doc(db, 'pods/omega'), {
      ownerUid: 'alice',
      name: 'Omega',
    }));
    await assertFails(setDoc(doc(db, 'pods/omega/messages/message-1'), {
      senderUid: 'alice',
      text: 'Should not exist yet',
    }));
  });
});

describe('Closed Circle pod', () => {
  test('members can read while verified nonmembers cannot', async () => {
    await seedPod();
    await assertSucceeds(getDoc(doc(firestoreFor('alice', verifiedClaims), 'pods/omega')));
    await assertFails(getDoc(doc(firestoreFor('mallory', verifiedClaims), 'pods/omega')));
    await assertFails(getDoc(doc(firestoreFor('alice', unverifiedClaims), 'pods/omega')));
  });

  test('clients cannot self-add membership or promote roles', async () => {
    await seedPod();
    const bob = firestoreFor('bob', verifiedClaims);
    await assertFails(setDoc(doc(bob, 'pods/omega/members/mallory'), {
      role: 'MEMBER', joinedAt: serverTimestamp(),
    }));
    await assertFails(updateDoc(doc(bob, 'pods/omega/members/bob'), { role: 'OWNER' }));
    await assertSucceeds(deleteDoc(doc(bob, 'pods/omega/members/bob')));
  });

  test('member message derives sender authority and bounds content', async () => {
    await seedPod();
    const alice = firestoreFor('alice', verifiedClaims);
    const message = doc(alice, 'pods/omega/messages/m1');
    await assertSucceeds(setDoc(message, {
      senderUid: 'alice', senderPseudonym: 'HopefulAlice', text: 'Keep going',
      createdAt: serverTimestamp(), updatedAt: serverTimestamp(), schemaVersion: 1,
    }));
    await assertFails(setDoc(doc(alice, 'pods/omega/messages/spoof'), {
      senderUid: 'bob', senderPseudonym: 'SteadyBob', text: 'spoof',
      createdAt: serverTimestamp(), updatedAt: serverTimestamp(), schemaVersion: 1,
    }));
    await assertSucceeds(updateDoc(message, { text: 'Edited', updatedAt: serverTimestamp() }));
    await assertSucceeds(deleteDoc(message));
  });

  test('reactions can be managed only by the authoring member', async () => {
    await seedPod();
    const alice = firestoreFor('alice', verifiedClaims);
    const bob = firestoreFor('bob', verifiedClaims);
    const mallory = firestoreFor('mallory', verifiedClaims);
    const message = doc(alice, 'pods/omega/messages/m1');
    await assertSucceeds(setDoc(message, {
      senderUid: 'alice', senderPseudonym: 'HopefulAlice', text: 'Hello',
      createdAt: serverTimestamp(), updatedAt: serverTimestamp(), schemaVersion: 1,
    }));

    const aliceReaction = doc(alice, 'pods/omega/messages/m1/reactions/alice');
    await assertSucceeds(setDoc(aliceReaction, { emoji: '👍' }));
    await assertSucceeds(getDoc(aliceReaction));

    // Bob cannot write Alice's reaction
    await assertFails(setDoc(doc(bob, 'pods/omega/messages/m1/reactions/alice'), { emoji: '❤️' }));
    // Mallory (nonmember) cannot read or write reactions
    await assertFails(getDoc(doc(mallory, 'pods/omega/messages/m1/reactions/alice')));
    await assertFails(setDoc(doc(mallory, 'pods/omega/messages/m1/reactions/mallory'), { emoji: '👍' }));
    // Alice can delete her reaction
    await assertSucceeds(deleteDoc(aliceReaction));
  });

  test('blocks are private to owner and self-blocking is prohibited', async () => {
    const alice = firestoreFor('alice', verifiedClaims);
    const bob = firestoreFor('bob', verifiedClaims);

    // Alice can block Bob
    await assertSucceeds(setDoc(doc(alice, 'users/alice/blocks/bob'), {
      blockedAt: serverTimestamp(),
    }));
    // Alice cannot block herself
    await assertFails(setDoc(doc(alice, 'users/alice/blocks/alice'), {
      blockedAt: serverTimestamp(),
    }));
    // Bob cannot read Alice's block list
    await assertFails(getDoc(doc(bob, 'users/alice/blocks/bob')));
    // Alice can unblock Bob
    await assertSucceeds(deleteDoc(doc(alice, 'users/alice/blocks/bob')));
  });

  test('reports stay private while moderator can review status', async () => {
    await seedPod();
    const bob = firestoreFor('bob', verifiedClaims);
    const report = doc(bob, 'pods/omega/messages/m1/reports/bob');
    await assertSucceeds(setDoc(report, {
      reason: 'Concerning content', createdAt: serverTimestamp(), status: 'OPEN',
    }));
    await assertSucceeds(getDoc(report));
    await assertFails(getDoc(doc(firestoreFor('mallory', verifiedClaims),
      'pods/omega/messages/m1/reports/bob')));
    await assertSucceeds(updateDoc(doc(firestoreFor('alice', verifiedClaims),
      'pods/omega/messages/m1/reports/bob'), { status: 'REVIEWED' }));
  });
});

describe('Realtime Database', () => {
  test('presence is membership-scoped and users write only themselves', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      const admin = context.database();
      await set(ref(admin, 'podMembers/omega/alice'), true);
      await set(ref(admin, 'podMembers/omega/bob'), true);
    });
    const guestDb = testEnv.unauthenticatedContext().database();
    const aliceDb = testEnv.authenticatedContext('alice', verifiedClaims).database();
    const malloryDb = testEnv.authenticatedContext('mallory', verifiedClaims).database();

    await assertFails(get(ref(guestDb, 'presence/omega')));
    await assertSucceeds(set(ref(aliceDb, 'presence/omega/alice'), {
      online: true, lastChanged: Date.now(),
    }));
    await assertSucceeds(get(ref(aliceDb, 'presence/omega')));
    await assertFails(set(ref(aliceDb, 'presence/omega/bob'), {
      online: true, lastChanged: Date.now(),
    }));
    await assertFails(get(ref(malloryDb, 'presence/omega')));
    await assertSucceeds(set(ref(aliceDb, 'podMembers/omega/alice'), null));
  });

  test('typing indicator is membership-scoped and users write only themselves', async () => {
    await testEnv.withSecurityRulesDisabled(async (context) => {
      const admin = context.database();
      await set(ref(admin, 'podMembers/omega/alice'), true);
      await set(ref(admin, 'podMembers/omega/bob'), true);
    });
    const guestDb = testEnv.unauthenticatedContext().database();
    const aliceDb = testEnv.authenticatedContext('alice', verifiedClaims).database();
    const malloryDb = testEnv.authenticatedContext('mallory', verifiedClaims).database();

    await assertFails(get(ref(guestDb, 'typing/omega')));
    await assertSucceeds(set(ref(aliceDb, 'typing/omega/alice'), {
      typing: true, lastChanged: Date.now(),
    }));
    await assertSucceeds(get(ref(aliceDb, 'typing/omega')));
    await assertFails(set(ref(aliceDb, 'typing/omega/bob'), {
      typing: true, lastChanged: Date.now(),
    }));
    await assertFails(set(ref(aliceDb, 'typing/omega/alice'), {
      typing: true, lastChanged: Date.now(), maliciousField: 'hack',
    }));
    await assertFails(get(ref(malloryDb, 'typing/omega')));
  });
});
