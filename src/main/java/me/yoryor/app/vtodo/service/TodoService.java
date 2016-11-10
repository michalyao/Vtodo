package me.yoryor.app.vtodo.service;

import io.vertx.core.Future;
import me.yoryor.app.vtodo.entity.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoService {

  /**
   * Init database service.
   * @return
   */
  Future<Boolean> initDB();

  /**
   * Insert a new todo.
   * @param todo
   * @return
   */
  Future<Boolean> insert(Todo todo);

  /**
   * List all todo.
   * @return
   */
  Future<List<Todo>> getTodoList();

  /**
   * Get todo by id.
   * @param id
   * @return
   */
  Future<Optional<Todo>> getTodoById(String id);

  /**
   * Update a todo.
   * @param id
   * @param newTodo
   * @return
   */
  Future<Todo> updateTodo(String id, Todo newTodo);

  /**
   * Delete a todo by id.
   * @param id
   * @return
   */
  Future<Boolean> deleteTodoById(String id);

  /**
   * Delete all todo.
   * @return
   */
  Future<Boolean> deleteTodoList();
}
