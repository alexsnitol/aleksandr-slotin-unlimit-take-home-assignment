package com.aleksandr_slotin.issue_analyzer.exception;

public class AiException extends RuntimeException {
    private final String rawOutput;

    public AiException(String message, String rawOutput) {
        super(formatMessage(message, rawOutput));
        this.rawOutput = rawOutput;
    }

    public AiException(String message, String rawOutput, Exception e) {
        super(formatMessage(message, rawOutput), e);
        this.rawOutput = rawOutput;
    }

    public String getRawOutput() {
        return rawOutput;
    }

    private static String formatMessage(String message, String rawOutput) {
        return message;
    }

}
