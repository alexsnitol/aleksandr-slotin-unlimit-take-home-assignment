package com.aleksandr_slotin.issue_analyzer.document;

import com.aleksandr_slotin.issue_analyzer.dto.SeverityEnum;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Document(collection = "analyze_issue_reports")
public class AnalyzeIssueReport {

    @Id
    private UUID id = UUID.randomUUID();
    private String category;
    private String summary;
    private SeverityEnum severity;
    private List<AnalyzeIssueReportHypothesis> hypotheses;
    private Instant createdAt = Instant.now();

}
