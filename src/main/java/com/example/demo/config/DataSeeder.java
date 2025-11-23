package com.example.demo.config;

import com.example.demo.model.Project;
import com.example.demo.model.Task;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(ProjectRepository projectRepository, TaskRepository taskRepository,
            com.example.demo.repository.CompanyRepository companyRepository) {
        return args -> {
            if (projectRepository.count() == 0) {
                com.example.demo.model.Company company = new com.example.demo.model.Company();
                company.setName("Acme Corp");
                companyRepository.save(company);

                Project project1 = new Project();
                project1.setName("Project Alpha");
                project1.setDescription("First project description");
                project1.setStatus("IN_PROGRESS");
                project1.setStartDate(java.time.LocalDate.now());
                project1.setCompany(company);
                projectRepository.save(project1);

                Project project2 = new Project();
                project2.setName("Project Beta");
                project2.setDescription("Second project description");
                project2.setStatus("PLANNED");
                project2.setStartDate(java.time.LocalDate.now().plusDays(1));
                project2.setCompany(company);
                projectRepository.save(project2);

                if (taskRepository.count() == 0) {
                    Task task1 = new Task();
                    task1.setTitle("Task 1");
                    task1.setDescription("Description for task 1");
                    task1.setStatus("TODO");
                    task1.setPriority("HIGH");
                    task1.setProject(project1);
                    taskRepository.save(task1);

                    Task task2 = new Task();
                    task2.setTitle("Task 2");
                    task2.setDescription("Description for task 2");
                    task2.setStatus("IN_PROGRESS");
                    task2.setPriority("MEDIUM");
                    task2.setProject(project1);
                    taskRepository.save(task2);
                }
            }
        };
    }
}
