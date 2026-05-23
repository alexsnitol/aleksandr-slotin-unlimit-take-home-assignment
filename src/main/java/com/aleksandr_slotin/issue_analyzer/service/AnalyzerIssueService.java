package com.aleksandr_slotin.issue_analyzer.service;

import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueReportDto;
import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueResponse;
import com.aleksandr_slotin.issue_analyzer.mapper.AnalyzeIssueReportMapper;
import com.aleksandr_slotin.issue_analyzer.repository.AnalyzeIssueReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzerIssueService {

    private final AnalyseIssueAiService analyseIssueAiService;
    private final AnalyzeIssueReportRepository analyzeIssueReportRepository;
    private final AnalyzeIssueReportMapper analyzeIssueReportMapper;
    @Lazy
    @Autowired
    private AnalyzerIssueService self;
    private static final int MAX_HISTORY_SIZE = 10;


    public AnalyzeIssueResponse analyzeIssue(String issueText) {
        log.info("Analyzing issue: {}", issueText);
        List<AnalyzeIssueReportDto> issueHistory = analyzeIssueReportRepository.findAllByOrderByCreatedAtDesc(
                        Limit.of(MAX_HISTORY_SIZE)
                )
                .stream()
                .map(analyzeIssueReportMapper::toDto)
                .toList();
        AnalyzeIssueResponse rs = analyseIssueAiService.analyzeIssue(issueText, issueHistory);
        log.info("Analysis result: {}", rs);
        self.saveIssueReport(rs);
        log.info("Analysis saved");
        return rs;
    }

    @Transactional
    public void saveIssueReport(@NotNull AnalyzeIssueResponse rs) {
        Objects.requireNonNull(rs);
        analyzeIssueReportRepository.save(analyzeIssueReportMapper.toDocument(rs));
    }

}
