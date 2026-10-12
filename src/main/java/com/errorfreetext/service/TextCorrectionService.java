package com.errorfreetext.service;

import com.errorfreetext.entity.Language;
import com.errorfreetext.util.OptionsDetector;
import com.errorfreetext.util.TextSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextCorrectionService {

    private static final Logger log = LoggerFactory.getLogger(TextCorrectionService.class);

    private final YandexSpellerClient spellerClient;
    private final TextSplitter textSplitter;
    private final OptionsDetector optionsDetector;

    public TextCorrectionService(YandexSpellerClient spellerClient,
                                  TextSplitter textSplitter,
                                  OptionsDetector optionsDetector) {
        this.spellerClient = spellerClient;
        this.textSplitter = textSplitter;
        this.optionsDetector = optionsDetector;
    }

    public String correctText(String text, Language language) {
        log.info("Starting text correction, length={}, language={}", text.length(), language);

        int options = optionsDetector.detectOptions(text);
        log.debug("Detected options mask: {}", options);

        List<String> chunks = textSplitter.split(text);
        log.info("Text split into {} chunks", chunks.size());

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            log.debug("Processing chunk {}/{}, length={}", i + 1, chunks.size(), chunk.length());

            List<YandexSpellerClient.SpellerCorrection> corrections =
                    spellerClient.checkText(chunk, language, options);

            String correctedChunk = applyCorrections(chunk, corrections);
            result.append(correctedChunk);
        }

        log.info("Text correction completed");
        return result.toString();
    }

    private String applyCorrections(String text, List<YandexSpellerClient.SpellerCorrection> corrections) {
        if (corrections.isEmpty()) {
            return text;
        }

        // Sort by position ascending and apply with offset tracking
        List<YandexSpellerClient.SpellerCorrection> sorted = new ArrayList<>(corrections);
        sorted.sort((a, b) -> Integer.compare(a.pos(), b.pos()));

        StringBuilder sb = new StringBuilder(text);
        int offset = 0;
        for (YandexSpellerClient.SpellerCorrection correction : sorted) {
            if (correction.replacement() != null && !correction.replacement().isEmpty()) {
                int start = correction.pos() + offset;
                int end = start + correction.len();
                sb.replace(start, end, correction.replacement());
                offset += correction.replacement().length() - correction.len();
            }
        }
        return sb.toString();
    }
}
