package com.prottasha.circle.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.ListenerRegistration;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.circle.domain.PodMessage;
import com.prottasha.core.result.ResultCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class PodRepository {
    public static final String SHOWCASE_POD_ID = "academic-showcase";

    private final FirebasePodDataSource dataSource;

    public PodRepository(FirebasePodDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static boolean isValidMessageText(@Nullable String text) {
        if (text == null) return false;
        String trimmed = text.trim();
        return !trimmed.isEmpty() && trimmed.length() <= 1000;
    }

    public static boolean isValidReportReason(@Nullable String reason) {
        if (reason == null) return false;
        String trimmed = reason.trim();
        return !trimmed.isEmpty() && trimmed.length() <= 500;
    }

    public static List<PodMessage> filterBlockedMessages(
            @NonNull List<PodMessage> messages,
            @NonNull Set<String> blockedUids) {
        if (blockedUids.isEmpty()) return messages;
        List<PodMessage> filtered = new ArrayList<>();
        for (PodMessage msg : messages) {
            if (!blockedUids.contains(msg.senderUid)) {
                filtered.add(msg);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    public ListenerRegistration listen(FirebasePodDataSource.MessageListener listener) {
        return dataSource.listen(SHOWCASE_POD_ID, listener);
    }

    public void send(SessionUser user, String pseudonym, String text,
            ResultCallback<Boolean> callback) {
        dataSource.send(SHOWCASE_POD_ID, user.uid(), pseudonym, text, callback);
    }

    public void edit(String messageId, String newText, ResultCallback<Boolean> callback) {
        dataSource.edit(SHOWCASE_POD_ID, messageId, newText, callback);
    }

    public void delete(String messageId, ResultCallback<Boolean> callback) {
        dataSource.delete(SHOWCASE_POD_ID, messageId, callback);
    }

    public void setReaction(String messageId, String uid, String emoji, ResultCallback<Boolean> callback) {
        dataSource.setReaction(SHOWCASE_POD_ID, messageId, uid, emoji, callback);
    }

    public void removeReaction(String messageId, String uid, ResultCallback<Boolean> callback) {
        dataSource.removeReaction(SHOWCASE_POD_ID, messageId, uid, callback);
    }

    public void report(String messageId, String uid, String reason, ResultCallback<Boolean> callback) {
        dataSource.report(SHOWCASE_POD_ID, messageId, uid, reason, callback);
    }

    public void blockUser(String currentUid, String targetUid, ResultCallback<Boolean> callback) {
        dataSource.blockUser(currentUid, targetUid, callback);
    }

    public void unblockUser(String currentUid, String targetUid, ResultCallback<Boolean> callback) {
        dataSource.unblockUser(currentUid, targetUid, callback);
    }

    public ListenerRegistration listenBlocks(String currentUid, FirebasePodDataSource.BlocksListener listener) {
        return dataSource.listenBlocks(currentUid, listener);
    }

    public void leavePod(String uid, ResultCallback<Boolean> callback) {
        dataSource.leavePod(SHOWCASE_POD_ID, uid, callback);
    }
}
