package com.example.offermanagementsystem.util;

public class AuditActionLabel {

    public static String label(String action) {
        return switch (action) {
            case "EMAIL_SENT" -> "📧 Email odeslán";
            case "EMAIL_OPENED", "VIEW" -> "👁️ Email otevřen";
            case "REMINDER_7" -> "🕒 Připomenutí po 7 dnech";
            case "REMINDER_14" -> "⏰ Připomenutí po 14 dnech";
            case "ACCEPT" -> "✅ Nabídka přijata";
            case "REJECT" -> "❌ Nabídka zamítnuta";
            case "EXPIRED" -> "⛔ Nabídka expirovala";
            default -> "ℹ️ " + action;
        };
    }
}
