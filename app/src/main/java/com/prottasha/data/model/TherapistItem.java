package com.prottasha.data.model;

public class TherapistItem {
    private String name;
    private String specialization;
    private String phone;
    private String hours;
    private String website;

    public TherapistItem(String name, String specialization, String phone, String hours, String website) {
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;
        this.hours = hours;
        this.website = website;
    }

    public String getName() { return name; }
    public String getSpecialization() { return specialization; }
    public String getPhone() { return phone; }
    public String getHours() { return hours; }
    public String getWebsite() { return website; }
}
