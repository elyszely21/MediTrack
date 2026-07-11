package edu.cit.mabini.meditrack.controller;

import edu.cit.mabini.meditrack.dto.DashboardSummaryDto;
import edu.cit.mabini.meditrack.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(reportsService.getSummary());
    }
}