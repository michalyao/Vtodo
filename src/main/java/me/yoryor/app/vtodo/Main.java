package me.yoryor.app.vtodo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import me.yoryor.app.vtodo.verticles.TodoVerticle;

/**
 * for test.
 */
public class Main extends AbstractVerticle {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new TodoVerticle());
  }
}
