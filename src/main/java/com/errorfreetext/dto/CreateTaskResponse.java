package com.errorfreetext.dto;

import java.util.UUID;

public class CreateTaskResponse {

    private UUID taskId;

    public CreateTaskResponse() {
    }

    public CreateTaskResponse(UUID taskId) {
        this.taskId = taskId;
    }

    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }
}
