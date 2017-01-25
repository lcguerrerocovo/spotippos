package com.vivareal.spotippos

/**
  * Created by luisguerrero
  */
import org.scalatest._

class RangeTreeSpec extends FlatSpec with Matchers with PrivateMethodTester {

  behavior of "a rangeTree"

  val indexedPointsOnlyX = Set((1,1),(2,1),(3,1),(4,1),(5,1),
    (6,1),(7,1))

  val indexedPoints = Set((3,1),(3,4),(3,6),(7,1),(7,4),(6,1),(6,5),(5,1),(5,6),(5,3),(4,1),(4,5))

  val simpleRangeTree = new RangeTree(indexedPointsOnlyX.toArray)

  val rangeTree = new RangeTree(indexedPoints.toArray)

  def checkLeafValues(range: (Int,Int), node: Tree, f: ((Int,Int),(Int,Int)) => Boolean): List[Boolean] = node match {
    case Inner(left, right, xy, innerD) =>
      checkLeafValues(xy, left, (x, y) => (y._1 < x._1)) :::
        checkLeafValues(xy, right, (x, y) => (y._1 >= x._1))
    case Leaf(coordinates) => coordinates.foldLeft(true)(_ && f(range, _)) :: Nil
  }

  def getSetOfLeaves(rangeTree: RangeTree,tree: Tree): Set[(Int,Int)] = {
    val leaves = PrivateMethod[List[Leaf]]('leaves)
    (rangeTree invokePrivate leaves(tree))
    .flatMap(_ match { case Leaf(x) => x })
    .toSet
  }

  it should "contain all indexed coordinates (x,y) as leaves" in {
    getSetOfLeaves(simpleRangeTree,simpleRangeTree.root) should equal (indexedPointsOnlyX)
  }

  it should "have inner nodes with leaves to the left that are less than " +
    "x coordinate of inner node and leaves to the right that are greater than or equal " in {
    checkLeafValues((0,0),simpleRangeTree.root,(x,y)=>true).reduceLeft(_ && _) should equal (true)
  }

  it should "have nodes that each have inner trees built based on the second " +
    "dimension (y) coordinate that are range trees themselves but with coordinate values inverted" in {
    val innerD = rangeTree.root match { case Inner(left, right, xy, innerD) => innerD}
    getSetOfLeaves(rangeTree,rangeTree.root) should equal (
      getSetOfLeaves(rangeTree,innerD).map(x => (x._2,x._1)))
    (checkLeafValues((0,0),innerD,(x,y)=>true).reduceLeft(_ && _)) should equal (true)
  }

  it should "return a list of desired coordinate values stored in leaves when searched " +
    "based on a viewport of upper left, lower right coordinate values" in {
      rangeTree.search((3,7),(4,6)).toSet should equal (Set((3,4),(4,5),(3,6),(5,6),(6,5),(7,4)))
  }

}