package com.vertx.api

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.Router

import scala.util.{Failure, Success}

object Application extends App {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(new ServerVertical)


  class ServerVertical extends ScalaVerticle {
    override def start(): Unit = {
      val port: Int = 8080
      val router = Router.router(vertx)
      router.get("/hello").handler(req => {
        req.response().putHeader("content-type", "application/json")
        req.response().end("{\"message\":\"Hello world\"}")

      })
      vertx.createHttpServer().requestHandler(router.accept)
        .listenFuture(port, "0.0.0.0").onComplete {
        case Success(result) => println(s"Server is running on port $port")
        case Failure(exception) => println(s"Exception: ${exception.getMessage}")
      }
    }
  }

}
