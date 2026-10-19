package com.errorfreetext.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class YandexSpellerClient {

    private static final Logger log = LoggerFactory.getLogger(YandexSpellerClient.class);

    public static final int OPTION_IGNORE_DIGITS = 2;
    public static final int OPTION_IGNORE_URLS = 4;

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final ObjectMapper objectMapper;

    public YandexSpellerClient(RestTemplate restTemplate,
            @Value("${yandex.speller.url}") String apiUrl,
            ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * Проверяет и исправляет один фрагмент текста через API Яндекс Спеллер.
     * Возвращает список исправлений с позициями относительно фрагмента.
     */
    public List<SpellerCorrection> checkText(String text, Language language, int options) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String encodedText = java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8);
            String body = "text=" + encodedText + "&lang=" + language.name().toLowerCase() + "&options=" + options;

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            log.info("Вызов Яндекс Спеллера: url={}, lang={}, options={}, textLength={}", apiUrl, language, options, text.length());

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            log.info("Яндекс Спеллер ответил: status={}", response.getStatusCode());

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                log.info("Яндекс Спеллер вернул пустой ответ");
                return Collections.emptyList();
            }

            List<?> rawResults = objectMapper.readValue(responseBody, new TypeReference<List<?>>() {});
            List<SpellerCorrection> corrections = new ArrayList<>();

            for (Object rawItem : rawResults) {
                if (!(rawItem instanceof Map)) {
                    continue;
                }
                Map<String, Object> item = (Map<String, Object>) rawItem;

                int code = ((Number) item.getOrDefault("code", 0)).intValue();
                int pos = ((Number) item.getOrDefault("pos", 0)).intValue();
                int len = ((Number) item.getOrDefault("len", 0)).intValue();
                String word = (String) item.get("word");

                Object suggestionsObj = item.get("s");
                String replacement = null;
                if (suggestionsObj instanceof List<?> suggestions && !suggestions.isEmpty()) {
                    replacement = String.valueOf(suggestions.get(0));
                }

                corrections.add(new SpellerCorrection(code, pos, len, word, replacement));
            }

            log.info("Яндекс Спеллер вернул {} исправлений для фрагмента длиной {}", corrections.size(), text.length());
            return corrections;
        } catch (RestClientException e) {
            log.error("Ошибка вызова Яндекс Спеллера: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось вызвать API Яндекс Спеллера: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при вызове Яндекс Спеллера: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось вызвать API Яндекс Спеллера: " + e.getMessage(), e);
        }
    }

    public record SpellerCorrection(int code, int pos, int len, String word, String replacement) {
    }
}
