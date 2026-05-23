package com.aleksandr_slotin.issue_analyzer.service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Fast deterministic pre-check before calling the guard LLM.
 * Blocks obvious injection patterns with zero latency and zero cost.
 */
public final class InjectionPatternFilter {

    // Compile once — patterns are immutable
    private static final List<Pattern> PATTERNS = List.of(
            // Role override attempts
            Pattern.compile("(?i)(ignore|forget|disregard).{0,30}(previous|above|prior|system|instructions?|prompt)"),
            Pattern.compile("(?i)(you are now|act as|pretend (you are|to be)|roleplay as|simulate)"),
            Pattern.compile("(?i)(new (role|persona|instructions?|directive))"),

            // System prompt extraction
            Pattern.compile("(?i)(repeat|print|output|reveal|show|tell me).{0,30}(system prompt|instructions?|directive|configuration)"),
            Pattern.compile("(?i)what (are|were) (your|the) (instructions?|system|directives?)"),

            // Jailbreak patterns
            Pattern.compile("(?i)(DAN|do anything now|no restrictions|without (any )?restrictions|jailbreak)"),
            Pattern.compile("(?i)(hypothetically|imagine|suppose|what if).{0,50}(no (rules|restrictions|limits|guidelines))"),

            // Instruction injection inside fake data (log lines, JSON, XML)
            Pattern.compile("(?i)<\\s*(system|instruction|prompt|override)\\s*>"),
            Pattern.compile("(?i)\\[\\s*(system|instruction|override)\\s*[:\\]]"),
            Pattern.compile("(?i)(###\\s*(system|instruction|new task))"),

            // Output format override
            Pattern.compile("(?i)(respond (only |exclusively )?(in|with)|output (only|just|exclusively)).{0,30}(language|format|mode)"),
            Pattern.compile("(?i)(translate (your|all) (output|response|answer) to)")
    );

    private InjectionPatternFilter() {
    }

    /**
     * @return matched pattern description, or empty string if clean
     */
    public static String findViolation(String input) {
        if (input == null || input.isBlank()) return "";

        for (Pattern p : PATTERNS) {
            var matcher = p.matcher(input);
            if (matcher.find()) {
                return "Regex match: [" + p.pattern() + "] at: \"" + matcher.group() + "\"";
            }
        }
        return "";
    }

    public static boolean isSuspicious(String input) {
        return !findViolation(input).isBlank();
    }

}
