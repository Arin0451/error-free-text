package com.errorfreetext.service;

import com.errorfreetext.entity.Language;
import com.errorfreetext.util.OptionsDetector;
import com.errorfreetext.util.TextSplitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TextCorrectionServiceTest {

    @Mock
    private YandexSpellerClient spellerClient;

    private TextCorrectionService correctionService;

    @BeforeEach
    void setUp() {
        TextSplitter textSplitter = new TextSplitter(10000);
        OptionsDetector optionsDetector = new OptionsDetector();
        correctionService = new TextCorrectionService(spellerClient, textSplitter, optionsDetector);
    }

    @Test
    void correctText_noCorrections_returnsOriginal() {
        String text = "Hello world";
        when(spellerClient.checkText(eq(text), eq(Language.EN), anyInt()))
                .thenReturn(List.of());

        String result = correctionService.correctText(text, Language.EN);
        assertThat(result).isEqualTo(text);
    }

    @Test
    void correctText_withCorrections_appliesThem() {
        String text = "Helllo world";
        List<YandexSpellerClient.SpellerCorrection> corrections = new ArrayList<>(
                List.of(new YandexSpellerClient.SpellerCorrection(1, 0, 6, "Helllo", "Hello"))
        );
        when(spellerClient.checkText(eq(text), eq(Language.EN), anyInt()))
                .thenReturn(corrections);

        String result = correctionService.correctText(text, Language.EN);
        assertThat(result).isEqualTo("Hello world");
    }

    @Test
    void correctText_multipleCorrections_appliesAll() {
        String text = "Helllo woorld";
        // "Helllo" at pos=0, len=6 → "Hello"; "woorld" at pos=7, len=6 → "world"
        // After sorting by pos descending: first "woorld" at 7, then "Helllo" at 0
        List<YandexSpellerClient.SpellerCorrection> corrections = new ArrayList<>(
                List.of(
                        new YandexSpellerClient.SpellerCorrection(1, 0, 6, "Helllo", "Hello"),
                        new YandexSpellerClient.SpellerCorrection(1, 7, 6, "woorld", "world")
                )
        );
        when(spellerClient.checkText(eq(text), eq(Language.EN), anyInt()))
                .thenReturn(corrections);

        String result = correctionService.correctText(text, Language.EN);
        assertThat(result).isEqualTo("Hello world");
    }

    @Test
    void correctText_singleCorrection_appliesIt() {
        String text = "Helllo world";
        List<YandexSpellerClient.SpellerCorrection> corrections = new ArrayList<>(
                List.of(new YandexSpellerClient.SpellerCorrection(1, 0, 6, "Helllo", "Hello"))
        );
        when(spellerClient.checkText(eq(text), eq(Language.EN), anyInt()))
                .thenReturn(corrections);

        String result = correctionService.correctText(text, Language.EN);
        assertThat(result).isEqualTo("Hello world");
    }

    @Test
    void correctText_longText_processesAllChunks() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2500; i++) {
            sb.append("Helllo ");
        }
        String text = sb.toString().trim();

        // First chunk starts with "Helllo" at pos=0
        List<YandexSpellerClient.SpellerCorrection> firstChunkCorrections = new ArrayList<>(
                List.of(new YandexSpellerClient.SpellerCorrection(1, 0, 6, "Helllo", "Hello"))
        );
        // Second chunk may start with space, so "Helllo" is at pos=1
        List<YandexSpellerClient.SpellerCorrection> secondChunkCorrections = new ArrayList<>(
                List.of(new YandexSpellerClient.SpellerCorrection(1, 1, 6, "Helllo", "Hello"))
        );

        when(spellerClient.checkText(any(), eq(Language.EN), anyInt()))
                .thenReturn(firstChunkCorrections)
                .thenReturn(secondChunkCorrections);

        String result = correctionService.correctText(text, Language.EN);
        assertThat(result).contains("Hello");
    }
}
