

package com.project.HackthonStarter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TaskService {

    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Task createTask(Task task) {
        Long id = idGenerator.getAndIncrement();

        Task newTask = new Task(
            id,
            task.getTitle(),
            task.getDescription(),
            task.getStatus()
        );

        tasks.put(id, newTask);
        return newTask;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(Long id) {
        Task task = tasks.get(id);

        if (task == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Task not found with ID: " + id
            );
        }

        return task;
    }

    public Task updateTask(Long id, Task updatedTask) {
        Task existingTask = getTaskById(id);

        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        return existingTask;
    }

    public void deleteTask(Long id) {
        getTaskById(id);
        tasks.remove(id);
    }
}
