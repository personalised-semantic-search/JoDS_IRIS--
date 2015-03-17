/*
 * <Project Name>
 * <Project Description>
 * 
 * Copyright (C) 2010 ICT Institute - Politecnico di Milano, Via Ponzio 34/5, 20133 Milan, Italy.
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
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
import org.deri.iris.api.queryrewriting.IQueryRewriter;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.facts.IFacts;
import org.deri.iris.queryrewriting.DepGraphUtils;
import org.deri.iris.queryrewriting.NDMRewriter;
import org.deri.iris.queryrewriting.ParallelRewriter;
import org.deri.iris.queryrewriting.RewritingUtils;
import org.deri.iris.queryrewriting.SQLRewriter;
import org.deri.iris.queryrewriting.configuration.DecompositionStrategy;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.RewritingLanguage;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.storage.StorageManager;
import org.deri.iris.utils.UniqueList;

import com.google.gson.JsonArray;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> ICT Institute - Politecnico di Milano.
 * @version 0.1b
 */
public class SQLRewritingEvaluationStrategy implements IEvaluationStrategy {

	// TGDs
	private final List<IRule> mTGDs;
	// SBox (Storage Box) Rules
	private final List<IRule> mSBox;
	// Queries (in form of rules)
	private final List<IRule> mRuleQueries;
	// EGDs and Negative Constraints
	private final Set<IRule> mConstraints;

	// The Logger
	private static Logger logger;

	/**
	 * @param facts
	 * @param rules
	 * @param queries
	 * @param configuration
	 * @throws EvaluationException
	 */
	public SQLRewritingEvaluationStrategy(final IFacts facts, final List<IRule> rules, final List<IQuery> queries,
	        final Configuration configuration) throws EvaluationException {

		// Get the log4j logger for this class
		logger = Logger.getLogger(SQLRewritingEvaluationStrategy.class.getName());

		if (facts == null)
			throw new IllegalArgumentException("'facts' argument must not be null.");

		if (rules == null)
			throw new IllegalArgumentException("'rules' argument must not be null.");

		if (queries == null)
			throw new IllegalArgumentException("'queries' argument must not be null.");

		if (configuration == null)
			throw new IllegalArgumentException("'configuration' argument must not be null.");

		// Get the TGDs from the set of rules
		mTGDs = RewritingUtils.getTGDs(rules, queries);

		// Get the query bodies
		final List<IRule> bodies = new ArrayList<IRule>(rules);
		mRuleQueries = RewritingUtils.getQueries(bodies, queries);

		// Get the constraints from the set of rules
		mConstraints = RewritingUtils.getConstraints(rules, queries);

		// Get the SBox rules from the set of rules
		mSBox = RewritingUtils.getSBoxRules(rules, queries);

		// Check that the SBox program is Safe Datalog
		final IRuleSafetyProcessor ruleProc = new StandardRuleSafetyProcessor();
		ruleProc.process(mSBox);

	}

	@Override
	public IRelation evaluateQuery(final IQuery query, final List<IVariable> outputVariables)
	        throws ProgramNotStratifiedException, RuleUnsafeException, EvaluationException {

		// Get the Factories
		final IRelationFactory rf = new RelationFactory();

		// Get the Rewriter Engine
		final ParallelRewriter rewriter = new ParallelRewriter(DecompositionStrategy.DECOMPOSE, RewritingLanguage.UCQ,
		        SubCheckStrategy.TAIL, NCCheck.TAIL);

		// Get the rule corresponding to the query
		final IRule ruleQuery = getRuleQuery(query);
		logger.info("Executing Query: " + ruleQuery);

		final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils
		        .computePositionDependencyGraph(mTGDs);

		final Set<Expressivity> exprs = RewritingUtils.getExpressivity(mTGDs);

		// Compute the FO-Rewriting
		logger.info("Computing TBox Rewriting");
		float duration = -System.nanoTime();
		final Set<IRule> rewriting = rewriter.getRewriting(ruleQuery, mTGDs, mConstraints, deps, exprs);
		duration = ((duration + System.nanoTime()) / 1000000);
		logger.info(rewriting.size() + " rewritings produced in " + duration + " [ms]\n");
		int count = 0;
		for (final IRule r : rewriting) {
			logger.debug("(Qr" + ++count + ")" + r);
		}

		// Produce the rewriting according to the Nyaya Data Model
		final IQueryRewriter ndmRewriter = new NDMRewriter(mSBox);

		// Create a buffer for the output
		final IRelation result = rf.createRelation();

		// Get the SBox rewriting
		final Set<IRule> sboxRewriting = new LinkedHashSet<IRule>();
		logger.info("Computing SBox Rewriting");
		duration = -System.nanoTime();
		for (final IRule pr : rewriting) {
			sboxRewriting.addAll(ndmRewriter.getRewriting(pr));
		}
		duration = ((duration + System.nanoTime()) / 1000000);
		logger.info(sboxRewriting.size() + " rewritings produced in " + duration + " [ms]\n");
		count = 0;
		for (final IRule r : sboxRewriting) {
			logger.debug("(Qn" + ++count + ")" + r);
		}

		// Produce the SQL rewriting for each query in the program
		final SQLRewriter sqlRewriter = new SQLRewriter(sboxRewriting);

		logger.info("Computing SQL Rewriting");
		try {
			// Get the SQL rewriting as Union of Conjunctive Queries (UCQ)
			duration = -System.nanoTime();
			final List<String> ucqSQLRewriting = sqlRewriter.getSQLRewritings("",10000,0);
			duration = ((duration + System.nanoTime()) / 1000000);
			logger.info(ucqSQLRewriting.size() + " queries produced in " + duration + " [ms]\n");
			count = 0;
			for (final String q : ucqSQLRewriting) {
				logger.debug("(Qs" + ++count + ")" + q);
			}

			// Execute the UCQ
			logger.info("Executing SQL Rewriting");
			duration = -System.nanoTime();
			for (final String q : ucqSQLRewriting) {
				result.addAll(StorageManager.executeQuery(q));
			}
			duration = ((duration + System.nanoTime()) / 1000000);
			logger.info(result.size() + " tuples in " + duration + " [ms]\n");

		} catch (final SQLException e) {
			e.printStackTrace();
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
			List<IVariable> outputVariables, JsonArray preferences, int k, PreferenceStrategy strategy)
			throws ProgramNotStratifiedException, RuleUnsafeException,
			EvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

}
