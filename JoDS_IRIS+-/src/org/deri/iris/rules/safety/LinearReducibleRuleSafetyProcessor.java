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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.rules.IRuleSafetyProcessor;

/**
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 */
public class LinearReducibleRuleSafetyProcessor extends GuardedRuleSafetyProcessor implements IRuleSafetyProcessor{

    /**
     * Default constructor. Initialises with most flexible rule-safety
     * parameters.
     */
    public LinearReducibleRuleSafetyProcessor() {
	super(true, true, true);
    }
    
    public LinearReducibleRuleSafetyProcessor(
	    boolean allowUnlimitedVariablesInNegatedOrdinaryPredicates,
	    boolean ternaryTargetsImplyLimited, boolean allowGuardedRules) {
	super(allowUnlimitedVariablesInNegatedOrdinaryPredicates, ternaryTargetsImplyLimited, allowGuardedRules);
    }
    
    public List<IRule> process(List<IRule> rules) throws RuleUnsafeException {
	List<IRule> processedRules = new ArrayList<IRule>();

	for (IRule rule : rules) {
	    if (rule.getBody().size() > 1) {
		// Check if it is a single frontier variable rule
		Set<IVariable> frontierVariables = new HashSet<IVariable>();
		frontierVariables.addAll(rule.getHeadVariables());
		frontierVariables.removeAll(rule.getBodyVariables());
		if (frontierVariables.size() > 1)
		    throw new RuleUnsafeException("The rule " + rule + " is not a Linear Rule: you must have at most 1 body atom in order to be linear or a frontier of a single variable.");
	    } 	
	    // Check if it is guarded
	    processedRules.add(super.process(rule));
	}
	return processedRules;
    }
    
}
