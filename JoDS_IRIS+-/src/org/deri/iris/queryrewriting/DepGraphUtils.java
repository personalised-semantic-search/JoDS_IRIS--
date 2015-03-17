/**
 * 
 */
package org.deri.iris.queryrewriting;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.deri.iris.Reporter;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.Position;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author jd
 */
public class DepGraphUtils {

	public static Logger LOGGER = Logger.getLogger(RewritingUtils.class);
	public static Reporter rep = Reporter.getInstance();

	private static PositionJoin getPositionJoin(final Set<PositionJoin> list, final IPosition left,
	        final IPosition right) {
		for (final PositionJoin pj : list)
			if ((pj.getLeftPosition().equals(left) && pj.getRightPosition().equals(right))
			        || (pj.getLeftPosition().equals(right) && pj.getRightPosition().equals(left)))
				return (pj);
		return (null);
	}

	public static Set<PositionJoin> computePositionJoins(final IRule rule) {
		final Set<PositionJoin> result = new LinkedHashSet<PositionJoin>();

		if (rule.getBody().size() > 1) {
			for (int k = 0; k < (rule.getBody().size() - 1); k++) {
				final IAtom a1 = Iterators.get(rule.getBody().iterator(), k).getAtom();
				for (int l = k + 1; l < rule.getBody().size(); l++) {
					final IAtom a2 = Iterators.get(rule.getBody().iterator(), l).getAtom();
					int i = 0;
					for (final ITerm t1 : a1.getTuple()) {
						i++;
						int j = 0;
						for (final ITerm t2 : a2.getTuple()) {
							j++;
							if ((t1 instanceof IVariable) && (t2 instanceof IVariable) && t1.equals(t2)) {
								final IPosition p1 = new Position(a1.getPredicate().getPredicateSymbol(), i);
								final IPosition p2 = new Position(a2.getPredicate().getPredicateSymbol(), j);

								final PositionJoin pj = DepGraphUtils.getPositionJoin(result, p1, p2);
								if (pj == null) {
									result.add(new PositionJoin(p1, p2, 1));
								} else {
									pj.setCount(pj.getCount() + 1);
								}

							}
						}
					}
				}
			}
		}
		return (result);
	}

	public static Set<PositionJoin> computeExistentialJoins(final IRule query, final List<IRule> tgds,
	        final Map<IPosition, Set<IRule>> exPos) {

		final Set<PositionJoin> exJoins = new LinkedHashSet<PositionJoin>();
		if (query.getBody().size() > 1) {

			final Set<PositionJoin> posJoin = computePositionJoins(query);

			for (final PositionJoin p : posJoin) {
				if (exPos.keySet().contains(p.getLeftPosition())
				        && exPos.keySet().contains(p.getRightPosition())
				        && !Sets.intersection(exPos.get(p.getLeftPosition()), exPos.get(p.getRightPosition()))
				                .isEmpty()) {
					exJoins.add(p);
				}
			}
		}
		return (exJoins);
	}

	public static Map<Pair<IPosition, IPosition>, Set<List<IRule>>> computePositionDependencyGraph(
	        final List<IRule> tgds) {
		final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> posDeps = new LinkedHashMap<Pair<IPosition, IPosition>, Set<List<IRule>>>();

		for (final IRule r : tgds) {

			// self loops
			for (final IPosition p : r.getRulePositions()) {
				final Pair<IPosition, IPosition> pair = Pair.of(p, p);
				if (!posDeps.containsKey(pair)) {
					final Set<List<IRule>> ways = new LinkedHashSet<List<IRule>>();
					final List<IRule> emptyPath = ImmutableList.of();
					ways.add(emptyPath);
					posDeps.put(pair, ways);
				}
			}

			// direct dependencies
			final Set<IVariable> fVars = r.getFrontierVariables();
			for (final IVariable v : fVars) {
				final Set<IPosition> bodyPos = r.getTermBodyPositions(v);
				final Set<IPosition> headPos = r.getTermHeadPositions(v);
				for (final IPosition bp : bodyPos) {
					for (final IPosition hp : headPos) {
						final Pair<IPosition, IPosition> dep = Pair.of(bp, hp);
						Set<List<IRule>> ways = posDeps.get(dep);
						if (ways == null) {
							ways = new LinkedHashSet<List<IRule>>();
						}
						final List<IRule> labels = Lists.newLinkedList();
						labels.add(r);
						ways.add(labels);
						posDeps.put(Pair.of(bp, hp), ways);
					}
				}
			}
		}

		return posDeps;
	}

	public static Map<Pair<IPosition, IPosition>, Set<List<IRule>>> computeAtomCoverageGraph(
	        final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps) {

		final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> closure = Maps.newLinkedHashMap(deps);

		LOGGER.trace("Computing inferred dependencies.");
		Map<Pair<IPosition, IPosition>, Set<List<IRule>>> tempDeps = new LinkedHashMap<Pair<IPosition, IPosition>, Set<List<IRule>>>();
		do {

			tempDeps = ImmutableMap.copyOf(closure);
			for (final Pair<IPosition, IPosition> depj : tempDeps.keySet()) {
				for (final Pair<IPosition, IPosition> depk : tempDeps.keySet()) {
					if (!depj.getLeft().equals(depj.getRight()) && !depk.getLeft().equals(depk.getRight())
					        && !depj.equals(depk)) {
						if (depj.getRight().equals(depk.getLeft())) {

							for (final List<IRule> premise : tempDeps.get(depj)) {
								for (final List<IRule> consequent : tempDeps.get(depk)) {
									if (RewritingUtils.resolves(premise.get(premise.size() - 1), consequent.get(0),
									        new HashMap<IVariable, ITerm>())) {

										// update the labels
										final Pair<IPosition, IPosition> dep = Pair.of(depj.getLeft(), depk.getRight());

										Set<List<IRule>> ways = tempDeps.get(dep);
										final List<IRule> labels = Lists.newLinkedList();
										labels.addAll(premise);
										labels.addAll(consequent);

										if (ways == null) {
											ways = new LinkedHashSet<List<IRule>>();
											ways.add(labels);
											closure.put(dep, ways);
										} else {
											// check that the new path is not a superset of something already known
											boolean superset = false;
											for (final List<IRule> existingPath : ImmutableSet.copyOf(ways)) {
												if ((existingPath.size() != 0)
												        && ((Collections.indexOfSubList(labels, existingPath) >= 0) || (Collections
												                .indexOfSubList(existingPath, labels) > 0))) {
													superset = true;
													break;
												}
											}
											if (!superset) {
												ways.add(labels);
												closure.put(dep, ways);
											}
										}
									}
								}
							}
						}
					}
				}
			}

		} while (!tempDeps.equals(closure));

		return closure;

	}

	public static Map<IPosition, Set<IRule>> computeAffectedPositions(final List<IRule> tgds,
	        final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> posDeps) {

		// get all declared existential positions in the set of TGDs (initial marking)
		final Map<IPosition, Set<IRule>> exPosSet = new HashMap<IPosition, Set<IRule>>();
		for (final IRule tgd : tgds) {
			final Set<IPosition> exPossInTGD = tgd.getExistentialPositions();
			for (final IPosition exPosInTGD : exPossInTGD) {
				if (exPosSet.containsKey(exPosInTGD)) {
					exPosSet.get(exPosInTGD).add(tgd);
				} else {
					exPosSet.put(exPosInTGD, Sets.newHashSet(tgd));
				}
			}
		}

		// compute inferred existential positions (marking propagation)
		for (final IPosition p : Sets.newHashSet(exPosSet.keySet())) {
			for (final Pair<IPosition, IPosition> posDep : posDeps.keySet()) {
				if (p.equals(posDep.getLeft())) {
					// add the new position to the set
					if (exPosSet.containsKey(posDep.getRight())) {
						exPosSet.get(posDep.getRight()).addAll((exPosSet.get(posDep.getLeft())));
					} else {
						exPosSet.put(posDep.getRight(), Sets.newHashSet(exPosSet.get(posDep.getLeft())));
					}
				}
			}
		}

		return (exPosSet);
	}
}
