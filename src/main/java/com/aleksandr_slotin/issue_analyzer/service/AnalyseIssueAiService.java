package com.aleksandr_slotin.issue_analyzer.service;

import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueReportDto;
import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueResponse;
import com.aleksandr_slotin.issue_analyzer.exception.AiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.Resource;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import tools.jackson.core.exc.StreamReadException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyseIssueAiService {

    public static final String SERVICES_CONTEXT = "services_context";
    public static final String ISSUE_HISTORY_SYS_KEY = "issue_history";

    private final ChatClient analyzeIssueChatClient;
    @Lazy
    @Autowired
    private AnalyseIssueAiService self;

    @Value("classpath:/prompts/analyze-issue-sys.md")
    private Resource analyzeIssueSystemPrompt;
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
            throw new RuntimeException("Failed to read services context", e);
        }

        String issueHistoryStr;
        try {
            issueHistoryStr = objectMapper.writeValueAsString(issueHistory);
        } catch (Exception e) {
            log.error("Failed to serialize issue history", e);
            throw new RuntimeException("Failed to serialize issue history", e);
        }

        AnalyzeIssueResponse rs = self.callWithRetry(issueText, issueHistoryStr, servicesContext);

        Objects.requireNonNull(rs);
        return rs;
    }

    @Retryable(value = {StreamReadException.class, AiException.class})
    public AnalyzeIssueResponse callWithRetry(String issueText, String issueHistoryStr, String servicesContext) {
        AnalyzeIssueResponse rs = analyzeIssueChatClient.prompt()
                .system(s -> s.text(analyzeIssueSystemPrompt)
                        .param(ISSUE_HISTORY_SYS_KEY, issueHistoryStr)
                        .param(SERVICES_CONTEXT, servicesContext)
                )
                .user(issueText)
                .call()
                .entity(AnalyzeIssueResponse.class);

        Objects.requireNonNull(rs);
        Objects.requireNonNull(rs.getCategory());
        Objects.requireNonNull(rs.getSummary());
        Objects.requireNonNull(rs.getSeverity());

        if (rs.getCategory().isBlank()) {
            throw new AiException("Category is blank");
        }

        if (rs.getSummary().isBlank()) {
            throw new AiException("Summary is blank");
        }

        if (rs.getHypotheses().isEmpty()) {
            throw new AiException("No hypotheses generated");
        }

        if (rs.getHypotheses().size() > 3) {
            rs.setHypotheses(rs.getHypotheses().subList(0, 3));
        }

        return rs;
    }

}
