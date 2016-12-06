package me.yoryor.app.vtodo.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import me.yoryor.app.vtodo.entity.Todo;
import me.yoryor.app.vtodo.service.TodoService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TodoServiceSQLImpl implements TodoService {
  private final Vertx vertx;
  private final JsonObject config;
  private final JDBCClient client;

  private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `todo` \n" + "(\n"
      + "  `id` INT(11) NOT NULL AUTO_INCREMENT,\n" + "  `title` VARCHAR(255) DEFAULT NULL,\n"
      + "  `completed` tinyint(1) DEFAULT NULL,\n" + "  `order` INT(11) DEFAULT NULL,\n"
      + "  `url` VARCHAR(255) DEFAULT NULL,\n" + "  PRIMARY KEY (`id`)\n" + ")";
  private static final String INSERT_TODO = "INSERT INTO `todo` (`id`, `title`, `completed`, `order`, `url`) \n"
      + "    VALUES (?, ?, ?, ?, ?);";
  private static final String QUERY_TODO = "SELECT * FROM todo WHERE id = ?";
  private static final String QUERY_ALL_TODO = "SELECT * FROM todo";
  private static final String UPDATE_TODO = "UPDATE `todo` SET `id` = ?, `title` = ?, `completed` = ?, `order` = ?, `url` = ? WHERE `id` = ?";
  private static final String DELETE_TODO = "DELETE FROM `todo` WHERE `id` = ?";
  private static final String DELETE_ALL_TODO = "DELETE FROM `todo`";

  public TodoServiceSQLImpl(JsonObject config) {
    this(Vertx.vertx(), config);
  }
  public TodoServiceSQLImpl(Vertx vertx, JsonObject config) {
    this.vertx = vertx;
    this.config = config;
    this.client = JDBCClient.createShared(vertx, config);
  }

  private Handler<AsyncResult<SQLConnection>> connHandler(Future future, Handler<SQLConnection> handler) {
    return conn -> {
      if (conn.succeeded()) {
        final SQLConnection connection = conn.result();
        handler.handle(connection);
      } else {
        future.fail(conn.cause());
      }
    };
  }
  @Override
  public Future<Boolean> initDB() {
    Future<Boolean> result = Future.future();
    client.getConnection(connHandler(result, conn -> {
      conn.execute(CREATE_TABLE, created -> {
        if (created.succeeded()) {
          result.complete(true);
        } else {
          result.fail(created.cause());
        }
      });
      conn.close();
    }));
    return result;
  }

  @Override
  public Future<Boolean> insert(Todo todo) {
    Future<Boolean> result = Future.future();
    client.getConnection(connHandler(result, conn -> {
      conn.updateWithParams(INSERT_TODO,
          new JsonArray().add(todo.getId())
              .add(todo.getTitle())
              .add(todo.getCompleted())
              .add(todo.getOrder())
              .add(todo.getUrl()), res -> {
            if (res.succeeded()) {
              result.complete(true);
            } else {
              result.fail(res.cause());
            }
          });
      conn.close();
    }));
    return result;
  }

  @Override
  public Future<List<Todo>> getTodoList() {
    Future<List<Todo>> result = Future.future();
    client.getConnection(connHandler(result, conn -> {
      conn.query(QUERY_ALL_TODO, res -> {
        if (res.succeeded()) {
          List<Todo> todos = res.result().getRows().stream()
              .map(Todo::new).collect(Collectors.toList());
          result.complete(todos);
        } else {
          result.fail(res.cause());
        }
      });
      conn.close();
    }));
    return result;
  }

  @Override
  public Future<Optional<Todo>> getTodoById(String id) {
    Future<Optional<Todo>> result = Future.future();
    client.getConnection(connHandler(result, conn -> {
      conn.queryWithParams(QUERY_TODO, new JsonArray().add(id) ,res -> {
        if (res.succeeded()) {
          List<JsonObject> rows = res.result().getRows();
          if (rows.isEmpty() || rows == null) {
            result.complete(Optional.empty());
          } else {
            result.complete(Optional.of(new Todo(rows.get(0))));
          }
        }
      });
      conn.close();
    }));
    return result;
  }

  @Override
  public Future<Todo> updateTodo(String id, Todo newTodo) {
    Future<Todo> result = Future.future();
    client.getConnection(connHandler(result, conn -> {
      this.getTodoById(id).setHandler(r -> {
        if (r.succeeded()) {
          Optional<Todo> oldTodo = r.result();
          if (!oldTodo.isPresent()) {
            result.complete(null);
            return;
          }
          Todo fnTodo = oldTodo.get().merge(newTodo);
          String updateId = oldTodo.get().getId();
          conn.updateWithParams(UPDATE_TODO, new JsonArray().add(updateId)
          .add(fnTodo.getTitle()).add(fnTodo.getOrder()).add(fnTodo.getCompleted()).add(fnTodo.getUrl()), res -> {
            if (res.succeeded()) {
              result.complete(fnTodo);
            } else {
              result.fail(res.cause());
            }
          });
        } else {
          result.fail(r.cause());
        }
      });
      conn.close();
    }));
    return result;
  }

  @Override
  public Future<Boolean> deleteTodoById(String id) {
    Future<Boolean> result = Future.future();
    client.getConnection(connHandler(result, conn -> {
      conn.updateWithParams(DELETE_TODO, new JsonArray().add(id), r -> {
        if (r.succeeded()) {
          result.complete(true);
        } else {
          result.complete(false);
        }
      });
      conn.close();
    }));
    return result;
  }

  @Override
  public Future<Boolean> deleteTodoList() {
    Future<Boolean> result = Future.future();
    client.getConnection(connHandler(result, conn -> {
      conn.update(DELETE_ALL_TODO, res -> {
        if (res.succeeded()) {
          result.complete(true);
        } else {
          result.complete(false);
        }
      });
      conn.close();
    }));
    return result;
  }
}
