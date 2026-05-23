package com.aleksandr_slotin.issue_analyzer.controller;

import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueResponse;
import com.aleksandr_slotin.issue_analyzer.service.AnalyzerIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Issue Analysis", description = "Analyze incoming issue text and return a structured summary.")
public class AnalyzeIssueController {

    private final AnalyzerIssueService analyzeIssueService;


    @Operation(
            summary = "Analyze issue text",
            description = "Accepts plain text and returns a structured analysis result."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Analysis result.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AnalyzeIssueResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input.")
    })
    @PostMapping(
            path = "/analyze-issue",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AnalyzeIssueResponse> analyzeIssue(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Plain text issue description.",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(type = "string")
                    )
            )
            @RequestBody @NotBlank String issueText
    ) {
        return ResponseEntity.ok(
                analyzeIssueService.analyzeIssue(issueText)
        );
    }

}
