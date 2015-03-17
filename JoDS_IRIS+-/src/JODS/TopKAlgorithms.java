package JODS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;

public class TopKAlgorithms {

	protected static Set<ITuple> getSkiline(PreferencesGraph preferenceModel) {
		Set<ITuple> vertices = new HashSet<ITuple>();
		for (ITuple tuple : preferenceModel.g.vertexSet()) {
			if (preferenceModel.g.inDegreeOf(tuple) == 0) {
				vertices.add(tuple);
			}

		}
		return vertices;
	}

	public static List<ITuple> getTopK(PreferencesGraph preferenceModel, int k) {
		List<ITuple> topk = new ArrayList<ITuple>();
		int nbNodes = preferenceModel.getVertexesSize();
		int toBeAdded = 0;
		if (k > nbNodes)
			toBeAdded = nbNodes;
		else
			toBeAdded = k;
		System.out.println("Nodes before: "+preferenceModel.getVertexesSize());
		while (toBeAdded > 0) {
			Set<ITuple> skiline = getSkiline(preferenceModel);
			int sizeSkiline = skiline.size();
			if (toBeAdded >= sizeSkiline) {
				topk.addAll(skiline);
				
				preferenceModel.g.removeAllVertices(skiline);
				
				toBeAdded -= sizeSkiline;
			} else {
				for (Iterator<ITuple> it = skiline.iterator(); it.hasNext()
						&& toBeAdded > 0;) {
					ITuple t = it.next();
					topk.add(t);
					toBeAdded--;
				}

			}
			if (preferenceModel.getVertexesSize()==0) toBeAdded=0;
		}
		System.out.println("Nodes after: "+preferenceModel.getVertexesSize());
		return topk;
	}

	public static List<ITuple> topkPrefsGen(PreferencesGraph pg,
			Map<String, Double> probabilisticModel, double t, int k) {
		PreferencesGraph g = CombinationAlgorithms.combPrefsGen(pg,
				probabilisticModel, t);
		//System.out.println(g);
		return getTopK(g, k);
	}

	public static List<ITuple> topkPrefsRank(Map<ITuple, Integer> ranks, int k) {

		List<ITuple> topk = new ArrayList<ITuple>();
		List<Integer> orderedValues = CombinationAlgorithms
				.rmDuplicatesInt(CombinationAlgorithms.asSortedListAsc(ranks
						.values()));
		Map<Integer, Set<ITuple>> depthMap = new HashMap<Integer, Set<ITuple>>();
		for (ITuple t : ranks.keySet()) {
			int rank = ranks.get(t);
			if (!depthMap.containsKey(rank)) {
				depthMap.put(rank, new HashSet<ITuple>());
			}
			depthMap.get(rank).add(t);
		}

		int toBeAdded = (k > ranks.keySet().size()) ? ranks.keySet().size() : k;
		int i = 0;
		System.out.println("Nodes before: "+ ranks.keySet().size());
		while (toBeAdded > 0) {
			Set<ITuple> skiline = depthMap.get(orderedValues.get(i));
			int sizeSkiline = skiline.size();
			if (toBeAdded >= sizeSkiline) {
				topk.addAll(skiline);
				toBeAdded -= sizeSkiline;
			} else {
				for (Iterator<ITuple> it = skiline.iterator(); it.hasNext()
						&& toBeAdded > 0;) {
					ITuple t = it.next();
					topk.add(t);
					toBeAdded--;
				}

			}
			i++;
		}
		System.out.println("Nodes after: "+ ranks.keySet().size());
		return topk;

	}

	public static List<ITuple> topkPrefsRank(PreferencesGraph pg,
			Map<String, Double> probabilisticModel, Function f, int k) {
		Map<ITuple, Integer> ranks = CombinationAlgorithms.combPrefsRank(pg,
				probabilisticModel, Function.Max);

		List<ITuple> topk = new ArrayList<ITuple>();
		List<Integer> orderedValues = CombinationAlgorithms
				.rmDuplicatesInt(CombinationAlgorithms.asSortedListAsc(ranks
						.values()));
		Map<Integer, Set<ITuple>> depthMap = new HashMap<Integer, Set<ITuple>>();
		for (ITuple t : ranks.keySet()) {
			int rank = ranks.get(t);
			if (!depthMap.containsKey(rank)) {
				depthMap.put(rank, new HashSet<ITuple>());
			}
			depthMap.get(rank).add(t);
		}

		int toBeAdded = (k >= ranks.keySet().size()) ? ranks.keySet().size()
				: k;
		int i = 0;
		while (toBeAdded > 0) {
			Set<ITuple> skiline = depthMap.get(orderedValues.get(i));
			int sizeSkiline = skiline.size();
			if (toBeAdded >= sizeSkiline) {
				topk.addAll(skiline);
				toBeAdded -= sizeSkiline;
			} else {
				for (Iterator<ITuple> it = skiline.iterator(); it.hasNext()
						&& toBeAdded > 0;) {
					ITuple t = it.next();
					topk.add(t);
					toBeAdded--;
				}

			}
			i++;
		}
		return topk;

	}

	public static List<ITuple> topkPrefsPT(PreferencesGraph pg,
			Map<String, Double> probabilisticModel, double p, int k) {
		PreferencesGraph g = CombinationAlgorithms.combPrefsPT(pg,
				probabilisticModel, p);
		return getTopK(g, k);

	}

	public static List<ITuple> topkPrefsSort(PreferencesGraph pg,
			Map<String, Double> probabilisticModel, int k) {
		PreferencesGraph g = CombinationAlgorithms.combPrefsSort(pg,
				probabilisticModel);

		return getTopK(g, k);
	}

}
