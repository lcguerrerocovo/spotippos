package com.vivareal.spotippos

import io.circe.parser._
import cats.syntax.either._

import java.io.FileInputStream
import io.circe.Json

import scala.collection.mutable
import scala.io.Source

import io.circe.generic.auto._

/**
  * Created by luisguerrero
  */
case class Property(id: Int, title: String, price: Int, description: String,
                    lat: Int, long: Int, beds: Int, baths: Int,
                    squareMeters: Int)

case class PropertyWithProvince(id: Int, title: String, price: Int, description: String,
                    lat: Int, long: Int, beds: Int, baths: Int,
                    squareMeters: Int, provinces: List[String])

object Property {

  var propertyMapById: mutable.Map[Int, Property] = null
  var propertyMapByCoordinate: mutable.Map[(Int,Int), Int] = null
  var provinces: List[Province] = null
  var propertiesToIndex: List[Int] = Nil
  var rangeTree: RangeTree = null

  def readPropertyFile = {
    val propertyList = fileToJsonDecoder[List[Property]](
      "properties.json", ((x: Json) => x.hcursor.downField("properties")
                                        .as[List[Property]].getOrElse(List())))
    val provinceJson = fileToJsonDecoder[Json]("provinces.json", x => x)
    val provinceNameList = provinceJson.hcursor.fieldSet.getOrElse(Set())
    provinces = provinceNameList.map(x =>
      ((provinceJson.hcursor.downField(x).as[String => Province].map(y => y(x)).getOrElse(null)))).toList

    propertyMapById = scala.collection.concurrent.TrieMap(propertyList.map(x => x.id -> x).toMap.toSeq: _*)
    propertyMapByCoordinate = scala.collection.concurrent.TrieMap(propertyList.map(x => (x.lat,x.long) -> x.id).toMap.toSeq: _*)
  }

  // range tree will only be rebuilt if there were properties added since it was last built
  def buildRangeTree = {
    if(!propertiesToIndex.isEmpty) {
      println("building range tree for optimized coordinate grid search")
      rangeTree = new RangeTree(propertyMapById.toArray.map(x => (x._2.lat, x._2.long)))
    }
    propertiesToIndex = Nil
  }

  def fileToJsonDecoder[T](filename: String, transform: Json => T): T = {
    val stream = new FileInputStream(filename)
    val json = try { parse(Source.fromInputStream(stream).mkString).getOrElse(Json.Null) } finally { stream.close() }
    transform(json)
  }

  def nextId = Property.propertyMapById.keys.max + 1

  def inProvince(xy: (Int,Int), p: Province): Boolean =
    xy._1 >= p.boundaries.upperLeft.x &&
      xy._1 <= p.boundaries.bottomRight.x &&
      xy._2 <= p.boundaries.upperLeft.y &&
      xy._2 >= p.boundaries.bottomRight.y

  // this is not optimized but since provinces are constant this will only affect
  // each property returned by a constant factor
  def provinces(p: Property): PropertyWithProvince = p match {
    case Property(id,title,price,description,lat,
                  long,beds,baths,squareMeters) =>
      PropertyWithProvince(id,title,price,description,lat,
        long,beds,baths,squareMeters,
        (for {
         i <- 0 until provinces.length
         if inProvince((lat,long),provinces(i))
        } yield(provinces(i).name)).toList)
  }

  def addProperty(property: Property) = {
    this.synchronized {
      Property.propertyMapById += property.id -> property;
      Property.propertyMapByCoordinate += (property.lat, property.long) -> property.id;
      propertiesToIndex = property.id :: propertiesToIndex
    }
    property
  }

  // these two functions here could be simplified, don't know how yet
  def getPropertyByIdWithException(id: Int): PropertyWithProvince = {
    propertyMapById.get(id) match {
      case Some(property) => Property.provinces(property)
      case None => throw new Exception()
    }
  }

  def getPropertyById(id: Int): PropertyWithProvince = {
    propertyMapById.get(id) match {
      case Some(property) => Property.provinces(property)
    }
  }

  def getPropertyByCoordinates(xy: (Int,Int)): PropertyWithProvince = {
    getPropertyById(propertyMapByCoordinate.get(xy) match { case Some(id) => id })
  }
}

case class Properties (foundProperties: Int, properties: List[PropertyWithProvince])

case class Province(name: String, boundaries: Boundary)
case class Boundary(upperLeft: Coordinates, bottomRight: Coordinates)
case class Coordinates(x: Int, y: Int)
case class PropertyNotFound(id: Int) extends Exception {
  override def getMessage: String = s"Property(${id.toString}) not found."
}

