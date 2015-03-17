/*
 * Integrated Rule Inference System (IRIS):
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.deri.iris.Reporter;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.Position;
import org.deri.iris.factory.Factory;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;
import org.deri.iris.utils.TermMatchingAndSubstitution;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A class to produce a conjunctive query rewriting of a datalog query w.r.t. a set of FO-reducible TGDs
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 */
public class FORewriter implements QueryRewriter {

	private static final Logger LOGGER = Logger.getLogger(FORewriter.class);
	protected static final Reporter rep = Reporter.getInstance();;

	protected final IRule query;
	protected final List<IRule> mRules;
	protected final Set<IRule> cns;
	protected final SubCheckStrategy subchkStrategy;
	protected final NCCheck ncchkStrategy;
	protected final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps;

	@Override
	public Set<IRule> call() throws Exception {
		return rewrite();
	}

	FORewriter(final IRule query, final List<IRule> rules, final Set<IRule> constraints,
	        final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> posDeps, final SubCheckStrategy subchkStrategy,
	        final NCCheck ncchkStrategy) {

		this.query = query;
		mRules = rules;
		cns = constraints;
		deps = posDeps;
		this.subchkStrategy = subchkStrategy;
		this.ncchkStrategy = ncchkStrategy;

	}

	@Override
	public Set<IRule> rewrite() {

		// partial rewritings (will be returned)
		final QueryGraph partialRewritings = new QueryGraph();

		// rewritten queries (mark all those queries that have been already used in a rewriting step)
		final Set<IRule> exploredQueries = new HashSet<IRule>();

		final List<IRule> newQueries = Lists.newArrayList(RenamingUtils.canonicalRenaming(query, "U"));

		// The input query is always part of the rewriting
		partialRewritings.addRule(newQueries.iterator().next());

		// The temporary rewritings
		List<IRule> tempRew;
		do {

			tempRew = ImmutableList.copyOf(newQueries);
			newQueries.clear();

			// For each new query q
			for (final IRule qn : tempRew) {

				// avoid double exploration
				if (!exploredQueries.contains(qn)) {
					exploredQueries.add(qn);

					// For each TGD
					for (IRule r : mRules) {

						r = RenamingUtils.canonicalRenaming(r, "V");

						IRule qPrime = Factory.BASIC.createRule(qn.getHead(), qn.getBody());

						/*
						 * Check if the new query factorizes w.r.t the TGD r
						 */
						if ((r.getExistentialVariables().size() > 0)
						        && (qPrime.getBody().size() > 1)
						        && qPrime.getPredicates().contains(
						                r.getHead().iterator().next().getAtom().getPredicate())) {
							final Map<IVariable, ITerm> sbstMap = new HashMap<IVariable, ITerm>();
							final long factorTime = System.currentTimeMillis();
							if (factorisable(qPrime, r, sbstMap)) {
								qPrime = RenamingUtils.canonicalRenaming(qPrime, "U");
								exploredQueries.add(qPrime);
							}
							rep.addToValue(RewMetric.FACTOR_TIME, (System.currentTimeMillis() - factorTime));
						}
						/*
						 * Apply the TGD until it is possible
						 */

						for (final ILiteral l : qPrime.getBody()) {
							// get the atom a
							final IAtom a = l.getAtom();

							final Map<IVariable, ITerm> gamma = new HashMap<IVariable, ITerm>();
							// Check if the rule is applicable to the atom a
							if (RewritingUtils.isApplicable(r, qPrime, a, gamma)) {

								// rewrite the atom a with the body of the rule sigma
								final IRule qrew = RenamingUtils.canonicalRenaming(
								        RewritingUtils.rewrite(qPrime, a, r.getBody(), gamma), "U");

								if (!exploredQueries.contains(qrew)) {
									newQueries.add(qrew);
									partialRewritings.addRule(qn, qrew);

								}

							}
						}
					}
				}
			}
			LOGGER.trace("|" + partialRewritings.size() + " [" + newQueries.size() + "]");
			rep.addToValue(RewMetric.GENERATED_QUERIES, (long) newQueries.size());

			// Checking subsumption
			if (subchkStrategy.equals(SubCheckStrategy.INTRAREW)) {
				LOGGER.debug("Applying intra-rewriting subsumption check on " + tempRew.size() + " queries.");
				final long subCheckTime = System.currentTimeMillis();
				RewritingUtils.purgeSubsumed(partialRewritings);
				rep.addToValue(RewMetric.SUBCHECK_TIME, System.currentTimeMillis() - subCheckTime);
			}
		} while (!newQueries.isEmpty());

		// Cleaning auxiliary predicates
		LOGGER.debug("Cleaning auxiliary predicates from " + partialRewritings.getRules().size() + " queries.");
		final long auxCleaningTime = System.currentTimeMillis();
		CleaningUtils.cleanRewriting(partialRewritings, new String[] { "aux_" });
		rep.addToValue(RewMetric.AUX_CLEANING_TIME, System.currentTimeMillis() - auxCleaningTime);
		LOGGER.debug("done.");

		if (subchkStrategy.equals(SubCheckStrategy.INTRADEC)) {
			LOGGER.debug("Applying intra-decomposition subsumption check on " + partialRewritings.getRules().size()
			        + " queries.");
			final long subCheckTime = System.currentTimeMillis();
			RewritingUtils.purgeSubsumed(partialRewritings);
			rep.addToValue(RewMetric.SUBCHECK_TIME, System.currentTimeMillis() - subCheckTime);
			LOGGER.debug("done.");
		}

		rep.addToValue(RewMetric.EXPLORED_QUERIES, (long) exploredQueries.size());
		return (ImmutableSet.copyOf(partialRewritings.getRules()));
	}

	/**
	 * Checks if the n atoms are sharing the same variables in all the existential positions
	 * @param q a conjunctive query
	 * @param r a TGD
	 * @param a1 the first atom
	 * @param a2 the second atom
	 * @return true if they share the same variables in all the existential positions
	 */
	protected boolean factorisable(IRule q, final IRule r, final Map<IVariable, ITerm> sbstMap) {

		rep.incrementValue(RewMetric.FACTOR_COUNT);

		if (q.getBody().size() > 1) {
			// Get the atoms in body(q) that unify with head(r).

			final IAtom rheadAtom = r.getHead().iterator().next().getAtom();
			final Set<IPosition> headExPos = r.getExistentialPositions();

			final Set<IAtom> potentialUnifiableAtoms = new LinkedHashSet<IAtom>();
			for (final ILiteral l : q.getBody()) {
				final IAtom qbodyAtom = l.getAtom();

				if (qbodyAtom.getPredicate().equals(rheadAtom.getPredicate())) {
					potentialUnifiableAtoms.add(qbodyAtom);
				}
			}
			if (potentialUnifiableAtoms.size() < 2)
				return false;
			else {

				// compute the powerset of atoms that are potentially unifiable in the body in the query.
				final Set<Set<IAtom>> atomsPowSet = Sets.powerSet(potentialUnifiableAtoms);
				// sort the set by size
				final List<Set<IAtom>> sortedPowSet = Lists.newArrayList(atomsPowSet);
				Collections.sort(sortedPowSet, new SetSizeComparator());

				for (final Set<IAtom> candidateSet : sortedPowSet) {
					// check that we have at least two atoms in the candidate set.
					if (candidateSet.size() > 1) {
						final Map<IVariable, ITerm> unifier = new HashMap<IVariable, ITerm>();
						if (TermMatchingAndSubstitution.unify(candidateSet, unifier)) {
							// the atoms have a unifier, check that there is a well-behaved existential variable

							// get variables in existential positions
							final Set<IVariable> variables = getVariablesInPositions(candidateSet, headExPos);
							for (final IVariable var : variables) {
								// check that the variable does not occur in non-existential positions
								if (headExPos.containsAll(q.getPositions(var))
								        && containedInAllAtoms(var, candidateSet)) {
									q = RewritingUtils.factoriseQuery(q, unifier);
									return true;
								}
							}

						}
					}
				}

				return false;
			}
		} else
			return false;
	}

	protected boolean containedInAllAtoms(final IVariable var, final Set<IAtom> candidateSet) {

		for (final IAtom atom : candidateSet) {
			if (!atom.getTuple().contains(var))
				return false;
		}
		return true;
	}

	protected Set<IVariable> getVariablesInPositions(final Set<IAtom> candidateSet, final Set<IPosition> positions) {
		final Set<IVariable> exPosVariables = new LinkedHashSet<IVariable>();
		for (final IAtom atom : candidateSet) {
			int pos = 0;
			for (final ITerm t : atom.getTuple()) {
				pos++;
				final IPosition curPos = new Position(atom.getPredicate().getPredicateSymbol(), pos);
				if ((t instanceof IVariable) && positions.contains(curPos)) {
					exPosVariables.add((IVariable) t);
				}
			}
		}
		return exPosVariables;
	}
}
