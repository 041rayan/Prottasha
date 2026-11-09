package com.prottasha.habits.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prottasha.auth.domain.SessionUser;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;
import com.prottasha.habits.domain.HabitAction;
import com.prottasha.habits.domain.HabitModels;
import com.prottasha.habits.domain.HabitSchedule;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public final class HabitRepositoryTest {

    @Test
    public void duplicateNonceWritesOneEventAndAccountsRemainIsolated() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        UserSessionDatabaseProvider provider = new UserSessionDatabaseProvider(context);
        HabitRepository repository = new HabitRepository(provider, Runnable::run);
        SessionUser alice = user("alice");
        SessionUser bob = user("bob");
        long day = LocalDate.now().toEpochDay();

        try {
            provider.open(alice);
            AtomicReference<Long> habitId = new AtomicReference<>();
            repository.save(new HabitModels.HabitSnapshot(
                    0L, "Read", HabitModels.Type.BUILD, 1, HabitSchedule.daily(day)),
                    result -> habitId.set(result.valueOrNull()));
            HabitAction action = HabitAction.complete("stable-nonce").forHabit(habitId.get(), day);
            repository.record(action, ignored -> { });
            repository.record(action, ignored -> { });
            assertEquals(1, provider.requireOpen().habitDao().countEventsByNonce("stable-nonce"));

            provider.close();
            provider.open(bob);
            assertTrue(provider.requireOpen().habitDao().listActive().isEmpty());
        } finally {
            provider.close();
            provider.destroy(alice);
            provider.destroy(bob);
        }
    }

    private static SessionUser user(String prefix) {
        String token = UUID.randomUUID().toString();
        return new SessionUser(prefix + token, prefix, prefix + "@example.test", true);
    }
}
