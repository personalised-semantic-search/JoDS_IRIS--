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
package org.deri.iris.evaluation.topdown.sldnf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.builtins.IBuiltinAtom;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.builtins.EqualBuiltin;
import org.deri.iris.builtins.ExactEqualBuiltin;
import org.deri.iris.evaluation.topdown.ILiteralSelector;
import org.deri.iris.evaluation.topdown.ITopDownEvaluator;
import org.deri.iris.evaluation.topdown.MaximumRecursionDepthReachedException;
import org.deri.iris.evaluation.topdown.QueryWithSubstitution;
import org.deri.iris.evaluation.topdown.StandardLiteralSelector;
import org.deri.iris.evaluation.topdown.TopDownHelper;
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rules.RuleManipulator;
import org.deri.iris.rules.optimisation.ReOrderLiteralsOptimiser;
import org.deri.iris.rules.ordering.SimpleReOrdering;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.utils.TermMatchingAndSubstitution;

/**
 * Implementation of the SLDNF evaluator. Please keep in mind that SLDNF evaluation is not capable of any tabling and
 * can get trapped in an infinite loop. For details see 'Deduktive Datenbanken' by Cremers, Griefahn and Hinze (ISBN
 * 978-3528047009).
 * @author gigi
 */
public class SLDNFEvaluator implements ITopDownEvaluator {

	private static final int _MAX_NESTING_LEVEL = 45;

	/** Debug stuff */
	private final boolean DEBUG;

	private IQuery mInitialQuery;

	private final IFacts mFacts;

	private List<IRule> mRules;

	private static final String IRIS_DEBUG_FLAG = "IRIS_DEBUG";

	private static final RelationFactory srf = new RelationFactory();

	static final RuleManipulator rm = new RuleManipulator();

	/**
	 * Constructor
	 * @param facts one or many facts
	 * @param rules list of rules
	 */
	public SLDNFEvaluator(final IFacts facts, final List<IRule> rules) {
		mFacts = facts;
		mRules = new LinkedList<IRule>();
		final ReOrderLiteralsOptimiser rolo = new ReOrderLiteralsOptimiser();
		for (final IRule rule : rules) {
			mRules.add(rolo.optimise(rule));
		}
		final SimpleReOrdering sro = new SimpleReOrdering();
		mRules = sro.reOrder(mRules);

		// Check if the debug environment variable is set.
		DEBUG = System.getenv(IRIS_DEBUG_FLAG) != null;
	}

	/**
	 * Evaluate given query
	 */
	@Override
	public IRelation evaluate(final IQuery query) throws EvaluationException {
		// Process the query
		mInitialQuery = query;
		final IRelation relation = findAndSubstitute(query);

		if (DEBUG) {
			System.out.println("------------");
			System.out.println("Relation " + relation);
			System.out.println("Original Query: " + query);
		}

		return relation;
	}

	/**
	 * Return variables of the initial query
	 */
	@Override
	public List<IVariable> getOutputVariables() {
		return mInitialQuery.getVariables();
	}

	private IRelation findAndSubstitute(final IQuery query) throws EvaluationException {
		return findAndSubstitute(query, 0, false);
	}

	/**
	 * Find recursive the next subgoal (the next query) by matching the given rules and facts with the current query.
	 * @param query The current query that should match a rule head or a fact
	 * @param recursionDepth Current recursion depth
	 * @param inNegationAsFailureFlip <code>true</code> if this evaluation step uses NAF, <code>false</code> otherwise
	 * @return true if the query leads to a success node, false otherwise
	 * @throws EvaluationException If something went wrong
	 * @throws MaximumRecursionDepthReachedException since SLDNF evaluation does not detect infinite loops, this
	 *             exception is thrown at a nesting level of 25
	 */
	private IRelation findAndSubstitute(final IQuery query, int recursionDepth, final boolean inNegationAsFailureFlip)
	        throws EvaluationException, MaximumRecursionDepthReachedException {

		// To stop infinite loop, remove this later
		// when tabling is implemented
		if (recursionDepth >= _MAX_NESTING_LEVEL)
			throw new MaximumRecursionDepthReachedException(
			        "You may ran into an infinite loop. SLDNF evaluation does not support tabling.");

		String debugPrefix = getDebugPrefix(recursionDepth, inNegationAsFailureFlip);
		if (DEBUG) {
			System.out.println(debugPrefix + query);
		}

		// Selection Rule
		final ILiteralSelector standardSelector = new StandardLiteralSelector();
		final ILiteral selectedLiteral = standardSelector.select(query.getLiterals());

		if (selectedLiteral == null)
			throw new EvaluationException("The selected literal must not be null.");

		if (DEBUG) {
			System.out.println(debugPrefix + "Selected: " + selectedLiteral);
		}

		// The results are stored in this relation
		IRelation relationReturned;

		// Process selected literal
		// Every possibility (every child/subtree of this node) is stored in a
		// list, which is iterated later.
		if (selectedLiteral.isPositive()) { // Positive Query Literal - search
			// for a success node

			// Get all possible sub-queries (branches)
			final List<QueryWithSubstitution> subQueryList = new LinkedList<QueryWithSubstitution>(); // List
			// of
			// new
			// queries
			// (incl.
			// substitutions)
			// to
			// process,
			// generated
			// by
			// facts
			// and
			// rules
			subQueryList.addAll(getSubQueryList(query, selectedLiteral));

			relationReturned = srf.createRelation();

			for (final QueryWithSubstitution qws : subQueryList) { // process new
				// queries
				final IQuery newQuery = qws.getQuery();
				final Map<IVariable, ITerm> variableMap = qws.getSubstitution();

				if (DEBUG) {
					debugPrefix = getDebugPrefix(recursionDepth, inNegationAsFailureFlip);
					System.out.println(debugPrefix + "QWS: " + qws);
				}

				// Success node (empty clause)
				if (newQuery.getLiterals().isEmpty()) {
					final ITuple tuple = TopDownHelper.resolveTuple(query, variableMap);
					relationReturned.add(tuple);

					continue;
				}

				// Evaluate the new query (walk the subtree)
				final IRelation relationFromSubtree = findAndSubstitute(newQuery, ++recursionDepth,
				        inNegationAsFailureFlip);

				if (DEBUG) {
					System.out.println(debugPrefix + "Old query: " + query.getVariables() + query);
					System.out.println(debugPrefix + "New query: " + newQuery.getVariables() + newQuery + " | "
					        + variableMap);
					System.out.println(debugPrefix + "Subtree: " + relationFromSubtree);
				}

				if (relationFromSubtree.size() == 0) {
					if (!inNegationAsFailureFlip) {
						continue; // Failure node (subtree returned false) - try
						// next branch
					} else {
						if (DEBUG) {
							System.out.println(debugPrefix + "NAF FAILURE NODE " + relationReturned);
						}
						break; // Failure node, and we do NAF. So it is a
						       // success node
					}
				}

				// Subtree contains success node
				relationReturned.addAll(getFullSubgoalRelation(query, qws, relationFromSubtree));

				if (DEBUG) {
					System.out.println(debugPrefix + "Return: " + relationReturned);
				}
			}
		} else { // Negative query literal - NAF - Negation As Failure
			final ILiteral queryLiteralNAF = Factory.BASIC.createLiteral(true, selectedLiteral.getAtom());
			final IRelation relationFromNAFSubtree = findAndSubstitute(Factory.BASIC.createQuery(queryLiteralNAF),
			        ++recursionDepth, !inNegationAsFailureFlip);

			if (DEBUG) {
				System.out.println(debugPrefix + "NAF Subtree: " + relationFromNAFSubtree);
			}

			if (relationFromNAFSubtree.size() == 0) {
				// Subtree is a failure node
				// => since we do NAF here, this is a success node (or the
				// parent of one)
				// Process the rest of the query (the part without the negative
				// literal)

				relationReturned = srf.createRelation();

				// Remove the negated literal, ...
				final LinkedList<ILiteral> literalsWithoutNegatedLiteral = new LinkedList<ILiteral>(query.getLiterals());
				literalsWithoutNegatedLiteral.remove(selectedLiteral);

				final IQuery queryWithoutNegatedLiteral = Factory.BASIC.createQuery(literalsWithoutNegatedLiteral);

				if (DEBUG) {
					System.out.println(debugPrefix + "Rest of query: " + queryWithoutNegatedLiteral);
				}

				// Evaluate the rest of the query
				if (!queryWithoutNegatedLiteral.getLiterals().isEmpty()) {
					relationReturned = findAndSubstitute(queryWithoutNegatedLiteral, ++recursionDepth,
					        inNegationAsFailureFlip);
				} else {
					relationReturned.add(Factory.BASIC.createTuple());
				}

			} else {
				// Subtree contains a success node
				// => since we do NAF here, this is a failure node,
				// no matter what substitution was used in the subtree.
				relationReturned = srf.createRelation();
			}

			if (DEBUG) {
				System.out.println(debugPrefix + "Return: " + relationReturned);
			}

		}

		return relationReturned;
	}

	/**
	 * Get possible sub-queries of this node by evaluating built-ins or applying rules and facts.
	 * @param query current query
	 * @param selectedLiteral selected literal
	 * @return list where the new queries are stored
	 * @throws EvaluationException on failure
	 */
	private List<QueryWithSubstitution> getSubQueryList(final IQuery query, final ILiteral selectedLiteral)
	        throws EvaluationException {
		final List<QueryWithSubstitution> subQueryList = new LinkedList<QueryWithSubstitution>();

		final IAtom queryLiteralAtom = selectedLiteral.getAtom();

		if (queryLiteralAtom instanceof IBuiltinAtom) { // BuiltIn
			subQueryList.addAll(processBuiltin(query, selectedLiteral, queryLiteralAtom));
		} else { // Not BuiltIn
			subQueryList.addAll(processQueryAgainstFacts(query, selectedLiteral));
			subQueryList.addAll(processQueryAgainstRules(query, selectedLiteral));
		}
		return subQueryList;
	}

	/**
	 * Creates a relation by combining the relation from subgoal evaluation and the variable bindings of the current
	 * query. e.g. ?- p( ?X, ?Y, ?Z ) | ?- q( ?Y, ?Z ) // subgoal When a mapping ?X = 1 is already known, and the
	 * subgoal evaluation returned ( 2, 3 ) - which means that ?Y = 2 and ?Z = 3 - a relation ( 1, 2, 3 ) for the
	 * variables ( ?X, ?Y, ?Z ) will be created.
	 * @param query a query
	 * @param qws a query with proper substitution, which is a subgoal of <code>query</code>
	 * @param relationFromSubtree relation returned by evaluation the subgoal <code>qws</code>
	 * @return a relation which covers all variables in <code>query</code>
	 */
	private IRelation getFullSubgoalRelation(final IQuery query, final QueryWithSubstitution qws,
	        final IRelation relationFromSubtree) {

		final IRelation relation = srf.createRelation();

		final List<IVariable> queryVariableList = TopDownHelper.getVariables(query);
		final List<IVariable> newQueryVariableList = TopDownHelper.getVariables(qws.getQuery());

		if (relationFromSubtree.size() == 0) {
			// This is a success node
			final ITuple tuple = TopDownHelper.resolveTuple(query, qws.getSubstitution());
			relation.add(tuple);

		} else { // relationFromSubtree.size() != 0
			// (means that this is a parent of a success node)

			// For every branch of the subtree
			for (int i = 0; i < relationFromSubtree.size(); i++) {

				if (queryVariableList.isEmpty()) {
					// No variables, but success node
					// => create empty tuple (true)
					relation.add(Factory.BASIC.createTuple());
					continue;
				}

				final ITuple branchTuple = relationFromSubtree.get(i);

				assert !queryVariableList.isEmpty() : "The query MUST contain variables";
				assert branchTuple.getVariables().isEmpty() : "A success-branch MUST NOT have variables";

				final Map<IVariable, ITerm> variableMapFromSubtree = TopDownHelper.createVariableMapFromTupleAndQuery(
				        qws.getQuery(), branchTuple);

				final List<ITerm> termsPerTuple = new LinkedList<ITerm>();

				// Create a tuple for each success branch to build the returned
				// relation
				for (final IVariable var : queryVariableList) {
					// For every Variable of the original query, get the
					// mappings
					final ITerm termFromVariableMapping = qws.getSubstitution().get(var);

					if ((termFromVariableMapping == null) && newQueryVariableList.contains(var)) {
						// No Mapping: The subtree has computed a mapping

						// Extract the results we need
						ITerm termToAdd = null;
						if (branchTuple.isEmpty()) {
							termToAdd = var;
						} else if (branchTuple.size() != newQueryVariableList.size()) {
							continue;
						} else {
							termToAdd = branchTuple.get(newQueryVariableList.indexOf(var));
						}

						if (termToAdd == null) {
							// Resolution did not return a result. This could be
							// due to unsafe rules
							// Simply return the variable again, since the
							// variable won't be needed in the final result
							// anyway
							termToAdd = var;
						}

						termsPerTuple.add(termToAdd);

					} else if (termFromVariableMapping != null) {
						// There is a mapping for the variable (already computed
						// earlier on this stage)
						termsPerTuple.add(termFromVariableMapping);
					}

					if (termsPerTuple.size() == queryVariableList.size()) {
						ITuple tuple = Factory.BASIC.createTuple(termsPerTuple);
						tuple = TermMatchingAndSubstitution.substituteVariablesInToTuple(tuple, variableMapFromSubtree);
						relation.add(tuple);
					}
				}
			}
		}

		return relation;
	}

	/**
	 * Scans the knowledge base for rules that match the selected query literal. If a unifiable match was found the
	 * substitution and the new query will be saved and added to a list of new queries, which is returned.
	 * @param query the whole query
	 * @param selectedLiteral the selected literal
	 * @param queryVariableList list of unique variables in the query, in order of appearance
	 * @return list of queries with substitutions
	 * @throws EvaluationException on failure
	 */
	private List<QueryWithSubstitution> processQueryAgainstRules(final IQuery query, final ILiteral selectedLiteral)
	        throws EvaluationException {

		final List<QueryWithSubstitution> newQueryList = new LinkedList<QueryWithSubstitution>();

		for (final IRule rule : mRules) {
			final ILiteral ruleHead = rule.getHead().iterator().next();

			// Potential match?
			if (TopDownHelper.match(ruleHead, selectedLiteral)) {
				final Map<IVariable, ITerm> variableMapUnify = new HashMap<IVariable, ITerm>();
				final ITuple queryTuple = selectedLiteral.getAtom().getTuple();

				// Occur Check
				// Replace all variables of the rule with unused ones (variables
				// that are not in the query)
				final Map<IVariable, ITerm> variableMapForOccurCheck = TopDownHelper.getVariableMapForVariableRenaming(
				        rule, query);
				final IRule ruleAfterOccurCheck = TopDownHelper.replaceVariablesInRule(rule, variableMapForOccurCheck);

				final ITuple ruleHeadAfterOCTuple = ruleAfterOccurCheck.getHead().iterator().next().getAtom()
				        .getTuple(); // ruleHead changed

				// Unifiable?
				final boolean unifyable = TermMatchingAndSubstitution.unify(queryTuple, ruleHeadAfterOCTuple,
				        variableMapUnify);

				IQuery newQuery = query;
				if (unifyable) {
					// Replace the rule head with the rule body
					// This replacement has to be save, because we did an occur
					// check before
					newQuery = TopDownHelper.substituteRuleHeadWithBody(query, selectedLiteral, ruleAfterOccurCheck);
				}

				// Substitute the whole query
				newQuery = TopDownHelper.substituteVariablesInToQuery(newQuery, variableMapUnify);

				final QueryWithSubstitution qws = new QueryWithSubstitution(newQuery, variableMapUnify);
				newQueryList.add(qws);

			}
		}
		return newQueryList;
	}

	/**
	 * Scans the knowledge base for rules that match the selected query literal. If a unifiable match was found the
	 * substitution and the new query will be saved and added to a list of new queries, which is returned.
	 * @param query the whole query
	 * @param queryLiteral the selected literal
	 * @param queryVariableList list of unique variables in the query, in order of appearance
	 * @return list of queries with substitutions
	 */
	private List<QueryWithSubstitution> processQueryAgainstFacts(final IQuery query, final ILiteral queryLiteral) {

		final List<QueryWithSubstitution> newQueryList = new LinkedList<QueryWithSubstitution>();
		final List<Map<IVariable, ITerm>> variableMapList = new LinkedList<Map<IVariable, ITerm>>();
		if (getMatchingFacts(queryLiteral, variableMapList)) {
			for (final Map<IVariable, ITerm> variableMap : variableMapList) {
				// For every fact

				// Substitute the whole query
				final IQuery substitutedQuery = TopDownHelper.substituteVariablesInToQuery(query, variableMap);

				// Remove the fact, ...
				final LinkedList<ILiteral> literalsWithoutMatch = new LinkedList<ILiteral>(
				        substitutedQuery.getLiterals());
				literalsWithoutMatch.remove(queryLiteral);

				// Add the new query to the query list
				final IQuery newQuery = Factory.BASIC.createQuery(literalsWithoutMatch);

				final QueryWithSubstitution qws = new QueryWithSubstitution(newQuery, variableMap);
				newQueryList.add(qws);
			}
		}

		return newQueryList;
	}

	/**
	 * Process a builtin atom.
	 * @param query the whole query
	 * @param selectedQueryLiteral the selected literal
	 * @param queryLiteralAtom
	 * @return List of new queries and the associated substitutions
	 * @throws EvaluationException on failure
	 */
	private List<QueryWithSubstitution> processBuiltin(final IQuery query, final ILiteral selectedQueryLiteral,
	        final IAtom queryLiteralAtom) throws EvaluationException {
		final IBuiltinAtom builtinAtom = (IBuiltinAtom) queryLiteralAtom;
		final ITuple builtinTuple = builtinAtom.getTuple();
		final List<QueryWithSubstitution> newQueryList = new LinkedList<QueryWithSubstitution>();

		ITuple builtinEvaluation = null;
		boolean unifyable = false;
		boolean evaluationNeeded = false;

		final Map<IVariable, ITerm> varMapCTarg = new HashMap<IVariable, ITerm>();

		if ((builtinAtom instanceof EqualBuiltin) || (builtinAtom instanceof ExactEqualBuiltin)) {
			// UNIFICATION

			assert builtinTuple.size() == 2;
			unifyable = TermMatchingAndSubstitution.unify(builtinTuple.get(0), builtinTuple.get(1), varMapCTarg);

		} else {
			// EVALUATION - every builtin except EqualBuiltin
			evaluationNeeded = true;
		}

		try {
			builtinEvaluation = builtinAtom.evaluate(builtinTuple);
		} catch (final IllegalArgumentException iae) {
			// The builtin can't be evaluated yet, simply continue
		}

		final List<ILiteral> literalsWithoutBuiltin = new LinkedList<ILiteral>(query.getLiterals());
		literalsWithoutBuiltin.remove(selectedQueryLiteral);
		IQuery newQuery = Factory.BASIC.createQuery(literalsWithoutBuiltin);

		if (builtinEvaluation != null) {

			if (builtinTuple.getVariables().isEmpty()) {
				// Builtin tuple contained no variables, the result is
				// true or false, e.g. ADD(1, 2, 3) = true
				final QueryWithSubstitution qws = new QueryWithSubstitution(newQuery, new HashMap<IVariable, ITerm>());
				newQueryList.add(qws);

			} else {
				// Builtin tuple contained variables, so there is a
				// computed answer, e.g. ADD(1, 2, ?X) => ?X = 3
				final Map<IVariable, ITerm> varMap = new HashMap<IVariable, ITerm>();
				final Set<IVariable> variablesPreEvaluation = builtinTuple.getVariables();

				if (variablesPreEvaluation.size() != builtinEvaluation.size())
					throw new EvaluationException("Builtin Evaluation failed. Expected "
					        + variablesPreEvaluation.size() + " results, got " + builtinEvaluation.size());

				// Add every computed variable to the mapping
				int variableIndex = 0;
				for (final IVariable var : variablesPreEvaluation) {

					if (unifyable) { // unification
						varMap.putAll(varMapCTarg);
					} else if (evaluationNeeded) { // evaluation
						final ITerm termPostEvaluation = builtinEvaluation.get(variableIndex); // get evaluated term
						varMap.put(var, termPostEvaluation);
					} else { // no new query / branch
						variableIndex++;
						continue;
					}

					// add the new query to the query list
					newQuery = TopDownHelper.substituteVariablesInToQuery(newQuery, varMap);
					final QueryWithSubstitution qws = new QueryWithSubstitution(newQuery, varMap);
					newQueryList.add(qws);

					variableIndex++;
				}
			}
		} else if (unifyable) {
			// Builtin evaluation failed, unification succeeded
			// Take unify result as mapping
			final Map<IVariable, ITerm> varMap = new HashMap<IVariable, ITerm>();

			varMap.putAll(varMapCTarg);

			// add the new query to the query list
			newQuery = TopDownHelper.substituteVariablesInToQuery(newQuery, varMap);
			final QueryWithSubstitution qws = new QueryWithSubstitution(newQuery, varMap);
			newQueryList.add(qws);
		}

		return newQueryList;
	}

	/**
	 * Tries to find a fact that matches the given query. The variableMap will be populated if a matching fact was
	 * found.
	 * @param queryLiteral the given query
	 * @param variableMap a <i>Map</i> that stores the resulting substitution if a fact was found
	 * @return true if a matching fact is found, false otherwise
	 */
	private boolean getMatchingFacts(final ILiteral queryLiteral, final List<Map<IVariable, ITerm>> variableMapList) {

		// Check all the facts
		for (final IPredicate factPredicate : mFacts.getPredicates()) {
			// Check if the predicate and the arity matches
			if (TopDownHelper.match(queryLiteral, factPredicate)) {
				// We've found a match (predicates and arity match)
				// Is the QueryTuple unifiable with one of the FactTuples?

				final IRelation factRelation = mFacts.get(factPredicate);

				// Substitute variables into the query
				for (int i = 0; i < factRelation.size(); i++) {
					ITuple queryTuple = queryLiteral.getAtom().getTuple();
					boolean tupleUnifyable = false;
					final ITuple factTuple = factRelation.get(i);
					final Map<IVariable, ITerm> variableMap = new HashMap<IVariable, ITerm>();
					tupleUnifyable = TermMatchingAndSubstitution.unify(queryTuple, factTuple, variableMap);
					if (tupleUnifyable) {
						queryTuple = TermMatchingAndSubstitution.substituteVariablesInToTuple(queryTuple, variableMap);
						variableMapList.add(variableMap);
					}
				}
			}
		}
		if (variableMapList.isEmpty())
			return false; // No fact found

		return true;
	}

	/**
	 * Creates a debug prefix for nice output
	 * @param recursionDepth depth of recursion (0 = root)
	 * @param inNegationAsFailureFlip <code>true</code> is this a NAF tree, <code>false</code> otherwise
	 * @return debug prefix string
	 */
	private String getDebugPrefix(final int recursionDepth, final boolean inNegationAsFailureFlip) {
		// Debug prefix for proper output
		String debugPrefix = "";
		if (DEBUG) {
			for (int i = 0; i < recursionDepth; i++) {
				debugPrefix += "  ";
			}

			if (inNegationAsFailureFlip) {
				debugPrefix += "{NAF} ";
			}
		}
		return debugPrefix;
	}

}
