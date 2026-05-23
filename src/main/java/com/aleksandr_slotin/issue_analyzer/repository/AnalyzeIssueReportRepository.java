package com.aleksandr_slotin.issue_analyzer.repository;

import com.aleksandr_slotin.issue_analyzer.document.AnalyzeIssueReport;
import org.springframework.data.domain.Limit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface AnalyzeIssueReportRepository extends MongoRepository<AnalyzeIssueReport, UUID> {
    List<AnalyzeIssueReport> findAllByOrderByCreatedAtDesc(Limit limit);
}
