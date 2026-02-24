package com.example.turgo;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

public final class AuthEmailChangeManager {
    static final String TAG = AuthEmailChangeManager.class.getSimpleName();
    private AuthEmailChangeManager() {
    }

    public interface VerificationCallback {
        void onVerificationSent();
        void onRecentLoginRequired();
        void onError(String message);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    public static boolean isGoogleProvider(FirebaseUser user) {
        if (user == null || user.getProviderData() == null) {
            return false;
        }
        for (UserInfo info : user.getProviderData()) {
            if (info != null && GoogleAuthProvider.PROVIDER_ID.equals(info.getProviderId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasPasswordProvider(FirebaseUser user) {
        if (user == null || user.getProviderData() == null) {
            return false;
        }
        for (UserInfo info : user.getProviderData()) {
            if (info != null && EmailAuthProvider.PROVIDER_ID.equals(info.getProviderId())) {
                return true;
            }
        }
        return false;
    }

    public static String validateEmailInputs(String newEmail, String confirmEmail, String currentEmail) {
        if (TextUtils.isEmpty(newEmail) || TextUtils.isEmpty(confirmEmail)) {
            return "Please complete both email fields.";
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            return "Please enter a valid email address.";
        }
        if (!newEmail.equalsIgnoreCase(confirmEmail)) {
            return "Email confirmation does not match.";
        }
        if (Tool.boolOf(currentEmail) && newEmail.equalsIgnoreCase(currentEmail)) {
            return "New email must be different from your current email.";
        }
        return null;
    }

    public static void verifyBeforeUpdateEmail(FirebaseUser user, String newEmail, VerificationCallback callback) {
        if (callback == null) {
            return;
        }
        if (user == null) {
            callback.onError("No signed-in user found.");
            Log.d(TAG, "no signed in user found");
            return;
        }
        if (!Tool.boolOf(newEmail)) {
            callback.onError("Please enter a valid email.");
            Log.d(TAG, "new email is FALSE" );
            return;
        }
        FirebaseAuth.getInstance().useAppLanguage();
        user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener(unused -> callback.onVerificationSent())
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                        callback.onRecentLoginRequired();
                    } else {
                        String message = mapVerifyBeforeUpdateError(e);
                        Log.e(TAG, "Failed to send verifyBeforeUpdateEmail. " + describeAuthError(e));
                        callback.onError(message);
                    }
                });
    }

    public static void reauthenticateWithPassword(FirebaseUser user, String currentEmail, String password, SimpleCallback callback) {
        if (callback == null) {
            return;
        }
        if (user == null || !Tool.boolOf(currentEmail)) {
            callback.onError("Session expired. Please sign in again.");
            return;
        }
        if (!Tool.boolOf(password)) {
            callback.onError("Password is required.");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(e -> {
                    String message = e != null && Tool.boolOf(e.getMessage())
                            ? e.getMessage()
                            : "Reauthentication failed.";
                    callback.onError(message);
                });
    }

    private static String mapVerifyBeforeUpdateError(Exception e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "The new email address format is invalid.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "This email is already used by another account.";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "This account is no longer valid. Please sign in again.";
        }
        if (e instanceof FirebaseTooManyRequestsException) {
            return "Too many attempts. Please wait and try again.";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Network error. Check your internet and try again.";
        }
        if (e instanceof FirebaseAuthException) {
            String code = ((FirebaseAuthException) e).getErrorCode();
            if ("ERROR_OPERATION_NOT_ALLOWED".equals(code)) {
                return "Email/password auth is disabled in Firebase Console.";
            }
            if ("ERROR_UNAUTHORIZED_CONTINUE_URI".equals(code)) {
                return "Continue URL domain is not authorized in Firebase Auth settings.";
            }
            if ("ERROR_INVALID_CONTINUE_URI".equals(code)) {
                return "Continue URL is invalid. Check ActionCodeSettings URL.";
            }
            if ("ERROR_MISSING_CONTINUE_URI".equals(code)) {
                return "Missing continue URL in ActionCodeSettings.";
            }
            if ("ERROR_MISSING_ANDROID_PKG_NAME".equals(code)) {
                return "Android package name is missing in ActionCodeSettings.";
            }
            if ("ERROR_INVALID_RECIPIENT_EMAIL".equals(code)) {
                return "The destination email cannot receive Firebase emails.";
            }
        }
        if (e != null && Tool.boolOf(e.getMessage())) {
            return e.getMessage();
        }
        return "Failed to request email verification.";
    }

    private static String describeAuthError(Exception e) {
        if (e == null) {
            return "error=null";
        }
        String code = e instanceof FirebaseAuthException
                ? ((FirebaseAuthException) e).getErrorCode()
                : "N/A";
        return "type=" + e.getClass().getSimpleName() + ", code=" + code + ", message=" + e.getMessage();
    }
}
