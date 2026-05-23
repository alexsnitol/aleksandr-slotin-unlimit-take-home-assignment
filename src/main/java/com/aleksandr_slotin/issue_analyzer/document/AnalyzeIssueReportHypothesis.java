package com.aleksandr_slotin.issue_analyzer.document;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AnalyzeIssueReportHypothesis(
        String title,
        String reasoning,
        @JsonProperty("next_steps") List<String> nextSteps
) {
}
