package com.example.ideaformaayanzohar.ai;

import com.example.ideaformaayanzohar.model.Reminder;

import java.util.ArrayList;
import java.util.List;

public class AiEngine {

    /**
     * סוכן ה-AI כעוזר אישי חכם המציע תשובות מורכבות, ניתוחים וייעוץ להתנהלות
     */
    public static String processPersonalAssistantQuery(String query, List<Reminder> activeReminders) {
        if (query == null || query.trim().isEmpty()) {
            return "שלום! אני סוכן ה-AI האישי שלך. במה אוכל לעזור לך היום?";
        }

        String lower = query.toLowerCase();

        // 1. בקשת ייעוץ לסדר יום / תעדוף
        if (lower.contains("מה לעשות") || lower.contains("סדר יום") || lower.contains("תעדוף") || lower.contains("מה להתחיל")) {
            if (activeReminders == null || activeReminders.isEmpty()) {
                return "💡 הטיפ שלי להיום: אין לך משימות פתוחות כרגע! זה זמן מצוין לנוח, לשתות מים או לתכנן יעדים חדשים לשבוע הקרוב.";
            }

            List<Reminder> highPriority = new ArrayList<>();
            for (Reminder r : activeReminders) {
                if (r.getPriority() == 3 && !r.isCompleted()) {
                    highPriority.add(r);
                }
            }

            if (!highPriority.isEmpty()) {
                Reminder top = highPriority.get(0);
                return "🎯 המלצת ה-AI לסדר היום: להתחיל מיד מ-'" + top.getTitle() + "' (בדחיפות גבוהה). לאחר מכן להמשיך לשאר המשימות בלחיצה על הקטגוריה המתאימה.";
            } else {
                return "👍 סדר היום שלך מאוזן! יש לך " + activeReminders.size() + " משימות ברגיל. מומלץ לסיים קודם את הסידורים הקרובים למיקומך.";
            }
        }

        // 2. טיפים להתנהלות / בריאות / לחץ
        if (lower.contains("טיפ") || lower.contains("לחץ") || lower.contains("מים") || lower.contains("מנוחה") || lower.contains("עייף")) {
            return "🌿 טיפ מאיץ פריון מ-AI: הקפד לשתות כוס מים כל שעה, וקח 5 דקות הפסקה בין משימות כדי לשמור על ריכוז ואנרגיה גבוהה!";
        }

        // 3. יצירת תזכורת חדשה מטקסט חופשי
        Reminder r = parseFreeTextToReminder(query);
        return "✨ פענחתי עבורך תזכורת חדשה:\n• כותרת: " + r.getTitle() + "\n• קטגוריה: " + r.getCategory() + "\n• מיקום: " + (r.getLocationName().isEmpty() ? "ללא מיקום ספציפי" : r.getLocationName());
    }

    /**
     * מנוע AI לפענוח דיבור/טקסט חופשי והפיכתו לתזכורת מובנית (Intent Recognition)
     */
    public static Reminder parseFreeTextToReminder(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new Reminder("תזכורת חדשה", "", "כללי", "", 1);
        }

        String lowerText = text.toLowerCase();
        String title = text.trim();
        String category = "כללי";
        String location = "";
        int priority = 1;

        // זיהוי קטגוריות
        if (lowerText.contains("לקנות") || lowerText.contains("חלב") || lowerText.contains("סופר") || lowerText.contains("חנות") || lowerText.contains("קניות")) {
            category = "קניות";
            if (lowerText.contains("סופר")) location = "סופרמרקט";
            else if (lowerText.contains("מכולת")) location = "מכולת";
            else location = "מרכז קניות";
        } else if (lowerText.contains("עבודה") || lowerText.contains("מייל") || lowerText.contains("פגישה") || lowerText.contains("דוח") || lowerText.contains("משרד")) {
            category = "עבודה";
            location = "משרד";
            priority = 2;
        } else if (lowerText.contains("לימודים") || lowerText.contains("מבחן") || lowerText.contains("שיעור") || lowerText.contains("תרגיל") || lowerText.contains("מכללה") || lowerText.contains("בית ספר")) {
            category = "לימודים";
            location = "בית ספר / מכללה";
            priority = 2;
        } else if (lowerText.contains("בית") || lowerText.contains("לנקות") || lowerText.contains("סידור") || lowerText.contains("זבל")) {
            category = "בית";
            location = "בית";
        }

        // זיהוי דחיפות
        if (lowerText.contains("דחוף") || lowerText.contains("היום") || lowerText.contains("מיד") || lowerText.contains("חשוב")) {
            priority = 3;
        } else if (lowerText.contains("מחר") || lowerText.contains("השבוע")) {
            if (priority < 2) priority = 2;
        }

        return new Reminder(title, "נוצר אוטומטית ע\"י סוכן ה-AI האישי", category, System.currentTimeMillis(), 0.0, 0.0, location, false, priority);
    }

    /**
     * מחשב סיכום חכם של ה-AI עבור מסך הבית
     */
    public static String generateDailySummary(List<Reminder> activeReminders) {
        if (activeReminders == null || activeReminders.isEmpty()) {
            return "כל הכבוד! אין לך תזכורות פתוחות כרגע ✨";
        }

        int total = activeReminders.size();
        int highPriorityCount = 0;
        int shoppingCount = 0;

        for (Reminder r : activeReminders) {
            if (r.getPriority() == 3) highPriorityCount++;
            if ("קניות".equals(r.getCategory())) shoppingCount++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("יש לך ").append(total).append(" תזכורות אקטיביות.");

        if (highPriorityCount > 0) {
            sb.append(" ⚠️ ").append(highPriorityCount).append(" בדחיפות גבוהה!");
        }
        if (shoppingCount > 0) {
            sb.append(" 🛒 ").append(shoppingCount).append(" קניות קרובות.");
        }

        return sb.toString();
    }
}
