const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendPushOnNotificationCreate = functions.database
.ref("/notifications/{notifId}")
.onCreate(async (snapshot, context) => {
  const notif = snapshot.val();
  if (!notif) return null;

  const toUid = notif.to;
  const title = notif.title || "TurGo";
  const body = notif.content || "You have a new notification.";
  const notifId = context.params.notifId;

  if (!toUid) return null;

  // Get recipient role from /users/roles/{uid} (fallback to legacy /user_id_roles/{uid})
  let roleSnap = await admin.database().ref(`/users/roles/${toUid}`).get();
  if (!roleSnap.exists()) {
    roleSnap = await admin.database().ref(`/user_id_roles/${toUid}`).get();
  }
  let roleRaw = roleSnap.child("role").val();
  if (!roleRaw) {
    roleRaw = roleSnap.val();
  }
  if (!roleRaw) return null;

  const role = String(roleRaw).trim().toUpperCase();
  const rolePathMap = {
    STUDENT: "users/students",
    TEACHER: "users/teachers",
    PARENT: "users/parents",
    ADMIN: "users/admins"
  };
  const userPath = rolePathMap[role];
  if (!userPath) return null;

  // Read FCM token from user node
  let tokenSnap = await admin.database().ref(`/${userPath}/${toUid}/fcmToken`).get();
  // Legacy fallback paths.
  if (!tokenSnap.exists()) {
    const legacyPathMap = {
      STUDENT: "student",
      TEACHER: "teacher",
      PARENT: "parent",
      ADMIN: "admin"
    };
    const legacyUserPath = legacyPathMap[role];
    if (legacyUserPath) {
      tokenSnap = await admin.database().ref(`/${legacyUserPath}/${toUid}/fcmToken`).get();
    }
  }
  const token = tokenSnap.val();
  if (!token) {
    console.log("No fcmToken for uid:", toUid);
    return null;
  }

  // Send push
  const message = {
    token,
    notification: {
      title,
      body
    },
    data: {
      notifId: String(notifId),
      title: String(title),
      body: String(body)
    },
    android: {
      priority: "high",
      notification: {
        channelId: "turgo_general_channel"
      }
    }
  };

  try {
    const messageId = await admin.messaging().send(message);
    console.log("Push sent:", messageId, "to uid:", toUid);
  } catch (err) {
    console.error("Push failed:", err);
    if (err.code === "messaging/registration-token-not-registered") {
      await admin.database().ref(`/${userPath}/${toUid}/fcmToken`).remove();
    }
  }

  return null;
});
