package com.example.offermanagementsystem.model;

public enum OfferStatus {
    NOVA,        // nově vytvořená nabídka
    ODESLANA,    // odesláno zákazníkovi (email / link)
    K_UPRAVE,    // vráceno zákazníkem k úpravě
    PRIJATA,    // přijato zákazníkem
    ZAMITNUTA   // zamítnuto zákazníkem
}