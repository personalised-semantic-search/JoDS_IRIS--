/**
 * 
 */
package org.deri.iris.queryrewriting;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * @author jd
 */
public class LinearRewriter extends FORewriter implements QueryRewriter {

	private static final Logger LOGGER = Logger.getLogger(LinearRewriter.class);

	@Override
	public Set<IRule> call() throws Exception {
		return rewrite();
	}

	LinearRewriter(final IRule query, final List<IRule> rules, final Set<IRule> constraints,
	        final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> posDeps, final SubCheckStrategy subchkStrategy,
	        final NCCheck ncCheckStrategy) {
		super(query, rules, constraints, posDeps, subchkStrategy, ncCheckStrategy);
	}

	@Override
	public Set<IRule> rewrite() {

		// partial rewritings (will be returned)
		final QueryGraph partialRewritings = new QueryGraph();

		// rewritten queries (mark all those queries that have been used in a rewriting step)
		final Set<IRule> exploredQueries = new HashSet<IRule>(5000000);

		final List<IRule> newQueries = Lists.newArrayList(RenamingUtils.canonicalRenaming(query, "U"));

		// The (reduced) input query is always part of the rewriting
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
						final long factorTime = System.currentTimeMillis();
						if ((r.getExistentialVariables().size() > 0)
						        && (qPrime.getBody().size() > 1)
						        && qPrime.getPredicates().contains(
						                r.getHead().iterator().next().getAtom().getPredicate())) {
							final Map<IVariable, ITerm> sbstMap = new HashMap<IVariable, ITerm>();

							if (factorisable(qPrime, r, sbstMap)) {
								qPrime = RenamingUtils.canonicalRenaming(RewritingUtils.reduceQuery(qPrime, deps), "U");
								exploredQueries.add(qPrime);
							}
						}
						rep.addToValue(RewMetric.FACTOR_TIME, (System.currentTimeMillis() - factorTime));
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
									final IRule qRed = RenamingUtils.canonicalRenaming(RewritingUtils.reduceQuery(qrew, deps),"U");
									if (!exploredQueries.contains(qRed)) {
										newQueries.add(qRed);
										partialRewritings.addRule(qn, qRed);
									}
								}

							}
						}
					}
				}
			}
			rep.addToValue(RewMetric.GENERATED_QUERIES, (long)newQueries.size());
			//LOGGER.trace("|" + partialRewritings.getRules().size() + " [" + newQueries.size() + "]");

			// Checking subsumption
			if (subchkStrategy.equals(SubCheckStrategy.INTRAREW)) {
				//LOGGER.debug("Applying intra-rewriting subsumption check on " + tempRew.size() + " queries.");
				final long subCheckTime = System.currentTimeMillis();
				RewritingUtils.purgeSubsumed(partialRewritings);
				rep.addToValue(RewMetric.SUBCHECK_TIME, System.currentTimeMillis() - subCheckTime);
			}
		} while (!newQueries.isEmpty());

		// Cleaning auxiliary predicates
		//LOGGER.debug("Cleaning auxiliary predicates from " + partialRewritings.getRules().size() + " queries.");
		final long auxCleaningTime = System.currentTimeMillis();
		CleaningUtils.cleanRewriting(partialRewritings, new String[] { "aux_" });
		rep.addToValue(RewMetric.AUX_CLEANING_TIME, System.currentTimeMillis() - auxCleaningTime);
		//LOGGER.debug("done.");

		if (subchkStrategy.equals(SubCheckStrategy.INTRADEC)) {
			//LOGGER.debug("Applying intra-decomposition subsumption check on " + partialRewritings.getRules().size()
			//        + " queries.");
			final long subCheckTime = System.currentTimeMillis();
			RewritingUtils.purgeSubsumed(partialRewritings);
			rep.addToValue(RewMetric.SUBCHECK_TIME, System.currentTimeMillis() - subCheckTime);
			//LOGGER.debug("done.");
		}

		if (ncchkStrategy.equals(NCCheck.INTRADEC)) {
			final long cnsViolationTime = System.currentTimeMillis();
			RewritingUtils.purgeConstraintsViolations(partialRewritings, cns);
			rep.addToValue(RewMetric.CNS_VIOLATION_TIME, System.currentTimeMillis() - cnsViolationTime);
		}

		rep.addToValue(RewMetric.EXPLORED_QUERIES, (long) exploredQueries.size());

		return (ImmutableSet.copyOf(partialRewritings.getRules()));
	}
}
