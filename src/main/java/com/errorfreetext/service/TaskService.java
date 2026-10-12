package com.errorfreetext.service;

import com.errorfreetext.dto.CreateTaskRequest;
import com.errorfreetext.dto.CreateTaskResponse;
import com.errorfreetext.dto.TaskResponse;
import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.TaskStatus;
import com.errorfreetext.exception.InvalidTextException;
import com.errorfreetext.exception.TaskNotFoundException;
import com.errorfreetext.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public CreateTaskResponse createTask(CreateTaskRequest request) {
        validateText(request.getText());

        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setOriginalText(request.getText());
        task.setLanguage(request.getLanguage());
        task.setStatus(TaskStatus.NEW);
        task.setCreatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);
        log.info("Created task with id={}", saved.getId());
        return new CreateTaskResponse(saved.getId());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return new TaskResponse(
                task.getStatus(),
                task.getCorrectedText(),
                task.getErrorMessage()
        );
    }

    private void validateText(String text) {
        // Text must contain at least one letter (not only special chars and digits)
        boolean hasLetter = text.chars().anyMatch(Character::isLetter);
        if (!hasLetter) {
            throw new InvalidTextException(
                    "Text cannot contain only special characters and digits");
        }
    }
}
