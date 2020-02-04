package com.vertx.api

import com.jsoniter.output.JsonStream
import com.vertx.api.model.Response
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.{FileUpload, Router, RoutingContext}
import io.vertx.scala.ext.web.handler.BodyHandler

import scala.util.{Failure, Success}

object Application extends App {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(new ServerVertical)
  sys.addShutdownHook(() => vertx.close())

  class ServerVertical extends ScalaVerticle {
    def uploadHandler(req: RoutingContext): Unit = {}
    override def start(): Unit = {
      val port: Int = 8080
      val router = Router.router(vertx)
      router
        .get("/")
        .handler(req => {
          req.response().putHeader("content-type", "application/json")
          req.response().end("{\"message\":\"Validator core API\"}")
        })
      router.route.handler(BodyHandler.create())
      router
        .post("/upload")
        .handler(req => {
          val fileOpt = req.fileUploads().headOption
          if (fileOpt.isDefined) {
            println(s"Uploaded file name${fileOpt.get.uploadedFileName}")
          }
          req.response().putHeader("content-type", "application/json")
          req.response().end("{\"message\":\"Upload OK\"}")
        })
      router
        .post("/result")
        .handler(req => {
          req.response().putHeader("content-type", "application/json")
          req.response().end("{\"message\":\"Result OK\"}")
        })
      router
        .route("/*")
        .handler(req => jError(req, new Throwable("API path not found")))
      vertx
        .createHttpServer()
        .requestHandler(router.accept)
        .listenFuture(port, "0.0.0.0")
        .onComplete {
          case Success(_) => println(s"Server is running on port $port")
          case Failure(exception) =>
            println(s"Exception: ${exception.getMessage}")
        }
    }

    def jError(req: RoutingContext, throwable: Throwable): Unit =
      req
        .response()
        .end(JsonStream.serialize(Response.error(throwable.getMessage)))
    def jSuccess[A](req: RoutingContext, load: Response[A]): Unit =
      req.response.end(JsonStream.serialize(load))
  }
}
