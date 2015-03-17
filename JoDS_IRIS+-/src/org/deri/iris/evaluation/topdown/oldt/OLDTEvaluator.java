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
package org.deri.iris.evaluation.topdown.oldt;

import java.util.HashMap;
import java.util.HashSet;
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
import org.deri.iris.evaluation.topdown.FirstLiteralSelector;
import org.deri.iris.evaluation.topdown.ILiteralSelector;
import org.deri.iris.evaluation.topdown.IPredicateTagger;
import org.deri.iris.evaluation.topdown.ITopDownEvaluator;
import org.deri.iris.evaluation.topdown.RecursivePredicateTagger;
import org.deri.iris.evaluation.topdown.SafeStandardLiteralSelector;
import org.deri.iris.evaluation.topdown.TopDownHelper;
import org.deri.iris.factory.Factory;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rules.RuleManipulator;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.utils.TermMatchingAndSubstitution;

/**
 * Implementation of the OLDT evaluator. For details see 'Deduktive Datenbanken' by Cremers, Griefahn and Hinze (ISBN
 * 978-3528047009).
 * @author gigi
 */
public class OLDTEvaluator implements ITopDownEvaluator {

	/** Debug stuff */
	private final boolean DEBUG;

	private IQuery mInitialQuery;

	private final IFacts mFacts;

	private final List<IRule> mRules;

	private MemoTable mMemoTable;

	private final Set<IPredicate> mMemoPredicates;

	private final ILiteralSelector mLiteralSelector;

	public static final String IRIS_DEBUG_FLAG = "IRIS_DEBUG";

	public static final RelationFactory srf = new RelationFactory();

	static final RuleManipulator rm = new RuleManipulator();

	/**
	 * Constructor
	 * @param facts one or many facts
	 * @param rules list of rules
	 */
	public OLDTEvaluator(final IFacts facts, final List<IRule> rules) {
		// Initialize Facts and Rules
		mFacts = facts;
		mRules = rules;

		// Initialize Memo Predicates
		mMemoPredicates = new HashSet<IPredicate>();

		// Initialize Memo Table
		mMemoTable = new MemoTable();

		// Initialize Literal Selector
		mLiteralSelector = new SafeStandardLiteralSelector();

		// Check if the debug environment variable is set.
		DEBUG = System.getenv(IRIS_DEBUG_FLAG) != null;
	}

	public MemoTable getMemoTable() {
		return mMemoTable;
	}

	/**
	 * Evaluate given query
	 */
	@Override
	public IRelation evaluate(final IQuery query) throws EvaluationException {

		mInitialQuery = query;

		// Tag and get memo predicates
		final IPredicateTagger predicateTagger = new RecursivePredicateTagger(mRules, query);
		// IPredicateTagger predicateTagger = new AllPredicateTagger(mRules);
		mMemoPredicates.addAll(predicateTagger.getMemoPredicates());

		if (DEBUG) {
			System.out.println("------------");
			System.out.println("Memo predicates: " + mMemoPredicates);
			System.out.println("------------");
		}

		final Node root = new Node(query);
		root.registerAtMemoTable();
		root.evaluate();
		final IRelation relation = root.getEvaluation();

		if (DEBUG) {
			System.out.println("------------");
			System.out.println("Relation " + relation);
			System.out.println("Original Query: " + query);
			System.out.println("Memo Table: " + mMemoTable);
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

	/**
	 * Inner class representing a node in the proof tree.
	 */
	class Node {
		private Node predecessor;

		private List<Node> successors;

		private IQuery query;

		private Map<IVariable, ITerm> substitution;

		private NodeState state;

		private IRelation evaluation;

		private ILiteral selectedLiteral;

		private int pointer;

		private NodeType type;

		private int numLiteralsLeft;

		/**
		 * Literal count of parenting answer node - 1. If reached, the evaluation is pushed up to the answer node
		 */

		private Node() {
		}

		/**
		 * Root node. Has no substitution and no predecessor.
		 * @param query the initial query
		 * @throws EvaluationException on failure
		 */
		public Node(final IQuery query) throws EvaluationException {
			this();
			this.query = query;
			substitution = null;
			predecessor = null;
			state = NodeState.INITIALIZED;
			evaluation = srf.createRelation();
			selectedLiteral = mLiteralSelector.select(query.getLiterals());
			pointer = 0;
			type = NodeType.NORMAL;
			numLiteralsLeft = 0;

			successors = new LinkedList<Node>();
		}

		/**
		 * Standard constructor.
		 * @param predecessor predecessor node
		 * @param query sub-query related to this node
		 * @param substitution substitution that was applied to form this node
		 * @throws EvaluationException on failure
		 */
		public Node(final Node predecessor, final IQuery query, final Map<IVariable, ITerm> substitution)
		        throws EvaluationException {
			this(query);
			this.substitution = substitution;
			this.predecessor = predecessor;

			/*
			 * Count the literals of parent node. If a child node has ($PARENT_LITERALS - 1) literals, the selected
			 * literal was successfully eliminated.
			 * 
			 * This is needed for the solo refutation, hence literals are only counted for answer nodes.
			 */
			if (predecessor.isAnswerNode()) {
				numLiteralsLeft = predecessor.getQuery().getLiterals().size() - 1;
			}
			if (predecessor.numLiteralsLeft > 0) {
				numLiteralsLeft = predecessor.numLiteralsLeft;
			}

			if (getNumLiteralsLeft() == getQuery().getLiterals().size()) {
				soloRefutation(getSubstitution());
			}
		}

		@Override
		public String toString() {
			// return ((this.getPredecessor() == null) ? "" :
			// this.getPredecessor() + " -" + this.getSubstitution() + "-> ") +
			// this.getQuery();
			return getQuery().toString();
		}

		protected IRelation evaluate() throws EvaluationException {
			extendNodeOLD();
			extendNodeLink();
			return getEvaluation();
		}

		/**
		 * Extend the node and it's child-nodes by 'classical' OLD resolution. If the literal selection rule selects a
		 * memo predicate, the node is paused. If the selected literal can be evaluated, this node becomes a success
		 * node, a failure node, or a node containing child nodes.
		 * @throws EvaluationException
		 */
		private void extendNodeOLD() throws EvaluationException {
			if (isEvaluated() || isPaused())
				return;

			setState(NodeState.EVALUATING);

			if (getQuery().getLiterals().isEmpty()) {
				setState(NodeState.SUCCESS);
				printNodeDebug();

				pushTupleUp(getSubstitution()); // Push tuple up
				// recursively

				return; // Success node
			}

			ILiteral selectedLiteral = getSelectedLiteral();

			if (selectedLiteral == null) {
				// Additional selection
				// If SafeStandardLiteralSelector failed, take any literal
				final ILiteralSelector firstSelector = new FirstLiteralSelector();
				selectedLiteral = firstSelector.select(getQuery().getLiterals());

				if (selectedLiteral != null) {
					setSelectedLiteral(selectedLiteral);
				}
			}

			if (selectedLiteral == null) {
				setState(NodeState.FAILURE);
				printNodeDebugi();
				if (DEBUG) {
					System.out.println(" (literal selection not possible)");
				}
				return; // Literal selection not possible
			}

			if (!selectedLiteral.isPositive()) {
				// Negate the literal again to get a positive literal
				final ILiteral negatedLiteral = Factory.BASIC.createLiteral(true, selectedLiteral.getAtom());

				// Build up the NAF query
				final List<ILiteral> newLiteralList = new LinkedList<ILiteral>();
				newLiteralList.add(negatedLiteral);
				newLiteralList.addAll(getQuery().getLiterals());
				newLiteralList.remove(selectedLiteral);

				IQuery newQuery = Factory.BASIC.createQuery(newLiteralList);
				final Node nafNode = new Node(newQuery);
				if (DEBUG) {
					System.out.println("\n===================================");
				}
				final IRelation evaluation = nafNode.evaluate();
				if (DEBUG) {
					System.out.println("===================================\n");
				}
				printNodeDebug();
				if (DEBUG) {
					System.out.println("\t NAF evaluation: " + evaluation);
				}

				if ((evaluation == null) || (evaluation.size() == 0)) {
					// Since NAF failed, we can remove the negated literal
					newLiteralList.clear();
					newLiteralList.addAll(getQuery().getLiterals());
					newLiteralList.remove(selectedLiteral);

					newQuery = Factory.BASIC.createQuery(newLiteralList);
					final Node childNode = new Node(this, newQuery, new HashMap<IVariable, ITerm>());
					addChildNode(childNode);
				} else {
					setState(NodeState.FAILURE);
					printNodeDebug();
					return; // NAF returned a result, so this node fails.
				}
			}

			if (isPaused()) {
				if (DEBUG) {
					printNodeDebugi();
					System.out.println(" paused.");
				}
				return;
			}

			// Do the OLD extension if not done already
			if (!isEvaluated()) {
				addChildNodesOLD();
			}

			// Do the table registration for all new nodes
			for (final Node child : getSuccessors()) {
				if (child.getSelectedLiteral() != null) {
					child.registerAtMemoTable();
				}
			}

			// If the node has no successors and is no success node, it is a
			// failure node
			if (getSuccessors().isEmpty()) {
				setState(NodeState.FAILURE);
				printNodeDebug();
				return;
			}

			evaluateChilds();
		}

		/**
		 * Extend this node and it's child-nodes using answers stored in the memo table.
		 * @throws EvaluationException on failure
		 */
		private void extendNodeLink() throws EvaluationException {

			if (isLinkNode()) {
				while (hasUnprocessedMemoAnswer()) {
					// Use stored answers to evaluate paused nodes (aka link
					// nodes)
					final ITuple answer = mMemoTable.get(getSelectedLiteral().getAtom(), getPointer());
					incrementPointer();
					if (DEBUG) {
						printNodeDebug();
						System.out.println("\t link extension with " + answer);
					}

					final List<IVariable> varList = TopDownHelper.getVariables(getSelectedLiteral().getAtom()
					        .getTuple());

					if (answer.size() == varList.size()) {
						final Map<IVariable, ITerm> variableMapForSubstitution = new HashMap<IVariable, ITerm>();

						int j = 0;
						for (final IVariable var : varList) {
							variableMapForSubstitution.put(var, answer.get(j++));
						}

						// Substitute variable bindings into query
						IQuery newQuery = TopDownHelper.substituteVariablesInToQuery(getQuery(),
						        variableMapForSubstitution);

						// Remove selected Literal
						final List<ILiteral> newQueryLiterals = new LinkedList<ILiteral>();
						newQueryLiterals.addAll(newQuery.getLiterals());
						final int indexOfLiteralToRemove = getQuery().getLiterals().indexOf(getSelectedLiteral());

						newQueryLiterals.remove(indexOfLiteralToRemove);

						// Create a brand new query and add it as a child of the
						// current node
						newQuery = Factory.BASIC.createQuery(newQueryLiterals);
						final Node childNode = new Node(this, newQuery, variableMapForSubstitution);
						childNode.registerAtMemoTable();
						addChildNode(childNode);
						evaluateChild(childNode);

						// Update the memo table
						updateMemoTable();

					} else {
						// Answer does not match selected literal
						if (DEBUG) {
							printNodeDebugi();
							System.out.println(" has stored answer " + answer + " for " + getSelectedLiteral());
						}
					}
				}
			} else if (isParentOfLinkNode()) {
				for (final Node c : getSuccessors()) {
					c.extendNodeLink();
				}
			}
		}

		/**
		 * Determines the node type, as defined in <code>NodeType</code> NORMAL: default node type LINK: A paused node
		 * (link node). If the selected literal is a memo literal and is already in the memo table. ANSWER: A answer
		 * node. If the selected literal is a memo literal but occurred for the first time. Note: Actually, the
		 * predicates are stored in the memo table. Speaking of 'memo literals' refers to literals which consist of memo
		 * predicates.
		 */
		private void classifyNodeType() {
			final ILiteral selectedLiteral = getSelectedLiteral();

			if (selectedLiteral == null)
				return;

			final IPredicate selectedPredicate = selectedLiteral.getAtom().getPredicate();

			if (DEBUG) {
				printNodeDebug();
			}

			if (mMemoPredicates.contains(selectedPredicate)) {
				if (DEBUG) {
					System.out.print("\t" + selectedLiteral + " is a memo predicate! Already in Memo Table? ");
				}

				if (mMemoTable.containsKey(selectedLiteral.getAtom())) {

					// Memo predicate is already in memo table
					if (DEBUG) {
						System.out.print(" Yes. ");
					} // In memo table ==> Link Node
					setState(NodeState.PAUSED_N);
					setType(NodeType.LINK);
					if (DEBUG) {
						System.out.println("Changed type to [" + getType() + "] ");
					}

				} else {

					// Memo predicate is not in memo table
					if (DEBUG) {
						System.out.print(" No. ");
					} // In memo table ==> Answer Node
					setType(NodeType.ANSWER);
					if (DEBUG) {
						System.out.println("Changed type to [" + getType() + "] ");
					}
				}

			}
		}

		private void soloRefutation(Map<IVariable, ITerm> inputMap) {
			if ((inputMap == null) || (getSelectedLiteral() == null))
				return;

			final ITuple tupleToAddToRelation = TopDownHelper.resolveTuple(getSelectedLiteral(), inputMap);
			final IAtom atom = getSelectedLiteral().getAtom();

			if (isAnswerNode()) {
				if (DEBUG) {
					System.out.println("\t SR: adding " + tupleToAddToRelation + " to " + atom
					        + " by reverse substituting " + TopDownHelper.getVariables(atom.getTuple()) + " with "
					        + inputMap + " in " + getQuery());
				}
				if (tupleToAddToRelation.getVariables().isEmpty()) {
					// If there are variables in the tuple
					// due to pushing the tuple upwards in the tree,
					// do not add it to the memo table since it is irrelevant
					// (tuple was relevant for another node, further down in the
					// tree)
					mMemoTable.add(atom, tupleToAddToRelation);
				}
			}

			if (getSubstitution() != null) {
				inputMap = TopDownHelper.mergeSubstitutions(inputMap, getSubstitution());
			}

			if (getPredecessor() != null) {
				getPredecessor().soloRefutation(inputMap); // Push the
				// tuple
				// (actually the
				// variable map)
				// up one level
			}
		}

		/**
		 * Add the tuple which is generated relative to the unique variables of the query to the relation of this node
		 * @param inputMap variable map where the current substitutions are stored
		 */
		private void pushTupleUp(Map<IVariable, ITerm> inputMap) {

			final ITuple tupleToAddToRelation = TopDownHelper.resolveTuple(getQuery(), inputMap);
			if (DEBUG) {
				System.out.println("\t ...adding " + tupleToAddToRelation + " to " + getQuery()
				        + " by reverse substituting " + TopDownHelper.getVariables(getQuery()) + " with " + inputMap);
			}

			if (getEvaluation().contains(tupleToAddToRelation))
				return; // Node computed tuple earlier, no need to push it up
			// further

			if (tupleToAddToRelation.getVariables().isEmpty()) {
				addToEvaluation(tupleToAddToRelation);
			}

			if (getSubstitution() != null) {
				inputMap = TopDownHelper.mergeSubstitutions(inputMap, getSubstitution());
			}

			if (getPredecessor() != null) {
				getPredecessor().pushTupleUp(inputMap); // Push the tuple
				// (actually the
				// variable map) up
				// one level
			}

		}

		/**
		 * Get all successor nodes (child nodes) and evaluate them. Add the evaluations to the current node and update
		 * this node's state.
		 * @throws EvaluationException
		 */
		private void evaluateChilds() throws EvaluationException {
			boolean hasPausedChilds = false;

			// Expand child nodes
			if (!isEvaluated() || (isLinkNode() && hasUnprocessedMemoAnswer())) {
				for (final Node childNode : getSuccessors()) {
					NodeState childState = childNode.getState();
					if (!childNode.isEvaluated() || (childNode.isPaused() && childNode.hasUnprocessedMemoAnswer())) {
						childState = evaluateChild(childNode);
					}
					if ((childState == NodeState.PAUSED_N) || (childState == NodeState.PAUSED_C)) {
						hasPausedChilds = true;
					}
				}
			}

			if (!isPaused()) {
				// If this node is paused, no matter what,
				// the node stays paused so more and more memo answers can be
				// pushed into the node

				// If this node is NOT paused, it is either a parent of a paused
				// node (CONTAINSPAUSED)
				// or a parent of success- and/or failure nodes (DONE).
				printNodeDebugi();
				setState(hasPausedChilds ? NodeState.PAUSED_C : NodeState.DONE);
				if (DEBUG) {
					System.out.println(" changed state to [" + getState() + "]");
				}
			}
		}

		/**
		 * Evaluate a child node and add the evaluation to this node.
		 * @param childNode
		 * @return state of the child node
		 * @throws EvaluationException on failure
		 */
		private NodeState evaluateChild(final Node childNode) throws EvaluationException {
			childNode.evaluate();
			final NodeState childState = childNode.getState();

			printNodeDebug();
			return childState;
		}

		/**
		 * Checks if this node has a answer stored in the memo table.
		 * @return <code>true</code> if a answer was found, <code>false</code> otherwise
		 */
		private boolean hasUnprocessedMemoAnswer() {

			if (getSelectedLiteral() == null)
				return false;

			final HashMap<IVariable, ITerm> variableMap = new HashMap<IVariable, ITerm>();
			final IRelation memoRelation = mMemoTable.get(getSelectedLiteral().getAtom(), variableMap);
			if ((memoRelation != null) && (memoRelation.size() > 0)) {
				final int pointer = getPointer();
				if ((pointer >= 0) && (pointer < memoRelation.size()))
					return true;
			}

			return false;
		}

		/**
		 * Registers the given query and relation (answers) at the link- and/or memo table. If the selected literal is
		 * already in the memo table, a part of the query is already being computed and thus the query is paused.
		 * Otherwise, the predicate of the selected literal is added to the memo table, including the answers given in
		 * <code>relation</code>.
		 */
		private void registerAtMemoTable() {
			// Initialize tables if necessary
			mMemoTable = (mMemoTable == null ? new MemoTable() : mMemoTable);

			// Classify the node type to save some effort later
			classifyNodeType();

			// Update numbers of literals left for solo refutation
			if (isAnswerNode()) {
				setNumLiteralsLeft(getQuery().getLiterals().size() - 1);
			}

			final ILiteral selectedLiteral = getSelectedLiteral();

			if ((selectedLiteral == null) || !mMemoPredicates.contains(selectedLiteral.getAtom().getPredicate()))
				return; // This is not a memo predicate

			if (mMemoTable.containsKey(selectedLiteral.getAtom())) {
				// 1) selected literal is already in the memo table
			} else {
				// 2) selected literal is not in the memo table
				mMemoTable.add(selectedLiteral.getAtom(), null);
			}
		}

		/**
		 * Add child nodes by evaluating built-ins, facts and rules.
		 * @throws EvaluationException on failure
		 */
		private void addChildNodesOLD() throws EvaluationException {

			if (isEvaluated())
				return;

			final ILiteral selectedLiteral = getSelectedLiteral();

			if (selectedLiteral == null)
				return; // Literal selection not possible (no literals, no
			// executable literal)

			final IAtom queryLiteralAtom = selectedLiteral.getAtom();

			if (queryLiteralAtom instanceof IBuiltinAtom) { // BuiltIn
				addChildNodesByEvaluatingBuiltins();
			} else { // Not BuiltIn
				addChildNodesByFacts();
				addChildNodesByRules();
			}
		}

		/**
		 * Add new child nodes by evaluating the selected literal, which has to be a builtin.
		 * @throws EvaluationException on failure
		 */
		private void addChildNodesByEvaluatingBuiltins() throws EvaluationException {

			if (!(getSelectedLiteral().getAtom() instanceof IBuiltinAtom))
				return; // Selected literal is not a builtin

			final IBuiltinAtom builtinAtom = (IBuiltinAtom) getSelectedLiteral().getAtom();
			final ITuple builtinTuple = builtinAtom.getTuple();

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
			literalsWithoutBuiltin.remove(getSelectedLiteral());
			IQuery newQuery = Factory.BASIC.createQuery(literalsWithoutBuiltin);

			if (builtinEvaluation != null) {

				if (builtinTuple.getVariables().isEmpty()) {
					// Builtin tuple contained no variables, the result is
					// true or false, e.g. ADD(1, 2, 3) = true
					final Node childNode = new Node(this, newQuery, new HashMap<IVariable, ITerm>());
					addChildNode(childNode);

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

						// Add the new query to the query list
						newQuery = TopDownHelper.substituteVariablesInToQuery(newQuery, varMap);
						final Node childNode = new Node(this, newQuery, varMap);
						addChildNode(childNode);

						variableIndex++;
					}
				}
			} else if (unifyable) {
				// Builtin evaluation failed, unification succeeded
				// Take unify result as mapping
				final Map<IVariable, ITerm> varMap = new HashMap<IVariable, ITerm>();

				varMap.putAll(varMapCTarg);

				// Add the new query to the query list
				newQuery = TopDownHelper.substituteVariablesInToQuery(newQuery, varMap);
				final Node childNode = new Node(this, newQuery, varMap);
				addChildNode(childNode);
			}
		}

		/**
		 * Scans the knowledge base for rules that match the selected query literal. If a unifiable match was found the
		 * substitution and the new query will be saved and added to a list of new queries, which is returned.
		 * @param query the whole query
		 * @param selectedLiteral the selected literal
		 * @throws EvaluationException on failure
		 */
		private void addChildNodesByRules() throws EvaluationException {

			for (final IRule rule : mRules) {
				final ILiteral ruleHead = rule.getHead().iterator().next();

				// Potential match?
				if (TopDownHelper.match(ruleHead, getSelectedLiteral())) {
					final Map<IVariable, ITerm> mgu = new HashMap<IVariable, ITerm>();
					final ITuple queryTuple = getSelectedLiteral().getAtom().getTuple();

					// Replace all variables of the rule head with unused ones
					// (variables that are not in the query)
					final Map<IVariable, ITerm> variableRenaming = TopDownHelper.getVariableMapForVariableRenaming(
					        rule, getQuery());
					final IRule ruleAfterVariableRenaming = TopDownHelper
					        .replaceVariablesInRule(rule, variableRenaming);
					final ITuple ruleHeadTuple = ruleAfterVariableRenaming.getHead().iterator().next().getAtom()
					        .getTuple(); // ruleHead
					// changed

					// Get most general unifier
					final boolean unifyable = TermMatchingAndSubstitution.unify(queryTuple, ruleHeadTuple, mgu);

					IQuery newQuery = getQuery();
					if (unifyable) {
						// Replace the rule head with the rule body
						newQuery = TopDownHelper.substituteRuleHeadWithBody(newQuery, getSelectedLiteral(),
						        ruleAfterVariableRenaming);

						// Substitute the whole query
						newQuery = TopDownHelper.substituteVariablesInToQuery(newQuery, mgu);
					}

					final Node childNode = new Node(this, newQuery, mgu);
					addChildNode(childNode);

				}
			}
		}

		/**
		 * Scans the knowledge base for facts that match the selected query literal. If a unifiable match was found the
		 * substitution and the new query will be saved and added to a list of new queries, which is returned.
		 * @param query the whole query
		 * @param queryLiteral the selected literal
		 * @return list of queries with substitutions
		 * @throws EvaluationException
		 */
		private void addChildNodesByFacts() throws EvaluationException {

			final List<Map<IVariable, ITerm>> variableMapList = new LinkedList<Map<IVariable, ITerm>>();
			if (getMatchingFacts(variableMapList)) {
				for (final Map<IVariable, ITerm> variableMap : variableMapList) {
					// For every fact

					// Remove the fact, ...
					final LinkedList<ILiteral> literalsWithoutMatch = new LinkedList<ILiteral>(getQuery().getLiterals());
					literalsWithoutMatch.remove(getSelectedLiteral());

					// Add the new query to the query list
					final IQuery newQuery = Factory.BASIC.createQuery(literalsWithoutMatch);

					// Substitute the whole query
					final IQuery substitutedQuery = TopDownHelper.substituteVariablesInToQuery(newQuery, variableMap);

					final Node childNode = new Node(this, substitutedQuery, variableMap);
					addChildNode(childNode);
				}
			}
		}

		/**
		 * Tries to find a fact that matches the given query. The variableMap will be populated if a matching fact was
		 * found.
		 * @param queryLiteral the given query
		 * @param variableMapList a <i>List</i> of <i>Maps</i> that stores the resulting substitution if a fact was
		 *            found
		 * @return true if a matching fact is found, false otherwise
		 */
		private boolean getMatchingFacts(final List<Map<IVariable, ITerm>> variableMapList) {

			// Check all the facts
			for (final IPredicate factPredicate : mFacts.getPredicates()) {
				// Check if the predicate and the arity matches
				if (TopDownHelper.match(getSelectedLiteral(), factPredicate)) {
					// We've found a match (predicates and arity match)
					// Is the QueryTuple unifiable with one of the FactTuples?

					final IRelation factRelation = mFacts.get(factPredicate);

					// Substitute variables into the query
					for (int i = 0; i < factRelation.size(); i++) {
						ITuple queryTuple = getSelectedLiteral().getAtom().getTuple();
						boolean tupleUnifyable = false;
						final ITuple factTuple = factRelation.get(i);
						final Map<IVariable, ITerm> variableMap = new HashMap<IVariable, ITerm>();
						tupleUnifyable = TermMatchingAndSubstitution.unify(queryTuple, factTuple, variableMap);
						if (tupleUnifyable) {
							queryTuple = TermMatchingAndSubstitution.substituteVariablesInToTuple(queryTuple,
							        variableMap);
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
		 * Add a tuple to this nodes evaluation
		 * @param tuple the <code>ITuple</code> to add
		 */
		private void addToEvaluation(final ITuple tuple) {
			final IRelation evaluation = getEvaluation();
			evaluation.add(tuple);
			setEvaluation(evaluation);

			// If this node is a answer node, add the evaluation to the memo
			// table as well
			if (isAnswerNode()) {
				final Map<IVariable, ITerm> memoTupleVarMap = TopDownHelper.createVariableMapFromTupleAndQuery(
				        getQuery(), tuple);
				final ITuple memoTuple = TopDownHelper.resolveTuple(getSelectedLiteral(), memoTupleVarMap);
				mMemoTable.add(getSelectedLiteral().getAtom(), memoTuple);
			}
		}

		/**
		 * Update the corresponding entry of the memo table which is related to the selected literal.
		 */
		private void updateMemoTable() {
			if (getSelectedLiteral() == null)
				return;

			final IAtom atom = getSelectedLiteral().getAtom();
			if (mMemoTable.get(atom) != null) {
				final IRelation eval = getEvaluation();

				for (int i = 0; i < eval.size(); i++) {
					final ITuple tuple = eval.get(i);
					final Map<IVariable, ITerm> nodeVarMap = TopDownHelper.createVariableMapFromTupleAndQuery(
					        getQuery(), tuple);
					final ITuple nodeMemoTuple = TopDownHelper.resolveTuple(getSelectedLiteral(), nodeVarMap);

					if (!mMemoTable.get(atom).contains(nodeMemoTuple)) {
						mMemoTable.add(atom, nodeMemoTuple);
					}
				}
			}
		}

		private int getNumLiteralsLeft() {
			return numLiteralsLeft;
		}

		private void setNumLiteralsLeft(final int numLiteralsLeft) {
			this.numLiteralsLeft = numLiteralsLeft;
		}

		private IQuery getQuery() {
			return query;
		}

		private int getPointer() {
			return pointer;
		}

		private Map<IVariable, ITerm> getSubstitution() {
			return substitution;
		}

		private Node getPredecessor() {
			return predecessor;
		}

		private List<Node> getSuccessors() {
			return successors;
		}

		private NodeState getState() {
			return state;
		}

		private IRelation getEvaluation() {
			return evaluation;
		}

		/**
		 * Checks if this node was already fully evaluated.
		 * @return true if node was fully evaluated (DONE, SUCCESS or FAILURE), false otherwise
		 */
		private boolean isEvaluated() {
			if ((getState() == NodeState.DONE) || (getState() == NodeState.SUCCESS)
			        || (getState() == NodeState.FAILURE))
				return true;

			return false;
		}

		private boolean isPaused() {
			return (getState() == NodeState.PAUSED_N);
		}

		private boolean isLinkNode() {
			return (getType() == NodeType.LINK);
		}

		private boolean isAnswerNode() {
			return (getType() == NodeType.ANSWER);
		}

		private boolean isParentOfLinkNode() {
			return (getState() == NodeState.PAUSED_C);
		}

		private NodeType getType() {
			return type;
		}

		private void addChildNode(final Node successor) {
			successors.add(successor);
		}

		private void incrementPointer() {
			pointer++;
		}

		private void setState(final NodeState state) {
			this.state = state;
		}

		private void setType(final NodeType type) {
			this.type = type;
		}

		private void setEvaluation(final IRelation evaluation) {
			this.evaluation = evaluation;
		}

		private ILiteral getSelectedLiteral() {
			return selectedLiteral;
		}

		private void setSelectedLiteral(final ILiteral selectedLiteral) {
			this.selectedLiteral = selectedLiteral;
		}

		private void printNodeDebug() {
			if (DEBUG) {
				printNodeDebugi();
				System.out.println();
			}
		}

		private void printNodeDebugi() {
			if (DEBUG) {
				System.out.print(getNumLiteralsLeft() + " " + getQuery().getLiterals().size() + " [" + getType() + "\t"
				        + getState() + "]\t" + getQuery() + "\t= " + getEvaluation());
			}
		}

	}

}
