package JODS;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.DirectedGraph;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;

public class TraversalDepthConstructor<V, E>  implements TraversalListener<V,E> {
	public TraversalDepthConstructor(DirectedGraph<V, E> graph) {
		m_graph = graph;

		// Initialise the depth map with 0
		for (V v : graph.vertexSet()) {
			m_depthMap.put(v, 1);
		}
	}

	public int getDepth(V v) {
		return m_depthMap.get(v);
	}

	public void vertexFinished(VertexTraversalEvent<V> v) {
	};

	public void vertexTraversed(VertexTraversalEvent<V> v) {
	};

	public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
	}

	public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
	}

	public void edgeTraversed(EdgeTraversalEvent<V, E> e) {
		// System.out.println("edgeTraversed:"
		// + m_graph.getEdgeSource(e.getEdge()).toString() + " -> "
		// + m_graph.getEdgeTarget(e.getEdge()).toString());

		// Depth is the source vertex's depth + 1
		int depth = m_depthMap.get(m_graph.getEdgeSource(e.getEdge())) + 1;
		m_depthMap.put(m_graph.getEdgeTarget(e.getEdge()), depth);

	}

	// The graph being traversed
	DirectedGraph<V, E> m_graph;

	// The depth map
	Map<V, Integer> m_depthMap = new HashMap<V, Integer>();

}
