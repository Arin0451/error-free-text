package com.errorfreetext.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextSplitterTest {

    private final TextSplitter splitter = new TextSplitter(10000);

    @Test
    void split_shortText_returnsSingleChunk() {
        String text = "Hello world";
        List<String> chunks = splitter.split(text);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEqualTo(text);
    }

    @Test
    void split_exactSizeText_returnsSingleChunk() {
        String text = "a".repeat(10000);
        List<String> chunks = splitter.split(text);
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).hasSize(10000);
    }

    @Test
    void split_longText_returnsMultipleChunks() {
        String text = "a".repeat(25000);
        List<String> chunks = splitter.split(text);
        assertThat(chunks.size()).isGreaterThan(1);
        // Verify total length is preserved
        int totalLength = chunks.stream().mapToInt(String::length).sum();
        assertThat(totalLength).isEqualTo(25000);
    }

    @Test
    void split_textWithSentenceBoundary_splitsAtSentence() {
        // Create text with sentence boundary around position 10000
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 900; i++) {
            sb.append("Word ");
        }
        sb.append("End. ");
        while (sb.length() < 15000) {
            sb.append("More ");
        }

        List<String> chunks = splitter.split(sb.toString());
        assertThat(chunks.size()).isGreaterThan(1);

        // Verify no chunk exceeds max size
        for (String chunk : chunks) {
            assertThat(chunk.length()).isLessThanOrEqualTo(10000);
        }
    }

    @Test
    void split_emptyText_returnsSingleEmptyChunk() {
        List<String> chunks = splitter.split("");
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEmpty();
    }
}
