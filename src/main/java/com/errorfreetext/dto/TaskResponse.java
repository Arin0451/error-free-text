package com.errorfreetext.dto;

import com.errorfreetext.entity.TaskStatus;

public class TaskResponse {

    private TaskStatus status;
    private String correctedText;
    private String errorMessage;

    public TaskResponse() {
    }

    public TaskResponse(TaskStatus status, String correctedText, String errorMessage) {
        this.status = status;
        this.correctedText = correctedText;
        this.errorMessage = errorMessage;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getCorrectedText() {
        return correctedText;
    }

    public void setCorrectedText(String correctedText) {
        this.correctedText = correctedText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
