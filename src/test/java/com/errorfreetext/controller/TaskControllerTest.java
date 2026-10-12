package com.errorfreetext.controller;

import com.errorfreetext.dto.CreateTaskRequest;
import com.errorfreetext.dto.CreateTaskResponse;
import com.errorfreetext.dto.TaskResponse;
import com.errorfreetext.entity.Language;
import com.errorfreetext.entity.TaskStatus;
import com.errorfreetext.exception.GlobalExceptionHandler;
import com.errorfreetext.exception.InvalidTextException;
import com.errorfreetext.exception.TaskNotFoundException;
import com.errorfreetext.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    void createTask_withValidRequest_returnsCreated() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Hello world", Language.EN);
        UUID taskId = UUID.randomUUID();
        when(taskService.createTask(any())).thenReturn(new CreateTaskResponse(taskId));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    void createTask_withInvalidLanguage_returnsBadRequest() throws Exception {
        String json = "{\"text\": \"Hello world\", \"language\": \"FR\"}";

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(40001));
    }

    @Test
    void createTask_withShortText_returnsBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Hi", Language.EN);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_withOnlyDigitsAndSpecialChars_returnsBadRequest() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("123 !!!", Language.EN);
        when(taskService.createTask(any()))
                .thenThrow(new InvalidTextException("Text cannot contain only special characters and digits"));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(40001));
    }

    @Test
    void getTask_whenExists_returnsTask() throws Exception {
        UUID taskId = UUID.randomUUID();
        TaskResponse taskResponse = new TaskResponse(TaskStatus.COMPLETED, "Corrected text", null);
        when(taskService.getTask(taskId)).thenReturn(taskResponse);

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.correctedText").value("Corrected text"));
    }

    @Test
    void getTask_whenNotFound_returns404() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(taskService.getTask(taskId))
                .thenThrow(new TaskNotFoundException(taskId));

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(40401))
                .andExpect(jsonPath("$.errorMessage").value(
                        "Task with id: " + taskId + " not found"));
    }

    @Test
    void getTask_whenInProgress_returnsOnlyStatus() throws Exception {
        UUID taskId = UUID.randomUUID();
        TaskResponse taskResponse = new TaskResponse(TaskStatus.IN_PROGRESS, null, null);
        when(taskService.getTask(taskId)).thenReturn(taskResponse);

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
