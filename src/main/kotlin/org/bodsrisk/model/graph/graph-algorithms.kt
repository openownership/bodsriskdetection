package org.bodsrisk.model.graph

import java.util.*

typealias RelationshipPath = List<Relationship>

fun Collection<Relationship>.shortestPath(source: String, destination: String): RelationshipPath? {
    return allPaths(source, destination).minByOrNull { it.size }
}

fun Collection<Relationship>.allPaths(source: String, destination: String): List<RelationshipPath> {
    val paths = mutableListOf<RelationshipPath>()
    val currentPath = Stack<Relationship>()
    traverse(source, destination, currentPath) {
        paths.add(it)
    }
    return paths
}

private fun Collection<Relationship>.traverse(
    sourceId: String,
    destId: String,
    currentPath: Stack<Relationship>,
    currentPathNodes: MutableSet<String> = mutableSetOf(),
    onPathFound: (List<Relationship>) -> Unit
) {
    filter { it.parentId == sourceId && !currentPathNodes.contains(sourceId) }
        .forEach { relationship ->
            currentPath.push(relationship)
            currentPathNodes.add(relationship.parentId)
            if (relationship.childId == destId) {
                onPathFound(currentPath.toList())
            } else {
                traverse(relationship.childId, destId, currentPath, currentPathNodes, onPathFound)
            }
            currentPathNodes.remove(relationship.parentId)
            currentPath.pop()
        }
}