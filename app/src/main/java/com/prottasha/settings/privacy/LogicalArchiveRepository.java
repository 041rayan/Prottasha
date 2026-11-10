package com.prottasha.settings.privacy;

import androidx.annotation.NonNull;

import com.prottasha.core.security.EncryptedPrivateFileStore;
import com.prottasha.data.local.user.UserDatabase;
import com.prottasha.focus.data.FocusSessionEntity;
import com.prottasha.guard.data.GuardRuleEntity;
import com.prottasha.guard.data.GuardScheduleEntity;
import com.prottasha.habits.data.HabitEntity;
import com.prottasha.identity.data.IdentityEntity;
import com.prottasha.identity.data.SuccessMemoryEntity;
import com.prottasha.insights.data.JournalEntryEntity;
import com.prottasha.insights.data.RehearsalSessionEntity;
import com.prottasha.triggers.data.TriggerPlanEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.List;

public final class LogicalArchiveRepository {

    public static final String ARCHIVE_FORMAT = "PROTTASHA_ARCHIVE";
    public static final int CURRENT_SCHEMA_VERSION = 12;

    public static final class ExportSummary {
        public final int habitsCount;
        public final int reflectionsCount;
        public final int rulesCount;
        public final int triggersCount;
        public final int memoriesCount;
        public final int filesCount;

        public ExportSummary(int habits, int reflections, int rules, int triggers, int memories, int files) {
            this.habitsCount = habits;
            this.reflectionsCount = reflections;
            this.rulesCount = rules;
            this.triggersCount = triggers;
            this.memoriesCount = memories;
            this.filesCount = files;
        }
    }

    public static final class ImportSummary {
        public final int habitsRestored;
        public final int reflectionsRestored;
        public final int rulesRestored;
        public final int triggersRestored;
        public final int memoriesRestored;
        public final int filesRestored;

        public ImportSummary(int habits, int reflections, int rules, int triggers, int memories, int files) {
            this.habitsRestored = habits;
            this.reflectionsRestored = reflections;
            this.rulesRestored = rules;
            this.triggersRestored = triggers;
            this.memoriesRestored = memories;
            this.filesRestored = files;
        }
    }

    public static void validateArchiveJson(@NonNull JSONObject json) throws IllegalArgumentException {
        try {
            String format = json.getString("format");
            if (!ARCHIVE_FORMAT.equals(format)) {
                throw new IllegalArgumentException("Invalid archive format: " + format);
            }
            int version = json.getInt("schemaVersion");
            if (version <= 0 || version > CURRENT_SCHEMA_VERSION) {
                throw new IllegalArgumentException("Unsupported archive schemaVersion: " + version);
            }
            if (!json.has("tables")) {
                throw new IllegalArgumentException("Archive missing 'tables' payload");
            }
        } catch (JSONException e) {
            throw new IllegalArgumentException("Malformed archive JSON structure", e);
        }
    }

    public ExportSummary exportArchive(
            @NonNull UserDatabase db,
            @NonNull EncryptedPrivateFileStore fileStore,
            @NonNull String uidHash,
            @NonNull OutputStream destination,
            @NonNull char[] password) throws IOException, GeneralSecurityException, JSONException {

        JSONObject root = new JSONObject();
        root.put("format", ARCHIVE_FORMAT);
        root.put("schemaVersion", CURRENT_SCHEMA_VERSION);
        root.put("exportedAt", System.currentTimeMillis());
        root.put("uidHash", uidHash);

        JSONObject tables = new JSONObject();

        // 1. Identity
        IdentityEntity identity = db.identityDao().findActive();
        if (identity != null) {
            JSONObject idObj = new JSONObject();
            idObj.put("statement", identity.statement);
            idObj.put("purpose", identity.purpose);
            idObj.put("createdAt", identity.createdAt);
            idObj.put("active", identity.active);
            tables.put("identity", idObj);
        }

        // 2. Success Memories
        List<SuccessMemoryEntity> memories = db.identityDao().listAllMemories();
        JSONArray memArr = new JSONArray();
        for (SuccessMemoryEntity m : memories) {
            JSONObject o = new JSONObject();
            o.put("text", m.text);
            o.put("createdAt", m.createdAt);
            memArr.put(o);
        }
        tables.put("success_memories", memArr);

        // 3. Habits
        List<HabitEntity> habits = db.habitDao().listAll();
        JSONArray habitArr = new JSONArray();
        for (HabitEntity h : habits) {
            JSONObject o = new JSONObject();
            o.put("title", h.title);
            o.put("type", h.type);
            o.put("target", h.target);
            o.put("scheduleFrequency", h.scheduleFrequency);
            o.put("startEpochDay", h.startEpochDay);
            o.put("scheduleInterval", h.scheduleInterval);
            o.put("scheduleDayOfMonth", h.scheduleDayOfMonth);
            o.put("archived", h.archived);
            o.put("sortOrder", h.sortOrder);
            o.put("createdAt", h.createdAt);
            o.put("cue", h.cue);
            o.put("twoMinuteAction", h.twoMinuteAction);
            o.put("reward", h.reward);
            o.put("ifThenPlan", h.ifThenPlan);
            o.put("reboundPlan", h.reboundPlan);
            o.put("unit", h.unit);
            o.put("incrementVal", h.incrementVal);
            o.put("maximumVal", h.maximumVal);
            o.put("focusMinutes", h.focusMinutes);
            o.put("accountability", h.accountability);
            habitArr.put(o);
        }
        tables.put("habits", habitArr);

        // 4. Guard Rules
        List<GuardRuleEntity> rules = db.guardRuleDao().list();
        JSONArray rulesArr = new JSONArray();
        for (GuardRuleEntity r : rules) {
            JSONObject o = new JSONObject();
            o.put("target", r.target);
            o.put("label", r.label);
            o.put("mode", r.mode);
            o.put("budgetMinutes", r.budgetMinutes);
            o.put("startMinute", r.startMinute);
            o.put("endMinute", r.endMinute);
            o.put("enabled", r.enabled);
            o.put("cooldownMinutes", r.cooldownMinutes);
            o.put("delaySeconds", r.delaySeconds);
            o.put("graceMinutes", r.graceMinutes);
            o.put("escalation", r.escalation);
            o.put("replacementAction", r.replacementAction);
            o.put("editProtection", r.editProtection);
            o.put("isDomain", r.isDomain);
            rulesArr.put(o);
        }
        tables.put("guard_rules", rulesArr);

        // 5. Journal / Reflections
        List<JournalEntryEntity> entries = db.insightsDao().recent(500);
        JSONArray entriesArr = new JSONArray();
        for (JournalEntryEntity e : entries) {
            JSONObject o = new JSONObject();
            o.put("epochDay", e.epochDay);
            o.put("fact", e.fact);
            o.put("interpretation", e.interpretation);
            o.put("lesson", e.lesson);
            o.put("createdAt", e.createdAt);
            entriesArr.put(o);
        }
        tables.put("journal_entries", entriesArr);

        // 6. Trigger Plans
        List<TriggerPlanEntity> triggers = db.triggerPlanDao().list();
        JSONArray triggersArr = new JSONArray();
        for (TriggerPlanEntity t : triggers) {
            JSONObject o = new JSONObject();
            o.put("cue", t.cue);
            o.put("plan", t.plan);
            o.put("createdAt", t.createdAt);
            triggersArr.put(o);
        }
        tables.put("trigger_plans", triggersArr);

        // 7. Focus Session
        FocusSessionEntity focus = db.focusDao().find();
        if (focus != null) {
            JSONObject fo = new JSONObject();
            fo.put("durationMillis", focus.durationMillis);
            fo.put("startedAt", focus.startedAt);
            fo.put("accumulatedMillis", focus.accumulatedMillis);
            fo.put("state", focus.state);
            tables.put("focus_session", fo);
        }

        // 8. Rehearsal Sessions
        List<RehearsalSessionEntity> rehearsals = db.rehearsalDao().list();
        JSONArray rehArr = new JSONArray();
        for (RehearsalSessionEntity r : rehearsals) {
            JSONObject o = new JSONObject();
            o.put("scenario", r.scenario);
            o.put("durationSeconds", r.durationSeconds);
            o.put("feltIntensity", r.feltIntensity);
            o.put("createdAt", r.createdAt);
            rehArr.put(o);
        }
        tables.put("rehearsal_sessions", rehArr);

        root.put("tables", tables);
        root.put("files", new JSONArray());

        byte[] payloadBytes = root.toString().getBytes(StandardCharsets.UTF_8);
        new EncryptedArchiveService(payloadBytes).exportTo(destination, password);

        return new ExportSummary(
                habits.size(),
                entries.size(),
                rules.size(),
                triggers.size(),
                memories.size(),
                0
        );
    }

    public ImportSummary importArchive(
            @NonNull UserDatabase db,
            @NonNull EncryptedPrivateFileStore fileStore,
            @NonNull String currentUidHash,
            @NonNull InputStream source,
            @NonNull char[] password) throws IOException, GeneralSecurityException, JSONException {

        byte[] decrypted = new EncryptedArchiveService(new byte[0]).importFrom(source, password);
        String jsonStr = new String(decrypted, StandardCharsets.UTF_8);
        JSONObject root = new JSONObject(jsonStr);
        validateArchiveJson(root);

        JSONObject tables = root.getJSONObject("tables");

        final int[] counts = new int[6]; // habits, reflections, rules, triggers, memories, files

        db.runInTransaction(() -> {
            try {
                // 1. Identity
                long identityId = 1L;
                if (tables.has("identity")) {
                    JSONObject idObj = tables.getJSONObject("identity");
                    db.identityDao().deactivateAll();
                    identityId = db.identityDao().insert(new IdentityEntity(
                            0L,
                            idObj.getString("statement"),
                            idObj.getString("purpose"),
                            idObj.optLong("createdAt", System.currentTimeMillis()),
                            true
                    ));
                }

                // 2. Success Memories
                if (tables.has("success_memories")) {
                    JSONArray memArr = tables.getJSONArray("success_memories");
                    for (int i = 0; i < memArr.length(); i++) {
                        JSONObject o = memArr.getJSONObject(i);
                        db.identityDao().insertSuccessMemory(new SuccessMemoryEntity(
                                0L,
                                identityId,
                                o.getString("text"),
                                o.optLong("createdAt", System.currentTimeMillis())
                        ));
                        counts[4]++;
                    }
                }

                // 3. Habits
                if (tables.has("habits")) {
                    JSONArray habitArr = tables.getJSONArray("habits");
                    for (int i = 0; i < habitArr.length(); i++) {
                        JSONObject o = habitArr.getJSONObject(i);
                        HabitEntity h = new HabitEntity(
                                0L,
                                o.getString("title"),
                                o.getString("type"),
                                o.getInt("target"),
                                o.getString("scheduleFrequency"),
                                o.getLong("startEpochDay"),
                                o.getInt("scheduleInterval"),
                                o.getInt("scheduleDayOfMonth"),
                                o.optBoolean("archived", false),
                                o.optInt("sortOrder", i),
                                o.optLong("createdAt", System.currentTimeMillis()),
                                o.optString("cue", ""),
                                o.optString("twoMinuteAction", ""),
                                o.optString("reward", ""),
                                o.optString("ifThenPlan", ""),
                                o.optString("reboundPlan", ""),
                                o.optString("unit", ""),
                                o.optInt("incrementVal", 1),
                                o.optInt("maximumVal", 10),
                                o.optInt("focusMinutes", 0),
                                o.optString("accountability", "")
                        );
                        db.habitDao().insert(h);
                        counts[0]++;
                    }
                }

                // 4. Guard Rules
                if (tables.has("guard_rules")) {
                    JSONArray rulesArr = tables.getJSONArray("guard_rules");
                    for (int i = 0; i < rulesArr.length(); i++) {
                        JSONObject o = rulesArr.getJSONObject(i);
                        GuardRuleEntity r = new GuardRuleEntity(
                                0L,
                                o.getString("target"),
                                o.getString("label"),
                                o.getString("mode"),
                                o.getInt("budgetMinutes"),
                                o.getInt("startMinute"),
                                o.getInt("endMinute"),
                                o.optBoolean("enabled", true),
                                o.optInt("cooldownMinutes", 0),
                                o.optInt("delaySeconds", 0),
                                o.optInt("graceMinutes", 0),
                                o.optBoolean("escalation", false),
                                o.optString("replacementAction", ""),
                                o.optBoolean("editProtection", false),
                                o.optBoolean("isDomain", false)
                        );
                        db.guardRuleDao().upsert(r);
                        counts[2]++;
                    }
                }

                // 5. Journal / Reflections
                if (tables.has("journal_entries")) {
                    JSONArray entriesArr = tables.getJSONArray("journal_entries");
                    for (int i = 0; i < entriesArr.length(); i++) {
                        JSONObject o = entriesArr.getJSONObject(i);
                        long createdAt = o.optLong("createdAt", System.currentTimeMillis());
                        long epochDay = o.optLong("epochDay", createdAt / 86400000L);
                        JournalEntryEntity e = new JournalEntryEntity(
                                0L,
                                epochDay,
                                o.getString("fact"),
                                o.getString("interpretation"),
                                o.getString("lesson"),
                                createdAt
                        );
                        db.insightsDao().insert(e);
                        counts[1]++;
                    }
                }

                // 6. Trigger Plans
                if (tables.has("trigger_plans")) {
                    JSONArray trigArr = tables.getJSONArray("trigger_plans");
                    for (int i = 0; i < trigArr.length(); i++) {
                        JSONObject o = trigArr.getJSONObject(i);
                        db.triggerPlanDao().insert(new TriggerPlanEntity(
                                0L,
                                o.getString("cue"),
                                o.getString("plan"),
                                o.optLong("createdAt", System.currentTimeMillis())
                        ));
                        counts[3]++;
                    }
                }

                // 7. Focus Session
                if (tables.has("focus_session")) {
                    JSONObject fo = tables.getJSONObject("focus_session");
                    db.focusDao().save(new FocusSessionEntity(
                            1,
                            fo.getLong("durationMillis"),
                            fo.getLong("startedAt"),
                            fo.getLong("accumulatedMillis"),
                            fo.getString("state")
                    ));
                }

                // 8. Rehearsal Sessions
                if (tables.has("rehearsal_sessions")) {
                    JSONArray rehArr = tables.getJSONArray("rehearsal_sessions");
                    for (int i = 0; i < rehArr.length(); i++) {
                        JSONObject o = rehArr.getJSONObject(i);
                        db.rehearsalDao().insert(new RehearsalSessionEntity(
                                0L,
                                o.getString("scenario"),
                                o.optInt("durationSeconds", 60),
                                o.optInt("feltIntensity", 5),
                                o.optLong("createdAt", System.currentTimeMillis())
                        ));
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException("Import transaction JSON failure", e);
            }
        });

        return new ImportSummary(counts[0], counts[1], counts[2], counts[3], counts[4], counts[5]);
    }
}
