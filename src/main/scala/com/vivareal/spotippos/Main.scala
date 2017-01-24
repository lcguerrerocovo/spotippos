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

  def getProperty: Endpoint[PropertyWithProvince] = get("properties" :: int) { id: Int =>
    Ok(Property.propertyMapById.get(id) match { case Some(property) => Property.provinces(property) case None => throw new Exception()})
  }

  def createProperty: Endpoint[Property]
        = post("properties" :: jsonBody[Int => Property].map(_(Property.nextId))
                .should("have coordinates in Range")
                  {x => x.lat >= 0 && x.lat <= 1400 && x.long >= 0 && x.long <= 1000}
                .should("have rooms in range")
                  {x => x.beds >= 1 && x.beds <= 5}
                .should("have bathrooms in range")
                  {x => x.baths >= 0 && x.baths <= 4}
                .should("have area in range")
                  {x => x.squareMeters >= 20 && x.squareMeters <= 240}) { p: Property =>
    val id = Property.nextId
    Property.propertyMapById += id -> p;
    Property.propertyMapByCoordinate += (p.lat,p.long) -> id;
    Created(p)
  }

  def inRange(low: Int, high: Int, dimension: Char)
        = ValidationRule[Int]("be in Range of " + dimension + " coordinates [" + low + "," + high + "]")
            {x => x >= low && x <= high}

  def inRangeX = inRange(0,1400,'x')

  def inRangeY = inRange(0,1000,'y')

  def searchPropertiesWithArea: Endpoint[Properties]
        = get("properties" :: param("ax").as[Int].should(inRangeX) ::
              param("ay").as[Int].should(inRangeY) ::
              param("bx").as[Int].should(inRangeX) ::
              param("by").as[Int].should(inRangeY)) { (ax: Int, ay: Int, bx: Int, by: Int) =>
    val properties = Property.rangeTree.search((ax, bx), (ay, by))
      .map(Property.getPropertyByCoordinates(_))
    Ok(Properties(properties.length, properties))
  }

  val api: Service[Request, Response] = (
    getProperty :+: searchPropertiesWithArea :+: createProperty
    ).handle({
    case e: Exception => NotFound(e)
  }).toServiceAs[Application.Json]

  def main(): Unit = {
    log.info("Spotippos server running")
    Property.readPropertyFile

    val server = Http.server
      .withStatsReceiver(statsReceiver)
      .serve(s":${port()}", api)

    onExit { server.close() }

    Await.ready(adminHttpServer)
  }
}