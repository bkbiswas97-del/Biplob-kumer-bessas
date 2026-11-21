const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_bk_001";
const ADMIN_EMAIL = "bkbiswas97@gmail.com";
const ALICE_UID = "alice_buyer_001";
const BOB_UID = "bob_buyer_002";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function adminContext(emailVerified = true) {
  return testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: emailVerified,
  });
}

function buyerContext(uid, email = `${uid}@example.com`) {
  return testEnv.authenticatedContext(uid, {
    email,
    email_verified: true,
  });
}

function sampleTractorPayload(createdBy = ADMIN_UID, status = "available") {
  const now = new Date(Date.now() - 2000);
  return {
    brand: "মাহিন্দ্রা",
    model: "Mahindra 575 DI XP Plus",
    title: "মাহিন্দ্রা ৫৭৫ ডিআই এক্সপি প্লাস (২০২০)",
    year: 2020,
    originalPrice: 950000,
    usedHours: 1350,
    totalDue: 120000,
    sellingPrice: 685000,
    condition: "EXCELLENT",
    conditionNote: "ইঞ্জিন ও গিয়ার বক্স সম্পূর্ণ ভালো, কোনো লিকেজ নেই।",
    features: ["৪৭ হর্সপাওয়ার ইঞ্জিন", "পাওয়ার স্টিয়ারিং", "অয়েল ইমার্জড ব্রেক"],
    location: "বগুড়া সদর",
    imageKey: "mahindra_575",
    videoKey: "mahindra_575",
    status,
    createdBy,
    createdAt: now,
    updatedAt: now,
  };
}

function sampleOrderPayload(userId, tractorId = "tractor_1") {
  const now = new Date(Date.now() - 2000);
  return {
    userId,
    tractorId,
    tractorTitle: "মাহিন্দ্রা ৫৭৫ ডিআই এক্সপি প্লাস (২০২০)",
    tractorBrand: "মাহিন্দ্রা",
    sellingPrice: 685000,
    customerName: "রহিম উদ্দিন",
    address: "গ্রাম: নওদাপাড়া, জেলা: রাজশাহী",
    phone: "01711223344",
    status: "PENDING",
    createdAt: now,
    updatedAt: now,
  };
}

function sampleReviewPayload(userId, tractorId = "tractor_1") {
  const now = new Date(Date.now() - 2000);
  return {
    tractorId,
    tractorStatus: "available",
    userId,
    reviewerName: "রহিম উদ্দিন",
    rating: 5,
    comment: "ট্রাক্টরের ইঞ্জিন খুব ভালো আছে, দামও যুক্তিসংগত।",
    createdAt: now,
    updatedAt: now,
  };
}

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

// 1. Unauthenticated bounds
test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("tractors").where("status", "==", "available").get());
  await assertFails(unauthDb.collection("orders").where("userId", "==", ALICE_UID).get());
  await assertFails(unauthDb.collection("reviews").where("tractorStatus", "==", "available").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("tractors").doc("tractor_1").set(sampleTractorPayload())
  );
});

// 2. Email Spoofing Test for Admin
test("Admin check rejects unverified email spoof and non-admin buyers", async () => {
  const spoofedAdminDb = adminContext(false).firestore();
  await assertFails(
    spoofedAdminDb.collection("tractors").doc("tractor_1").set(sampleTractorPayload(ADMIN_UID))
  );

  const aliceDb = buyerContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("tractors").doc("tractor_1").set(sampleTractorPayload(ALICE_UID))
  );
  // Prevent self-assigned admin doc
  await assertFails(
    aliceDb.collection("admins").doc(ALICE_UID).set({ role: "admin" })
  );
});

// 3. Verified Admin tractor CRUD & Buyer query alignment
test("Verified admin can create/update tractors; buyers can list only available tractors", async () => {
  const adminDb = adminContext(true).firestore();
  await assertSucceeds(
    adminDb.collection("tractors").doc("tractor_1").set(sampleTractorPayload(ADMIN_UID, "available"))
  );
  await assertSucceeds(
    adminDb.collection("tractors").doc("tractor_sold").set(sampleTractorPayload(ADMIN_UID, "sold"))
  );

  // Admin can update sellingPrice & condition
  await assertSucceeds(
    adminDb.collection("tractors").doc("tractor_1").update({
      sellingPrice: 670000,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Buyer filtered query (.whereEqualTo("status", "available")) succeeds
  const aliceDb = buyerContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("tractors").where("status", "==", "available").get()
  );

  // Unfiltered buyer query or reading sold tractor fails
  await assertFails(aliceDb.collection("tractors").get());
  await assertFails(aliceDb.collection("tractors").doc("tractor_sold").get());
});

// 4. Orders: PII isolation, creation, buyer cancel, admin management, shadow fields
test("Orders enforce owner/admin isolation, valid tractor reference, and state transitions", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("tractors").doc("tractor_1").set(sampleTractorPayload());
  });

  const aliceDb = buyerContext(ALICE_UID).firestore();
  const bobDb = buyerContext(BOB_UID).firestore();
  const adminDb = adminContext(true).firestore();

  // Alice creates a valid order
  await assertSucceeds(
    aliceDb.collection("orders").doc("order_1").set(sampleOrderPayload(ALICE_UID, "tractor_1"))
  );

  // Reject order with @DocumentId `id` field or ghost field or orphaned tractorId
  await assertFails(
    aliceDb.collection("orders").doc("order_bad_id").set({
      ...sampleOrderPayload(ALICE_UID, "tractor_1"),
      id: "order_bad_id",
    })
  );
  await assertFails(
    aliceDb.collection("orders").doc("order_orphan").set(sampleOrderPayload(ALICE_UID, "nonexistent_tractor"))
  );
  await assertFails(
    aliceDb.collection("orders").doc("order_spoof_status").set({
      ...sampleOrderPayload(ALICE_UID, "tractor_1"),
      status: "CONFIRMED",
    })
  );

  // PII Blanket Test: Bob cannot get Alice's order or list unfiltered orders
  await assertFails(bobDb.collection("orders").doc("order_1").get());
  await assertFails(bobDb.collection("orders").get());
  await assertSucceeds(bobDb.collection("orders").where("userId", "==", BOB_UID).get());

  // Alice can list her own orders
  await assertSucceeds(aliceDb.collection("orders").where("userId", "==", ALICE_UID).get());

  // Admin can list all orders and update status to CONFIRMED -> DELIVERED
  await assertSucceeds(adminDb.collection("orders").get());
  await assertSucceeds(
    adminDb.collection("orders").doc("order_1").update({
      status: "CONFIRMED",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Once CONFIRMED, buyer cannot cancel/tamper
  await assertFails(
    aliceDb.collection("orders").doc("order_1").update({
      status: "CANCELLED",
      updatedAt: new Date(Date.now() - 200),
    })
  );
});

// 5. Reviews & Star Ratings: 1-5 validation, author edit/delete, public read for available tractors
test("Reviews validate 1-5 star rating, author isolation, and tractor existence", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("tractors").doc("tractor_1").set(sampleTractorPayload());
  });

  const aliceDb = buyerContext(ALICE_UID).firestore();
  const bobDb = buyerContext(BOB_UID).firestore();

  // Invalid rating (0 or 6) must fail
  await assertFails(
    aliceDb.collection("reviews").doc("rev_bad").set({
      ...sampleReviewPayload(ALICE_UID, "tractor_1"),
      rating: 6,
    })
  );

  // Valid review succeeds
  await assertSucceeds(
    aliceDb.collection("reviews").doc("rev_1").set(sampleReviewPayload(ALICE_UID, "tractor_1"))
  );

  // Bob can read available tractor reviews, cannot edit/delete Alice's review
  await assertSucceeds(
    bobDb.collection("reviews").where("tractorStatus", "==", "available").get()
  );
  await assertFails(
    bobDb.collection("reviews").doc("rev_1").update({
      rating: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(bobDb.collection("reviews").doc("rev_1").delete());

  // Alice can update her own review
  await assertSucceeds(
    aliceDb.collection("reviews").doc("rev_1").update({
      rating: 4,
      comment: "টেস্ট ড্রাইভের পর আপডেট রিভিউ।",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

// 6. Future timestamp tampering fails
test("Future timestamp tampering is rejected", async () => {
  const adminDb = adminContext(true).firestore();
  const futurePayload = sampleTractorPayload();
  futurePayload.createdAt = new Date(Date.now() + 60000);
  await assertFails(adminDb.collection("tractors").doc("tractor_future").set(futurePayload));
});
