package edu.cit.mabini.meditrack.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepo;

    public void log(String action, String entity, String entityId, String details) {
        String performedBy = "system";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                performedBy = auth.getName();
            }
        } catch (Exception ignored) {
        }

        AuditLog log = AuditLog.builder()
                .action(action)
                .entity(entity)
                .entityId(entityId)
                .performedBy(performedBy)
                .details(details)
                .build();

        auditLogRepo.save(log);
    }

    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepo.findTop20ByOrderByPerformedAtDesc();
    }
}
