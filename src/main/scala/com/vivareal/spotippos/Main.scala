package com.vivareal.spotippos

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.twitter.app.Flag
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import com.twitter.io.{Reader, Buf}
import io.circe.parser._
import scala.io.Source
import cats.syntax.either._

import java.io.FileInputStream
import io.circe.Json
import io.circe.Decoder

object Main extends TwitterServer {

  val (rangeTree,propertyMapById,propertyMapByCoordinate) = readPropertyFile

  val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

  def readPropertyFile: (RangeTree,Map[Int,Property],Map[(Int,Int),Property]) = {
    val stream = new FileInputStream("properties.json")
    val json = try {  parse(Source.fromInputStream(stream).mkString).getOrElse(Json.Null) } finally { stream.close() }
    val propertyList: Decoder.Result[List[Property]] = json.hcursor.downField("properties").as[List[Property]]
    (new RangeTree(propertyList.getOrElse(List()).map(x => (x.lat,x.long)).toArray),
      propertyList.getOrElse(List()).map(x => x.id -> x).toMap,
      propertyList.getOrElse(List()).map(x => (x.lat,x.long) -> x).toMap)
  }

  def getProperty: Endpoint[Property] = get("properties" :: int) { id: Int =>
    Ok(propertyMapById.get(id) match { case Some(property) => property case None => throw new Exception()})
  }

  def searchPropertiesWithArea: Endpoint[Properties]
        = get("properties" :: param("ax").as[Int] :: param("ay").as[Int] ::
            param("bx").as[Int] :: param("by").as[Int]) { (ax: Int, ay: Int, bx: Int, by: Int) =>
    val properties = rangeTree.search((ax, bx), (ay, by))
      .map(propertyMapByCoordinate.get(_))
      .collect { case Some(property) => property}
    Ok(Properties(properties.length, properties))
  }

  val api: Service[Request, Response] = (
    getProperty :+: searchPropertiesWithArea
    ).handle({
    case e: Exception => NotFound(e)
  }).toServiceAs[Application.Json]

  def main(): Unit = {
    log.info("Spotippos server running")

    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }
}