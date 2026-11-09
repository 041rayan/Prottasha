package com.prottasha.core.security;

public final class StoredPrivateImage {

    private final String storageId;
    private final int width;
    private final int height;
    private final int plaintextBytes;

    StoredPrivateImage(String storageId, int width, int height, int plaintextBytes) {
        this.storageId = storageId;
        this.width = width;
        this.height = height;
        this.plaintextBytes = plaintextBytes;
    }

    public String storageId() {
        return storageId;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int plaintextBytes() {
        return plaintextBytes;
    }

    public String mimeType() {
        return "image/jpeg";
    }
}
