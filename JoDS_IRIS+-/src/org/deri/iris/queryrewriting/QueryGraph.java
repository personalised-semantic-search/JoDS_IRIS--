package org.deri.iris.queryrewriting;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.deri.iris.api.basics.IRule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

/**
 * @author Giorgio Orsi
 */
public class QueryGraph {

	private final DirectedGraph<IRule, DefaultEdge> queryGraph;

	public QueryGraph() {
		queryGraph = new SimpleDirectedGraph<IRule, DefaultEdge>(DefaultEdge.class);
	}

	public void addRule(final IRule father, final IRule child) {
		queryGraph.addVertex(father);
		queryGraph.addVertex(child);
		queryGraph.addEdge(father, child);
	}

	public void addRule(final IRule rule) {
		queryGraph.addVertex(rule);
	}

	public void removeRule(final IRule rule) {
		queryGraph.removeVertex(rule);
	}

	public void removeAndBypass(final IRule rule) {
		final Collection<DefaultEdge> inEdges = ImmutableList.copyOf(queryGraph.incomingEdgesOf(rule));
		final Collection<DefaultEdge> outEdges = ImmutableList.copyOf(queryGraph.outgoingEdgesOf(rule));

		final Set<IRule> ancestors = Sets.newHashSet();
		for (final DefaultEdge e : inEdges) {
			ancestors.add(queryGraph.getEdgeSource(e));
		}

		final Set<IRule> descendants = Sets.newHashSet();
		for (final DefaultEdge e : outEdges) {
			descendants.add(queryGraph.getEdgeTarget(e));
		}

		queryGraph.removeVertex(rule);

		for (final IRule ancestor : ancestors) {
			for (final IRule descendant : descendants) {
				queryGraph.addEdge(ancestor, descendant);
			}
		}

	}

	// public Set<IRule> getDescendantsOrSelf(final IRule rule) {
	// final Set<IRule> desc = Sets.newHashSet(rule);
	//
	// for (final DefaultEdge e : queryGraph.outgoingEdgesOf(rule)) {
	// final IRule nextNode = queryGraph.getEdgeTarget(e);
	// desc.addAll(getDescendantsOrSelf(nextNode));
	// }
	//
	// return desc;
	// }
	//
	// public Set<IRule> removeDescendantsOrSelf(final IRule rule) {
	// final Set<IRule> removed = Sets.newHashSet();
	//
	// removed.addAll(getDescendantsOrSelf(rule));
	//
	// for (final IRule r : removed) {
	// queryGraph.removeAllEdges(rule, r);
	// }
	//
	// return removed;
	// }

	public boolean contains(final IRule rule) {

		return queryGraph.containsVertex(rule);
	}

	public int size() {
		return queryGraph.vertexSet().size();
	}

	public List<IRule> getRules() {
		return ImmutableList.copyOf(queryGraph.vertexSet());
	}
}
