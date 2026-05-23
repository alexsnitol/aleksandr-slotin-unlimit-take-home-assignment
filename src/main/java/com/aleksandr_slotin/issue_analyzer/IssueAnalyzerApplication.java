package com.aleksandr_slotin.issue_analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@EnableResilientMethods
@SpringBootApplication
public class IssueAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IssueAnalyzerApplication.class, args);
	}

}
