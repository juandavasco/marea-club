// Pruebas de las reglas de seguridad de Firestore de Marea Club.
// Se ejecutan con el emulador gratuito: `npm test` dentro de la carpeta firebase/.
import { test, before, beforeEach, after } from 'node:test';
import { readFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails,
} from '@firebase/rules-unit-testing';
import {
  doc, getDoc, setDoc, updateDoc, deleteDoc, collection, getDocs, addDoc,
  writeBatch, serverTimestamp, Timestamp,
} from 'firebase/firestore';

const FID = 'fam1';
const OWNER = 'adulto1';
const SECOND = 'adulto2';
const CHILD = 'nino1';
const STRANGER = 'extrano';

let env;
const sha = (code) => createHash('sha256').update(code).digest('hex');

const google = (uid) => env.authenticatedContext(uid, { firebase: { sign_in_provider: 'google.com' } }).firestore();
const anon = (uid) => env.authenticatedContext(uid, { firebase: { sign_in_provider: 'anonymous' } }).firestore();
const nobody = () => env.unauthenticatedContext().firestore();

/** Prepara datos sin pasar por las reglas. */
async function seed(fn) {
  await env.withSecurityRulesDisabled(async (ctx) => fn(ctx.firestore()));
}

async function seedFamily(extra = {}) {
  await seed((db) => setDoc(doc(db, 'families', FID), {
    ownerUid: OWNER, adultUids: [OWNER], adultNames: { [OWNER]: 'Juan' },
    childUid: null, childName: 'Navegante', createdAt: Timestamp.now(), lastLinkCode: '', ...extra,
  }));
}

async function seedCode(code, { role = 'child', minutesAgo = 1, used = false, familyId = FID } = {}) {
  await seed((db) => setDoc(doc(db, 'linkCodes', sha(code)), {
    familyId, role, createdBy: OWNER,
    createdAt: Timestamp.fromMillis(Date.now() - minutesAgo * 60_000),
    used, usedBy: used ? 'alguien' : null,
  }));
}

/** El vínculo que hace la app del niño: tres escrituras en un solo lote. */
function childLinkBatch(db, uid, code, familyId = FID) {
  const cid = sha(code);
  const b = writeBatch(db);
  b.update(doc(db, 'linkCodes', cid), { used: true, usedBy: uid });
  b.update(doc(db, 'families', familyId), { childUid: uid, lastLinkCode: cid });
  b.set(doc(db, 'users', uid), { role: 'child', familyId, displayName: 'Navegante' });
  return b;
}

function adultLinkBatch(db, uid, code) {
  const cid = sha(code);
  const b = writeBatch(db);
  b.update(doc(db, 'linkCodes', cid), { used: true, usedBy: uid });
  b.update(doc(db, 'families', FID), {
    adultUids: [OWNER, uid], adultNames: { [OWNER]: 'Juan', [uid]: 'Ana' }, lastLinkCode: cid,
  });
  b.set(doc(db, 'users', uid), { role: 'adult', familyId: FID, displayName: 'Ana' });
  return b;
}

before(async () => {
  env = await initializeTestEnvironment({
    projectId: 'demo-marea-club',
    firestore: { rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8') },
  });
});
beforeEach(async () => env.clearFirestore());
after(async () => env.cleanup());

// ------------------------------------------------------------------ crear familia

test('un adulto con Google crea su familia y su perfil en un lote', async () => {
  const db = google(OWNER);
  const b = writeBatch(db);
  b.set(doc(db, 'families', FID), {
    ownerUid: OWNER, adultUids: [OWNER], adultNames: { [OWNER]: 'Juan' },
    childUid: null, childName: 'Navegante', createdAt: serverTimestamp(), lastLinkCode: '',
  });
  b.set(doc(db, 'users', OWNER), { role: 'adult', familyId: FID, displayName: 'Juan' });
  await assertSucceeds(b.commit());
});

test('un perfil anónimo (niño) no puede crear una familia', async () => {
  await assertFails(setDoc(doc(anon(CHILD), 'families', FID), {
    ownerUid: CHILD, adultUids: [CHILD], adultNames: {}, childUid: null, childName: '',
    createdAt: serverTimestamp(), lastLinkCode: '',
  }));
});

test('no se puede crear una familia ya con un niño vinculado', async () => {
  await assertFails(setDoc(doc(google(OWNER), 'families', FID), {
    ownerUid: OWNER, adultUids: [OWNER], adultNames: {}, childUid: CHILD, childName: '',
    createdAt: serverTimestamp(), lastLinkCode: '',
  }));
});

test('sin sesión no se lee nada', async () => {
  await seedFamily();
  await assertFails(getDoc(doc(nobody(), 'families', FID)));
});

test('un extraño no puede leer la familia', async () => {
  await seedFamily();
  await assertFails(getDoc(doc(google(STRANGER), 'families', FID)));
});

// ------------------------------------------------------------------ códigos

const newCode = (db, role = 'child') => setDoc(doc(db, 'linkCodes', sha('K7QX-M2PA')), {
  familyId: FID, role, createdBy: OWNER, createdAt: serverTimestamp(), used: false, usedBy: null,
});

test('el adulto de la familia crea un código', async () => {
  await seedFamily();
  await assertSucceeds(newCode(google(OWNER)));
});

test('un extraño no puede crear códigos para otra familia', async () => {
  await seedFamily();
  const db = google(STRANGER);
  await assertFails(setDoc(doc(db, 'linkCodes', sha('K7QX-M2PA')), {
    familyId: FID, role: 'child', createdBy: STRANGER, createdAt: serverTimestamp(), used: false, usedBy: null,
  }));
});

test('un código no puede nacer usado ni con fecha inventada', async () => {
  await seedFamily();
  const db = google(OWNER);
  await assertFails(setDoc(doc(db, 'linkCodes', sha('A')), {
    familyId: FID, role: 'child', createdBy: OWNER, createdAt: serverTimestamp(), used: true, usedBy: null,
  }));
  await assertFails(setDoc(doc(db, 'linkCodes', sha('B')), {
    familyId: FID, role: 'child', createdBy: OWNER, createdAt: Timestamp.fromMillis(Date.now() + 3600_000), used: false, usedBy: null,
  }));
});

test('no hay código de segundo adulto si ya hay dos adultos', async () => {
  await seedFamily({ adultUids: [OWNER, SECOND] });
  await assertFails(newCode(google(OWNER), 'adult'));
});

test('solo el dueño crea códigos para un segundo adulto', async () => {
  await seedFamily({ adultUids: [OWNER, SECOND] });
  await seed((db) => updateDoc(doc(db, 'families', FID), { adultUids: [OWNER, SECOND] }));
  const db = google(SECOND);
  await assertFails(setDoc(doc(db, 'linkCodes', sha('C')), {
    familyId: FID, role: 'adult', createdBy: SECOND, createdAt: serverTimestamp(), used: false, usedBy: null,
  }));
});

test('los códigos no se pueden listar', async () => {
  await seedFamily();
  await seedCode('K7QX-M2PA');
  await assertFails(getDocs(collection(anon(STRANGER), 'linkCodes')));
});

// ------------------------------------------------------------------ vínculo del niño

test('el niño se vincula con un código válido', async () => {
  await seedFamily();
  await seedCode('K7QX-M2PA');
  await assertSucceeds(childLinkBatch(anon(CHILD), CHILD, 'K7QX-M2PA').commit());
  await assertSucceeds(getDoc(doc(anon(CHILD), 'families', FID)));
});

test('un código vencido (más de 10 minutos) no sirve', async () => {
  await seedFamily();
  await seedCode('K7QX-M2PA', { minutesAgo: 11 });
  await assertFails(childLinkBatch(anon(CHILD), CHILD, 'K7QX-M2PA').commit());
});

test('un código ya usado no sirve otra vez', async () => {
  await seedFamily();
  await seedCode('K7QX-M2PA');
  await assertSucceeds(childLinkBatch(anon(CHILD), CHILD, 'K7QX-M2PA').commit());
  await assertFails(childLinkBatch(anon('otro_nino'), 'otro_nino', 'K7QX-M2PA').commit());
});

test('un código inexistente no sirve', async () => {
  await seedFamily();
  await assertFails(childLinkBatch(anon(CHILD), CHILD, 'NOEX-ISTE').commit());
});

test('un código de adulto no vincula a un niño', async () => {
  await seedFamily();
  await seedCode('K7QX-M2PA', { role: 'adult' });
  await assertFails(childLinkBatch(anon(CHILD), CHILD, 'K7QX-M2PA').commit());
});

test('el niño no puede vincularse sin gastar el código', async () => {
  await seedFamily();
  await seedCode('K7QX-M2PA');
  const db = anon(CHILD);
  await assertFails(updateDoc(doc(db, 'families', FID), { childUid: CHILD, lastLinkCode: sha('K7QX-M2PA') }));
});

test('un código de una familia no abre otra familia', async () => {
  await seedFamily();
  await seed((db) => setDoc(doc(db, 'families', 'fam2'), {
    ownerUid: STRANGER, adultUids: [STRANGER], adultNames: {}, childUid: null, childName: '',
    createdAt: Timestamp.now(), lastLinkCode: '',
  }));
  await seedCode('K7QX-M2PA', { familyId: FID });
  await assertFails(childLinkBatch(anon(CHILD), CHILD, 'K7QX-M2PA', 'fam2').commit());
});

test('al vincularse, el niño no puede cambiar otros campos de la familia', async () => {
  await seedFamily();
  await seedCode('K7QX-M2PA');
  const db = anon(CHILD);
  const cid = sha('K7QX-M2PA');
  const b = writeBatch(db);
  b.update(doc(db, 'linkCodes', cid), { used: true, usedBy: CHILD });
  b.update(doc(db, 'families', FID), { childUid: CHILD, lastLinkCode: cid, adultUids: [CHILD] });
  await assertFails(b.commit());
});

test('el niño puede volver a vincularse desde otro teléfono con un código nuevo', async () => {
  await seedFamily({ childUid: CHILD });
  await seedCode('NUEV-OCOD');
  await assertSucceeds(childLinkBatch(anon('nino1_telefono_nuevo'), 'nino1_telefono_nuevo', 'NUEV-OCOD').commit());
});

test('el niño puede salir de la familia', async () => {
  await seedFamily({ childUid: CHILD });
  await assertSucceeds(updateDoc(doc(anon(CHILD), 'families', FID), { childUid: null }));
});

// ------------------------------------------------------------------ segundo adulto

test('un segundo adulto con Google se vincula con un código de adulto', async () => {
  await seedFamily();
  await seedCode('ADUL-TODO', { role: 'adult' });
  await assertSucceeds(adultLinkBatch(google(SECOND), SECOND, 'ADUL-TODO').commit());
});

test('un perfil anónimo no puede entrar como adulto', async () => {
  await seedFamily();
  await seedCode('ADUL-TODO', { role: 'adult' });
  await assertFails(adultLinkBatch(anon(SECOND), SECOND, 'ADUL-TODO').commit());
});

test('el dueño puede retirar al segundo adulto y desvincular al niño', async () => {
  await seedFamily({ adultUids: [OWNER, SECOND], childUid: CHILD });
  const db = google(OWNER);
  await assertSucceeds(updateDoc(doc(db, 'families', FID), { adultUids: [OWNER] }));
  await assertSucceeds(updateDoc(doc(db, 'families', FID), { childUid: null }));
});

test('el segundo adulto no puede retirar al dueño ni cambiar de dueño', async () => {
  await seedFamily({ adultUids: [OWNER, SECOND] });
  const db = google(SECOND);
  await assertFails(updateDoc(doc(db, 'families', FID), { adultUids: [SECOND] }));
  await assertFails(updateDoc(doc(db, 'families', FID), { ownerUid: SECOND }));
  await assertSucceeds(updateDoc(doc(db, 'families', FID), { adultUids: [OWNER] }));
});

test('un adulto no puede poner a otro niño en la familia por su cuenta', async () => {
  await seedFamily();
  await assertFails(updateDoc(doc(google(OWNER), 'families', FID), { childUid: 'nino_inventado' }));
});

// ------------------------------------------------------------------ perfiles

test('cada quien solo ve su propio perfil', async () => {
  await seed((db) => setDoc(doc(db, 'users', CHILD), { role: 'child', familyId: FID, displayName: 'N' }));
  await assertSucceeds(getDoc(doc(anon(CHILD), 'users', CHILD)));
  await assertFails(getDoc(doc(google(OWNER), 'users', CHILD)));
});

test('nadie se declara niño de una familia que no lo tiene vinculado', async () => {
  await seedFamily();
  await assertFails(setDoc(doc(anon(CHILD), 'users', CHILD), { role: 'child', familyId: FID, displayName: 'N' }));
});

// ------------------------------------------------------------------ Mis salvavidas

const request = (uid) => ({ childUid: uid, createdAt: serverTimestamp(), status: 'enviada', respondedBy: null, respondedAt: null });

test('el niño vinculado pide acompañamiento y sus adultos lo ven', async () => {
  await seedFamily({ adultUids: [OWNER, SECOND], childUid: CHILD });
  const ref = await assertSucceeds(addDoc(collection(anon(CHILD), 'families', FID, 'requests'), request(CHILD)));
  await assertSucceeds(getDoc(doc(google(OWNER), 'families', FID, 'requests', ref.id)));
  await assertSucceeds(getDoc(doc(google(SECOND), 'families', FID, 'requests', ref.id)));
  await assertFails(getDoc(doc(google(STRANGER), 'families', FID, 'requests', ref.id)));
});

test('el pedido no admite texto libre ni datos emocionales', async () => {
  await seedFamily({ childUid: CHILD });
  await assertFails(addDoc(collection(anon(CHILD), 'families', FID, 'requests'), { ...request(CHILD), marea: 'TORMENTA' }));
  await assertFails(addDoc(collection(anon(CHILD), 'families', FID, 'requests'), { ...request(CHILD), mensaje: 'hola' }));
});

test('un niño no vinculado no puede enviar pedidos', async () => {
  await seedFamily({ childUid: CHILD });
  await assertFails(addDoc(collection(anon('otro_nino'), 'families', FID, 'requests'), request('otro_nino')));
});

test('el adulto responde con una opción válida; el niño puede cancelar', async () => {
  await seedFamily({ childUid: CHILD });
  const ref = await addDoc(collection(anon(CHILD), 'families', FID, 'requests'), request(CHILD));
  const adult = google(OWNER);
  await assertFails(updateDoc(doc(adult, 'families', FID, 'requests', ref.id), { status: 'otra_cosa', respondedBy: OWNER, respondedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(doc(adult, 'families', FID, 'requests', ref.id), { status: 'voy', respondedBy: OWNER, respondedAt: serverTimestamp() }));
  await assertSucceeds(updateDoc(doc(anon(CHILD), 'families', FID, 'requests', ref.id), { status: 'cancelada' }));
});

test('los pedidos no se borran', async () => {
  await seedFamily({ childUid: CHILD });
  const ref = await addDoc(collection(anon(CHILD), 'families', FID, 'requests'), request(CHILD));
  await assertFails(deleteDoc(doc(google(OWNER), 'families', FID, 'requests', ref.id)));
});

// ------------------------------------------------------------------ todo lo demás

test('no existe ninguna colección para guardar mareas, diario o respuestas', async () => {
  await seedFamily({ childUid: CHILD });
  await assertFails(setDoc(doc(anon(CHILD), 'mareas', 'x'), { type: 'TORMENTA' }));
  await assertFails(setDoc(doc(anon(CHILD), 'families', FID, 'diario', 'x'), { text: 'secreto' }));
});
