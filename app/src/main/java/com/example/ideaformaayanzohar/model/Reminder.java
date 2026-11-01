package com.example.ideaformaayanzohar.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;
    private String category; // קניות, עבודה, לימודים, אישי, בית
    private long dateTime; // timestamp in millis
    private double latitude;
    private double longitude;
    private String locationName; // למשל: "סופרמרקט", "בית", "מכללה"
    private boolean isCompleted;
    private int priority; // 1: נמוכה, 2: בינונית, 3: גבוהה

    public Reminder(String title, String content, String category, long dateTime,
                    double latitude, double longitude, String locationName,
                    boolean isCompleted, int priority) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.dateTime = dateTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.isCompleted = isCompleted;
        this.priority = priority;
    }

    @Ignore
    public Reminder(String title, String content, String category, String locationName, int priority) {
        this(title, content, category, System.currentTimeMillis(), 0.0, 0.0, locationName, false, priority);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
