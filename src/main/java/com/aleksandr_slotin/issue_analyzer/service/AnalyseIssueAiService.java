package com.aleksandr_slotin.issue_analyzer.service;

import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueHypothesis;
import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueReportDto;
import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueResponse;
import com.aleksandr_slotin.issue_analyzer.exception.AiException;
import com.aleksandr_slotin.issue_analyzer.exception.AnalyseIssuePreparationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyseIssueAiService {

    public static final String SERVICES_CONTEXT = "services_context";
    public static final String ISSUE_HISTORY_SYS_KEY = "issue_history";
    private static final int MAX_RETRIES = 3;

    private final ChatClient analyzeIssueChatClient;

    @Value("classpath:/prompts/analyze-issue-sys.md")
    private Resource analyzeIssueSystemPrompt;
    @Value("classpath:/prompts/repair-analyze-issue-response.md")
    private Resource repairAnalyzeIssueSystemPrompt;
    @Value("classpath:/prompts/services-context.txt")
    private Resource servicesContextResource;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);


    public AnalyzeIssueResponse analyzeIssue(String issueText, List<AnalyzeIssueReportDto> issueHistory) {
        String servicesContext;
        try {
            servicesContext = servicesContextResource.getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Failed to read services context", e);
            throw new AnalyseIssuePreparationException("Failed to read services context", e);
        }

        String issueHistoryStr;
        try {
            issueHistoryStr = objectMapper.writeValueAsString(issueHistory);
        } catch (Exception e) {
            log.error("Failed to serialize issue history", e);
            throw new AnalyseIssuePreparationException("Failed to serialize issue history", e);
        }

        return analyzeWithRepair(issueText, issueHistoryStr, servicesContext, 0, null);
    }

    private AnalyzeIssueResponse analyzeWithRepair(
            String issueText,
            String issueHistoryStr,
            String servicesContext,
            int attempt,
            AiException previousException
    ) {
        try {
            return attempt == 0
                    ? call(issueText, issueHistoryStr, servicesContext)
                    : repair(issueText, issueHistoryStr, servicesContext, previousException);
        } catch (AiException e) {
            int currentAttempt = attempt + 1;
            log.warn("AI response failed validation on attempt {}/{}: {}", currentAttempt, MAX_RETRIES, e.getMessage());

            if (currentAttempt >= MAX_RETRIES) {
                throw e;
            }

            return analyzeWithRepair(issueText, issueHistoryStr, servicesContext, currentAttempt, e);
        }
    }

    public AnalyzeIssueResponse call(String issueText, String issueHistoryStr, String servicesContext) {
        String rsRaw = analyzeIssueChatClient.prompt()
                .system(s -> s.text(analyzeIssueSystemPrompt)
                        .param(ISSUE_HISTORY_SYS_KEY, issueHistoryStr)
                        .param(SERVICES_CONTEXT, servicesContext)
                )
                .user(issueText)
                .call()
                .content();

        return parseAndValidate(rsRaw);
    }

    public AnalyzeIssueResponse repair(String issueText, String issueHistoryStr, String servicesContext, AiException previousException) {
        String rsRaw = analyzeIssueChatClient.prompt()
                .system(s -> s.text(repairAnalyzeIssueSystemPrompt)
                        .param("error", Objects.toString(previousException.getMessage(), "Unknown AI error"))
                        .param("raw_json", Objects.toString(previousException.getRawOutput(), ""))
                        .param(ISSUE_HISTORY_SYS_KEY, issueHistoryStr)
                        .param(SERVICES_CONTEXT, servicesContext)
                )
                .user(issueText)
                .call()
                .content();

        return parseAndValidate(rsRaw);
    }

    private AnalyzeIssueResponse parseAndValidate(String rsRaw) {
        AnalyzeIssueResponse rs = deserializeResponse(rsRaw);
        validateRequiredFields(rs, rsRaw);
        trimHypotheses(rs);
        validateHypotheses(rs.getHypotheses(), rsRaw);
        return rs;
    }

    private AnalyzeIssueResponse deserializeResponse(String rsRaw) {
        try {
            if (rsRaw.startsWith("```json")) {
                rsRaw = rsRaw.substring(7, rsRaw.length() - 3).trim();
            }
            if (rsRaw.startsWith("```")) {
                rsRaw = rsRaw.substring(3).trim();
            }
            if (rsRaw.endsWith("```")) {
                rsRaw = rsRaw.substring(0, rsRaw.length() - 3).trim();
            }
            return objectMapper.readValue(rsRaw, AnalyzeIssueResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", rsRaw, e);
            throw new AiException("Failed to parse AI response", rsRaw, e);
        }
    }

    private void validateRequiredFields(AnalyzeIssueResponse rs, String rsRaw) {
        if (rs == null) {
            throw new AiException("AI response is null", rsRaw);
        }

        requireText(rs.getCategory(), "Category is blank", rsRaw);
        requireText(rs.getSummary(), "Summary is blank", rsRaw);

        if (rs.getSeverity() == null) {
            throw new AiException("Severity is missing", rsRaw);
        }

        if (rs.getHypotheses() == null || rs.getHypotheses().isEmpty()) {
            throw new AiException("No hypotheses generated", rsRaw);
        }
    }

    private void trimHypotheses(AnalyzeIssueResponse rs) {
        if (rs.getHypotheses().size() > 3) {
            rs.setHypotheses(rs.getHypotheses().subList(0, 3));
        }
    }

    private void validateHypotheses(List<AnalyzeIssueHypothesis> hypotheses, String rsRaw) {
        for (AnalyzeIssueHypothesis hypothesis : hypotheses) {
            if (hypothesis == null) {
                throw new AiException("Hypothesis is missing", rsRaw);
            }

            requireText(hypothesis.title(), "Hypothesis title is blank", rsRaw);
            requireText(hypothesis.reasoning(), "Hypothesis reasoning is blank", rsRaw);

            if (hypothesis.nextSteps() == null || hypothesis.nextSteps().isEmpty()) {
                throw new AiException("Hypothesis next steps are missing", rsRaw);
            }

            for (String step : hypothesis.nextSteps()) {
                requireText(step, "Hypothesis next step is blank", rsRaw);
            }
        }
    }

    private void requireText(String value, String message, String rsRaw) {
        if (value == null || value.isBlank()) {
            throw new AiException(message, rsRaw);
        }
    }

}
