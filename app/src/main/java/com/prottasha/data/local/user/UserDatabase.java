package com.prottasha.data.local.user;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.prottasha.data.local.user.dao.ImportJournalDao;
import com.prottasha.data.local.user.dao.LocalAccountDao;
import com.prottasha.data.local.user.entity.ImportJournalEntity;
import com.prottasha.data.local.user.entity.LocalAccountMetadataEntity;
import com.prottasha.identity.data.IdentityDao;
import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.identity.data.SuccessMemoryEntity;
import com.prottasha.habits.data.HabitDao;
import com.prottasha.habits.data.HabitDayAggregateEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.habits.data.HabitEventEntity;
import com.prottasha.habits.data.HabitSubstepEntity;
import com.prottasha.habits.data.HabitVacationEntity;
import com.prottasha.habits.data.HabitReminderEntity;
import com.prottasha.habits.data.HabitDayNoteEntity;
import com.prottasha.habits.data.HabitDayImageEntity;
import com.prottasha.guard.data.GuardRuleDao;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.data.GuardScheduleEntity;
import com.prottasha.usage.data.UsageDao;
import com.prottasha.usage.data.UsageDailyEntity;
import com.prottasha.intervention.data.InterventionDao;
import com.prottasha.intervention.data.InterventionEventEntity;
import com.prottasha.focus.data.FocusDao;
import com.prottasha.focus.data.FocusSessionEntity;
import com.prottasha.insights.data.InsightsDao;
import com.prottasha.insights.data.JournalEntryEntity;
import com.prottasha.insights.data.JournalImageEntity;
import com.prottasha.triggers.data.TriggerDao;
import com.prottasha.triggers.data.TriggerPlanEntity;

@Database(
        entities = {
                LocalAccountMetadataEntity.class,
                ImportJournalEntity.class,
                IdentityEntity.class,
                SuccessMemoryEntity.class,
                HabitEntity.class,
                HabitSubstepEntity.class,
                HabitEventEntity.class,
                HabitDayAggregateEntity.class,
                HabitVacationEntity.class,
                HabitReminderEntity.class,
                HabitDayNoteEntity.class,
                HabitDayImageEntity.class,
                GuardRuleEntity.class,
                GuardScheduleEntity.class,
                UsageDailyEntity.class,
                InterventionEventEntity.class,
                FocusSessionEntity.class,
                JournalEntryEntity.class,
                JournalImageEntity.class,
                TriggerPlanEntity.class
        },
        version = 10,
        exportSchema = true)
public abstract class UserDatabase extends RoomDatabase {
    public abstract LocalAccountDao localAccountDao();
    public abstract ImportJournalDao importJournalDao();
    public abstract IdentityDao identityDao();
    public abstract HabitDao habitDao();
    public abstract GuardRuleDao guardRuleDao();
    public abstract UsageDao usageDao();
    public abstract InterventionDao interventionDao();
    public abstract FocusDao focusDao();
    public abstract InsightsDao insightsDao();
    public abstract TriggerDao triggerPlanDao();
}
