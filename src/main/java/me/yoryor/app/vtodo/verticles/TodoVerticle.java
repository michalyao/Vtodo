package me.yoryor.app.vtodo.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.EncodeException;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.RedisOptions;
import me.yoryor.app.vtodo.entity.Todo;
import me.yoryor.app.vtodo.service.TodoService;
import me.yoryor.app.vtodo.service.impl.TodoServiceImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class TodoVerticle extends AbstractVerticle {

  public static final String API_PREFIX = "/api/v1";
  /** API Route */
  public static final String API_GET = "/todos/:todoId";
  public static final String API_GET__ALL = "/todos";
  public static final String API_CREATE = "/todos";
  public static final String API_UPDATE = "/todos/:todoId";
  public static final String API_DELETE = "/todos/:todoId";
  public static final String API_DELETE_ALL = "/todos";

  /** Docker Link  Redis Service Env */
  public static final String DAOREDISADDR = "DB_PORT_6379_TCP_ADDR";
  public static final String DAOREDISPORT = "DB_PORT_6379_TCP_PORT";


  private static final Logger LOG = LoggerFactory.getLogger(TodoVerticle.class);
  private TodoService service;

  @Override
  public void start(Future<Void> startFuture) throws Exception {
    initRedis();

    Router router = Router.router(vertx);
    // CORS support
    Set<String> allowHeaders = new HashSet<>();
    allowHeaders.add("x-requested-with");
    allowHeaders.add("Access-Control-Allow-Origin");
    allowHeaders.add("origin");
    allowHeaders.add("Content-Type");
    allowHeaders.add("accept");
    Set<HttpMethod> allowMethods = new HashSet<>();
    allowMethods.add(HttpMethod.GET);
    allowMethods.add(HttpMethod.POST);
    allowMethods.add(HttpMethod.DELETE);
    allowMethods.add(HttpMethod.PATCH);

    router.route().handler(BodyHandler.create());
    router.route().handler(CorsHandler.create("*")
            .allowedHeaders(allowHeaders)
            .allowedMethods(allowMethods));

    router.get(API_PREFIX + API_GET).handler(this::handleGet);
    router.get(API_PREFIX + API_GET__ALL).handler(this::handleGetAll);
    router.post(API_PREFIX + API_CREATE).handler(this::handleCreate);
    router.patch(API_PREFIX + API_UPDATE).handler(this::handleUpdate);
    router.delete(API_PREFIX + API_DELETE).handler(this::handleDelete);
    router.delete(API_PREFIX + API_DELETE_ALL).handler(this::handleDeleteAll);

    vertx.createHttpServer()
        .requestHandler(router::accept)
        .listen(config().getInteger("http.port", 8888), config().getString("http.host", "localhost"), ar -> {
          if (ar.succeeded()) {
            startFuture.complete();
          } else {
            LOG.error("Verticle start fail!");
            startFuture.fail(ar.cause());
          }
        });

  }

  private void handleDeleteAll(RoutingContext routingContext) {
    service.deleteTodoList()
        .setHandler(deleteHandler(routingContext));
  }

  private void handleDelete(RoutingContext routingContext) {
    String todoID = routingContext.request().getParam("todoId");
    service.deleteTodoById(todoID)
        .setHandler(deleteHandler(routingContext));
  }

  private void handleUpdate(RoutingContext routingContext) {
    try {
      String todoID = routingContext.request().getParam("todoId");
      final Todo newTodo = new Todo(routingContext.getBodyAsString());
      if (todoID == null) {
        sendError(400, routingContext.response());
        LOG.error("Create todo Failed.");
        return;
      }
      service.updateTodo(todoID, newTodo)
          .setHandler(resultHandler(routingContext, serviceResult -> {
            if (serviceResult == null) {
              notFound(routingContext);
              LOG.error("Create todo Failed.");
            } else {
              final String jsonStr = Json.encodePrettily(serviceResult);
              routingContext.response()
                  .putHeader("content-type", "application/json")
                  .end(jsonStr);
              LOG.info("Update todo Succeeded.");
            }
          }));
    } catch (Exception e) {
      badRequest(routingContext);
    }

  }

  private void handleCreate(RoutingContext routingContext) {
    try {
      final Todo todo = wrapTodo(new Todo(routingContext.getBodyAsString()), routingContext);
      final String jsonStr = Json.encodePrettily(todo);
      service.insert(todo).setHandler(resultHandler(routingContext, insertResult -> {
        if (insertResult) {
          routingContext.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(jsonStr);
          LOG.info("Create todo Succeeded.");
        } else {
          serviceUnavailable(routingContext);
          LOG.error("Create todo failed.");
        }
      }));
    } catch (EncodeException e) {
      sendError(400, routingContext.response());
    }
  }

  private void handleGetAll(RoutingContext routingContext) {
    try {
      service.getTodoList().setHandler(resultHandler(routingContext, serviceResult -> {
        if (serviceResult == null || serviceResult.isEmpty()) {
          serviceUnavailable(routingContext);
          LOG.error("Get todo list failed.");
        } else {
          final String jsonStr = Json.encodePrettily(serviceResult);
          routingContext.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(jsonStr);
          LOG.info("Get todo list Succeeded.");
        }
      }));
    } catch (Exception e) {
      badRequest(routingContext);
    }
  }

  private void handleGet(RoutingContext routingContext) {
    try {
      String id = routingContext.request().getParam("todoId");
      if (id == null) {
        sendError(404, routingContext.response());
        LOG.error("Get a todo failed.");
        return;
      }
      service.getTodoById(id).setHandler(resultHandler(routingContext, serviceResult -> {
        if (!serviceResult.isPresent()) {
          notFound(routingContext);
          LOG.error("Get a todo failed.");
        } else {
          final String jsonStr = Json.encodePrettily(serviceResult.get());
          routingContext.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json")
              .end(jsonStr);
          LOG.error("Get a todo Succeeded.");
        }
      }));
    } catch (Exception e) {
      badRequest(routingContext);
    }
  }

  /** delete handler */
  private Handler<AsyncResult<Boolean>> deleteHandler(RoutingContext routingContext) {
    return res -> {
      if (res.succeeded()) {
        if (res.result()) {
          routingContext.response().setStatusCode(204).end();
          LOG.info("Delete Succeeded.");
        } else {
          serviceUnavailable(routingContext);
        }
      } else {
        serviceUnavailable(routingContext);
      }
    };
  }


  /** result handler */
  private <T> Handler<AsyncResult<T>> resultHandler(RoutingContext routingContext, Consumer<T> consumer) {
    return res -> {
      if (res.succeeded()) {
        consumer.accept(res.result());
      } else {
        serviceUnavailable(routingContext);
      }
    };
  }

  /** Error handle */
  private void sendError(int statusCode, HttpServerResponse response) {
    response.setStatusCode(statusCode).end();
  }

  private void notFound(RoutingContext context) {
    context.response().setStatusCode(404).end();
  }

  private void badRequest(RoutingContext context) {
    context.response().setStatusCode(400).end();
  }

  private void serviceUnavailable(RoutingContext context) {
    context.response().setStatusCode(503).end();
  }

  private void initRedis() {
    RedisOptions redisConfig = new RedisOptions();
    String daoHost = System.getenv(DAOREDISADDR);
    String daoPort = System.getenv(DAOREDISPORT);
    // Use link internal service
    if (daoHost != null && daoPort != null) {
      redisConfig
          .setHost(daoHost)
          .setPort(Integer.parseInt(daoPort));
    } else {
      redisConfig
          .setHost(config().getString("redis.host", "0.0.0.0"))
          .setPort(config().getInteger("redis.port", 6379));
    }
    LOG.info("redis config init:" + Json.encodePrettily(redisConfig));
    service = new TodoServiceImpl(vertx, redisConfig);
    service.initDB().setHandler(res -> {
      if (res.failed()) {
        LOG.error("Service init fail!", res.cause());
      }
    });
  }

  private Todo wrapTodo(Todo todo, RoutingContext routingContext) {
    todo.setId(UUID.randomUUID().toString());
    todo.setUrl(routingContext.request().absoluteURI() + "/" + todo.getId());
    return todo;
  }
}
