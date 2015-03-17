package org.deri.iris.evaluation.forewriting;

import group.models.PreferenceStrategy;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
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

import reader.Helper;
import JODS.PreferencesGraph;

import com.google.gson.JsonArray;

public class GroupSQLRewritingEvaluationStrategy implements IEvaluationStrategy {

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
	public GroupSQLRewritingEvaluationStrategy(final IFacts facts,
			final List<IRule> rules, final List<IQuery> queries,
			final Configuration configuration) throws EvaluationException {

		// Get the log4j logger for this class
		logger = Logger.getLogger(GroupSQLRewritingEvaluationStrategy.class
				.getName());
		if (facts == null)
			throw new IllegalArgumentException(
					"'facts' argument must not be null.");

		if (rules == null)
			throw new IllegalArgumentException(
					"'rules' argument must not be null.");

		if (queries == null)
			throw new IllegalArgumentException(
					"'queries' argument must not be null.");

		if (configuration == null)
			throw new IllegalArgumentException(
					"'configuration' argument must not be null.");

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
	public IRelation evaluateQuery(final IQuery query,
			final List<IVariable> outputVariables, JsonArray preferences,
			int k, PreferenceStrategy strategy) throws ProgramNotStratifiedException,
			RuleUnsafeException, EvaluationException, FileNotFoundException {

		// Get the Factories
		final IRelationFactory rf = new RelationFactory();

		// Get the Rewriter Engine
		final ParallelRewriter rewriter = new ParallelRewriter(
				DecompositionStrategy.DECOMPOSE, RewritingLanguage.UCQ,
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
		// the rewriting has all list of queries we are interested in at the
		// moment so from
		// here you should construct the dependency graph for each of the
		// elements
		final Set<IRule> rewriting = rewriter.getRewriting(ruleQuery, mTGDs,
				mConstraints, deps, exprs);

		duration = ((duration + System.nanoTime()) / 1000000);
		logger.info(rewriting.size() + " rewritings produced in " + duration
				+ " [ms]\n");
		int count = 0;
		for (final IRule r : rewriting) {
			logger.debug("(Qr" + ++count + ")" + r);
		}

		// Produce the rewriting according to the Nyaya Data Model
		final IQueryRewriter ndmRewriter = new NDMRewriter(mSBox);

		// Create a buffer for the output
		final IRelation result = rf.createRelation();

		//

		logger.info("Computing SBox Rewriting");
		duration = -System.nanoTime();
		Map<IPredicate, IRelation> results = new HashMap<IPredicate, IRelation>();
		for (final IRule pr : rewriting) {
			// Get the SBox rewriting
			final Set<IRule> sboxRewriting = new LinkedHashSet<IRule>();

			Set<IRule> rules = ndmRewriter.getRewriting(pr);
			// System.out.print("Comp"+pr+"  "+rules.toString());
			sboxRewriting.addAll(rules);

			duration = ((duration + System.nanoTime()) / 1000000);
			logger.info(sboxRewriting.size() + " rewritings produced in "
					+ duration + " [ms]\n");
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
				final List<String> ucqSQLRewriting = sqlRewriter
						.getSQLRewritings("", 10000, 0);
				duration = ((duration + System.nanoTime()) / 1000000);
				logger.info(ucqSQLRewriting.size() + " queries produced in "
						+ duration + " [ms]\n");
				count = 0;
				for (int i = 0; i < ucqSQLRewriting.size(); i++) {
					logger.debug("(Qs" + ++count + ")" + ucqSQLRewriting.get(i));
				}
				// Execute the UCQ
				logger.info("Executing SQL Rewriting");
				duration = -System.nanoTime();
				IRelation resultAux = rf.createRelation();
				for (final String q : ucqSQLRewriting) {
					IRelation r = StorageManager.executeQuery(q);
					if (System.getenv("IRIS_DEBUG") != null) {
						System.out.println("IRIS query");
						System.out.println("==========");
						System.out.println(q);
					}
					resultAux.addAll(r);

				}
				for (IPredicate predicate : pr.getBodyPredicates()) {
					results.put(predicate, resultAux);
				}
				result.addAll(resultAux);
				duration = ((duration + System.nanoTime()) / 1000000);
				logger.info(result.size() + " tuples in " + duration
						+ " [ms]\n");

			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
		// construct the graph
		List<group.models.Pair<IPredicate, IPredicate>> prefs = Helper
				.getPreferences(preferences, mTGDs);
		final PreferencesGraph prefGraph = org.deri.iris.factory.Factory.PGRAPH
				.createPreferencesGraph();

		for (group.models.Pair<IPredicate, IPredicate> pairPreference : prefs) {
			IRelation morePrefs = results.get(pairPreference.getElement0());
			IRelation lessPrefs = results.get(pairPreference.getElement1());
			for (int j = 0; j < morePrefs.size(); j++) {
				ITuple el1 = morePrefs.get(j);
				if (!lessPrefs.contains(el1)) {
					for (int i = 0; i < lessPrefs.size(); i++) {
						ITuple el2 = lessPrefs.get(i);
						prefGraph.addPreference(el1,el2);
					}
				}
			}
		}

		for (int i = 0; i < result.size(); i++) {
			ITuple v = result.get(i);
			prefGraph.addVertex(v);

		}
		System.out.println("Prefs edge: " + prefGraph.getEdgesSize()
				+ "vertex size " + prefGraph.getVertexesSize());

		return null;
	}

	/**
	 * @param query
	 *            the head of the query to be retrieved.
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
	public IRelation evaluateQuery(IQuery query, List<IVariable> outputVariables)
			throws ProgramNotStratifiedException, RuleUnsafeException,
			EvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

}
