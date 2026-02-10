package com.example.offermanagementsystem.service;

import com.example.offermanagementsystem.model.AuditAction;
import org.springframework.stereotype.Service;

@Service
public class AuditActionLabelService {

    public String labelFor(AuditAction action) {
        return switch (action) {
            case EMAIL_SENT      -> "📧 Email odeslán";
            case EMAIL_OPENED    -> "👁 Email otevřen";
            case VIEW            -> "👁 Zobrazení nabídky";
            case REMINDER_7      -> "⏰ Připomenutí po 7 dnech";
            case REMINDER_14     -> "⏰ Připomenutí po 14 dnech";
            case ACCEPT          -> "✅ Nabídka přijata";
            case REJECT          -> "❌ Nabídka zamítnuta";
            case EXPIRED         -> "⌛ Nabídka expirovala";
            case PDF_EXPORTED    -> "📄 Export PDF";
            case OFFER_CREATED   -> "➕ Nabídka vytvořena";
            case OFFER_UPDATED   -> "✏️ Nabídka upravena";
            case OFFER_ARCHIVED  -> "📦 Nabídka archivována";
            default              -> action.name();
        };
    }
}