package edu.cit.mabini.meditrack.reports;

import edu.cit.mabini.meditrack.reports.DashboardSummaryDto;
import edu.cit.mabini.meditrack.reports.ReportsService;
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
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(reportsService.getSummary());
    }
}