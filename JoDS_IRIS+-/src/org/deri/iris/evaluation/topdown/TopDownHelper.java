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
package org.deri.iris.evaluation.topdown;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.deri.iris.EvaluationException;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.builtins.IBuiltinAtom;
import org.deri.iris.api.terms.IConstructedTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.builtins.EqualBuiltin;
import org.deri.iris.builtins.ExactEqualBuiltin;
import org.deri.iris.evaluation.topdown.oldt.OLDTEvaluator;
import org.deri.iris.factory.Factory;
import org.deri.iris.rules.RuleManipulator;
import org.deri.iris.storage.IRelation;
import org.deri.iris.utils.TermMatchingAndSubstitution;

/**
 * Collection of helper functions for top-down evaluation strategies
 * @author gigi
 */
public class TopDownHelper {

	private static RuleManipulator rm = new RuleManipulator();

	/**
	 * Replaces a rule head in a Query with the rule body. Example: Query: ?- a(?X), b(?X), c(?X). Rule: b(?X) :- r(?X),
	 * s(?X). Return: ?- a(?X), r(?X), s(?X), c(?X). Note that the rule variables and the query variables MUST NOT
	 * intersect. If they do an <code>EvaluationException</code> is thrown. To avoid intersection, do an occur check
	 * first.
	 * @param query a query element that contains the rule head
	 * @param selectedLiteral the selected literal
	 * @param rule a rule element
	 * @return A new query element where the rule head is replaced with the rule body, if unifyable with the selected
	 *         literal.
	 * @throws EvaluationException
	 */
	public static IQuery substituteRuleHeadWithBody(final IQuery query, final ILiteral selectedLiteral, final IRule rule)
	        throws EvaluationException {
		final LinkedList<ILiteral> newLiterals = new LinkedList<ILiteral>();

		assert TopDownHelper.match(selectedLiteral, rule.getHead().iterator().next()) == true : "Selected literal MUST match rule head";

		for (final ILiteral queryLiteral : query.getLiterals()) {
			// Replace selected literal with rule body
			if (selectedLiteral.equals(queryLiteral)) {
				for (final ILiteral ruleBodyLiteral : rule.getBody()) {
					newLiterals.add(ruleBodyLiteral);
				}
			} else {
				newLiterals.add(queryLiteral);
			}
		}

		return Factory.BASIC.createQuery(newLiterals);
	}

	/**
	 * Replaces variables in a query. Example: Query: ?- a( ?X ), b( ?Y ), c( ?Z ). variableMap: { X => 3, Y => 9 }
	 * Return: ?- a( 3 ), b( 9 ), c( ?X ).
	 * @param query a query element
	 * @param variableMap a map of variables and terms
	 * @return A new query element where the matching variables are replaced with the terms from the
	 *         <code>variableMap</code>
	 */
	public static IQuery substituteVariablesInToQuery(final IQuery query, final Map<IVariable, ITerm> variableMap) {
		final LinkedList<ILiteral> substitutedLiterals = new LinkedList<ILiteral>();
		final LinkedList<ILiteral> literals = new LinkedList<ILiteral>();
		literals.addAll(query.getLiterals());

		for (final ILiteral queryLiteral : literals) {
			final ILiteral substitutedLiteral = TopDownHelper.substituteVariablesInToLiteral(queryLiteral, variableMap);
			substitutedLiterals.add(substitutedLiteral);
		}

		return Factory.BASIC.createQuery(substitutedLiterals);
	}

	/**
	 * Replaces Variables in a Literal, and gives back the substituted Literal
	 * @param literal a Literal element
	 * @param variableMap a map of variables and terms
	 * @return A new Literal element where the matching variables are replaced with the terms from the
	 *         <code>variableMap</code>
	 */
	public static ILiteral substituteVariablesInToLiteral(final ILiteral literal,
	        final Map<IVariable, ITerm> variableMap) {
		final ITuple tuple = literal.getAtom().getTuple();
		ITuple substitutedTuple = tuple;
		for (final IVariable variable : tuple.getVariables()) {
			if (variableMap.containsKey(variable)) {
				substitutedTuple = TermMatchingAndSubstitution.substituteVariablesInToTuple(tuple, variableMap);
			}
		}
		for (final ITerm term : tuple) {
			if (term instanceof IVariable) {
				// Variable
				final IVariable variable = (IVariable) term;
				if (variableMap.containsKey(variable)) {
					substitutedTuple = TermMatchingAndSubstitution.substituteVariablesInToTuple(tuple, variableMap);
				}
			} else

			if (term instanceof IConstructedTerm) {
				// Constructed Term
				final IConstructedTerm constructedTerm = (IConstructedTerm) term;
				final LinkedList<IVariable> variables = new LinkedList<IVariable>(constructedTerm.getVariables());

				// For all variables that are in the constructed term, do the
				// substitution
				for (final IVariable variable : variables) {
					if (variableMap.containsKey(variable)) {
						substitutedTuple = TermMatchingAndSubstitution.substituteVariablesInToTuple(tuple, variableMap);
					}
				}

			}
		}

		ILiteral substitutedLiteral = literal;
		if (literal.getAtom() instanceof IBuiltinAtom) {
			// Builtin
			IBuiltinAtom builtinAtom = (IBuiltinAtom) literal.getAtom();

			// Replace the original BuiltIn tuple with the substituted one
			// We need to do this via the RuleManipulator, because there
			// is no (easy) way to generate a BuiltinAtom - so we modify our
			// original
			for (int i = 0; i < substitutedTuple.size(); i++) {
				final ITerm remove = builtinAtom.getTuple().get(i);
				final ITerm replaceWith = substitutedTuple.get(i);
				builtinAtom = (IBuiltinAtom) rm.replace(builtinAtom, remove, replaceWith);
			}
			substitutedLiteral = Factory.BASIC.createLiteral(literal.isPositive(), builtinAtom);
		} else {
			// No builtin
			substitutedLiteral = Factory.BASIC.createLiteral(literal.isPositive(), literal.getAtom().getPredicate(),
			        substitutedTuple);
		}
		return substitutedLiteral;
	}

	/**
	 * Checks if two predicates match
	 * @param pred1 a predicate element
	 * @param pred2 a second predicate element
	 * @return <code>true</code> if the predicates have the same name and arity, <code>false</code> otherwise.
	 */
	public static boolean match(final IPredicate pred1, final IPredicate pred2) {
		if ((pred1.getArity() == pred2.getArity()) && pred1.equals(pred2))
			return true;

		return false;
	}

	/**
	 * Checks if two literals match
	 * @param lit1 a literal element
	 * @param lit2 a second literal element
	 * @return <code>true</code> if the predicates of the literals have the same name and arity, <code>false</code>
	 *         otherwise.
	 */
	public static boolean match(final ILiteral lit1, final ILiteral lit2) {
		return match(lit1.getAtom().getPredicate(), lit2.getAtom().getPredicate());
	}

	/**
	 * Wrapper. See <code>match(IPredicate pred1, IPredicate pred2)</code>.
	 * @param queryLiteral
	 * @param factPredicate
	 * @return <code>true</code> if the predicates of the literals have the same name and arity, <code>false</code>
	 *         otherwise.
	 */
	public static boolean match(final ILiteral queryLiteral, final IPredicate factPredicate) {
		return match(queryLiteral.getAtom().getPredicate(), factPredicate);
	}

	/**
	 * Get a list of variables, with no duplicates
	 * @param query the query
	 * @return A unique list of variables
	 */
	public static List<IVariable> getVariables(final IQuery query) {
		final Map<Integer, IVariable> varMap = new HashMap<Integer, IVariable>();
		final List<IVariable> variableList = query.getVariables(); // This list can
		// contain
		// variables twice
		final List<IVariable> uniqueList = new LinkedList<IVariable>();

		for (final IVariable var : variableList) {
			final Integer position = variableList.indexOf(var); // indexOf takes the
			// first occurrence
			if (varMap.put(position, var) == null) {
				uniqueList.add(var);
			}
		}

		return uniqueList;
	}

	/**
	 * Get a list of variables in the right order, with no duplicates
	 * @param tuple the ITuple tuple
	 * @return A unique list of variables. The list is empty if the tuple contains no variables.
	 */
	public static List<IVariable> getVariables(final ITuple tuple) {

		final List<IVariable> uniqueVarList = new LinkedList<IVariable>();

		for (int i = 0; i < tuple.size(); i++) {
			final ITerm term = tuple.get(i);
			if ((term instanceof IVariable) && !uniqueVarList.contains(term)) {
				uniqueVarList.add((IVariable) term);
			}
			// TODO gigi: check if recursion is needed here to get variables
			// inside constructed terms
		}

		return uniqueVarList;
	}

	/**
	 * Creates a variable map that will replace variable ?X with ?X1 if the variable occurs in both tuples.
	 * @param rule a rule
	 * @param query a query
	 * @return a variable map that maps the old variable names to the new ones
	 * @throws RuleUnsafeException thrown if the rule contains unbound variables
	 */
	public static Map<IVariable, ITerm> getVariableMapForVariableRenaming(final IRule rule, final IQuery query)
	        throws RuleUnsafeException {
		final Map<IVariable, ITerm> variableMapForOccurCheck = new HashMap<IVariable, ITerm>();

		final Set<IVariable> ruleVariables = getBodyVariables(rule);
		ruleVariables.addAll(getHeadVariables(rule));
		final List<IVariable> queryVariables = getVariables(query);

		for (final IVariable var : queryVariables) {

			if (!ruleVariables.contains(var)) {
				continue;
			}

			int i = 0;
			IVariable varRename = var;
			while (ruleVariables.contains(varRename)) {
				varRename = Factory.TERM.createVariable(var.getValue().toString() + ++i);
			}

			if (varRename != var) {
				variableMapForOccurCheck.put(var, varRename);
			}
		}

		return variableMapForOccurCheck;
	}

	/**
	 * Get all distinct variables in a rule head. No particular order is guaranteed.
	 * @param rule the rule
	 * @return a set of distinct variables
	 */
	private static Set<IVariable> getHeadVariables(final IRule rule) {
		return rule.getHead().iterator().next().getAtom().getTuple().getVariables();
	}

	/**
	 * Get all distinct variables in a rule body. No particular order is guaranteed.
	 * @param rule the rule
	 * @return a set of distinct variables
	 */
	private static Set<IVariable> getBodyVariables(final IRule rule) {
		final Set<IVariable> ruleVariables = new HashSet<IVariable>();
		for (final ILiteral ruleBodyLiteral : rule.getBody()) {
			ruleVariables.addAll(ruleBodyLiteral.getAtom().getTuple().getVariables());
		}

		return ruleVariables;
	}

	/**
	 * Given a query and a tuple, create a variable map with the corresponding variable mappings for each variable in
	 * the query.
	 * @param query the query
	 * @param branchTuple the tuple
	 * @return a new variable map containing mappings for all unique variables in <code>query</code>
	 */
	public static Map<IVariable, ITerm> createVariableMapFromTupleAndQuery(final IQuery query, final ITuple branchTuple) {

		final Map<IVariable, ITerm> variableMap = new HashMap<IVariable, ITerm>();

		// assert (getVariables(query).size() == branchTuple.size()) :
		// "Number of variables in query does not match tuple size.";
		if (getVariables(query).size() != branchTuple.size())
			return variableMap;

		int i = 0;
		for (final IVariable var : getVariables(query)) {
			if (branchTuple.isEmpty()) {
				variableMap.put(var, var);
			} else {
				variableMap.put(var, branchTuple.get(i++));
			}
		}

		return variableMap;
	}

	/**
	 * Given a query and a variable map, create a tuple with the corresponding terms for each variable in the query.
	 * @param query the query
	 * @param variableMap a map containing the variable mappings
	 * @return a new tuple
	 */
	@Deprecated
	public static ITuple createTupleFromQueryAndVariableMap(final IQuery query, final Map<IVariable, ITerm> variableMap) {

		assert query.getVariables().size() == variableMap.size() : "All variables must have a mapping";

		final List<ITerm> terms = new LinkedList<ITerm>();
		for (final IVariable var : query.getVariables()) {
			// For every Variable of the original query, get the mappings
			final ITerm term = variableMap.get(var);
			terms.add(term);
		}

		final ITuple tuple = Factory.BASIC.createTuple(terms);
		return tuple;
	}

	/**
	 * If <code>variableMap</code> contains variable mappings of variables that are contained in <code>query</code>,
	 * those mappings will be resolved and a proper <code>tuple</code> will be created and returned. e.g. query = ?-
	 * q(?X) variableMap = ?X = 1 return = (1)
	 * @param query a <code>IQuery</code> which can contain variables
	 * @param variableMap map that stores variable mappings
	 * @return a tuple containing the resolved variables of the query
	 */
	public static ITuple resolveTuple(final IQuery query, final Map<IVariable, ITerm> variableMap) {
		return resolveTuple(getVariables(query), variableMap, false);
	}

	public static ITuple resolveTuple(final ILiteral literal, final Map<IVariable, ITerm> variableMap) {
		return resolveTuple(getVariables(literal.getAtom().getTuple()), variableMap, false);
	}

	public static ITuple resolveTuple(final List<IVariable> varList, final Map<IVariable, ITerm> variableMap,
	        final boolean partialResult) {
		// Resolve evaluated variables
		final List<ITerm> resolvedVarList = new LinkedList<ITerm>();
		for (final IVariable var : varList) {
			final ITerm mappedTerm = variableMap.get(var);
			if (mappedTerm != null) { // If no mapping exists, do not add the
				// term to the tuple
				resolvedVarList.add(mappedTerm);
			} else if (partialResult) {
				resolvedVarList.add(var);
			}
		}

		// Create tuple from resolved variables
		final ITuple tuple = Factory.BASIC.createTuple(resolvedVarList);
		return tuple;
	}

	public static Map<IVariable, ITerm> mergeSubstitutions(final Map<IVariable, ITerm> deeperMap,
	        final Map<IVariable, ITerm> higherMap) {
		final Map<IVariable, ITerm> mergedMap = new HashMap<IVariable, ITerm>();
		final Map<IVariable, ITerm> deeperMapUnused = new HashMap<IVariable, ITerm>();
		deeperMapUnused.putAll(deeperMap);

		for (final Entry<IVariable, ITerm> e : higherMap.entrySet()) {
			ITerm substitutedTerm = null;
			final ITerm term = e.getValue();

			if (deeperMap.containsKey(e.getKey())) {
				deeperMapUnused.remove(e.getKey());
			}

			if (term instanceof IVariable) {
				deeperMapUnused.remove(term);
			}

			// TODO gigi: check if constructed terms need special treatment

			// if (term instanceof IConstructedTerm) {
			// IConstructedTerm ct = (IConstructedTerm)term;
			// for (IVariable var : ct.getVariables()) {
			// deeperMapUnused.remove(var);
			// }
			// }

			substitutedTerm = TermMatchingAndSubstitution.substituteVariablesInToTerm(term, deeperMap);
			assert substitutedTerm != null;
			mergedMap.put(e.getKey(), substitutedTerm);
		}

		mergedMap.putAll(deeperMapUnused);

		return mergedMap;
	}

	/**
	 * Replaces all variables in a rule with the mapped ones. Always modifies the head too.
	 * @param rule a rule
	 * @param variableMap a variable map
	 * @return rule with replaced variables
	 */
	public static IRule replaceVariablesInRule(final IRule rule, final Map<IVariable, ITerm> variableMap) {
		IRule replacedRule = rule;
		for (final Entry<IVariable, ITerm> entryPair : variableMap.entrySet()) {
			replacedRule = rm.replace(replacedRule, true, entryPair.getKey(), entryPair.getValue());
		}
		return replacedRule;
	}

	/**
	 * Does variable re-mapping. Inverse function to <code>replaceVariablesInRule(IRule, Map<IVariable, ITerm>)</code>
	 * @param rule a rule
	 * @param variableMap a variable map
	 * @return rule with replaced variables.
	 */
	public static IRule reMapVariablesInRule(final IRule rule, final Map<IVariable, ITerm> variableMap) {
		IRule replacedRule = rule;
		for (final Entry<IVariable, ITerm> entryPair : variableMap.entrySet()) {
			if (entryPair.getValue() instanceof IVariable) {
				replacedRule = rm.replace(replacedRule, true, entryPair.getValue(), entryPair.getKey());
			}
		}
		return replacedRule;
	}

	/**
	 * Creates a debug prefix for nice output
	 * @param recursionDepth depth of recursion (0 = root)
	 * @param inNegationAsFailureFlip <code>true</code> is this a NAF tree, <code>false</code> otherwise
	 * @return debug prefix string
	 */
	public static String getDebugPrefix(final int recursionDepth, final boolean inNegationAsFailureFlip) {
		// Debug prefix for proper output
		String debugPrefix = "";
		if (System.getenv(OLDTEvaluator.IRIS_DEBUG_FLAG) != null) {
			for (int i = 0; i < recursionDepth; i++) {
				debugPrefix += "  ";
			}

			if (inNegationAsFailureFlip) {
				debugPrefix += "{NAF} ";
			}
		}
		return debugPrefix;
	}

	public static void printDebug(final String msg, final int recursionDepth) {
		TopDownHelper.printDebug(msg, recursionDepth, false);
	}

	public static void printDebug(final String msg, final int recursionDepth, final boolean inNegationAsFailureFlip) {
		final String debugPrefix = getDebugPrefix(recursionDepth, inNegationAsFailureFlip);
		if (System.getenv(OLDTEvaluator.IRIS_DEBUG_FLAG) != null) {
			System.out.println(debugPrefix + msg);
		}
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
	public static IRelation getFullSubgoalRelation(final IQuery query, final QueryWithSubstitution qws,
	        final IRelation relationFromSubtree) {
		return getFullSubgoalRelation(query, qws.getQuery(), qws.getSubstitution(), relationFromSubtree);
	}

	public static IRelation getFullSubgoalRelation(final IQuery query, final IQuery subQuery,
	        final Map<IVariable, ITerm> substitution, final IRelation relationFromSubtree) {

		final IRelation relation = OLDTEvaluator.srf.createRelation();

		final List<IVariable> queryVariableList = getVariables(query);
		final List<IVariable> newQueryVariableList = getVariables(subQuery);

		if (relationFromSubtree.size() == 0) {
			// Do nothing...

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

				final Map<IVariable, ITerm> variableMapFromSubtree = createVariableMapFromTupleAndQuery(subQuery,
				        branchTuple);

				final List<ITerm> termsPerTuple = new LinkedList<ITerm>();

				// Create a tuple for each success branch to build the returned
				// relation
				for (final IVariable var : queryVariableList) {
					// For every Variable of the original query, get the
					// mappings
					final ITerm termFromVariableMapping = substitution.get(var);

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
	 * Process a builtin atom.
	 * @param query the whole query
	 * @param selectedQueryLiteral the selected literal
	 * @param queryLiteralAtom
	 * @return List of new queries and the associated substitutions
	 * @throws EvaluationException on failure
	 */
	public static List<QueryWithSubstitution> processBuiltin(final IQuery query, final ILiteral selectedQueryLiteral,
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
					newQuery = substituteVariablesInToQuery(newQuery, varMap);
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
			newQuery = substituteVariablesInToQuery(newQuery, varMap);
			final QueryWithSubstitution qws = new QueryWithSubstitution(newQuery, varMap);
			newQueryList.add(qws);
		}

		return newQueryList;
	}

}
