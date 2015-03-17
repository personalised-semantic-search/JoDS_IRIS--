package org.deri.iris.rules.safety;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.Position;

public class SafetyUtils {

	/**
	 * Computes all the affected positions in a set of rules.
	 * @param rules A set of rules.
	 * @return The list of all the affected positions in a given set of rules.
	 */
	public static List<IPosition> computeAffectedPositions(final List<IRule> rules) {

		final List<IPosition> result = new ArrayList<IPosition>();

		for (final IRule rule : rules) {

			final List<IVariable> bodyVars = new ArrayList<IVariable>();

			// Get Body Variables
			Iterator<ILiteral> litIt = rule.getBody().iterator();
			while (litIt.hasNext()) {
				final List<IVariable> bodyTuple = litIt.next().getAtom().getTuple().getAllVariables();
				for (int i = 0; i < bodyTuple.size(); i++)
					if (!(bodyVars.contains(bodyTuple.get(i)))) {
						bodyVars.add(bodyTuple.get(i));
					}
			}

			// Get Head Variables.
			litIt = rule.getHead().iterator();
			while (litIt.hasNext()) {
				final IAtom curAtom = litIt.next().getAtom();

				// Get directly affected positions.
				for (int i = 0; i < curAtom.getTuple().size(); i++)
					if ((curAtom.getTuple().get(i) instanceof IVariable)
					        && !(bodyVars.contains(curAtom.getTuple().get(i)))) {
						// This variable is existential and its position is
						// affected.
						final IPosition afctPosition = new Position(curAtom.getPredicate().getPredicateSymbol(), i + 1);
						if (!(result.contains(afctPosition))) {
							result.add(afctPosition);
						}
					}
			}
		}

		// Get indirectly affected positions.
		boolean newPosition = true;
		while (newPosition) {
			newPosition = false;
			for (final IRule rule : rules) {
				final List<IVariable> bodyVars = new ArrayList<IVariable>();

				// Get Body Variables
				Iterator<ILiteral> litIt = rule.getBody().iterator();
				while (litIt.hasNext()) {
					final List<IVariable> bodyTuple = litIt.next().getAtom().getTuple().getAllVariables();
					for (int i = 0; i < bodyTuple.size(); i++)
						if (!(bodyVars.contains(bodyTuple.get(i)))) {
							bodyVars.add(bodyTuple.get(i));
						}
				}

				// Get Head Variables.
				litIt = rule.getHead().iterator();
				while (litIt.hasNext()) {
					final IAtom curAtom = litIt.next().getAtom();

					for (int i = 0; i < curAtom.getTuple().size(); i++)
						if (curAtom.getTuple().get(i) instanceof IVariable) {
							final IVariable curVar = (IVariable) curAtom.getTuple().get(i);
							// check if this variable is a universally
							// quantified variable.
							if (bodyVars.contains(curVar)) {
								// Get all the positions of the current variable
								// in the body.
								final List<IPosition> bodyPositions = getVariableBodyPositions(curVar, rule.getBody());
								// Compute indirectly affected positions
								bodyPositions.removeAll(result);
								if (bodyPositions.size() == 0) {
									// Add the new affected position
									final IPosition affectedPos = new Position(curAtom.getPredicate()
									        .getPredicateSymbol(), i + 1);
									if (!result.contains(affectedPos)) {
										result.add(affectedPos);
										newPosition = true;
									}
								}
							}
						}
				}
			}
		}

		return (result);
	}

	/**
	 * Gets all the positions of the given variable within the body literals.
	 * @param body the body of the rule.
	 * @return the list of variable positions in the body
	 */
	public static List<IPosition> getVariableBodyPositions(final IVariable var, final Set<ILiteral> bodyLiterals) {

		final List<IPosition> result = new ArrayList<IPosition>();

		// Check if the literal contains the needed variable
		for (final ILiteral curLit : bodyLiterals) {

			final List<ITerm> terms = curLit.getAtom().getTuple();

			for (int i = 0; i < terms.size(); i++)
				if (terms.get(i).equals(var)) {
					result.add(new Position(curLit.getAtom().getPredicate().getPredicateSymbol(), i + 1));
				}
		}

		return (result);
	}
}
