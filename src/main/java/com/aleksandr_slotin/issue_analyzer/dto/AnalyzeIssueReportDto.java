package com.aleksandr_slotin.issue_analyzer.dto;

import java.time.Instant;

public record AnalyzeIssueReportDto(
        String category,
        String summary,
        SeverityEnum severity,
        Instant createdAt
) {
}
