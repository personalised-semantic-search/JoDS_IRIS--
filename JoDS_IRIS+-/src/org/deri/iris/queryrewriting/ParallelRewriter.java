/**
 * 
 */
package org.deri.iris.queryrewriting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.deri.iris.Expressivity;
import org.deri.iris.Reporter;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.queryrewriting.configuration.DecompositionStrategy;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.RewritingLanguage;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;

/**
 * @author Giorgio Orsi <giorgio.orsi@cs.ox.ac.uk> - Department of Computer Science, University of Oxford.
 * @version 1.0
 */
public class ParallelRewriter {

	Logger LOGGER = Logger.getLogger(ParallelRewriter.class);

	private final DecompositionStrategy decomposition;
	private final RewritingLanguage targetLanguage;
	private final SubCheckStrategy subchkStrategy;
	private final NCCheck ncchkStrategy;

	private final Reporter rep = Reporter.getInstance();

	public ParallelRewriter(final DecompositionStrategy decomposition, final RewritingLanguage targetLanguage,
	        final SubCheckStrategy subchkStrategy, final NCCheck ncchkStrategy) {
		this.decomposition = decomposition;
		this.targetLanguage = targetLanguage;
		this.subchkStrategy = subchkStrategy;
		this.ncchkStrategy = ncchkStrategy;
	}

	public Set<IRule> getRewriting(IRule query, final List<IRule> tgds, final Set<IRule> constraints,
	        final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps, final Set<Expressivity> exprs) {

		// choose the optimal expressivity
		final Expressivity expr = getPreferred(exprs);

		final QueryRewriterFactory qrf = QueryRewriterFactory.getInstance();

		//LOGGER.debug("Reducing query.");
		if (expr.equals(Expressivity.LINEAR)) {
			query = RewritingUtils.reduceQuery(query, deps);
		}

		//LOGGER.debug("Decomposing query.");
		final Set<IRule> components = new LinkedHashSet<IRule>();
		if (decomposition.equals(DecompositionStrategy.DECOMPOSE)) {

			// Decompose the query
			final long decompositionTime = System.currentTimeMillis();
			components.addAll(RewritingUtils.queryDecomposition(query, tgds, deps));
			rep.setValue(RewMetric.DECOMPOSITION_TIME, System.currentTimeMillis() - decompositionTime);
			rep.setValue(RewMetric.DECOMPOSITION_SIZE, (long) components.size());
		}

		//LOGGER.debug("Rewriting query.");
		final long rewTime = System.currentTimeMillis();
		final Set<IRule> rewriting = new LinkedHashSet<IRule>();
		if (components.size() > 1) {

			// Rewrite in parallel the components.
			final ExecutorService executor = Executors.newCachedThreadPool();
			final ExecutorCompletionService<Set<IRule>> compService = new ExecutorCompletionService<Set<IRule>>(
			        executor);
			final List<Future<Set<IRule>>> rewrittenComponents = new ArrayList<Future<Set<IRule>>>();

			for (final IRule comp : components) {

				final QueryRewriter rewriter = qrf.getRewriter(comp, tgds, constraints, deps, expr, subchkStrategy,
				        ncchkStrategy);

				//LOGGER.debug("spawned thread to process component: " + comp);
				final Future<Set<IRule>> submitted = compService.submit(rewriter);
				rewrittenComponents.add(submitted);
			}

			// Collect the results
			final Map<String, Set<IRule>> rewritingMap = new HashMap<String, Set<IRule>>();
			for (int i = 0; i < rewrittenComponents.size(); i++) {
				try {
					final Set<IRule> rewritten = compService.take().get();
					// get the component
					final IRule first = rewritten.iterator().next();
					final String key = first.getHeadPredicates().iterator().next().getPredicateSymbol();
					rewritingMap.put(key, rewritten);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				} catch (final ExecutionException e) {
					e.printStackTrace();
				}
			}
			// shutdown the executor
			executor.shutdown();

			// Merge the rewritten pieces
			final IRule reconciliationRule = RewritingUtils.createReconciliationRule(query, components);

			if (targetLanguage.equals(RewritingLanguage.UCQ)) {
				//LOGGER.debug("Unfolding the rewriting...");
				final long unfoldingTime = System.currentTimeMillis();
				rewriting.addAll(RewritingUtils.unfold(reconciliationRule, rewritingMap, constraints));
				rep.setValue(RewMetric.UNFOLD_TIME, System.currentTimeMillis() - unfoldingTime);
			} else {
				rewriting.add(reconciliationRule);
				for (final Set<IRule> rewComp : rewritingMap.values()) {
					rewriting.addAll(rewComp);
				}
			}
		} else {
			// Only one component --> no need for parallelization.
			final QueryRewriter rewriter = qrf.getRewriter(query, tgds, constraints, deps, expr, subchkStrategy,
			        ncchkStrategy);
			rewriting.addAll(rewriter.rewrite());
		}

		// //Checking subsumption
		// if (subchkStrategy.equals(SubCheckStrategy.TAIL)
		// || ((components.size() > 1) && (subchkStrategy.equals(SubCheckStrategy.INTRADEC) || subchkStrategy
		// .equals(SubCheckStrategy.INTRAREW)))) {
		// LOGGER.debug("Applying tail subsumption check on " + rewriting.size() + " queries.");
		// final long subCheckTime = System.currentTimeMillis();
		// rewriting = RewritingUtils.purgeSubsumed(rewriting, new QueryGraph());
		// rep.addToValue(RewMetric.SUBCHECK_TIME, System.currentTimeMillis() - subCheckTime);
		// }
		rep.setValue(RewMetric.REW_TIME, System.currentTimeMillis() - rewTime);
		return rewriting;
	}

	private Expressivity getPreferred(final Set<Expressivity> exprs) {
		if (exprs.contains(Expressivity.LINEAR))
			return Expressivity.LINEAR;
		else if (exprs.contains(Expressivity.STICKY))
			return Expressivity.STICKY;
		else
			return null;

	}
}
