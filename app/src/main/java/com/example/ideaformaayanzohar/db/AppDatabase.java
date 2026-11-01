package com.example.ideaformaayanzohar.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.ideaformaayanzohar.model.Reminder;

@Database(entities = {Reminder.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "geo_remind_db";
    private static AppDatabase instance;

    public abstract ReminderDao reminderDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME)
                    .allowMainThreadQueries() // למטרות פרויקט ופשטות
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
