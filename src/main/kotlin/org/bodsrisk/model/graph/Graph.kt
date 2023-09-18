package org.bodsrisk.model.graph

data class Graph<NodeData>(
    val nodes: Collection<GraphNode<NodeData>>,
    val relationships: Set<Relationship>,
) {

    fun shortestPath(source: String, destination: String): Graph<NodeData> {
        val shortestPath = relationships.shortestPath(source, destination)
        return Graph(
            nodes = nodes,
            relationships = shortestPath?.toSet() ?: emptySet()
        )
    }

    fun shortestPaths(fromIds: Collection<String>, toId: String): Graph<NodeData> {
        return shortestPaths(fromIds, listOf(toId))
    }

    fun shortestPaths(fromId: String, toIds: Collection<String>): Graph<NodeData> {
        return shortestPaths(listOf(fromId), toIds)
    }

    fun shortestPaths(fromIds: Collection<String>, toIds: Collection<String>): Graph<NodeData> {
        var graph = empty<NodeData>()
        fromIds.forEach { fromId ->
            toIds.forEach { toId ->
                graph += this.shortestPath(fromId, toId)
            }
        }
        return graph
    }

    operator fun plus(graph: Graph<NodeData>): Graph<NodeData> {
        return Graph(
            nodes = this.nodes.plus(graph.nodes),
            relationships = this.relationships.plus(graph.relationships),
        )
    }

    companion object {
        fun <NodeData> empty(): Graph<NodeData> {
            return Graph(emptyList(), emptySet())
        }
    }
}

data class GraphNode<NodeData>(
    val id: String,
    val name: String,
    var type: GraphNodeType,
    var data: NodeData?= null
)

enum class GraphNodeType {
    LEGAL_ENTITY,
    PERSON,
    UNKNOWN,
    ADDRESS
}
