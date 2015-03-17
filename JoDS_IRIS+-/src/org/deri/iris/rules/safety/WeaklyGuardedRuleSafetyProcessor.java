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
package org.deri.iris.rules.safety;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.evaluation.stratifiedbottomup.StratifiedBottomUpEvaluationStrategy;
import org.deri.iris.rules.IRuleSafetyProcessor;

/**
 * A rule-safety processor that checks if the rules are weakly-guarded. If not,
 * a rule unsafe exception is thrown by the process() method .
 * 
 * @author Giorgio Orsi (orsi at elet dot polimi dot it)
 */
public class WeaklyGuardedRuleSafetyProcessor extends
	GuardedRuleSafetyProcessor implements IRuleSafetyProcessor {

    /**
     * Default constructor. Initialises with most flexible rule-safety
     * parameters.
     */
    public WeaklyGuardedRuleSafetyProcessor() {
	/*
	 * Initializes the superclass (GuardedRuleSafetyProcessor) to check the
	 * rules against safety and (hard-)guardness.
	 */
	super(true, true, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deri.iris.rules.safety.GuardedRuleSafetyProcessor#process(java.util
     * .List)
     */
    public List<IRule> process(List<IRule> rules) throws RuleUnsafeException {
	List<IRule> processedRules = new ArrayList<IRule>();

	// Compute the set of affected positions in a program.
	List<IPosition> affectedPositions = StratifiedBottomUpEvaluationStrategy.getAffectedPositions();

	for (IRule rule : rules) {
	    if (checkSafety(rule))
		// the rule is safe or guarded.
		processedRules.add(rule);
	    else if (checkWeaklyGuardness(rule, affectedPositions))
		// check the rule for weakly-guardness.
		processedRules.add(rule);
	    else {
		StringBuilder buffer = new StringBuilder();
		buffer
			.append(rule)
			.append(
				" is not guarded. The head variable(s) could be unlimited.");

		throw new RuleUnsafeException(buffer.toString());
	    }
	}

	return (processedRules);
    }

    /**
     * Checks whether the rule is safe or guarded.
     * 
     * @param rule
     *            The rule to be checked against safety or (hard-)guardness.
     * @return true If the rule is either safe or guarded.
     */
    public boolean checkSafety(IRule rule) {
	try {
	    // Process the rule using GuardedRuleSafetyProcessor
	    super.process(rule);

	} catch (RuleUnsafeException ure) {
	    // The rule is not safe nor guarded.
	    return (false);
	}
	// The rule is safe or guarded
	return (true);
    }

    /**
     * Checks if there exists an atom in the rule body that contains all the
     * universally quantified variables that appears in affected positions only.
     * 
     * @param rule
     *            the rule to be checked against weakly-guardness.
     * @param affected
     *            the list of affected positions.
     * @return true if the rule is weakly guarded.
     */
    public boolean checkWeaklyGuardness(IRule rule, List<IPosition> affected) {

	// Get Body Variables
	List<IVariable> bodyVars = new ArrayList<IVariable>();

	Iterator<ILiteral> litIt = rule.getBody().iterator();
	while (litIt.hasNext()) {
	    List<IVariable> bodyTuple = litIt.next().getAtom().getTuple()
		    .getAllVariables();
	    for (int i = 0; i < bodyTuple.size(); i++)
		if (!(bodyVars.contains(bodyTuple.get(i))))
		    bodyVars.add(bodyTuple.get(i));
	}

	// Compute all the variables that appear in affected positions only.
	List<IVariable> affectedVars = new ArrayList<IVariable>();

	for (IVariable curVar : affectedVars) {
	    // Get all the positions of the current variable in the body.
	    List<IPosition> varPositions = SafetyUtils
		    .getVariableBodyPositions(curVar, rule.getBody());
	    // Check if the variable appears in affected positions only in the
	    // rule's body.
	    varPositions.removeAll(affected);
	    if (varPositions.size() == 0)
		// The variable appears only in affected positions in the body
		if (!affectedVars.contains(curVar))
		    affectedVars.add(curVar);
	}

	for (ILiteral curLit : rule.getBody()) {
	    List<IVariable> atomVars = curLit.getAtom().getTuple()
		    .getAllVariables();
	    if (atomVars.containsAll(affectedVars))
		// This atom is a weak-guard
		return (true);
	}

	return (false);
    }
}