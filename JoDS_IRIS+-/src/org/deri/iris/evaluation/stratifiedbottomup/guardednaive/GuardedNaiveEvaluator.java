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
package org.deri.iris.evaluation.stratifiedbottomup.guardednaive;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.basics.Position;
import org.deri.iris.evaluation.stratifiedbottomup.IRuleEvaluator;
import org.deri.iris.evaluation.stratifiedbottomup.StratifiedBottomUpEvaluationStrategy;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rules.compiler.BodyRuleElement;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.terms.Null;

/**
 * Guarded naive evaluation. See Calì, Gottlob, Lukasiewicz ICDT 2009
 */
public class GuardedNaiveEvaluator implements IRuleEvaluator {
	@Override
	public void evaluateRules(final List<ICompiledRule> rules, final IFacts facts, final Configuration configuration)
	        throws EvaluationException {

		// Keep a list of the open rules.
		final List<ICompiledRule> openRules = new ArrayList<ICompiledRule>();
		openRules.addAll(rules);

		// Affected positions.
		final List<IPosition> affectedPositions = StratifiedBottomUpEvaluationStrategy.getAffectedPositions();

		boolean cont = true;
		while (cont) {
			cont = false;

			// For each rule in the collection (stratum)
			for (final ICompiledRule rule : rules) {

				// System.out.println("-----------------------------");
				// System.out.println("Evaluating: " + rule);

				// Create the structure to keep the delta facts
				final IRelationFactory relFactory = new RelationFactory();
				IRelation delta = relFactory.createRelation();

				/*
				 * Check if the rule is an "exhausted TGD", i.e., from now on, this rule will generate only "fresh"
				 * terms that can be unified via an homomorphism with already generated "fresh" terms.
				 */
				if (openRules.size() > 0) {
					delta = rule.evaluate();
					if (isExhausted(rule, affectedPositions)) {
						openRules.remove(rule);
					}
				}

				// System.out.println("Delta: " + delta);
				// System.out.println("-----------------------------");

				// Add the delta facts to the program base.
				if ((delta != null) && (delta.size() > 0)) {

					final IPredicate predicate = rule.headPredicate();
					if (facts.get(predicate).addAll(delta)) {
						cont = true;
					}

				}
			}
		}
	}

	/**
	 * Check if the current rule is exhausted, i.e., it has only "fresh" terms in the left-most guard atom.
	 * @param rule the rule to process.
	 * @return true if the rule is exhausted.
	 * @throws EvaluationException if the rule is not guarded.
	 */
	private boolean isExhausted(final ICompiledRule rule, final List<IPosition> affectedPositions)
	        throws EvaluationException {

		// Get the leftmost guard atom in the rule
		final BodyRuleElement guard = rule.getLeftmostGuard(affectedPositions);

		if (guard != null) {
			final List<IPosition> affectedInGuard = getAffectedPositions(guard, affectedPositions);
			if (activeGuard(guard, affectedInGuard))
				return true;
			return false;
		} else
			// Something wrong, possibly a wrong evaluator!
			throw new EvaluationException("The rule " + rule + " is not guarded nor weakly-guarded.");
	}

	/**
	 * Check if the guard is active which means that the corresponding rule must be closed.
	 * @param guard The guard.
	 * @return true If the is active.
	 */
	public boolean activeGuard(final BodyRuleElement guard, final List<IPosition> affectedPositions) {

		final IRelation guardedChase = guard.getView();

		boolean weakGuard = false;

		for (int i = 0; (i < guardedChase.size()) && !weakGuard; i++) {
			final ITuple curTuple = guardedChase.get(i);

			weakGuard = true;

			if (affectedPositions.size() > 0) {
				// This is a weak-guard. Check nulls in affected positions.
				for (int j = 0; (j < curTuple.size()) && weakGuard; j++)
					if ((affectedPositions.contains(new Position(guard.getPredicate().getPredicateSymbol(), j + 1)) && !(curTuple
					        .get(j) instanceof Null))) {
						weakGuard = false;
					}
			} else {
				// This is a hard guard. Check nulls in all the positions.
				for (int j = 0; (j < curTuple.size()) && weakGuard; j++)
					if (!(curTuple.get(j) instanceof Null)) {
						weakGuard = false;
					}
			}
		}

		return (weakGuard);
	}

	private List<IPosition> getAffectedPositions(final BodyRuleElement atom, final List<IPosition> affectedPositions) {
		final List<IPosition> result = new ArrayList<IPosition>();

		for (int i = 0; i < atom.getView().variables().size(); i++) {
			final IPosition pos = new Position(atom.getPredicate().getPredicateSymbol(), i + 1);
			if (affectedPositions.contains(pos)) {
				result.add(pos);
			}
		}

		return (result);
	}

}
