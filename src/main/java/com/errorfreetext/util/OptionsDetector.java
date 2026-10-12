package com.errorfreetext.util;

import com.errorfreetext.service.YandexSpellerClient;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class OptionsDetector {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+", Pattern.CASE_INSENSITIVE);

    /**
     * Detects which Yandex Speller options to enable based on text content.
     * FIND_REPEAT_WORDS and IGNORE_CAPITALIZATION are always disabled.
     */
    public int detectOptions(String text) {
        int options = 0;

        if (DIGIT_PATTERN.matcher(text).find()) {
            options |= YandexSpellerClient.OPTION_IGNORE_DIGITS;
        }

        if (URL_PATTERN.matcher(text).find()) {
            options |= YandexSpellerClient.OPTION_IGNORE_URLS;
        }

        return options;
    }
}
