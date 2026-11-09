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

describe('Realtime Database', () => {
  test('stays deny-all until the presence schema is implemented', async () => {
    const guestDb = testEnv.unauthenticatedContext().database();
    const userDb = testEnv.authenticatedContext('alice', verifiedClaims).database();

    await assertFails(get(ref(guestDb, 'presence/alice')));
    await assertFails(set(ref(guestDb, 'presence/alice'), true));
    await assertFails(get(ref(userDb, 'presence/alice')));
    await assertFails(set(ref(userDb, 'presence/alice'), true));
  });
});
