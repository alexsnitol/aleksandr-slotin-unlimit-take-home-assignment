package com.aleksandr_slotin.issue_analyzer.service;

import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueReportDto;
import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueResponse;
import com.aleksandr_slotin.issue_analyzer.mapper.AnalyzeIssueReportMapper;
import com.aleksandr_slotin.issue_analyzer.repository.AnalyzeIssueReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerIssueService {

    private final AnalyseIssueAiService analyseIssueAiService;
    private final AnalyzeIssueReportRepository analyzeIssueReportRepository;
    private final AnalyzeIssueReportMapper analyzeIssueReportMapper;
    private static final int MAX_HISTORY_SIZE = 10;
    private static final int MIN_INPUT_LENGTH = 10;
    private static final int MAX_INPUT_LENGTH = 8_000;


    public AnalyzeIssueResponse analyzeIssue(String issueText) {
        if (issueText == null || issueText.length() < MIN_INPUT_LENGTH) {
            throw new IllegalArgumentException("Issue text is too short");
        }

        if (issueText.length() > MAX_INPUT_LENGTH) {
            throw new IllegalArgumentException("Issue text is too long");
        }

        if (InjectionPatternFilter.isSuspicious(issueText)) {
            throw new IllegalArgumentException("Issue text contains suspicious patterns");
        }

        log.info("Analyzing issue: {}", issueText);
        List<AnalyzeIssueReportDto> issueHistory = analyzeIssueReportRepository.findAllByOrderByCreatedAtDesc(
                        Limit.of(MAX_HISTORY_SIZE)
                )
                .stream()
                .map(analyzeIssueReportMapper::toDto)
                .toList();

        AnalyzeIssueResponse rs = analyseIssueAiService.analyzeIssue(issueText, issueHistory);
        log.info("Analysis result: {}", rs);
        saveIssueReport(rs);
        log.info("Analysis saved");
        return rs;
    }

    private void saveIssueReport(@NotNull AnalyzeIssueResponse rs) {
        Objects.requireNonNull(rs);
        analyzeIssueReportRepository.save(analyzeIssueReportMapper.toDocument(rs));
    }

}
