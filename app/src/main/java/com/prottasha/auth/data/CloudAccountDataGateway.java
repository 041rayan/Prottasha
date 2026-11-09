package com.prottasha.auth.data;

import com.prottasha.core.result.ResultCallback;

public interface CloudAccountDataGateway {
    void deleteOwnedData(String uid, ResultCallback<Boolean> callback);
}
