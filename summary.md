# Summary and Tutorial: Building a Spring Boot Backend

This document first outlines the basic steps to turn a minimal Spring Boot application into a web backend, and then provides a full tutorial for building a "Todo" REST API. It concludes with an explanation of the key software design patterns used in the project.

---

## Part 1: From Basic App to Web Backend

### 1. Understanding the Initial Project

Initially, this is a minimal Spring Boot application. It lacks the necessary components for web development. To add backend functionality, you must first include the Spring Web dependency.

### 2. How to Create REST APIs

#### Step 1: Add the Spring Web Dependency

To create REST controllers, add the `spring-boot-starter-web` dependency to your `pom.xml` file inside the `<dependencies>` section:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

#### Step 2: Create a REST Controller

A controller handles incoming web requests. Here is a simple example:

```java
package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, World!";
    }
}
```
- **`@RestController`**: Marks this class as a controller that returns data directly as a response.
- **`@GetMapping("/hello")`**: Maps HTTP GET requests for `/hello` to the `sayHello()` method.

#### Step 3: Run the Application

Run the application using the command: `mvn spring-boot:run`. A web server will start (usually on port 8080), and you can access the route at `http://localhost:8080/hello`.

### 3. What You Need for a Complete Backend

A full-featured backend typically requires:

1.  **Web Framework**: `spring-boot-starter-web` for creating REST APIs.
2.  **Data Persistence**: `spring-boot-starter-data-jpa` to work with databases.
3.  **Database Driver**: A driver for your chosen database, like H2 for development (`com.h2database:h2`).
4.  **Database Configuration**: Settings in `src/main/resources/application.properties` to connect to the database.

---

## Part 2: Tutorial - Building a "Todo" REST API

This tutorial will guide you through creating a complete REST API with Create, Read, Update, and Delete (CRUD) functionality.

### 1. Organize Your Project (Best Practice)

Create packages to structure your code by function:
- `src/main/java/com/example/demo/controller`
- `src/main/java/com/example/demo/model`
- `src/main/java/com/example/demo/repository`
- `src/main/java/com/example/demo/service`

### 2. Create the Data Model (`Todo.java`)

The model represents your data. Create `src/main/java/com/example/demo/model/Todo.java`:

```java
package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String task;
    private boolean completed;
    // Add constructors, getters, and setters
}
```
- `@Entity`: Marks this class as a database table.
- `@Id` & `@GeneratedValue`: Define the primary key.

### 3. Create the Repository (`TodoRepository.java`)

The repository handles database operations. Create `src/main/java/com/example/demo/repository/TodoRepository.java`:

```java
package com.example.demo.repository;

import com.example.demo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
}
```
- Extending `JpaRepository` provides all necessary CRUD methods automatically.

### 4. Create the Service Layer (`TodoService.java`)

The service layer holds the business logic. Create `src/main/java/com/example/demo/service/TodoService.java`:

```java
package com.example.demo.service;

import com.example.demo.model.Todo;
import com.example.demo.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TodoService {
    @Autowired
    private TodoRepository todoRepository;

    // Methods for getAll, getById, create, update, delete...
}
```
- `@Service`: Defines this as a service component.
- `@Autowired`: Injects the `TodoRepository`.

### 5. Create the Controller (`TodoController.java`)

The controller exposes your API endpoints. Create `src/main/java/com/example/demo/controller/TodoController.java`:

```java
package com.example.demo.controller;

import com.example.demo.model.Todo;
import com.example.demo.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {
    @Autowired
    private TodoService todoService;

    // Methods for GET, POST, PUT, DELETE endpoints...
}
```
- `@RequestMapping`: Sets a base path for all API routes in this controller.

---

## Part 3: Design Patterns Used

This project uses several common and powerful software design patterns that are central to the Spring framework.

### 1. Layered Architecture

This project follows a **Layered Architecture**, which separates the code into distinct layers, each with a specific responsibility. This is a variant of the Model-View-Controller (MVC) pattern, adapted for REST APIs.
- **Controller Layer (`TodoController`)**: The presentation layer. Its only job is to handle incoming HTTP requests, delegate the business logic to the service layer, and return an HTTP response. It does not contain any business logic itself.
- **Service Layer (`TodoService`)**: The business logic layer. It coordinates the application's logic, performs calculations, and calls the repository layer to fetch or persist data. It keeps the controller clean and focused.
- **Repository/Data Access Layer (`TodoRepository`)**: The persistence layer. Its responsibility is to communicate with the database. It abstracts the data source, so the rest of the application doesn't need to know how data is stored.

### 2. Repository Pattern

The **Repository Pattern** is used to decouple the business logic from the data access logic.
- `TodoRepository` is an interface that extends `JpaRepository`. By doing this, Spring Data JPA automatically provides a complete implementation with methods like `findAll()`, `findById()`, and `save()`.
- Our service layer (`TodoService`) depends on the `TodoRepository` interface, not a concrete implementation. This makes our code more modular and easier to test, as we can provide a "mock" repository during testing.

### 3. Dependency Injection (DI) and Inversion of Control (IoC)

**Inversion of Control (IoC)** is a principle where the control of object creation and management is transferred from your code to a container or framework (the Spring IoC container). **Dependency Injection (DI)** is the primary pattern used to achieve IoC.
- Instead of creating objects manually (e.g., `TodoService service = new TodoService()`), we let Spring do it for us.
- We use the `@Autowired` annotation to tell Spring to "inject" an instance of a required class (a dependency). For example, the `TodoRepository` is injected into the `TodoService`, and the `TodoService` is injected into the `TodoController`.
- This makes our code loosely coupled, as components don't create their own dependencies. It simplifies configuration and improves modularity.