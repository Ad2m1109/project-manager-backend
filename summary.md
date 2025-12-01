# Summary and Tutorial: Building a Spring Boot Backend

This document first outlines the basic steps to turn a minimal Spring Boot application into a web backend, and then provides a full tutorial for building a "Todo" REST API. It concludes with an explanation of the key software design patterns and the security implementation used in the project.

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

---

## Part 4: Securing the API with Spring Security and JWT

This project uses Spring Security with JSON Web Tokens (JWT) to secure the REST API. This is a standard approach for modern web applications that ensures only authenticated users can access protected resources.

### 1. Authentication Flow Overview

1.  **Login**: A user sends their email and password to the `/auth/login` endpoint.
2.  **Token Generation**: The server validates the credentials. If they are correct, it generates a JWT and sends it back to the user.
3.  **Authenticated Requests**: For all subsequent requests to protected endpoints, the user must include the JWT in the `Authorization` header (e.g., `Authorization: Bearer <token>`).
4.  **Token Validation**: A server-side filter intercepts each request, validates the JWT from the header, and if the token is valid, it grants access to the requested resource.

### 2. Core Security Components

#### Step 1: Add Security Dependencies

First, add the dependencies for Spring Security and JWT to your `pom.xml`:

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT Support -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

#### Step 2: Configure Spring Security (`SecurityConfig.java`)

This is the central point for security configuration.

```java
// In src/main/java/com/example/demo/config/SecurityConfig.java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // ... beans are injected ...

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults()) // Apply CORS configuration
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/swagger-ui/**").permitAll() // Public endpoints
                .anyRequest().authenticated() // All other requests need authentication
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Add our custom JWT filter

        return http.build();
    }
}
```
- **`@EnableWebSecurity`**: Enables Spring's web security support.
- **`.csrf(AbstractHttpConfigurer::disable)`**: CSRF protection is not needed for stateless REST APIs that use tokens.
- **`.authorizeHttpRequests(...)`**: Defines which URL paths are public and which require authentication.
- **`.sessionManagement(...)`**: Configures session management to be `STATELESS`, as we are not using HTTP sessions.
- **`.addFilterBefore(...)`**: Inserts our custom `JwtAuthenticationFilter` into the security filter chain before the standard username/password filter.

#### Step 3: Implement the JWT Filter (`JwtAuthenticationFilter.java`)

This filter runs for every request and is responsible for validating the JWT.

```java
// In src/main/java/com/example/demo/security/JwtAuthenticationFilter.java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // ... JwtUtil and UserDetailsService are injected ...

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // If no token, continue to the next filter
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail = jwtUtil.extractUsername(jwt);

        // If token is valid, set the authentication in the security context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtUtil.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```
- It extracts the token from the `Authorization` header.
- It uses `JwtUtil` to validate the token and extract the user's email.
- If the token is valid, it creates an `Authentication` object and places it in the `SecurityContextHolder`, effectively authenticating the user for the duration of the request.

#### Step 4: Create the JWT Utility (`JwtUtil.java`)

This class handles the logic for creating and validating JWTs.

```java
// In src/main/java/com/example/demo/security/JwtUtil.java
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}") // Injects the secret key from application.properties
    private String secretKey;

    public String generateToken(UserDetails userDetails) {
        // ... logic to build and sign the JWT ...
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
    // ... other helper methods to extract claims, check expiration, etc. ...
}
```
- It uses a secret key to sign and verify tokens. This key should be stored securely and not hardcoded. We use `@Value("${app.jwt.secret}")` to inject it from `application.properties`.

#### Step 5: Configure Authentication Providers (`ApplicationConfig.java`)

This class provides the necessary beans for the authentication process.

```java
// In src/main/java/com/example/demo/config/ApplicationConfig.java
@Configuration
public class ApplicationConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        // Loads a user from the database by email
        return username -> repository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Provides a password encoder for hashing passwords
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Binds the UserDetailsService and PasswordEncoder together
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    // ... other beans like AuthenticationManager ...
}
```

#### Step 6: Create Authentication Endpoints (`AuthController.java`)

Finally, the `AuthController` provides the public endpoints for registration and login.

```java
// In src/main/java/com/example/demo/controller/AuthController.java
@RestController
@RequestMapping("/auth")
public class AuthController {
    // ... beans are injected ...

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // ... logic to create a new user, hash their password, and save them ...
        // Generate a JWT for the new user
        String token = jwtUtil.generateToken(savedUser);
        return ResponseEntity.ok(new AuthResponse(token, savedUser, "..."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Authenticate the user with the AuthenticationManager
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // If authentication is successful, generate and return a JWT
        AppUser user = appUserService.findByEmail(request.getEmail()).orElseThrow(...);
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user, "..."));
    }
}
```
- The `/login` endpoint uses the `AuthenticationManager` to validate the user's credentials. If successful, it generates a JWT. If not, it throws an exception, which results in a `401 Unauthorized` response.
