package me.yoryor.app.vtodo.service.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import me.yoryor.app.vtodo.entity.Todo;
import me.yoryor.app.vtodo.service.TodoService;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TodoService implement using redis as db.
 */
public class TodoServiceImpl implements TodoService {

  private static final String REDIS_TODO_KEY = "vtodo";
  private final Vertx vertx;
  private final RedisOptions redisConfig;
  private final RedisClient redis;

  public TodoServiceImpl(RedisOptions redisConfig) {
    this(Vertx.vertx(), redisConfig);
  }

  public TodoServiceImpl(Vertx vertx, RedisOptions redisConfig) {
    this.vertx = vertx;
    this.redisConfig = redisConfig;
    this.redis = RedisClient.create(vertx, redisConfig);
  }

  @Override
  public Future<Boolean> initDB() {
    return insert(new Todo(UUID.randomUUID().toString(), "Todo Example...", false, 1, "todo/ex"));
  }

  @Override
  public Future<Boolean> insert(Todo todo) {
    Future<Boolean> result = Future.future();
    final String jsonStr = Json.encodePrettily(todo);
    redis.hset(REDIS_TODO_KEY, todo.getId(), jsonStr, res -> {
      if (res.succeeded()) {
        result.complete(true);
      } else {
        result.fail(res.cause());
      }
    });
    return result;
  }

  @Override
  public Future<List<Todo>> getTodoList() {
    Future<List<Todo>> result = Future.future();
    redis.hvals(REDIS_TODO_KEY, res -> {
      if (res.succeeded()) {
        result.complete(res.result()
            .stream()
            .map(todo -> new Todo((String) todo))
            .collect(Collectors.toList())
        );
      } else {
        result.fail(res.cause());
      }
    });
    return result;
  }

  @Override
  public Future<Optional<Todo>> getTodoById(String id) {
    Future<Optional<Todo>> result = Future.future();
    redis.hget(REDIS_TODO_KEY, id, res -> {
      if (res.succeeded()) {
        result.complete(Optional.ofNullable(
            res.result() == null ? null : new Todo(res.result())
        ));
      } else {
        result.fail(res.cause());
      }
    });
    return result;
  }

  @Override
  public Future<Todo> updateTodo(String id, Todo newTodo) {
    return this.getTodoById(id).compose(old -> {
      if (old.isPresent()) {
        Todo fnTodo = old.get().merge(newTodo);
        return this.insert(fnTodo)
            .map(r -> r ? fnTodo : null);
      } else {
        return Future.succeededFuture();
      }
    });
  }

  @Override
  public Future<Boolean> deleteTodoById(String id) {
    Future<Boolean> result = Future.future();
    redis.hdel(REDIS_TODO_KEY, id, res -> {
      if (res.succeeded()) {
        result.complete(true);
      } else {
        result.complete(false);
      }
    });
    return result;
  }

  @Override
  public Future<Boolean> deleteTodoList() {
    Future<Boolean> result = Future.future();
    redis.del(REDIS_TODO_KEY, res -> {
      if (res.succeeded()) {
        result.complete(true);
      } else {
        result.fail(res.cause());
      }
    });
    return result;
  }
}
