package JODS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.rules.optimisation.RemoveDuplicateLiteralOptimiser;
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.traverse.BreadthFirstIterator;

public class CombinationAlgorithms {

	public static <T extends Comparable<? super T>> List<T> asSortedListDesc(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list, Collections.reverseOrder());
		return list;
	}

	public static <T extends Comparable<? super T>> List<T> asSortedListAsc(
			Collection<T> c) {
		List<T> list = new ArrayList<T>(c);
		java.util.Collections.sort(list);
		return list;
	}

	public static List<Double> rmDuplicates(List<Double> list) {
		Iterator<Double> it = list.iterator();
		int index = 0;
		while (it.hasNext() && index < list.size() - 1) {
			Double current = it.next();
			index++;
			if (current.equals(list.get(index))) {
				it.remove();
				index--;
			}
		}
		return list;
	}

	public static List<Integer> rmDuplicatesInt(List<Integer> list) {
		Iterator<Integer> it = list.iterator();
		int index = 0;
		while (it.hasNext() && index < list.size() - 1) {
			Integer current = it.next();
			index++;
			if (current.equals(list.get(index))) {
				it.remove();
				index--;
			}
		}
		return list;
	}

	public static PreferencesGraph combPrefsGen(PreferencesGraph pg,
			Map<String, Double> probabilisticModel, double t) {
		PreferencesGraph preferenceModel = new PreferencesGraph(pg);
		//System.out.print("E"+preferenceModel.getEdgesSize());
		//TransitiveClosure c = TransitiveClosure.INSTANCE;
		// System.out.println("Start " + preferenceModel);
		//c.closeSimpleDirectedGraph(preferenceModel.g);
		//System.out.print("E tra"+preferenceModel.getEdgesSize());
		// System.out.println("Transitive " + preferenceModel);
		PreferencesGraph preferenceModelTrans = new PreferencesGraph(pg);
	//	int count =0;
		for (DefaultEdge e : new HashSet<DefaultEdge>(
				preferenceModelTrans.g.edgeSet())) {
			
		//	if(count%10000 == 0 ){
		//		System.out.println("Inspected "+count+" edges");
		//	}
			double target = probabilisticModel.get(((ITuple) e.getTarget())
					.get(0).toString().replace("'", ""));
			double source = probabilisticModel.get(((ITuple) e.getSource())
					.get(0).toString().replace("'", ""));
			// System.out.print(((ITuple) e.getSource()).get(0).toString()
			// .replace("'", "")
			// + " "
			// + ((ITuple) e.getTarget()).get(0).toString()
			// .replace("'", ""));
			//
			// System.out.println(" target - source > t " + target + " - "
			// + source + ">" + t + " is " + (target - source > t));
			if (target - source > t) {
				// System.out.println("Remove this and added inverse "
				// + ((ITuple) e.getSource()).get(0).toString()
				// .replace("'", "")
				// + " "
				// + ((ITuple) e.getTarget()).get(0).toString()
				// .replace("'", ""));

				preferenceModel.addPreference((ITuple) e.getTarget(),
						(ITuple) e.getSource());
				preferenceModel.removePreference((ITuple) e.getSource(),
						(ITuple) e.getTarget());

				if (preferenceModel.findVertexesForCycle((ITuple)e.getTarget())) {
				//if (preferenceModel.findVertexesForCycle().size() > 0) {
					// System.out.println("Remove (cycle) this and added inverse "
					// + ((ITuple) e.getSource()).get(0).toString()
					// .replace("'", "")
					// + " "
					// + ((ITuple) e.getTarget()).get(0).toString()
					// .replace("'", ""));
					//System.out.println("Cycle");
					preferenceModel.removePreference((ITuple) e.getTarget(),
							(ITuple) e.getSource());
					preferenceModel.addPreference((ITuple) e.getSource(),
							(ITuple) e.getTarget());
				}
			}
			//count++;
			
			// System.out.println("For " + (ITuple) e.getSource() + ","
			// + (ITuple) e.getTarget() + " ->" + preferenceModel);
		}
		//c.closeSimpleDirectedGraph(preferenceModel.g);
		return preferenceModel;
	}

	public static Map<ITuple, Integer> combPrefsRank(PreferencesGraph pg,
			Map<String, Double> probabilisticModel, Function f) {
		PreferencesGraph preferenceModel = new PreferencesGraph(pg);
//		
//		TransitiveClosure c = TransitiveClosure.INSTANCE;
//		c.closeSimpleDirectedGraph(preferenceModel.g);
		Map<ITuple, Integer> rankPreferenceModel = getRankLayer(preferenceModel);

		Map<ITuple, Integer> finalRank = new HashMap<ITuple, Integer>();
		Map<String, Integer> rankProbabilisticModel = new HashMap<String, Integer>();
		List<Double> orderedValues = rmDuplicates(asSortedListDesc(probabilisticModel
				.values()));
		int l = orderedValues.size();
		for (String t : probabilisticModel.keySet()) {
			for (int i = 0; i < l; i++) {
				if (orderedValues.get(i).equals(probabilisticModel.get(t))) {
					rankProbabilisticModel.put(t, i + 1);
					break;
				}
			}
		}
		// System.out.println("Prob" + rankProbabilisticModel);
		// System.out.println("Pref" + rankPreferenceModel);
		for (ITuple t : rankPreferenceModel.keySet()) {
			Integer r = rankPreferenceModel.get(t);
			if (f == Function.Max) {
				if (r < rankProbabilisticModel.get(t.get(0).toString()
						.replace("'", ""))) {
					r = rankProbabilisticModel.get(t.get(0).toString()
							.replace("'", ""));
				}
			} else {
				if (f == Function.Min) {
					if (r > rankProbabilisticModel.get(t.get(0).toString()
							.replace("'", ""))) {
						r = rankProbabilisticModel.get(t.get(0).toString()
								.replace("'", ""));
					}
				}
			}
			finalRank.put(t, r);
		}

		return finalRank;

	}

	public static PreferencesGraph combPrefsPT(PreferencesGraph pg,
			Map<String, Double> probabilisticModel, double p) {
		PreferencesGraph preferenceModel = new PreferencesGraph(pg);
		//TransitiveClosure c = TransitiveClosure.INSTANCE;
		//c.closeSimpleDirectedGraph(preferenceModel.g);
		for (ITuple atom : pg.g.vertexSet()) {
			if (probabilisticModel.get(atom.get(0).toString().replace("'", "")) < p) {
				preferenceModel.g.removeVertex(atom);
			}
		}
		return preferenceModel;

	}

	protected static Map<ITuple, Integer> getRankLayer(
			PreferencesGraph preferenceModel) {
		Map<ITuple, Integer> depthMap = new HashMap<ITuple, Integer>();
		int nbNodes = preferenceModel.getVertexesSize();
		int rank = 1;
		while (depthMap.size() < nbNodes) {
			List<ITuple> vertices = new ArrayList<ITuple>();
			for (ITuple tuple : preferenceModel.g.vertexSet()) {
				if (preferenceModel.g.inDegreeOf(tuple) == 0) {
					depthMap.put(tuple, rank);
					vertices.add(tuple);
				}
			}
			rank++;
			preferenceModel.g.removeAllVertices(vertices);
		}
		return depthMap;
	}

	protected static Map<Integer, Vector<ITuple>> getLayers(
			PreferencesGraph preferenceModel) {
		Map<Integer, Vector<ITuple>> depthMap = new HashMap<Integer, Vector<ITuple>>();
		int nbNodes = preferenceModel.getVertexesSize();
		int rank = 1;
		int values = 0;
		while (values < nbNodes) {
			List<ITuple> vertices = new ArrayList<ITuple>();
			for (ITuple tuple : preferenceModel.g.vertexSet()) {
				if (preferenceModel.g.inDegreeOf(tuple) == 0) {
					if (!depthMap.containsKey(rank)) {
						depthMap.put(rank, new Vector<ITuple>());
					}
					depthMap.get(rank).add(tuple);
					values++;
					vertices.add(tuple);
				}
			}
			rank++;
			preferenceModel.g.removeAllVertices(vertices);
		}
		return depthMap;
	}

	public static PreferencesGraph combPrefsSort(PreferencesGraph pg,
			Map<String, Double> probabilisticModel) {
		PreferencesGraph preferenceModel = new PreferencesGraph(pg);
		//TransitiveClosure c = TransitiveClosure.INSTANCE;
		//c.closeSimpleDirectedGraph(preferenceModel.g);
		// System.out.println(preferenceModel.g);
		PreferencesGraph preferenceModel2 = new PreferencesGraph(
				preferenceModel);
		 //System.out.println("pref model tra"+preferenceModel.g);
		 //System.out.println(preferenceModel2.g);
		Map<Integer, Vector<ITuple>> depthMap = getLayers(preferenceModel);
		for (Integer layer : depthMap.keySet()) {
			Vector<ITuple> tuples = depthMap.get(layer);
			for (int t1_i = 0; t1_i < tuples.size(); t1_i++) {
				ITuple t1 = tuples.get(t1_i);
				for (int t2_i = 0; t2_i < tuples.size(); t2_i++) {
					ITuple t2 = tuples.get(t2_i);
					double p_t1 = probabilisticModel.get(t1.get(0).toString()
							.replace("'", ""));
					double p_t2 = probabilisticModel.get(t2.get(0).toString()
							.replace("'", ""));
					if (p_t1 > p_t2
							&& (!preferenceModel2.g.containsEdge(t1, t2))) {
						preferenceModel2.addPreference(t1, t2);
					}
				}

			}
		}
		//c.closeSimpleDirectedGraph(preferenceModel2.g);
		return preferenceModel2;
	}
}
