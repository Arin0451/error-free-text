package com.errorfreetext.service;

import com.errorfreetext.dto.CreateTaskRequest;
import com.errorfreetext.dto.CreateTaskResponse;
import com.errorfreetext.dto.TaskResponse;
import com.errorfreetext.entity.Language;
import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.TaskStatus;
import com.errorfreetext.exception.InvalidTextException;
import com.errorfreetext.exception.TaskNotFoundException;
import com.errorfreetext.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_shouldReturnTaskId() {
        CreateTaskRequest request = new CreateTaskRequest("Hello world", Language.EN);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        CreateTaskResponse response = taskService.createTask(request);

        assertThat(response).isNotNull();
        assertThat(response.getTaskId()).isNotNull();
    }

    @Test
    void createTask_withOnlyDigitsAndSpecialChars_shouldThrow() {
        CreateTaskRequest request = new CreateTaskRequest("123 !!!", Language.EN);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(InvalidTextException.class)
                .hasMessageContaining("special characters and digits");
    }

    @Test
    void createTask_withOnlySpecialChars_shouldThrow() {
        CreateTaskRequest request = new CreateTaskRequest("!@#$%", Language.EN);

        assertThatThrownBy(() -> taskService.createTask(request))
                .isInstanceOf(InvalidTextException.class);
    }

    @Test
    void createTask_withValidText_shouldSucceed() {
        CreateTaskRequest request = new CreateTaskRequest("a", Language.EN);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        CreateTaskResponse response = taskService.createTask(request);
        assertThat(response.getTaskId()).isNotNull();
    }

    @Test
    void getTask_whenExists_shouldReturnTask() {
        UUID id = UUID.randomUUID();
        Task task = new Task(id, "Original", Language.EN, TaskStatus.COMPLETED, LocalDateTime.now());
        task.setCorrectedText("Corrected");

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTask(id);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        assertThat(response.getCorrectedText()).isEqualTo("Corrected");
    }

    @Test
    void getTask_whenNotExists_shouldThrow() {
        UUID id = UUID.randomUUID();

        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(id))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getTask_whenInProgress_shouldNotReturnText() {
        UUID id = UUID.randomUUID();
        Task task = new Task(id, "Original", Language.EN, TaskStatus.IN_PROGRESS, LocalDateTime.now());

        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTask(id);

        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.getCorrectedText()).isNull();
    }
}
