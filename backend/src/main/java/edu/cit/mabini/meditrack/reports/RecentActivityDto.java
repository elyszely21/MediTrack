package edu.cit.mabini.meditrack.reports;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RecentActivityDto {
    private Long id;
    private String action;
    private String entity;
    private String entityId;
    private String performedBy;
    private String details;
    private LocalDateTime performedAt;
}