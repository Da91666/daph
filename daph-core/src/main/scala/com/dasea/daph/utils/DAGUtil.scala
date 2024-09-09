package com.dasea.daph.utils

object DAGUtil {
  def findChildren[Node](rootNode: Node,
                         nodeToNodes: Map[Node, Set[Node]]): Set[Node] = {
    def findChildrenByRecursion(rootNode: Node): Set[Node] = {
      val children = nodeToNodes(rootNode)
      children ++ children.flatMap(findChildrenByRecursion)
    }

    findChildrenByRecursion(rootNode)
  }
}