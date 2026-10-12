package com.errorfreetext.dto;

import com.errorfreetext.entity.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {

    @NotBlank(message = "Text must not be blank")
    @Size(min = 3, message = "Text must contain at least 3 characters")
    private String text;

    @NotNull(message = "Language must not be null")
    private Language language;

    public CreateTaskRequest() {
    }

    public CreateTaskRequest(String text, Language language) {
        this.text = text;
        this.language = language;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
