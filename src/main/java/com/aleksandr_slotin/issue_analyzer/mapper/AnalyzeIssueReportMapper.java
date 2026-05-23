package com.aleksandr_slotin.issue_analyzer.mapper;

import com.aleksandr_slotin.issue_analyzer.document.AnalyzeIssueReport;
import com.aleksandr_slotin.issue_analyzer.document.AnalyzeIssueReportHypothesis;
import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueHypothesis;
import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueReportDto;
import com.aleksandr_slotin.issue_analyzer.dto.AnalyzeIssueResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalyzeIssueReportMapper {

    AnalyzeIssueReportDto toDto(AnalyzeIssueReport report);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    AnalyzeIssueReport toDocument(AnalyzeIssueResponse response);

    AnalyzeIssueReportHypothesis toDocument(AnalyzeIssueHypothesis hypothesis);

}
