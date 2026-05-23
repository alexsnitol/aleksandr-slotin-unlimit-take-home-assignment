package com.aleksandr_slotin.issue_analyzer.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Severity of the issue.")
public enum SeverityEnum {

    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    private final String value;

    SeverityEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SeverityEnum fromValue(String value) {
        for (SeverityEnum severity : values()) {
            if (severity.value.equalsIgnoreCase(value)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown severity: " + value);
    }

}
