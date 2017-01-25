package com.vivareal.spotippos

import org.scalatest.{FlatSpec, Matchers}
import io.finch.Input
import com.twitter.finagle.http.Status
import org.scalacheck.{Arbitrary, Gen}

/**
  * Created by luisguerrero
  */
class MainSpec extends FlatSpec with Matchers {

  // here we would test the server request/response behaviour

  /*import Main.createProperty

  def genValidPropertyWithoutId(: Gen[Property] = for {
    //id <- Gen.choose(Int.MinValue, Int.MaxValue)
    t <- Gen.alphaStr
    p <- Gen.choose(Int.MinValue, Int.MaxValue)
    d <- Gen.alphaStr
    lat <- Gen.choose(0, 1400)
    lon <- Gen.choose(0, 1000)
    be <- Gen.choose(1, 5)
    ba <- Gen.choose(1, 4)
    sq <- Gen.choose(20, 240)
  } yield Property(id,t,p,d,lat,lon,be,ba,sq)


  it should "give create a property if" in {
    check { (Int => Property)
    createProperty(Input.post("/properties"))
      .withBody[Application.Json](todoWithoutId, Some(StandardCharsets.UTF_8))
  }

  */
}
