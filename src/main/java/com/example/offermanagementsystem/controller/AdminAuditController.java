package com.example.offermanagementsystem.controller;

import com.example.offermanagementsystem.model.AuditAction;
import com.example.offermanagementsystem.model.AuditLog;
import com.example.offermanagementsystem.model.AuditSection;
import com.example.offermanagementsystem.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin/audit")
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // =====================================================
    // 🌍 GLOBÁLNÍ AUDIT
    // =====================================================
    @GetMapping
    public String globalAudit(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) AuditSection section,
            @RequestParam(required = false) AuditAction action,
            Model model
    ) {

        Pageable pageable = PageRequest.of(page, 30);
        Page<AuditLog> logs;

        if (section != null && action != null) {
            logs = auditLogRepository
                    .findBySectionAndActionOrderByPerformedAtDesc(
                            section, action, pageable);
        }
        else if (section != null) {
            logs = auditLogRepository
                    .findBySectionOrderByPerformedAtDesc(section, pageable);
        }
        else if (action != null) {
            logs = auditLogRepository
                    .findByActionOrderByPerformedAtDesc(action, pageable);
        }
        else {
            logs = auditLogRepository
                    .findAllByOrderByPerformedAtDesc(pageable);
        }

        model.addAttribute("logs", logs);
        model.addAttribute("selectedSection", section);
        model.addAttribute("selectedAction", action);
        model.addAttribute("sections", AuditSection.values());
        model.addAttribute("actions", AuditAction.values());

        return "admin/audit-global";
    }

    // =====================================================
    // 📄 CSV EXPORT (RESPEKTUJE FILTRY)
    // =====================================================
    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(required = false) AuditSection section,
            @RequestParam(required = false) AuditAction action,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=audit-export.csv"
        );

        // Excel UTF-8 fix
        response.getOutputStream().write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});

        Pageable bigPage = PageRequest.of(0, 10000);
        List<AuditLog> logs;

        if (section != null && action != null) {
            logs = auditLogRepository
                    .findBySectionAndActionOrderByPerformedAtDesc(
                            section, action, bigPage)
                    .getContent();
        }
        else if (section != null) {
            logs = auditLogRepository
                    .findBySectionOrderByPerformedAtDesc(
                            section, bigPage)
                    .getContent();
        }
        else if (action != null) {
            logs = auditLogRepository
                    .findByActionOrderByPerformedAtDesc(
                            action, bigPage)
                    .getContent();
        }
        else {
            logs = auditLogRepository
                    .findAllByOrderByPerformedAtDesc(bigPage)
                    .getContent();
        }

        PrintWriter writer = response.getWriter();

        writer.println("ID,Time,Section,Action,OfferId,User,IP,OldValue,NewValue,Detail");

        for (AuditLog log : logs) {

            String offerId = log.getOffer() != null
                    ? String.valueOf(log.getOffer().getId())
                    : "";

            String username = log.getPerformedBy() != null
                    ? log.getPerformedBy().getUsername()
                    : "SYSTEM";

            String ip = log.getIpAddress() != null
                    ? log.getIpAddress()
                    : "";

            String oldValue = sanitize(log.getOldValue());
            String newValue = sanitize(log.getNewValue());
            String detail = sanitize(log.getDetail());

            writer.printf(
                    "%d,%s,%s,%s,%s,%s,%s,\"%s\",\"%s\",\"%s\"%n",
                    log.getId(),
                    log.getPerformedAt(),
                    log.getSection(),
                    log.getAction(),
                    offerId,
                    username,
                    ip,
                    oldValue,
                    newValue,
                    detail
            );
        }

        writer.flush();
    }

    // =====================================================
    // HELPER – CSV ESCAPE
    // =====================================================
    private String sanitize(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}