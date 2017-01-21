package com.vivareal.spotippos

/**
  * Created by luisguerrero
  */
class RangeTree(var arr: Array[(Int,Int)]) {

  val area: Tree = createRangeTree(arr)

  private def createRangeTree(arr: Array[(Int,Int)]): Tree = {
    def groupSequential(arr: Array[(Int,Int)]): Array[List[(Int,Int)]] = {
      def groupSequential(xs: List[(Int,Int)]): List[List[(Int,Int)]] = xs match {
        case x :: y => List(xs takeWhile(_._1 == x._1)) ::: groupSequential(xs dropWhile(_._1 == x._1))
        case Nil => Nil
      }
      groupSequential(arr.toList).toArray
    }

    def createRangeTree(arr: Array[List[(Int, Int)]], dimension: Int): Tree = {
      if (arr.length == 1) Leaf(arr(0))
      else {
        val half = (arr.length / 2)
        val left = createRangeTree(arr.slice(0, half), dimension)
        val right = createRangeTree(arr.slice(half, arr.length), dimension)
        if (dimension == 1) {
          val invertedCoordArr = groupSequential(arr.flatMap(xy => xy.map(z => (z._2,z._1))).sortWith((x, y) => x._1 < y._1))
          val innerD = createRangeTree(invertedCoordArr,2)
          Inner(left, right, arr(half).head,innerD)
        } else Inner(left, right, arr(half).head)
      }
    }
    createRangeTree(groupSequential(arr.sortWith((x, y) => x._1 < y._1)),1)
  }

  private def pruneByRange(tree: Tree, range: (Int,Int)): List[Tree] = {
    def pruneByRange(tree: Tree, range: (Int, Int), xs: List[Tree]): List[Tree] = tree match {
      case Inner(left, right, xy, innerD) => {
        def greaterThanMin = xy._1 > range._1
        def lessThanMax = xy._1 < range._2
        if (greaterThanMin && lessThanMax && !xs.isEmpty) {
          Inner(left, right, xy, innerD) :: xs
        } else {
          val leftPrune = if (greaterThanMin) pruneByRange(left, range, xs) else xs
          if (xy._1 <= range._2) pruneByRange(right, range, leftPrune) else leftPrune
        }
      }
      case Leaf(coordinates) => {
        if (coordinates.head._1 >= range._1 && coordinates.head._1 <= range._2)
          Leaf(coordinates) :: xs
        else xs
      }
    }
    pruneByRange(tree,range,Nil)
  }

  private def leaves(tree: Tree): List[Leaf] = tree match {
    case Inner(left,right,xy,innerD) => leaves(left) ::: leaves(right)
    case Leaf(coordinates) => List(Leaf(coordinates))
  }

  def search(xRange: (Int,Int), yRange: (Int,Int)): List[(Int,Int)] = {
    val prunedXRange = pruneByRange(area,xRange)

    val innerTrees = prunedXRange.map {
      case Inner(_,_,_,innerD) => innerD
      case Leaf(x) => x.map(y => (y._2,y._1)).map(z => Leaf(List(z)))
    }
    val flattenedInnerTrees = innerTrees.collect{case a: List[Leaf] => a}.flatten :::
      innerTrees.collect{case b: Inner => b}
    flattenedInnerTrees.flatMap(pruneByRange(_,yRange))
      .flatMap(leaves(_))
      .map {
        case Leaf(x) => x.map(y => (y._2,y._1))
        case _ => Nil
      }.flatten
  }
}

