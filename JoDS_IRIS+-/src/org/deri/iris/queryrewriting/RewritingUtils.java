/*
 * Integrated Rule Inference System (IRIS+-):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2009 ICT Institute - Dipartimento di Elettronica e Informazione (DEI), 
 * Politecnico di Milano, Via Ponzio 34/5, 20133 Milan, Italy.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.queryrewriting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.deri.iris.Expressivity;
import org.deri.iris.ExpressivityChecker;
import org.deri.iris.Reporter;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.basics.Position;
import org.deri.iris.factory.Factory;
import org.deri.iris.queryrewriting.caching.CartesianCache;
import org.deri.iris.queryrewriting.caching.CoveringCache;
import org.deri.iris.queryrewriting.caching.CoveringCache.CacheType;
import org.deri.iris.queryrewriting.caching.MGUCache;
import org.deri.iris.queryrewriting.caching.MapsToCache;
import org.deri.iris.utils.TermMatchingAndSubstitution;
import org.deri.iris.utils.UniqueList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 * @version 1.0
 */
public class RewritingUtils {

	public static Logger LOGGER = Logger.getLogger(RewritingUtils.class);
	public static Reporter rep = Reporter.getInstance();

	public static List<IPredicate> getPredicates(final List<IRule> tgds) {
		final List<IPredicate> result = new UniqueList<IPredicate>();

		for (final IRule r : tgds) {
			for (final ILiteral l : r.getHead()) {
				result.add(l.getAtom().getPredicate());
			}
			for (final ILiteral l : r.getBody()) {
				result.add(l.getAtom().getPredicate());
			}
		}
		return (result);
	}

	public static Set<ILiteral> applyMGU(final Set<ILiteral> lits, final Map<IVariable, ITerm> map) {
		final Set<ILiteral> result = new LinkedHashSet<ILiteral>();

		for (final ILiteral l : lits) {
			result.add(applyMGU(l.getAtom(), map));
		}
		return (result);
	}

	/**
	 * Applies the MGU to the atoms to be unified
	 * @param a the atom
	 * @param map the substitution
	 * @return the unified atom
	 */
	public static ILiteral applyMGU(final IAtom a, final Map<IVariable, ITerm> map) {

		rep.incrementValue(RewMetric.MGU_COUNT);

		if (MGUCache.inCache(a, map)) {
			rep.incrementValue(RewMetric.MGU_CACHE_HITS);
			return MGUCache.getLiteral(a, map);
		}

		final IBasicFactory bf = BasicFactory.getInstance();
		final List<ITerm> freshTerms = new LinkedList<ITerm>();

		boolean applied;
		ITuple t = a.getTuple();
		final Set<ITuple> generated = new LinkedHashSet<ITuple>();

		do {
			generated.add(t);
			applied = false;
			final Iterator<IVariable> kIt = map.keySet().iterator();
			while (kIt.hasNext()) {
				freshTerms.clear();
				final IVariable k = kIt.next();
				final Iterator<ITerm> tIt = t.iterator();
				while (tIt.hasNext()) {
					final ITerm v = tIt.next();
					if (v.equals(k)) {
						freshTerms.add(map.get(k));
						applied = true;
					} else {
						freshTerms.add(v);
					}
				}
				t = bf.createTuple(freshTerms);
			}

		} while (applied && !generated.contains(t));

		final ILiteral literal = bf.createLiteral(true, bf.createAtom(a.getPredicate(), t));
		MGUCache.cache(a, map, literal);
		return (literal);
	}

	public static long atomsCount(final Set<IRule> rewriting) {
		long length = 0;
		for (final IRule q : rewriting) {
			length += q.getBody().size();
		}
		return (length);
	}

	public static long joinCount(final Set<IRule> rewriting) {

		long totJoins = 0;
		for (final IRule r : rewriting) {
			final Set<PositionJoin> jt = DepGraphUtils.computePositionJoins(r);
			for (final PositionJoin j : jt) {
				totJoins += j.getCount();
			}
		}
		return (totJoins);
	}

	/**
	 * Checks whether there exists a homomorphism from {@link IRule} r1 to {@link IRule} r2 and returns that inside
	 * {@link Map<IVariable,ITerm>} substitution.
	 * @pre r1 and r2 have disjoint sets of variables. Since the substitution operates on these variable, the caller has
	 *      to take care that the variables are properly renamed before calling this method.
	 * @param r1 the first rule.
	 * @param r2 the second rule.
	 * @param substitution the homomorphism (if any).
	 * @return is the homomorphism exists.
	 */
	public static boolean mapsTo(final IRule r1, final IRule r2) {
		rep.incrementValue(RewMetric.MAPSTO_CHECK_COUNT);

		if (!r2.getPredicates().containsAll(r1.getPredicates()))
			return false;

		final Set<ILiteral> s1 = r1.getAllLiterals();
		final Set<ILiteral> s2 = r2.getAllLiterals();

		if (s2.containsAll(s1)) {
			MapsToCache.cache(s1, s2, MapsToCache.CacheType.MAPSTO);
			return true;
		}

		if (MapsToCache.inCache(s1, s2, MapsToCache.CacheType.NOT_MAPSTO)) {
			rep.incrementValue(RewMetric.NOT_MAPSTO_CACHE_HITS);
			return false;
		}

		if (MapsToCache.inCache(s1, s2, MapsToCache.CacheType.MAPSTO)) {
			rep.incrementValue(RewMetric.MAPSTO_CACHE_HITS);
			return true;
		}

		final Collection<ILiteral> sr1 = RenamingUtils.canonicalRenaming(s1, "T", new HashMap<IVariable, IVariable>());
		final Collection<ILiteral> sr2 = RenamingUtils.canonicalRenaming(s2, "U", new HashMap<IVariable, IVariable>());

		if (mapsTo(sr1, sr2)) {
			MapsToCache.cache(s1, s2, MapsToCache.CacheType.MAPSTO);
			return (true);
		} else {
			MapsToCache.cache(s1, s2, MapsToCache.CacheType.NOT_MAPSTO);
			return (false);
		}
	}

	public static boolean mapsTo(final Collection<ILiteral> s1, final Collection<ILiteral> s2) {

		final Set<ITerm> tSet1 = new LinkedHashSet<ITerm>();
		final Set<ITerm> tSet2 = new LinkedHashSet<ITerm>();

		for (final ILiteral l : s1) {
			tSet1.addAll(l.getAtom().getTuple());
		}

		for (final ILiteral l : s2) {
			tSet2.addAll(l.getAtom().getTuple());
		}

		Set<Map<IVariable, ITerm>> cartesian = new LinkedHashSet<Map<IVariable, ITerm>>();

		if (CartesianCache.inCache(tSet1, tSet2)) {
			rep.incrementValue(RewMetric.CARTESIAN_CACHE_HITS);
			cartesian = CartesianCache.getCartesian(tSet1, tSet2);
		} else {
			@SuppressWarnings("unchecked")
			final Set<List<ITerm>> possibleSubstitutions = Sets.cartesianProduct(tSet1, tSet2);
			final Map<ITerm, Set<List<ITerm>>> substitutionSets = new LinkedHashMap<ITerm, Set<List<ITerm>>>();

			// Create one set for each term in the domain of the substitution.
			for (final List<ITerm> possibleSubstitution : possibleSubstitutions) {
				final ITerm keyTerm = possibleSubstitution.get(0);
				if (substitutionSets.containsKey(keyTerm)) {
					substitutionSets.get(keyTerm).add(possibleSubstitution);
				} else {
					final Set<List<ITerm>> substitutions = new LinkedHashSet<List<ITerm>>();
					substitutions.add(possibleSubstitution);
					substitutionSets.put(keyTerm, substitutions);
				}
			}

			// compute the valid substitutions
			final List<Set<List<ITerm>>> list = Lists.newArrayList();
			for (final Set<List<ITerm>> substitutionSet : substitutionSets.values()) {
				list.add(substitutionSet);
			}
			final Set<List<List<ITerm>>> validSubstitutions = Sets.cartesianProduct(list);

			// create the maps
			for (final List<List<ITerm>> validSubstitution : validSubstitutions) {
				final Map<IVariable, ITerm> map = new HashMap<IVariable, ITerm>();
				for (final List<ITerm> termMap : validSubstitution) {
					map.put((IVariable) termMap.get(0), termMap.get(1));
				}
				cartesian.add(map);
			}

			// Cache the cartesian product
			CartesianCache.cache(tSet1, tSet2, cartesian);
		}

		for (final Map<IVariable, ITerm> m : cartesian) {

			// Apply the substitution

			// LOGGER.trace("Applying substitution " + m.toString() + " to literals " + s1);
			boolean allMap = true;
			for (final ILiteral l1 : s1) {
				if (!s2.contains(applyMGU(l1.getAtom(), m))) {
					allMap = false;
					break;
				}
			}

			if (allMap)
				return true;

		}
		return (false);
	}

	public static IRule reduceQuery(IRule q, final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps) {
		final IBasicFactory bf = BasicFactory.getInstance();
		ILiteral coveredAtom = null;

		if (q.getBody().size() > 1) {
			boolean covered = true;
			do {
				covered = false;
				for (int i = 0; (i < (q.getBody().size() - 1)) && !covered; i++) {
					for (int j = i + 1; (j < q.getBody().size()) && !covered; j++) {

						final ILiteral la = Iterators.get(q.getBody().iterator(), i);
						final ILiteral lb = Iterators.get(q.getBody().iterator(), j);

						if (covers(la, lb, deps, q)) {
							coveredAtom = lb;
							covered = true;
						}
						if (covers(lb, la, deps, q)) {
							coveredAtom = la;
							covered = true;
						}
					}
				}
				if (covered) {
					final Set<ILiteral> reducedBody = new LinkedHashSet<ILiteral>();
					for (final ILiteral l : q.getBody())
						if (!l.equals(coveredAtom)) {
							reducedBody.add(l);
						}
					q = bf.createRule(q.getHead(), reducedBody);
				}
			} while (covered);
		}
		return (q);
	}

	public static boolean covers(final ILiteral a, final ILiteral b,
	        final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps, final IRule q) {

		// Check whether is in cache.
		rep.incrementValue(RewMetric.COVER_CHECK_COUNT);

		if (CoveringCache.inCache(a, b, CacheType.NOT_COVERING)) {
			rep.incrementValue(RewMetric.NON_COVERING_CACHE_HITS);
			return false;
		}

		if (CoveringCache.inCache(a, b, CacheType.COVERING)) {
			rep.incrementValue(RewMetric.COVERING_CACHE_HITS);
			rep.incrementValue(RewMetric.ELIM_ATOM_COUNT);
			return true;
		}

		final Set<ITerm> coveredTerms = new LinkedHashSet<ITerm>();

		int i = 0;
		for (final ITerm tb : b.getAtom().getTuple()) {
			i++;
			final IPosition tbPosInB = new Position(b.getAtom().getPredicate().getPredicateSymbol(), i);
			final Set<IPosition> tbPossInA = getTermPositionsInLiteral(tb, a);
			for (final IPosition tbPosInA : tbPossInA) {
				final Pair<IPosition, IPosition> dep = Pair.of(tbPosInA, tbPosInB);
				// check that a cover dependency exists.
				if (deps.containsKey(dep)) {
					coveredTerms.add(tb);
				}
			}
		}

		if (coveredTerms.containsAll(b.getAtom().getTuple())) {
			// add the pair of literals to the covering cache.
			CoveringCache.cache(a, b, CacheType.COVERING);
			rep.incrementValue(RewMetric.ELIM_ATOM_COUNT);
			return true;
		} else {
			CoveringCache.cache(a, b, CacheType.NOT_COVERING);
			return false;
		}

	}

	private static Set<IPosition> getTermPositionsInLiteral(final ITerm tb, final ILiteral a) {
		final Set<IPosition> pos = new HashSet<IPosition>();

		final List<ITerm> terms = a.getAtom().getTuple();
		for (int i = 0; i < terms.size(); i++) {
			if (terms.get(i).equals(tb)) {
				pos.add(new Position(a.getAtom().getPredicate().getPredicateSymbol(), i + 1));
			}
		}
		return pos;
	}

	public static IRule factoriseQuery(final IRule q, final Map<IVariable, ITerm> map) {

		// The list containing the literals for q'
		final Set<ILiteral> qPrimeBodyLiterals = new HashSet<ILiteral>();
		final Set<ILiteral> qPrimeHeadLiterals = new HashSet<ILiteral>();

		// Create and return the rewritten query q'
		final IBasicFactory bf = BasicFactory.getInstance();

		// For each literal in the body of q
		for (final ILiteral curLit : q.getBody()) {
			// add the non unified atoms of q to q'
			qPrimeBodyLiterals.add(bf.createLiteral(RewritingUtils.applyMGU(curLit.getAtom(), map)));
		}

		// for each literal (should be one) in the head of q
		for (final ILiteral curLit : q.getHead()) {
			// Apply the unification also to the head
			qPrimeHeadLiterals.add(bf.createLiteral(RewritingUtils.applyMGU(curLit.getAtom(), map)));
		}

		final IRule factor = bf.createRule(qPrimeHeadLiterals, qPrimeBodyLiterals);

		return (factor);
	}

	public static List<IRule> getTGDs(final List<IRule> rules, final List<IQuery> queryHeads) {

		final List<IRule> output = new UniqueList<IRule>();

		for (final IRule r : rules) {
			boolean tgd = true;

			// Check for storage predicate in the body
			for (final ILiteral l : r.getBody()) {
				if (l.getAtom().getPredicate().getPredicateSymbol().startsWith("@")) {
					tgd = false;
				}
			}

			// Check for builtin predicates, EGDs and negative Constraints.
			for (final ILiteral l : r.getHead())
				if (!l.isPositive() || l.getAtom().isBuiltin()) {
					tgd = false;
				} else {
					// Check whether this rule is a query definition
					for (final IQuery q : queryHeads)
						if (q.getLiterals().contains(l)) {
							tgd = false;
						}
				}

			// Return the tgd
			if (tgd) {
				output.add(r);
			}
		}
		return (output);
	}

	public static List<IRule> getSBoxRules(final List<IRule> rules, final List<IQuery> queryHeads) {

		final List<IRule> output = new UniqueList<IRule>();

		for (final IRule r : rules) {
			// Check for storage predicate in the body
			for (final ILiteral l : r.getBody()) {
				if (l.getAtom().getPredicate().getPredicateSymbol().startsWith("@")) {
					output.add(r);
				}
			}
		}
		return (output);
	}

	public static List<IRule> getQueries(final List<IRule> bodies, final List<IQuery> queryHeads) {
		final List<IRule> output = new UniqueList<IRule>();
		for (final IRule r : bodies) {
			for (final IQuery q : queryHeads)
				if (r.getHead().iterator().next().equals(q.getLiterals().get(0))) {
					output.add(r);
				}
		}

		return (output);
	}

	public static Set<IRule> getConstraints(final List<IRule> rules, final List<IQuery> queryHeads) {

		final Set<IRule> output = Sets.newHashSet();
		for (final IRule r : rules) {
			for (final ILiteral l : r.getHead())
				if (!l.isPositive() || l.getAtom().isBuiltin()) {
					for (final IQuery q : queryHeads)
						if (!q.getLiterals().contains(l)) {
							final Set<ILiteral> head = Sets.newHashSet(Factory.BASIC.createLiteral(true,
							        Factory.BASIC.createPredicate("Q_CNS", 0), Factory.BASIC.createTuple()));
							final Set<ILiteral> body = Sets.newHashSet();
							body.add(Factory.BASIC.createLiteral(true, l.getAtom()));
							body.addAll(r.getBody());
							output.add(Factory.BASIC.createRule(head, body));
						}
				}
		}
		return (output);
	}

	public static Map<IVariable, ITerm> invertSubstitution(final Map<IVariable, ITerm> m) {

		final Map<IVariable, ITerm> map = new LinkedHashMap<IVariable, ITerm>();

		for (final IVariable v : m.keySet()) {
			final ITerm t = m.get(v);
			if (t instanceof IVariable) {
				map.put((IVariable) m.get(v), v);
			}
		}
		return (map);
	}

	public static Set<IRule> queryDecomposition(final IRule query, final List<IRule> tgds,
	        final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> posDeps) {

		//LOGGER.debug("Computing affected positions.");
		final Map<IPosition, Set<IRule>> exPos = DepGraphUtils.computeAffectedPositions(tgds, posDeps);

		//LOGGER.debug("Get the components.");
		final Set<IRule> queryComponents = decomposeQuery(query, exPos, tgds);

		return queryComponents;
	}

	static IRule createReconciliationRule(final IRule query, final Set<IRule> queryComponents) {
		final IBasicFactory factory = BasicFactory.getInstance();

		final List<ILiteral> body = new LinkedList<ILiteral>();
		for (final IRule comp : queryComponents) {
			body.add(comp.getHead().iterator().next());
		}

		return factory.createRule(query.getHead(), body);
	}

	public static Set<IRule> decomposeQuery(final IRule query, final Map<IPosition, Set<IRule>> exPos,
	        final List<IRule> tgds) {

		Set<IRule> queryComponents = new LinkedHashSet<IRule>();

		final Set<PositionJoin> exJoins = DepGraphUtils.computeExistentialJoins(query, tgds, exPos);
		final Set<PositionJoin> joins = DepGraphUtils.computePositionJoins(query);

		if (exJoins.containsAll(joins) || (query.getBody().size() == 1)) {
			queryComponents.add(query);
			return queryComponents;
		} else if (exJoins.isEmpty()) {
			// each atom is a component
			final Set<Set<ILiteral>> decomposition = Sets.newLinkedHashSet();
			for (final ILiteral l : query.getBody()) {
				decomposition.add(ImmutableSet.of(l));
			}
			queryComponents = constructQueryComponents(query, exPos, decomposition);
		} else {
			// explore the decomposition space
			final Set<Set<Set<ILiteral>>> currentLevelDecompositions = new LinkedHashSet<Set<Set<ILiteral>>>();

			// create a level 0 decomposition (i.e., only singletons)
			final Set<ILiteral> body = query.getBody();
			final Set<Set<ILiteral>> decomposition = new LinkedHashSet<Set<ILiteral>>();
			for (final ILiteral l : body) {
				decomposition.add(ImmutableSet.of(l));
			}
			currentLevelDecompositions.add(decomposition);

			int level = 1;
			do {
				Set<Set<Set<ILiteral>>> nextLevelDecompositions = new LinkedHashSet<Set<Set<ILiteral>>>();

				for (final Set<Set<ILiteral>> currentDecomposition : currentLevelDecompositions) {
					// check validity of the decomposition
					queryComponents = constructQueryComponents(query, exPos, currentDecomposition);
					if (validDecomposition(queryComponents, exJoins, tgds, exPos))
						return queryComponents;
				}
				// compute next-level decompositions
				nextLevelDecompositions = mergeDecompositions(currentLevelDecompositions);
				currentLevelDecompositions.addAll(nextLevelDecompositions);
				nextLevelDecompositions.clear();
				level++;
			} while (level < body.size());
		}
		return queryComponents;
	}

	private static Set<Set<Set<ILiteral>>> mergeDecompositions(final Set<Set<Set<ILiteral>>> currentLevelDecompositions) {

		final Set<Set<Set<ILiteral>>> next = new LinkedHashSet<Set<Set<ILiteral>>>();

		for (final Set<Set<ILiteral>> decomposition : currentLevelDecompositions) {
			// merge the components of the decomposition to create one set of literal less than the current
			// decomposition
			for (final Set<ILiteral> comp1 : decomposition) {
				for (final Set<ILiteral> comp2 : decomposition) {
					if (!comp1.equals(comp2)) {
						final Set<Set<ILiteral>> mergedDecomposition = new LinkedHashSet<Set<ILiteral>>();
						mergedDecomposition.addAll(decomposition);
						final Set<ILiteral> mergedComponent = Sets.newLinkedHashSet(comp1);
						mergedComponent.addAll(comp2);
						mergedDecomposition.add(mergedComponent);
						mergedDecomposition.remove(comp1);
						mergedDecomposition.remove(comp2);
						next.add(mergedDecomposition);
					}
				}
			}
		}
		return next;
	}

	static Set<IRule> constructQueryComponents(final IRule query, final Map<IPosition, Set<IRule>> exPos,
	        final Set<Set<ILiteral>> set) {

		final IBasicFactory factory = BasicFactory.getInstance();
		final Set<IRule> out = new LinkedHashSet<IRule>();

		// final ITerm paddingConstant = Factory.TERM.createString("*");

		int count = 1;
		for (final Set<ILiteral> s : set) {

			final Set<IVariable> sVars = variablesFrom(s);
			final Set<IVariable> headVars = query.getHeadVariables();

			final List<ITerm> propVars = Lists.newLinkedList();
			for (final IVariable v : sVars) {

				if (headVars.contains(v)
				        || (query.isShared(v) && !Sets.difference(query.getBodyPositions(ImmutableSet.of(v)),
				                exPos.keySet()).isEmpty())) {
					propVars.add(v);
				}
			}

			final String headPredSym = query.getHead().iterator().next().getAtom().getPredicate().getPredicateSymbol();
			final IPredicate headPred = factory.createPredicate(headPredSym.concat("_" + count), propVars.size());

			final ILiteral head = factory.createLiteral(true, headPred, factory.createTuple(propVars));

			final IRule comp = factory.createRule(ImmutableList.of(head), s);

			out.add(comp);
			count++;
		}

		return out;
	}

	private static Set<IVariable> variablesFrom(final Set<ILiteral> literals) {
		final Set<IVariable> vars = new LinkedHashSet<IVariable>();

		for (final ILiteral l : literals) {
			vars.addAll(variablesFrom(l));
		}
		return vars;
	}

	private static Set<IVariable> variablesFrom(final ILiteral l) {

		final Set<IVariable> vars = new LinkedHashSet<IVariable>();
		for (final ITerm t : l.getAtom().getTuple()) {
			if (t instanceof IVariable) {
				vars.add((IVariable) t);
			}
		}
		return vars;
	}

	private static boolean validDecomposition(final Set<IRule> components, final Set<PositionJoin> exJoins,
	        final List<IRule> tgds, final Map<IPosition, Set<IRule>> exPos) {

		final Set<PositionJoin> compExJoins = new LinkedHashSet<PositionJoin>();
		for (final IRule c : components) {
			compExJoins.addAll(DepGraphUtils.computeExistentialJoins(c, tgds, exPos));
		}
		return compExJoins.equals(exJoins);
	}

	public static Collection<IRule> unfold(final IRule reconciliationQuery, final Map<String, Set<IRule>> rewritingMap,
	        final Set<IRule> cns) {

		final Collection<IRule> unfolded = new ArrayList<IRule>();
		unfolded.add(RenamingUtils.canonicalRenaming(reconciliationQuery, "U"));

		List<IRule> temp;
		for (final String key : rewritingMap.keySet()) {
			
			temp = ImmutableList.copyOf(unfolded);
			unfolded.clear();
			// get the corresponding expansions
			final Set<IRule> rewritings = rewritingMap.get(key);
			for (final IRule r : temp) {
				for (final ILiteral t : r.getBody()) {
					if (t.getAtom().getPredicate().getPredicateSymbol().equals(key)) {
						// possible expansion
						final Map<IVariable, ITerm> gamma = new LinkedHashMap<IVariable, ITerm>();
						for (IRule exp : rewritings) {
							exp = RenamingUtils.canonicalRenaming(exp, "V");
							if (TermMatchingAndSubstitution.unify(t.getAtom(), exp.getHead().iterator().next()
							        .getAtom(), gamma)) {
								final IRule qPrime = RenamingUtils.canonicalRenaming(
								        RewritingUtils.rewrite(r, t.getAtom(), exp.getBody(), gamma), "U");
								unfolded.add(qPrime);
							}
						}
					}
				}
			}

		}
		return unfolded;
	}

	public static void purgeConstraintsViolations(final QueryGraph queryGraph, final Set<IRule> cns) {

		for (final IRule r : queryGraph.getRules()) {
			for (final IRule c : cns) {
				if (r.getBodyPredicates().containsAll(c.getBodyPredicates())) {
					if (r.getBody().containsAll(c.getBody())) {
						queryGraph.removeAndBypass(r);
					}
					if (RewritingUtils.mapsTo(RenamingUtils.canonicalRenaming(c.getBody(), "V"), r.getBody())) {
						queryGraph.removeRule(r);
					}
				}
			}
		}
	}

	public static boolean resolves(final IRule r1, final IRule r2, final Map<IVariable, ITerm> gamma) {

		for (final ILiteral l1 : r1.getHead()) {
			for (final ILiteral l2 : r2.getBody())
				if (TermMatchingAndSubstitution.unify(l1.getAtom(), l2.getAtom(), gamma))
					return true;
		}
		return false;
	}

	public static boolean resolves(final IRule r, final IRule q, final IAtom a, final Map<IVariable, ITerm> gamma) {

		// check if the head of the rule unifies with the atom a
		return (TermMatchingAndSubstitution.unify(a, r.getHead().iterator().next().getAtom(), gamma));
	}

	public static boolean isApplicable(final IRule r, final IRule q, final IAtom a, final Map<IVariable, ITerm> gamma) {

		// check if the head of the rule unifies with the atom a
		if (!TermMatchingAndSubstitution.unify(a, r.getHead().iterator().next().getAtom(), gamma))
			return (false);
		else {
			if (r.getExistentialPositions().size() > 0) {
				// test if the shared variables in q will be preserved by the rewriting
				// For each term in a
				for (int i = 0; i < a.getTuple().size(); i++) {
					if ((a.getTuple().get(i).isGround() || q.isShared(a.getTuple().get(i)))
					        && !(r.getBodyVariables().contains(r.getHead().iterator().next().getAtom().getTuple()
					                .get(i))))
						return (false);
				}
			}
		}
		return (true);
	}

	public static IRule rewrite(final IRule resolvent, final IAtom resolvedAtom, final Set<ILiteral> resolver,
	        final Map<IVariable, ITerm> gamma) {

		// The list containing the literals for q'
		final Set<ILiteral> qPrimeHeadLiterals = new LinkedHashSet<ILiteral>();
		final Set<ILiteral> qPrimeBodyLiterals = new LinkedHashSet<ILiteral>();

		final IBasicFactory bf = BasicFactory.getInstance();

		// Apply the MGU also to the head of the query
		for (final ILiteral l : resolvent.getHead()) {
			qPrimeHeadLiterals.add(bf.createLiteral(RewritingUtils.applyMGU(l.getAtom(), gamma)));
		}

		// Rewrite the atom a in the query q with the atoms in body producing a query q'
		// For each literal in the body of q
		for (final ILiteral l : resolvent.getBody()) {
			if (!l.getAtom().equals(resolvedAtom)) {
				qPrimeBodyLiterals.add(bf.createLiteral(RewritingUtils.applyMGU(l.getAtom(), gamma)));
			}
		}
		for (final ILiteral l : resolver) {
			qPrimeBodyLiterals.add(bf.createLiteral(RewritingUtils.applyMGU(l.getAtom(), gamma)));
		}

		return (bf.createRule(qPrimeHeadLiterals, qPrimeBodyLiterals));
	}

	public static void purgeSubsumed(final QueryGraph queryGraph) {

		final long pre = queryGraph.getRules().size();

		boolean purged;
		do {
			purged = true;
			final List<IRule> rules = queryGraph.getRules();
			IRule subsumed = null;
			for (int i = 0; (i < (rules.size() - 1)) && purged; i++) {
				for (int j = i + 1; (j < rules.size()) && purged; j++) {

					final IRule q1 = rules.get(i);
					final IRule q2 = rules.get(j);

					if (RewritingUtils.mapsTo(q1, q2)) {

						subsumed = q2;
						purged = false;

					}
					if (RewritingUtils.mapsTo(q2, q1)) {
						subsumed = q1;
						purged = false;

					}
				}
			}
			if ((subsumed != null) && !purged) {
				queryGraph.removeAndBypass(subsumed);
			}
		} while (!purged);

		final long post = queryGraph.getRules().size();

		rep.addToValue(RewMetric.SUBCHECKPURGE_COUNT, pre - post);
	}

	public static Set<Expressivity> getExpressivity(final List<IRule> tgds) {

		final Set<Expressivity> exprs = new HashSet<Expressivity>();
		if (ExpressivityChecker.isLinear(tgds)) {
			exprs.add(Expressivity.LINEAR);
		}
		if (ExpressivityChecker.isGuarded(tgds)) {
			exprs.add(Expressivity.GUARDED);
		}
		if (ExpressivityChecker.isSticky(tgds)) {
			exprs.add(Expressivity.STICKY);
		}

		return exprs;
	}

}
