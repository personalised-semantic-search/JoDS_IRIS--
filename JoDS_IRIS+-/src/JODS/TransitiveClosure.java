package JODS;

import java.util.HashSet;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

public class TransitiveClosure {
	// ~ Static fields/initializers
	// ---------------------------------------------

	/**
	 * Singleton instance.
	 */
	public static final TransitiveClosure INSTANCE = new TransitiveClosure();

	// ~ Constructors
	// -----------------------------------------------------------

	/**
	 * Private Constructor.
	 */
	private TransitiveClosure() {
	}

	// ~ Methods
	// ----------------------------------------------------------------

	/**
	 * Computes the transitive closure of the given graph.
	 * 
	 * @param graph
	 *            - Graph to compute transitive closure for.
	 */
	public void closeSimpleDirectedGraph(
			DirectedMultigraph<ITuple, DefaultEdge> graph) {
		Set<ITuple> vertexSet = graph.vertexSet();

		Set<ITuple> newEdgeTargets = new HashSet<ITuple>();

		// At every iteration of the outer loop, we add a path of length 1
		// between nodes that originally had a path of length 2. In the worst
		// case, we need to make floor(log |V|) + 1 iterations. We stop earlier
		// if there is no change to the output graph.

		int bound = computeBinaryLog(vertexSet.size());
		boolean done = false;
		for (int i = 0; !done && (i < bound); ++i) {
			done = true;
			for (ITuple v1 : vertexSet) {
				newEdgeTargets.clear();

				for (DefaultEdge v1OutEdge : graph.outgoingEdgesOf(v1)) {
					ITuple v2 = graph.getEdgeTarget(v1OutEdge);
					for (DefaultEdge v2OutEdge : graph.outgoingEdgesOf(v2)) {
						ITuple v3 = graph.getEdgeTarget(v2OutEdge);
						if (v1.equals(v3)) {
							// Its a simple graph, so no self loops.
							continue;
						}
						if (graph.getEdge(v1, v3) != null) {
							// There is already an edge from v1 ---> v3,
							// skip;
							continue;
						}
						newEdgeTargets.add(v3);
						done = false;
					}
				}
				for (ITuple v3 : newEdgeTargets) {
					DefaultEdge curEdge = new DefaultEdge();
					curEdge.setSource(v1);
					curEdge.setTarget(v3);
					
					graph.addEdge(v1 ,
							v3,curEdge );
				}
			}
		}
	}

	/**
	 * Computes floor(log_2(n)) + 1
	 */
	private int computeBinaryLog(int n) {
		assert n >= 0;

		int result = 0;
		while (n > 0) {
			n >>= 1;
			++result;
		}

		return result;
	}
}
