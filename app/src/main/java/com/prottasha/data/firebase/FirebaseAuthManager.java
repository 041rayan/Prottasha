package com.prottasha.data.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthManager {

    public interface AuthCallback {
        void onSuccess(String userId);
        void onFailure(String error);
    }

    private FirebaseAuth auth;

    public FirebaseAuthManager() {
        try {
            this.auth = FirebaseAuth.getInstance();
        } catch (Exception ignored) {
            // Unconfigured Firebase fallback
            this.auth = null;
        }
    }

    public void signInAnonymously(AuthCallback callback) {
        if (auth == null) {
            if (callback != null) callback.onSuccess("anon-user-" + System.currentTimeMillis() % 1000);
            return;
        }

        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful() && auth.getCurrentUser() != null) {
                if (callback != null) callback.onSuccess(auth.getCurrentUser().getUid());
            } else {
                if (callback != null) {
                    callback.onFailure(task.getException() != null ? task.getException().getMessage() : "Auth failed");
                }
            }
        });
    }

    public String getCurrentUserId() {
        if (auth != null && auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return "warrior-42";
    }
}
