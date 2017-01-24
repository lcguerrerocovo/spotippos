package com.vivareal.spotippos

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.server.TwitterServer
import com.twitter.util.Await
import com.twitter.app.Flag
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._


object Main extends TwitterServer {

  val port: Flag[Int] = flag("port", 8081, "TCP port for HTTP server")

  def getProperty: Endpoint[Property] = get("properties" :: int) { id: Int =>
    Ok(Property.propertyMapById.get(id) match { case Some(property) => property case None => throw new Exception()})
  }

  def createProperty: Endpoint[Property]
        = post("properties" :: jsonBody[Int => Property].map(_(Property.nextId)).should("be in Range")
                {x => x.lat >= 0 && x.lat <= 1400 && x.long >= 0 && x.long <= 1000}) { p: Property =>
    Property.propertyMapById += (Property.propertyMapById.keys.max + 1) -> p;
    Property.propertyMapByCoordinate += (p.lat,p.long) -> p;
    Created(p)
  }

  def searchPropertiesWithArea: Endpoint[Properties]
        = get("properties" :: param("ax").as[Int] :: param("ay").as[Int] ::
            param("bx").as[Int] :: param("by").as[Int]) { (ax: Int, ay: Int, bx: Int, by: Int) =>
    val properties = Property.rangeTree.search((ax, bx), (ay, by))
      .map(Property.propertyMapByCoordinate.get(_))
      .collect { case Some(property) => property}
    Ok(Properties(properties.length, properties))
  }

  val api: Service[Request, Response] = (
    getProperty :+: searchPropertiesWithArea :+: createProperty
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