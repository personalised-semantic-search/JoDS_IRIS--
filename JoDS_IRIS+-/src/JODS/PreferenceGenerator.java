package JODS;

import group.models.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.sablecc.sablecc.node.PRegExp;

public class PreferenceGenerator {
	public static int randInt(int min, int max) {

		// Usually this can be a field rather than a method variable
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	public static PreferencesGraph generatePreferenceGraph(
			double sparsityPercentage, IRelation result) {
		final PreferencesGraph prefGraph = org.deri.iris.factory.Factory.PGRAPH
				.createPreferencesGraph();

		List<ITuple> nodes = new ArrayList<ITuple>();
		int numberVertexGraph = result.size();
		int m = (int) Math.round(numberVertexGraph * (numberVertexGraph - 1)
				/ 2 * sparsityPercentage);
		System.out.println("Creating a graph with "+numberVertexGraph+" nodes.");
		for (int i = 0; i < numberVertexGraph; i++) {
			ITuple v = result.get(i);
			prefGraph.addVertex(v);
			nodes.add(v);

		}

		// 1) Randomly generate a DAG with N nodes and sparsityPercentage * N * (N - 1/) 2
		// number of edges (that's sparsityPercentage% of the maximum possible).
		// 2) Compute the transitive closure.
		// 3) Store the number of edges of the new graph so we can report it.

		System.out.println("Adding "+m+" edges ...");
		
		for (int i = 0; i < m; i++) {
			boolean notAdded = true;
			while (notAdded) {

				int chosenSource = randInt(0, numberVertexGraph - 2);
				int chosenTarget = randInt(chosenSource + 1,
						numberVertexGraph - 1);
				ITuple s = result.get(chosenSource);
				ITuple t = result.get(chosenTarget );
				if (!prefGraph.g.containsEdge(s, t)) {
					prefGraph.addPreference(s, t);
					notAdded = false;
				}
			}
		}
		return prefGraph;
	}

	public static PreferencesGraph generatePreferenceGraph2(
			double sparsityPercentage, IRelation result) {
		final PreferencesGraph prefGraph = org.deri.iris.factory.Factory.PGRAPH
				.createPreferencesGraph();
		List<ITuple> nodes = new ArrayList<ITuple>();
		int numberVertexGraph = result.size();
		int m = (int) Math.round(numberVertexGraph * (numberVertexGraph - 1)
				/ 2 * sparsityPercentage);
		for (int i = 0; i < numberVertexGraph; i++) {
			ITuple v = result.get(i);
			prefGraph.addVertex(v);
			nodes.add(v);

		}
		// For i = 1 to N do:
		// Add edges from node i to all nodes from i+1 to N;
		// Let C = N * (N - 1) / 2 - m;
		List<Pair<ITuple, ITuple>> edges = new ArrayList<Pair<ITuple, ITuple>>();
		for (int i = 0; i < numberVertexGraph; i++) {
			ITuple s = result.get(i);
			for (int j = i + 1; j < numberVertexGraph; j++) {
				ITuple t = result.get(j);
				prefGraph.addPreference(s, t);
				edges.add(new Pair<ITuple, ITuple>(s, t));
			}
		}

		// Repeat
		// select one edge (u,v) at random
		// check if there are other paths from u to v; let p be the number of
		// such paths
		// If C - p - 1 < 0 then choose a different edge in step a.
		// Otherwise, remove (u,v) and also remove one edge (chosen at random)
		// from each of the p paths.
		// C:= C - p - 1;
		// Until C = 0;
		int edgesToRemove = numberVertexGraph * (numberVertexGraph - 1) / 2 - m;
		while (edgesToRemove > 0) {
			int nbEdges = edges.size();
			Random randomGenerator = new Random();
			int chosenid = randomGenerator.nextInt(nbEdges);

			ITuple source = edges.get(chosenid).getElement0();
			ITuple target = edges.get(chosenid).getElement1();

			prefGraph.removePreference(source, target);
			// System.out.println("Remove"+source+target);
			edges.remove(chosenid);

			// TransitiveHelper th = new TransitiveHelper();

			List<org.jgraph.graph.DefaultEdge> path = prefGraph.path(source,
					target);

			boolean hasPath = (path.size() > 0);

			while (hasPath) {
				int chosenNode = randomGenerator.nextInt(path.size() - 1);

				ITuple ss = (ITuple) path.get(chosenNode).getSource();
				ITuple tt = (ITuple) path.get(chosenNode + 1).getTarget();
				prefGraph.removePreference(ss, tt);
				for (Pair<ITuple, ITuple> e : edges) {
					if (e.getElement0().equals(ss)
							&& e.getElement1().equals(tt)) {
						edges.remove(e);
						// System.out.println("Re"+ss+tt);
						break;
					}
				}

				// th = new TransitiveHelper();
				// path = th.getTrans(prefGraph, source, target);
				hasPath = (path.size() > 0);

			}

		}

		return prefGraph;
	}
}
