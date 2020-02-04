package com.vertx.api

import java.nio.file.{Files, Path, Paths, StandardCopyOption}

import com.jsoniter.output.JsonStream
import com.vertx.api.model.Response
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Vertx
import io.vertx.scala.ext.web.handler.BodyHandler
import io.vertx.scala.ext.web.{FileUpload, Router, RoutingContext}

import scala.util.{Failure, Properties, Success}

object Application extends App {
  val vertx = Vertx.vertx()
  val uploadFolder = Properties.envOrNone("UPLOAD_PATH")
  vertx.deployVerticle(new ServerVertical)
  sys.addShutdownHook(() => vertx.close())

  class ServerVertical extends ScalaVerticle {
    def rename(source: String, newFileName: String): Path = {
      Files.move(
        Paths.get(source),
        Paths.get(
          (source.split("/").dropRight(1) :+ newFileName).mkString("/")
        ),
        StandardCopyOption.REPLACE_EXISTING
      )
    }

    def validateFile(fOpt: Option[FileUpload]): Either[String, Boolean] = {
      val MAX_SIZE: Long = 1024 * 1024
      val ACCEPTED_CONTENT_TYPES = List("application/CSV")
      if (fOpt.isEmpty) return Left("Invalid file uploaded")
      if (fOpt.get.size > MAX_SIZE) return Left("File size exceeded")
      if (!ACCEPTED_CONTENT_TYPES.contains(fOpt.get.contentType)) return Left("Content type not accepted")
      Right(true)
    }

    /**
     * Return a file path to client
     *
     * @param req
     */
    def uploadHandler(req: RoutingContext): Unit = {
      val fileOpt = req.fileUploads().headOption
      validateFile(fileOpt) match {
        case Left(s) =>
          req.response().putHeader("content-type", "application/json")
          jError(req, new Throwable(s))
        case Right(_) =>
          val filePath = rename(fileOpt.get.uploadedFileName, fileOpt.get.fileName)
          req.response().putHeader("content-type", "application/json")
          req.response().end("{\"filePath\": \"" + filePath + "\"}")
      }
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
            .setUploadsDirectory(uploadFolder.get)
        )
      router
        .post("/upload")
        .handler(uploadHandler)
      router
        .post("/result")
        .handler(req => {
          req.response.putHeader("content-type", "application/json")
          req.response.end("{\"message\":\"Result OK\"}")
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
