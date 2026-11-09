package com.prottasha.circle.data;

import com.google.firebase.firestore.ListenerRegistration;
import com.prottasha.auth.domain.SessionUser;
import com.prottasha.core.result.ResultCallback;

public final class PodRepository {
    public static final String SHOWCASE_POD_ID = "academic-showcase";
    private final FirebasePodDataSource dataSource;

    public PodRepository(FirebasePodDataSource dataSource) { this.dataSource = dataSource; }
    public ListenerRegistration listen(FirebasePodDataSource.MessageListener listener) {
        return dataSource.listen(SHOWCASE_POD_ID, listener);
    }
    public void send(SessionUser user, String pseudonym, String text,
            ResultCallback<Boolean> callback) {
        dataSource.send(SHOWCASE_POD_ID, user.uid(), pseudonym, text, callback);
    }
}
