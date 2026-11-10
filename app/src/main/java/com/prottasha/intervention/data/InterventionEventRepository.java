package com.prottasha.intervention.data;

import androidx.annotation.Nullable;

import com.prottasha.core.result.AppResult;
import com.prottasha.core.result.ResultCallback;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.intervention.domain.InterventionStateMachine;

import java.util.concurrent.Executor;

public final class InterventionEventRepository {
    private static final long RAPID_REOPEN_WINDOW_MILLIS = 60_000L;
    private final UserSessionDatabaseProvider provider;
    private final Executor disk;

    public InterventionEventRepository(UserSessionDatabaseProvider provider, Executor disk) {
        this.provider = provider;
        this.disk = disk;
    }

    public void recordPromptShown(
            long ruleId,
            String target,
            String type,
            int delaySeconds,
            String source,
            @Nullable ResultCallback<Long> callback) {
        disk.execute(() -> {
            try {
                if (provider == null || !provider.isOpen()) {
                    if (callback != null) callback.onResult(AppResult.failure(new IllegalStateException("No open session")));
                    return;
                }
                long now = System.currentTimeMillis();
                InterventionDao dao = provider.requireOpen().interventionDao();

                // Check prior recent event for rapid-reopen classification
                InterventionEventEntity latest = dao.findLatestForTarget(target);
                if (latest != null && "STEP_BACK".equals(latest.outcome) && "PENDING".equals(latest.followThrough)) {
                    InterventionStateMachine.State returnState = InterventionStateMachine.classifyReturn(
                            latest.resolvedAt > 0 ? latest.resolvedAt : latest.timestamp,
                            now,
                            RAPID_REOPEN_WINDOW_MILLIS);
                    dao.updateFollowThrough(latest.id, returnState.name());
                }

                String initialOutcome = "BLOCK".equals(type) ? "BLOCKED" : "SHOWN";
                InterventionEventEntity event = new InterventionEventEntity(
                        0L,
                        ruleId,
                        target,
                        type,
                        now,
                        0L,
                        delaySeconds,
                        source,
                        initialOutcome,
                        "",
                        "PENDING"
                );
                long id = dao.insert(event);
                if (callback != null) callback.onResult(AppResult.success(id));
            } catch (IllegalStateException noSession) {
                if (callback != null) callback.onResult(AppResult.failure(noSession));
            }
        });
    }

    public void recordResolution(
            long eventId,
            String outcome,
            String intention,
            @Nullable ResultCallback<Boolean> callback) {
        disk.execute(() -> {
            try {
                if (provider == null || !provider.isOpen()) {
                    if (callback != null) callback.onResult(AppResult.failure(new IllegalStateException("No open session")));
                    return;
                }
                long now = System.currentTimeMillis();
                InterventionDao dao = provider.requireOpen().interventionDao();
                dao.updateOutcome(eventId, outcome, now);
                if ("STEP_BACK".equals(outcome)) {
                    dao.updateFollowThrough(eventId, "PENDING");
                }
                if (callback != null) callback.onResult(AppResult.success(true));
            } catch (IllegalStateException noSession) {
                if (callback != null) callback.onResult(AppResult.failure(noSession));
            }
        });
    }

    public void record(String target, String type, String intention) {
        recordPromptShown(0L, target, type, 0, "ACCESSIBILITY", result -> {
            if (result.isSuccess() && result.valueOrNull() != null) {
                long id = result.valueOrNull();
                recordResolution(id, type, intention, null);
            }
        });
    }
}
