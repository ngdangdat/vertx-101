package com.vertx.api.model

import com.jsoniter.annotation.JsonProperty
import scala.collection.JavaConverters._

class Response[A](success: Boolean,
                  @JsonProperty() error: String,
                  @JsonProperty() data: java.util.List[A]) {
  def getError: String = error
  def getData: java.util.List[A] = data
}

object Response {
  def apply[A](success: Boolean, error: String, data: Option[List[A]]) =
    new Response[A](
      success,
      error,
      if (data.isDefined) data.get.asJava else null
    )
  def success[A](data: List[A]): Response[A] =
    Response[A](success = true, error = null, Some(data))
  def error(err: String): Response[Nothing] =
    Response[Nothing](success = false, err, None)
}
