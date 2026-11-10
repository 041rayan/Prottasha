package com.prottasha.settings.privacy;

import com.prottasha.core.security.EncryptedPrivateFileStore;
import com.prottasha.core.security.UserDatabaseIdentity;
import com.prottasha.data.local.user.UserSessionDatabaseProvider;

public final class LocalDeletionService {
    private final UserSessionDatabaseProvider databases;
    private final EncryptedPrivateFileStore files;

    public LocalDeletionService(
            UserSessionDatabaseProvider databases, EncryptedPrivateFileStore files) {
        this.databases = databases;
        this.files = files;
    }

    public void delete(String uidHash) throws Exception {
        databases.destroyByUidHash(uidHash);
        files.destroy(UserDatabaseIdentity.fromUidHash(uidHash));
    }
}
