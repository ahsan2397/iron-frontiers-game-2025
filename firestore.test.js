const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function buildCleanProfilePayload(uid) {
  const now = new Date(Date.now() - 1000);
  return {
    userId: uid,
    commanderName: "Player One",
    stateName: "Pakistan",
    setupCompleted: true,
    worldVersion: 3,
    kingMode: true,
    level: 1,
    xp: 0,
    workExp: 0,
    medals: 0,
    premiumActive: true,
    strength: 10,
    strLevel: 1,
    education: 10,
    eduLevel: 1,
    endurance: 100,
    endLevel: 1,
    energy: 100,
    maxEnergy: 100,
    tickets: 5,
    currentRegionId: "punjab",
    birthRegionId: "punjab",
    residencies: ["punjab"],
    partyId: "party_player",
    partyName: "National Reform Party",
    parliamentSeats: 60,
    money: 1000000000,
    gold: 100,
    wood: 100,
    oil: 50,
    ore: 50,
    uranium: 0,
    diamonds: 0,
    tanks: 0,
    bombers: 0,
    missiles: 0,
    energyDrinks: 10000,
    explorationUsed: 0,
    explorationLimit: 1500,
    factoryLevel: 1,
    factoryPayMoney: 1000000,
    factoryPayGold: 10,
    factoryWorkCount: 0,
    questProgressMask: 0,
    autoWorkActive: false,
    totalDamage: 0,
    warsWon: 0,
    lawsPassed: 0,
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user cannot read or write profiles, regions, factories, or chats", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(buildCleanProfilePayload(ALICE_UID))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("regions").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("factories").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("chats").get()
  );
});

test("Owner can create clean Lvl 1 profile & update stats; cross-user and ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(buildCleanProfilePayload(ALICE_UID))
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      strength: 11,
      strLevel: 2,
      workExp: 25,
      tickets: 4,
      wood: 500000,
      medals: 1,
      tanks: 10,
      bombers: 5,
      missiles: 2,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      ghostField: 123,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Player-owned factories can be created, upgraded, and are isolated per owner", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("factories").doc("fac_1").set({
      userId: ALICE_UID,
      name: "Lahore Royal Gold",
      factoryType: "GOLD_MINE",
      regionId: "lahore",
      regionName: "Lahore - Pakistan",
      ownerName: "Player One",
      level: 1,
      foundedLabel: "19 Nov 2026",
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("factories").doc("fac_1").update({
      level: 2,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Cross-user factory access fails
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("factories").doc("fac_1").get()
  );

  // Invalid factory type fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("factories").doc("fac_bad").set({
      userId: ALICE_UID,
      name: "Bad Mine",
      factoryType: "CRYPTOMINE",
      regionId: "lahore",
      regionName: "Lahore - Pakistan",
      ownerName: "Player One",
      level: 1,
      foundedLabel: "19 Nov 2026",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Real country resource bounds, state building upgrades, and Punjab shield enforced", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("regions").doc("punjab").set({
      userId: ALICE_UID,
      name: "Punjab - Pakistan",
      country: "Pakistan",
      controller: "Pakistan",
      parliamentTitle: "Punjab Parl - Real Country - Clean",
      conquerable: false,
      autonomy: false,
      regionLevel: 10,
      goldIndex: 250,
      oilIndex: 200,
      oreIndex: 180,
      woodIndex: 170,
      uraniumIndex: 80,
      diamondIndex: 120,
      defenseIndex: 75,
      hospitalLevel: 1,
      militaryBaseLevel: 1,
      schoolLevel: 1,
      powerPlantLevel: 1,
      housingLevel: 1,
      population: 127688000,
      taxRate: 10,
      hp: 1000000,
      maxHp: 1000000,
      updatedAt: now,
    })
  );

  // State leader upgrades state buildings
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("regions").doc("punjab").update({
      militaryBaseLevel: 2,
      defenseIndex: 90,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Out of bounds Ore (> 200) fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("regions").doc("tokyo").set({
      userId: ALICE_UID,
      name: "Tokyo - Japan",
      country: "Japan",
      controller: "Japan",
      parliamentTitle: "Tokyo Parl - Real Country - Clean",
      conquerable: true,
      autonomy: false,
      regionLevel: 12,
      goldIndex: 210,
      oilIndex: 120,
      oreIndex: 250,
      woodIndex: 140,
      uraniumIndex: 50,
      diamondIndex: 150,
      defenseIndex: 60,
      hospitalLevel: 1,
      militaryBaseLevel: 1,
      schoolLevel: 1,
      powerPlantLevel: 1,
      housingLevel: 1,
      population: 14000000,
      taxRate: 10,
      hp: 250000,
      maxHp: 250000,
      updatedAt: now,
    })
  );

  // Real War on Punjab rejected, Training War on Punjab allowed
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("wars").doc("war_punjab").set({
      userId: ALICE_UID,
      targetRegionId: "punjab",
      targetRegionName: "Punjab - Pakistan",
      warType: "COUP",
      attackerName: "Rebels",
      defenderName: "Pakistan",
      attackerDamage: 0,
      defenderDamage: 0,
      membersCount: 1,
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("wars").doc("war_train_punjab").set({
      userId: ALICE_UID,
      targetRegionId: "punjab",
      targetRegionName: "Punjab - Pakistan",
      warType: "TRAINING",
      attackerName: "Training Squad",
      defenderName: "Simulated Defense",
      attackerDamage: 0,
      defenderDamage: 0,
      membersCount: 1,
      status: "ACTIVE",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Party max 60 members & clean Articles + Chats + Global Chat validate properly", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc("party_1").set({
      userId: ALICE_UID,
      name: "Global Reform Party",
      tag: "GRP",
      colorHex: "#16A34A",
      seats: 24,
      membersCount: 30,
      maxMembers: 60,
      wedgeScore: 30,
      ideology: "Real Country Progress",
      treasuryGold: 500,
      userMember: true,
      updatedAt: now,
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("parties").doc("party_1").update({
      membersCount: 65,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("articles").doc("art_1").set({
      userId: ALICE_UID,
      authorName: "Citizen_01",
      newspaper: "World Chronicle",
      title: "40 Real Countries Connected",
      body: "Clean new world opened across all 40 real-world regions.",
      votes: 12,
      botGenerated: true,
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_1").set({
      userId: ALICE_UID,
      senderName: "Citizen_02",
      senderRole: "Citizen",
      message: "Welcome to the clean world! Which country did you start in?",
      botSender: true,
      createdAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("global_chat").doc("g_1").set({
      senderUid: ALICE_UID,
      senderName: "Player One",
      stateName: "Pakistan",
      regionName: "Punjab - Pakistan",
      isOwner: true,
      message: "Hello world!",
      createdAt: now,
    })
  );
});
