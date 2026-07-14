package edu.cit.mabini.meditrack.reports;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(reportsService.getSummary());
    }
}