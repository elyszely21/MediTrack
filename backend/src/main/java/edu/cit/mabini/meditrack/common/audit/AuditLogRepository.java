package edu.cit.mabini.meditrack.common.audit;

import edu.cit.mabini.meditrack.common.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop20ByOrderByPerformedAtDesc();
}