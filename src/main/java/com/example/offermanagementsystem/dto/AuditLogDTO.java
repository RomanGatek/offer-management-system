package com.example.offermanagementsystem.dto;

import java.time.LocalDateTime;

public class AuditLogDTO {

    private final Long id;
    private final String action;
    private final String section;
    private final String performedBy;
    private final String detail;
    private final String oldValue;
    private final String newValue;
    private final LocalDateTime performedAt;

    public AuditLogDTO(
            Long id,
            String action,
            String section,
            String performedBy,
            String detail,
            String oldValue,
            String newValue,
            LocalDateTime performedAt
    ) {
        this.id = id;
        this.action = action;
        this.section = section;
        this.performedBy = performedBy;
        this.detail = detail;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.performedAt = performedAt;
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getSection() { return section; }
    public String getPerformedBy() { return performedBy; }
    public String getDetail() { return detail; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public LocalDateTime getPerformedAt() { return performedAt; }
}