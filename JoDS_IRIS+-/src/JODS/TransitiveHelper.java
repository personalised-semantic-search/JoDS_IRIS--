package JODS;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import org.deri.iris.api.basics.ITuple;

public class TransitiveHelper {
	private Stack<ITuple> path; // the current path
	private Set<ITuple> onPath; // the set of vertices
								// on the path

	public TransitiveHelper() {
		path = new Stack<ITuple>(); // the current path
		onPath = new HashSet<ITuple>(); // the set of vertices
		result = new Stack<ITuple>();
	}

	private Stack<ITuple> result;

	public Stack<ITuple> getTrans(PreferencesGraph G, ITuple s, ITuple t) {
		enumerate(G, s, t);
	//	System.out.println(result);
		return result;
	}

	// use DFS
	
	private void enumerate(PreferencesGraph G, ITuple v, ITuple t) {

		// add node v to current path from s
		path.push(v);
		onPath.add(v);

		// found path from s to t - currently prints in reverse order because of
		// stack
		if (v.equals(t)) {
			// System.out.println(path);
			result = new Stack<ITuple>();
			for (ITuple tuple : path) {
				result.add(tuple);
			}
			return;
		}
		// consider all neighbors that would continue path with repeating a node
		else {
			for (ITuple w : G.getNeighBour(v)) {
				if (!onPath.contains(w))
					enumerate(G, w, t);

			}
		}

		// done exploring from v, so remove from path
		path.pop();
		onPath.remove(v);
	}

	
	private void enumerate2Paths(PreferencesGraph G, ITuple v, ITuple t) {

		// add node v to current path from s
		path.push(v);
		onPath.add(v);

		// found path from s to t - currently prints in reverse order because of
		// stack
		if (v.equals(t)) {
			// System.out.println(path);

			Stack<ITuple> p = new Stack<ITuple>();
			for (ITuple tuple : path) {
				p.add(tuple);
			}
			//result.add(p);
		}
		// consider all neighbors that would continue path with repeating a node
		else {
			for (ITuple w : G.getNeighBour(v)) {
				if (!onPath.contains(w))
					enumerate(G, w, t);

			}
		}

		// done exploring from v, so remove from path
		path.pop();
		onPath.remove(v);
	}

}
