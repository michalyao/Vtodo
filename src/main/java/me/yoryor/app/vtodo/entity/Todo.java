package me.yoryor.app.vtodo.entity;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@DataObject(generateConverter = true)
public class Todo {
  private String id;
  private String title;
  private Boolean completed;
  private Integer order;
  private String url;

  public Todo() {
  }

  public Todo(String id, String title, Boolean completed, Integer order, String url) {
    this.id = id;
    this.title = title;
    this.completed = completed;
    this.order = order;
    this.url = url;
  }

  public Todo(Todo other) {
    this(other.id, other.title, other.completed, other.order, other.url);
  }

  public Todo(JsonObject object) {
    TodoConverter.fromJson(object, this);
  }

  public Todo(String jsonStr) {
    TodoConverter.fromJson(new JsonObject(jsonStr), this);
  }

  public JsonObject toJson() {
    JsonObject jsonObject = new JsonObject();
    TodoConverter.toJson(this, jsonObject);
    return jsonObject;
  }

  private <T> T getOrElse(T value, T defaultValue) {
    return value == null ? defaultValue: value;
  }

  public Todo merge(Todo todo) {
    return new Todo(id,
        getOrElse(todo.title, title),
        getOrElse(todo.completed, completed),
        getOrElse(todo.order, order),
        url
    );
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Boolean getCompleted() {
    return completed;
  }

  public void setCompleted(Boolean completed) {
    this.completed = completed;
  }

  public Integer getOrder() {
    return order;
  }

  public void setOrder(Integer order) {
    this.order = order;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Todo todo = (Todo) o;
    return id == todo.id && Objects.equals(title, todo.title)
        && Objects.equals(completed, todo.completed) && Objects.equals(order, todo.order)
        && Objects.equals(url, todo.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, completed, order, url);
  }

  @Override
  public String toString() {
    return "Todo => {" + "id=" + id + ", title='" + title + '\'' + ", completed=" + completed
        + ", order=" + order + ", url='" + url + '\'' + '}';
  }
}
