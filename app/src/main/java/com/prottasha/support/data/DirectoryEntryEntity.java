package com.prottasha.support.data;

public final class DirectoryEntryEntity {
    public final String name;
    public final String phone;
    public final String hours;
    public final String source;
    public final String verifiedDate;
    public final String status;

    public DirectoryEntryEntity(String name, String phone, String hours,
            String source, String verifiedDate, String status) {
        this.name = name;
        this.phone = phone;
        this.hours = hours;
        this.source = source;
        this.verifiedDate = verifiedDate;
        this.status = status;
    }
}
