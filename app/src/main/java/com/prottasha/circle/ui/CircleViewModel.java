package com.prottasha.circle.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.prottasha.R;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.circle.data.FirebasePodDataSource;
import com.prottasha.circle.data.PodRepository;
import com.prottasha.circle.data.PresenceRepository;
import com.prottasha.circle.data.TypingRepository;
import com.prottasha.circle.domain.PodMessage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CircleViewModel extends ViewModel {

    private final PodRepository podRepository;
    private final PresenceRepository presenceRepository;
    private final TypingRepository typingRepository;

    private final MutableLiveData<List<PodMessage>> messagesLiveData = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> onlineCountLiveData = new MutableLiveData<>(0);
    private final MutableLiveData<String> typingTextLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isPodMemberLiveData = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> leftPodLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> pseudonymLiveData = new MutableLiveData<>("");

    private final Map<String, String> uidToPseudonymMap = new HashMap<>();
    private final Set<String> blockedUids = new HashSet<>();
    private List<PodMessage> rawMessages = Collections.emptyList();

    private ListenerRegistration messagesListener;
    private ListenerRegistration blocksListener;
    private ValueEventListener presenceListener;
    private ValueEventListener typingListener;

    private SessionUser currentUser;
    private boolean initialized = false;

    public CircleViewModel() {
        this(
                new PodRepository(new FirebasePodDataSource()),
                new PresenceRepository(),
                new TypingRepository()
        );
    }

    public CircleViewModel(
            PodRepository podRepository,
            PresenceRepository presenceRepository,
            TypingRepository typingRepository) {
        this.podRepository = podRepository;
        this.presenceRepository = presenceRepository;
        this.typingRepository = typingRepository;
    }

    public int statusText() {
        return R.string.circle_closed_status;
    }

    public LiveData<List<PodMessage>> messages() { return messagesLiveData; }
    public LiveData<Integer> onlineCount() { return onlineCountLiveData; }
    public LiveData<String> typingText() { return typingTextLiveData; }
    public LiveData<String> error() { return errorLiveData; }
    public LiveData<Boolean> isPodMember() { return isPodMemberLiveData; }
    public LiveData<Boolean> leftPod() { return leftPodLiveData; }
    public LiveData<String> pseudonym() { return pseudonymLiveData; }

    public void init(@NonNull SessionUser user) {
        if (initialized) return;
        initialized = true;
        this.currentUser = user;

        // 1. Fetch user pseudonym
        FirebaseFirestore.getInstance().collection("users").document(user.uid()).get()
                .addOnSuccessListener(profile -> {
                    String pseudonym = profile.getString("podPseudonym");
                    if (pseudonym != null && !pseudonym.isEmpty()) {
                        pseudonymLiveData.postValue(pseudonym);
                        uidToPseudonymMap.put(user.uid(), pseudonym);
                    }
                })
                .addOnFailureListener(e -> errorLiveData.postValue("Could not load user profile"));

        // 2. Connect presence
        presenceRepository.connect(PodRepository.SHOWCASE_POD_ID, user.uid());

        // 3. Listen to presence
        presenceListener = presenceRepository.listenPresence(PodRepository.SHOWCASE_POD_ID,
                (count, presenceMap) -> onlineCountLiveData.postValue(count));

        // 4. Listen to typing
        typingListener = typingRepository.listenTyping(PodRepository.SHOWCASE_POD_ID, user.uid(),
                typingUids -> {
                    if (typingUids.isEmpty()) {
                        typingTextLiveData.postValue(null);
                    } else {
                        String firstUid = typingUids.get(0);
                        String name = uidToPseudonymMap.get(firstUid);
                        if (name != null) {
                            typingTextLiveData.postValue(name + " is typing…");
                        } else {
                            typingTextLiveData.postValue("A member is typing…");
                        }
                    }
                });

        // 5. Listen to blocked users
        blocksListener = podRepository.listenBlocks(user.uid(), blocked -> {
            blockedUids.clear();
            blockedUids.addAll(blocked);
            applyFilter();
        });

        // 6. Listen to messages
        messagesListener = podRepository.listen(new FirebasePodDataSource.MessageListener() {
            @Override
            public void onMessages(List<PodMessage> messages) {
                rawMessages = messages;
                for (PodMessage msg : messages) {
                    uidToPseudonymMap.put(msg.senderUid, msg.pseudonym);
                }
                applyFilter();
            }

            @Override
            public void onError() {
                isPodMemberLiveData.postValue(false);
            }
        });
    }

    private void applyFilter() {
        List<PodMessage> filtered = PodRepository.filterBlockedMessages(rawMessages, blockedUids);
        messagesLiveData.postValue(filtered);
    }

    public void onUserTyping() {
        if (currentUser != null) {
            typingRepository.onUserKeyTyped(PodRepository.SHOWCASE_POD_ID, currentUser.uid());
        }
    }

    public void sendMessage(@NonNull String text) {
        if (currentUser == null) return;
        typingRepository.stopTyping(PodRepository.SHOWCASE_POD_ID, currentUser.uid());
        if (!PodRepository.isValidMessageText(text)) {
            errorLiveData.postValue("Message cannot be empty or exceed 1000 characters");
            return;
        }
        String pseudonym = pseudonymLiveData.getValue();
        if (pseudonym == null || pseudonym.isEmpty()) {
            pseudonym = "Member";
        }
        podRepository.send(currentUser, pseudonym, text.trim(), result -> {
            if (!result.isSuccess()) {
                errorLiveData.postValue("Message could not be sent");
            }
        });
    }

    public void editMessage(@NonNull String messageId, @NonNull String newText) {
        if (!PodRepository.isValidMessageText(newText)) {
            errorLiveData.postValue("Message cannot be empty or exceed 1000 characters");
            return;
        }
        podRepository.edit(messageId, newText.trim(), result -> {
            if (!result.isSuccess()) {
                errorLiveData.postValue("Failed to edit message");
            }
        });
    }

    public void deleteMessage(@NonNull String messageId) {
        podRepository.delete(messageId, result -> {
            if (!result.isSuccess()) {
                errorLiveData.postValue("Failed to delete message");
            }
        });
    }

    public void setReaction(@NonNull String messageId, @NonNull String emoji) {
        if (currentUser == null) return;
        podRepository.setReaction(messageId, currentUser.uid(), emoji, result -> {
            if (!result.isSuccess()) {
                errorLiveData.postValue("Failed to set reaction");
            }
        });
    }

    public void reportMessage(@NonNull String messageId, @NonNull String reason) {
        if (currentUser == null) return;
        if (!PodRepository.isValidReportReason(reason)) {
            errorLiveData.postValue("Report reason cannot be empty or exceed 500 characters");
            return;
        }
        podRepository.report(messageId, currentUser.uid(), reason.trim(), result -> {
            if (result.isSuccess()) {
                errorLiveData.postValue("Report submitted for review");
            } else {
                errorLiveData.postValue("Failed to submit report");
            }
        });
    }

    public void blockUser(@NonNull String targetUid) {
        if (currentUser == null) return;
        podRepository.blockUser(currentUser.uid(), targetUid, result -> {
            if (result.isSuccess()) {
                blockedUids.add(targetUid);
                applyFilter();
            } else {
                errorLiveData.postValue("Failed to block user");
            }
        });
    }

    public void leavePod() {
        if (currentUser == null) return;
        podRepository.leavePod(currentUser.uid(), result -> {
            if (result.isSuccess()) {
                leftPodLiveData.postValue(true);
            } else {
                errorLiveData.postValue("Failed to leave pod");
            }
        });
    }

    @Override
    protected void onCleared() {
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
        }
        if (blocksListener != null) {
            blocksListener.remove();
            blocksListener = null;
        }
        if (presenceListener != null) {
            presenceRepository.removeListener(PodRepository.SHOWCASE_POD_ID, presenceListener);
            presenceListener = null;
        }
        if (typingListener != null) {
            typingRepository.removeListener(PodRepository.SHOWCASE_POD_ID, typingListener);
            typingListener = null;
        }
        presenceRepository.disconnect();
        super.onCleared();
    }
}
