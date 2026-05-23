package com.aleksandr_slotin.issue_analyzer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;


@Data
@ToString
@Schema(description = "Analysis result for an issue report.")
public class AnalyzeIssueResponse {

    @Schema(description = "Issue category.")
    private String category;
    @Schema(description = "Short summary of the issue.")
    private String summary;
    @Schema(description = "Severity level.", allowableValues = {"low", "medium", "high"})
    private SeverityEnum severity;
    @Schema(description = "List of hypotheses.")
    private List<AnalyzeIssueHypothesis> hypotheses;

}
