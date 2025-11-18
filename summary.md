# Project Technical Summary

This document outlines the key technical features, architecture, and API structure utilized in this Spring Boot project, designed to manage project-related data.

## 1. Entities (Data Model)

The `src/main/java/com/example/demo/model/` package contains the JPA entities, which represent the core data structures and business objects of the application. Each entity maps to a table in the MySQL database.

Here's a summary of the key entities:

*   **`ActivityLog`**: Records user activities within the application. Includes `user`, `action`, `targetType`, `targetId`, `details` (JSON), and `createdAt`.
*   **`AIAnalysis`**: Stores results of AI-driven analyses related to other entities. Includes `entityType`, `entityId`, `analysisType`, `resultData` (JSON), and `createdAt`.
*   **`AppUser`**: Represents a user of the application. Includes `username`, `email`, `password`, `firstName`, `lastName`, `company`, `department`, `projectMemberships`, `createdAt`, and `updatedAt`.
*   **`Attachment`**: Represents files attached to tasks or projects. Includes `fileName`, `fileUrl`, `fileType`, `uploader`, `task`, `project`, and `createdAt`.
*   **`Comment`**: User comments on tasks. Includes `content`, `task`, `user`, `createdAt`, and `updatedAt`.
*   **`Company`**: Represents a company within the system. Includes `name`, `departments`, `users`, `projects`, `createdAt`, and `updatedAt`.
*   **`Department`**: Represents a department within a company. Includes `name`, `company`, `manager`, `users`, `createdAt`, and `updatedAt`.
*   **`Notification`**: Stores notifications for users. Includes `user`, `message`, `isRead`, `linkToResource`, and `createdAt`.
*   **`Project`**: Represents a project. Includes `name`, `description`, `priority`, `status`, `startDate`, `dueDate`, `company`, `members`, `sprints`, `tasks`, `createdAt`, and `updatedAt`.
*   **`ProjectMember`**: Links an `AppUser` to a `Project` with a specific `Role`. Includes `project`, `user`, and `role`.
*   **`Role`**: Defines roles within the project (e.g., "Developer", "Manager"). Includes `name`.
*   **`Sprint`**: Represents an agile sprint within a project. Includes `name`, `goal`, `startDate`, `endDate`, `project`, `tasks`, `createdAt`, and `updatedAt`.
*   **`Task`**: Represents a task within a project or sprint. Includes `title`, `description`, `status`, `priority`, `dueDate`, `project`, `sprint`, `assignee`, `reporter`, `parentTask`, `subTasks`, `comments`, `createdAt`, and `updatedAt`.

## 2. Project Structure

The project follows a standard Spring Boot application structure, adhering to a layered architecture for modularity and maintainability:

*   **`src/main/java/com/example/demo/`**: The main Java source code directory.
    *   **`config/`**: Contains Spring configuration classes (e.g., for Swagger/OpenAPI).
    *   **`controller/`**: Houses the RESTful API endpoints. These `@RestController` classes handle incoming HTTP requests, delegate business logic to services, and return HTTP responses.
    *   **`exception/`**: Contains custom exception classes (`ResourceNotFoundException`) and the `GlobalExceptionHandler` (`@ControllerAdvice`) for centralized, consistent error handling across the API.
    *   **`model/`**: Contains the JPA entity classes, defining the application's data model and mapping to database tables.
    *   **`repository/`**: Contains Spring Data JPA repository interfaces. These extend `JpaRepository` to provide powerful, boilerplate-free data access operations for entities.
    *   **`service/`**: Contains service classes that encapsulate the core business logic. They orchestrate operations, interact with repositories, and apply business rules.
    *   **`DemoApplication.java`**: The main Spring Boot application class, serving as the entry point.

*   **`src/main/resources/`**: Contains non-Java resources.
    *   **`application.properties`**: The primary configuration file for Spring Boot, including database connection details, server settings, and other application properties.

*   **`pom.xml`**: The Maven Project Object Model file, managing project dependencies (e.g., Spring Boot starters, MySQL connector, Lombok, SpringDoc OpenAPI), build plugins, and project metadata.

## 3. APIs (Controllers)

The `controller` package exposes RESTful APIs for each entity, providing standard CRUD (Create, Read, Update, Delete) operations. Each controller injects its corresponding service to perform operations.

Common API Endpoints Pattern:

*   **`GET /api/{entity-plural}`**: Retrieve a list of all entities (e.g., `GET /api/projects`).
*   **`GET /api/{entity-plural}/{id}`**: Retrieve a single entity by its unique ID (e.g., `GET /api/projects/1`). If the entity is not found, a `404 Not Found` response is returned via the global exception handler.
*   **`POST /api/{entity-plural}`**: Create a new entity. The entity data is provided in the request body (e.g., `POST /api/projects`).
*   **`PUT /api/{entity-plural}/{id}`**: Update an existing entity identified by its ID. The updated data is in the request body (e.g., `PUT /api/projects/1`). If the entity is not found, a `404 Not Found` response is returned.
*   **`DELETE /api/{entity-plural}/{id}`**: Delete an entity by its ID (e.g., `DELETE /api/projects/1`).

## 4. Global Technical Features

*   **Spring Boot:** Facilitates rapid application development with its auto-configuration and embedded server capabilities.
*   **Spring Data JPA:** Streamlines database interaction, reducing the need for manual SQL or ORM configuration.
*   **MySQL Database:** Provides a robust and widely-used relational database solution for data persistence.
*   **Lombok:** Minimizes boilerplate code, making Java entities and DTOs more concise and readable.
*   **SpringDoc OpenAPI (Swagger UI):** Automatically generates interactive API documentation, aiding developers in understanding and testing the API.
*   **Global Exception Handling:** Ensures a consistent and user-friendly error experience across all API endpoints, preventing raw stack traces from being exposed.
*   **Layered Architecture:** Promotes a clean separation of concerns, enhancing maintainability, scalability, and testability of the application.