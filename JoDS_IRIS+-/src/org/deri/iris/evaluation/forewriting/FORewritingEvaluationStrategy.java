/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2008 Semantic Technology Institute (STI) Innsbruck, 
 * University of Innsbruck, Technikerstrasse 21a, 6020 Innsbruck, Austria.
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
package org.deri.iris.evaluation.forewriting;

import group.models.PreferenceStrategy;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.Expressivity;
import org.deri.iris.ProgramNotStratifiedException;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.evaluation.IEvaluationStrategyFactory;
import org.deri.iris.evaluation.stratifiedbottomup.StratifiedBottomUpEvaluationStrategyFactory;
import org.deri.iris.evaluation.stratifiedbottomup.naive.NaiveEvaluatorFactory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.queryrewriting.DepGraphUtils;
import org.deri.iris.queryrewriting.ParallelRewriter;
import org.deri.iris.queryrewriting.RewritingUtils;
import org.deri.iris.queryrewriting.configuration.DecompositionStrategy;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.RewritingLanguage;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.rules.safety.LinearReducibleRuleSafetyProcessor;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.utils.UniqueList;

import com.google.gson.JsonArray;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> ICT Institute - Politecnico di Milano.
 * @version 0.1b
 */
public class FORewritingEvaluationStrategy implements IEvaluationStrategy {

	// Configuration
	private final Configuration mConfig;

	// TGDs
	private final List<IRule> mTGDs;
	// Queries (in form of rules)
	private final List<IRule> mRuleQueries;
	// EGDs and Negative Constraints
	private final Set<IRule> mConstraints;
	// Facts
	private final IFacts mFacts;

	/**
	 * @param facts
	 * @param rules
	 * @param mRuleEvaluatorFactory
	 * @param configuration
	 */
	public FORewritingEvaluationStrategy(final IFacts facts, final List<IRule> rules, final List<IQuery> queries,
	        final Configuration configuration) throws EvaluationException {
		if (facts == null)
			throw new IllegalArgumentException("'facts' argument must not be null.");

		if (rules == null)
			throw new IllegalArgumentException("'rules' argument must not be null.");

		if (queries == null)
			throw new IllegalArgumentException("'queries' argument must not be null.");

		if (configuration == null)
			throw new IllegalArgumentException("'configuration' argument must not be null.");

		// Get the configuration
		mConfig = configuration;

		// Get the TGDs from the set of rules

		mTGDs = RewritingUtils.getTGDs(rules, queries);

		// Convert the query bodies in rules
		final List<IRule> bodies = new LinkedList<IRule>(rules);
		bodies.removeAll(mTGDs);

		mRuleQueries = RewritingUtils.getQueries(bodies, queries);

		// Get the constraints from the set of rules
		mConstraints = RewritingUtils.getConstraints(rules, queries);

		// Get the facts
		mFacts = facts;

		// Check whether the program is FO-Rewritable
		final IRuleSafetyProcessor ruleProc = new LinearReducibleRuleSafetyProcessor();
		ruleProc.process(mTGDs);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deri.iris.evaluation.IEvaluationStrategy#evaluateQuery(org.deri.iris.api.basics.IQuery, java.util.List)
	 */
	@Override
	public IRelation evaluateQuery(final IQuery query, final List<IVariable> outputVariables)
	        throws ProgramNotStratifiedException, RuleUnsafeException, EvaluationException {

		// Get the Factories
		final IBasicFactory bf = BasicFactory.getInstance();
		final IRelationFactory rf = new RelationFactory();

		// Get the Rewriter Engine
		final ParallelRewriter rewriter = new ParallelRewriter(DecompositionStrategy.DECOMPOSE, RewritingLanguage.UCQ,
		        SubCheckStrategy.TAIL, NCCheck.TAIL);

		// Get the rule corresponding to the query
		final IRule ruleQuery = getRuleQuery(query);

		final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils
		        .computePositionDependencyGraph(mTGDs);

		final Set<Expressivity> exprs = RewritingUtils.getExpressivity(mTGDs);

		// Compute the Rewriting
		final List<IQuery> newQueries = new UniqueList<IQuery>();
		final Set<IRule> rewriting = rewriter.getRewriting(ruleQuery, mTGDs, mConstraints, deps, exprs);
		for (final IRule qr : rewriting) {
			newQueries.add(bf.createQuery(qr.getHead().iterator().next()));
		}

		/*
		 * Change the rule-safety processor. Now the program is a Union of Conjunctive Queries (UCQ)
		 */
		mConfig.ruleSafetyProcessor = new StandardRuleSafetyProcessor();

		// Get the Bottom-up evaluator
		final IEvaluationStrategyFactory esf = new StratifiedBottomUpEvaluationStrategyFactory(
		        new NaiveEvaluatorFactory());
		final IEvaluationStrategy es = esf.createEvaluator(mFacts, new ArrayList<IRule>(rewriting), mConfig);

		final IRelation result = rf.createRelation();
		for (final IQuery q : newQueries) {
			result.addAll(es.evaluateQuery(q, outputVariables));
		}

		return (result);
	}

	/**
	 * @param query the head of the query to be retrieved.
	 * @return the definition of the query whose head is given as input.
	 */
	private IRule getRuleQuery(final IQuery query) {
		final IBasicFactory bf = BasicFactory.getInstance();

		for (final IRule r : mRuleQueries) {
			if (r.getHead().contains(query.getLiterals().get(0)))
				return (r);
		}
		// Return a Boolean Conjunctive Query (BCQ)
		return (bf.createRule(new UniqueList<ILiteral>(), query.getLiterals()));
	}

	@Override
	public IRelation evaluateQuery(IQuery query,
			List<IVariable> outputVariables, JsonArray preferences,int k, PreferenceStrategy strategy)
			throws ProgramNotStratifiedException, RuleUnsafeException,
			EvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

}
