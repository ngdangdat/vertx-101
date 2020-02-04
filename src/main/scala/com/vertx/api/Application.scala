package com.vertx.api

import com.jsoniter.output.JsonStream
import com.vertx.api.model.Response
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.{Router, RoutingContext}
import io.vertx.scala.ext.web.handler.BodyHandler
import java.nio.file.{Files, Paths, StandardCopyOption}

import scala.util.{Failure, Success}

object Application extends App {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(new ServerVertical)
  sys.addShutdownHook(() => vertx.close())

  class ServerVertical extends ScalaVerticle {
    def rename(source: String, newFileName: String) = {
      Files.move(
        Paths.get(source),
        Paths.get(
          List(source.split("/").dropRight(1).mkString("/"), newFileName)
            .mkString("/")
        ),
        StandardCopyOption.REPLACE_EXISTING
      )
    }
    def uploadHandler(req: RoutingContext) = {
      val fileOpt = req.fileUploads().headOption
      if (fileOpt.isDefined) {
        val file = fileOpt.get
        val saved = rename(file.uploadedFileName, file.fileName)
      }
      req.response().putHeader("content-type", "application/json")
      req.response().end("{\"message\":\"Upload OK\"}")
    }

    override def start(): Unit = {
      val port: Int = 8080
      val router = Router.router(vertx)
      router
        .get("/")
        .handler(req => {
          req.response().putHeader("content-type", "application/json")
          req.response().end("{\"message\":\"Validator core API\"}")
        })
      router
        .route("/upload")
        .handler(
          BodyHandler.create
            .setUploadsDirectory("/Users/dat-nguyen/Code/upload")
        )
      router
        .post("/upload")
        .handler(uploadHandler)
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
