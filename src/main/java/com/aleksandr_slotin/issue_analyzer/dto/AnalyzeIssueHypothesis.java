package com.aleksandr_slotin.issue_analyzer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Potential explanation for the issue.")
public record AnalyzeIssueHypothesis(
        @Schema(description = "Hypothesis title.")
        String title,
        @Schema(description = "Reasoning behind the hypothesis.")
        String reasoning,
        @JsonProperty("next_steps")
        @Schema(description = "Actionable next steps to validate the hypothesis.")
        List<String> nextSteps
) {
}
