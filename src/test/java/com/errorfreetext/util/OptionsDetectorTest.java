package com.errorfreetext.util;

import com.errorfreetext.service.YandexSpellerClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OptionsDetectorTest {

    private final OptionsDetector detector = new OptionsDetector();

    @Test
    void detectOptions_textWithDigits_enablesIgnoreDigits() {
        String text = "Hello 123 world";
        int options = detector.detectOptions(text);
        assertThat(options & YandexSpellerClient.OPTION_IGNORE_DIGITS)
                .isEqualTo(YandexSpellerClient.OPTION_IGNORE_DIGITS);
    }

    @Test
    void detectOptions_textWithoutDigits_doesNotEnableIgnoreDigits() {
        String text = "Hello world";
        int options = detector.detectOptions(text);
        assertThat(options & YandexSpellerClient.OPTION_IGNORE_DIGITS).isEqualTo(0);
    }

    @Test
    void detectOptions_textWithUrl_enablesIgnoreUrls() {
        String text = "Visit https://example.com for more";
        int options = detector.detectOptions(text);
        assertThat(options & YandexSpellerClient.OPTION_IGNORE_URLS)
                .isEqualTo(YandexSpellerClient.OPTION_IGNORE_URLS);
    }

    @Test
    void detectOptions_textWithoutUrl_doesNotEnableIgnoreUrls() {
        String text = "Hello world";
        int options = detector.detectOptions(text);
        assertThat(options & YandexSpellerClient.OPTION_IGNORE_URLS).isEqualTo(0);
    }

    @Test
    void detectOptions_textWithBoth_enablesBothOptions() {
        String text = "Visit https://example.com at 123 Main St";
        int options = detector.detectOptions(text);
        assertThat(options & YandexSpellerClient.OPTION_IGNORE_DIGITS)
                .isEqualTo(YandexSpellerClient.OPTION_IGNORE_DIGITS);
        assertThat(options & YandexSpellerClient.OPTION_IGNORE_URLS)
                .isEqualTo(YandexSpellerClient.OPTION_IGNORE_URLS);
    }

    @Test
    void detectOptions_alwaysDisablesFindRepeatWords() {
        String text = "Hello hello world";
        int options = detector.detectOptions(text);
        assertThat(options & 8).isEqualTo(0); // FIND_REPEAT_WORDS = 8
    }

    @Test
    void detectOptions_alwaysDisablesIgnoreCapitalization() {
        String text = "HELLO world";
        int options = detector.detectOptions(text);
        assertThat(options & 512).isEqualTo(0); // IGNORE_CAPITALIZATION = 512
    }
}
