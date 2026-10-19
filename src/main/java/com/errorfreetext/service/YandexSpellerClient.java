package com.errorfreetext.service;

import com.errorfreetext.entity.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class YandexSpellerClient {

    private static final Logger log = LoggerFactory.getLogger(YandexSpellerClient.class);

    public static final int OPTION_IGNORE_DIGITS = 2;
    public static final int OPTION_IGNORE_URLS = 4;

    private final RestTemplate restTemplate;
    private final String apiUrl;

    public YandexSpellerClient(RestTemplate restTemplate,
            @Value("${yandex.speller.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
    }

    /**
     * Checks and corrects a single text chunk via Yandex Speller API.
     * Returns list of corrections with positions relative to the chunk.
     */
    public List<SpellerCorrection> checkText(String text, Language language, int options) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String encodedText = java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
            String body = "text=" + encodedText + "&lang=" + language.name().toLowerCase() + "&options=" + options;

            log.info("Calling Yandex Speller API: url={}, lang={}, options={}, textLength={}",
                    apiUrl, language, options, text.length());
            log.debug("Request body: {}", body);

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<List> response = restTemplate.postForEntity(apiUrl, request, List.class);

            log.info("Yandex Speller API responded with status: {}", response.getStatusCode());
            log.debug("Response body: {}", response.getBody());

            List<Map<String, Object>> results = response.getBody();
            List<SpellerCorrection> corrections = new ArrayList<>();

            if (results != null) {
                for (Map<String, Object> item : results) {
                    int code = ((Number) item.get("code")).intValue();
                    int pos = ((Number) item.get("pos")).intValue();
                    int len = ((Number) item.get("len")).intValue();
                    String word = (String) item.get("word");
                    List<?> suggestions = (List<?>) item.get("s");
                    String replacement = suggestions != null && !suggestions.isEmpty()
                            ? String.valueOf(suggestions.get(0))
                            : null;
                    corrections.add(new SpellerCorrection(code, pos, len, word, replacement));
                }
            }

            log.info("Yandex Speller returned {} corrections for chunk of length {}",
                    corrections.size(), text.length());
            return corrections;
        } catch (RestClientException e) {
            log.error("Error calling Yandex Speller API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Yandex Speller API: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error calling Yandex Speller API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Yandex Speller API: " + e.getMessage(), e);
        }
    }

    public record SpellerCorrection(int code, int pos, int len, String word, String replacement) {
    }
}
