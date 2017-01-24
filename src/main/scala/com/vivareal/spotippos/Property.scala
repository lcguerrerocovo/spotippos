package com.vivareal.spotippos

import io.circe.parser._
import cats.syntax.either._

import java.io.FileInputStream
import io.circe.Json
import io.circe.Decoder

import scala.io.Source

import io.circe.generic.auto._


case class Property(id: Int, title: String, price: Int, description: String,
                    lat: Int, long: Int, beds: Int, baths: Int,
                    squareMeters: Int)

object Property {

  val (rangeTree,
  propertyMapById,
  propertyMapByCoordinate) = readPropertyFile

  def nextId = Property.propertyMapById.keys.max + 1

  def readPropertyFile = {
    val stream = new FileInputStream("properties.json")
    val json = try { parse(Source.fromInputStream(stream).mkString).getOrElse(Json.Null) } finally { stream.close() }
    val propertyList: Decoder.Result[List[Property]] = json.hcursor.downField("properties").as[List[Property]]
    (new RangeTree(propertyList.getOrElse(List()).map(x => (x.lat,x.long)).toArray),
      scala.collection.mutable.Map(propertyList.getOrElse(List()).map(x => x.id -> x).toMap.toSeq: _*),
      scala.collection.mutable.Map(propertyList.getOrElse(List()).map(x => (x.lat,x.long) -> x).toMap.toSeq: _*))
  }
}

case class PropertyNotFound(id: Int) extends Exception {
  override def getMessage: String = s"Property(${id.toString}) not found."
}

