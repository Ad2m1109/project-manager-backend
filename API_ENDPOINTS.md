# API Endpoints

This document outlines the available API endpoints for the application.

## HelloController

-   **GET /hello**
    -   Returns a simple "Hello, World!" message.

## TodoController

Base Path: `/api/todos`

-   **GET /api/todos**
    -   Retrieves a list of all To-Do items.
-   **GET /api/todos/{id}**
    -   Retrieves a single To-Do item by its ID.
    -   `{id}`: The unique identifier of the To-Do item.
-   **POST /api/todos**
    -   Creates a new To-Do item.
    -   Requires a JSON request body representing the To-Do item.
-   **PUT /api/todos/{id}**
    -   Updates an existing To-Do item by its ID.
    -   `{id}`: The unique identifier of the To-Do item to update.
    -   Requires a JSON request body representing the updated To-Do item.
-   **DELETE /api/todos/{id}**
    -   Deletes a To-Do item by its ID.
    -   `{id}`: The unique identifier of the To-Do item to delete.
