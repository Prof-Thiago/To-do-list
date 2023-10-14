package br.com.thiago.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.thiago.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;
    
    @PostMapping("/")
    public ResponseEntity<String> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        
        var idUser = request.getAttribute("idUser");
        
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();

        var taskStartAt = taskModel.getStartAt();

        var taskEndAt = taskModel.getEndAt();

        if (currentDate.isAfter(taskStartAt) || currentDate.isAfter(taskEndAt)) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("StartAt and endAt should be after createdAt.");
        
        }

        if (taskStartAt.isAfter(taskEndAt)) {
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("EndAt should be after createdAt.");
        
        }
        
        var task = this.taskRepository.save(taskModel);

        return ResponseEntity.status(HttpStatus.OK).body(task.toString());
    
    }

    @GetMapping("/")
    public ResponseEntity<List<TaskModel>> list(HttpServletRequest request) {
        
        var idUser = request.getAttribute("idUser");

        var tasks = this.taskRepository.findByIdUser((UUID) idUser);

        return ResponseEntity.status(HttpStatus.FOUND).body(tasks);
    
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        
        var task = this.taskRepository.findById(id).orElse(null);

        if (task != null) {

            var idUser = request.getAttribute("idUser");

            if (!task.getIdUser().equals(idUser)) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user.");
            
            }

            Utils.copyNonNullProperties(taskModel, task);
            
            var updatedTask = this.taskRepository.save(task);

            return ResponseEntity.status(HttpStatus.OK).body(updatedTask.toString());

        } 
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found.");

    }

}
