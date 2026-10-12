package com.errorfreetext.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextSplitter {

    private final int maxChunkSize;

    public TextSplitter(@Value("${yandex.speller.max-chunk-size:10000}") int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    /**
     * Splits text into chunks of at most maxChunkSize characters.
     * Tries to split at sentence boundaries or spaces to avoid breaking words.
     */
    public List<String> split(String text) {
        List<String> chunks = new ArrayList<>();

        if (text.length() <= maxChunkSize) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChunkSize, text.length());

            if (end < text.length()) {
                // Try to find a good split point - prefer sentence end, then space
                int splitPoint = findSplitPoint(text, start, end);
                end = splitPoint;
            }

            chunks.add(text.substring(start, end));
            start = end;
        }

        return chunks;
    }

    private int findSplitPoint(String text, int start, int end) {
        // Look for sentence boundary (. ! ?) followed by space or end
        for (int i = end - 1; i > start; i--) {
            char c = text.charAt(i);
            if ((c == '.' || c == '!' || c == '?') && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == ' ' || next == '\n' || next == '\r') {
                    return i + 1;
                }
            }
        }

        // Look for newline
        int lastNewline = text.lastIndexOf('\n', end - 1);
        if (lastNewline > start) {
            return lastNewline + 1;
        }

        // Look for space
        int lastSpace = text.lastIndexOf(' ', end - 1);
        if (lastSpace > start) {
            return lastSpace + 1;
        }

        // Fallback: hard split at maxChunkSize
        return end;
    }
}
