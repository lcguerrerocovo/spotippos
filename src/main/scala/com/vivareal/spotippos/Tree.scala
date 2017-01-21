package com.vivareal.spotippos

/**
  * Created by luisguerrero
  */
trait Tree

case class Inner(var left: Tree,var right: Tree,var coordinates: (Int,Int),var innerDimension: Tree = null) extends Tree {
  override def toString = {
    "(" + coordinates._1 + ")" + " -> left(" + left.toString + ") -> right(" + right.toString + ")" + (if(innerDimension!=null) "-> innerD(" + innerDimension + ")")
  }
}

case class Leaf(coordinates: List[(Int,Int)]) extends Tree

