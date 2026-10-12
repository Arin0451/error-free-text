package com.errorfreetext.scheduler;

import com.errorfreetext.entity.Task;
import com.errorfreetext.entity.TaskStatus;
import com.errorfreetext.repository.TaskRepository;
import com.errorfreetext.service.TextCorrectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TextCorrectionScheduler {

    private static final Logger log = LoggerFactory.getLogger(TextCorrectionScheduler.class);

    private final TaskRepository taskRepository;
    private final TextCorrectionService correctionService;

    public TextCorrectionScheduler(TaskRepository taskRepository, TextCorrectionService correctionService) {
        this.taskRepository = taskRepository;
        this.correctionService = correctionService;
    }

    @Scheduled(fixedDelayString = "${task.scheduler.interval-ms:5000}")
    @Transactional
    public void processPendingTasks() {
        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.NEW);

        if (pendingTasks.isEmpty()) {
            return;
        }

        log.info("Found {} pending tasks to process", pendingTasks.size());

        for (Task task : pendingTasks) {
            processTask(task);
        }
    }

    private void processTask(Task task) {
        log.info("Processing task id={}", task.getId());
        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);

        try {
            String correctedText = correctionService.correctText(
                    task.getOriginalText(), task.getLanguage());

            task.setCorrectedText(correctedText);
            task.setStatus(TaskStatus.COMPLETED);
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);

            log.info("Task id={} completed successfully", task.getId());
        } catch (Exception e) {
            log.error("Task id={} failed: {}", task.getId(), e.getMessage(), e);
            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage(e.getMessage());
            task.setUpdatedAt(LocalDateTime.now());
            taskRepository.save(task);
        }
    }
}
