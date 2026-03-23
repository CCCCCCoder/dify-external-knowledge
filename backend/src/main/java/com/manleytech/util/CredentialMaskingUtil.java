package com.manleytech.util;

import java.util.regex.Pattern;

public final class CredentialMaskingUtil {

    private static final Pattern API_KEY_PATTERN = Pattern.compile(
            "\"?(api[keyK]ey|key|secret|password|token|auth)\\s*[\"']?\\s*[:=]\\s*[\"']?[^\"',}]+[\"']?",
            Pattern.CASE_INSENSITIVE
    );

    private static final String MASK = "***";
    private static final int VISIBLE_PREFIX_LENGTH = 4;
    private static final int VISIBLE_SUFFIX_LENGTH = 4;
    private static final int MIN_LENGTH_FOR_DETAILED_MASK = 12;

    private CredentialMaskingUtil() {
    }

    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return MASK;
        }
        if (apiKey.length() < MIN_LENGTH_FOR_DETAILED_MASK) {
            return MASK;
        }
        if (apiKey.length() <= VISIBLE_PREFIX_LENGTH + VISIBLE_SUFFIX_LENGTH) {
            return MASK;
        }
        return apiKey.substring(0, VISIBLE_PREFIX_LENGTH) + MASK + 
               apiKey.substring(apiKey.length() - VISIBLE_SUFFIX_LENGTH);
    }

    public static String maskHeader(String header) {
        if (header == null) {
            return null;
        }
        return header.replaceAll("([Aa]uthorization|[Kk]ey)[:\\s]*[^,]*", "$1: " + MASK);
    }

    public static String maskBody(String body) {
        if (body == null) {
            return null;
        }
        return body.replaceAll("\"(api[keyK]ey|key|secret|password|token|auth)\"\\s*:\\s*\"[^\"]*\"", "\"$1\": \"" + MASK + "\"");
    }

    public static String maskJson(String json) {
        if (json == null) {
            return null;
        }
        return API_KEY_PATTERN.matcher(json).replaceAll("$1: " + MASK);
    }
}