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
import java.util.List;

import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.rules.RuleValidator;

/**
 * A standard rule-safety processor that checks if the rules are guarded and
 * the TGDs are "sane". If not, a rule unsafe exception is thrown.
 * 
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 */
public class GuardedRuleSafetyProcessor implements IRuleSafetyProcessor {

    /**
     * Default constructor. Initialises with most flexible rule-safety
     * parameters.
     */
    public GuardedRuleSafetyProcessor() {
	this(true, true, true);
    }

    /**
     * Constructor.
     * 
     * @param allowUnlimitedVariablesInNegatedOrdinaryPredicates
     *            Indicates if a rule can still be considered safe if one or
     *            more variables occur in negative ordinary predicates and
     *            nowhere else, e.g. p(X) :- q(X), not r(Y) if true, the above
     *            rule would be safe
     * @param ternaryTargetsImplyLimited
     *            Indicates if ternary arithmetic built-ins can be used to
     *            deduce limited variables, e.g. p(Z) :- q(X, Y), X + Y = Z if
     *            true, then Z would be considered limited.
     */
    public GuardedRuleSafetyProcessor(
	    boolean allowUnlimitedVariablesInNegatedOrdinaryPredicates,
	    boolean ternaryTargetsImplyLimited, boolean allowGuardedRules) {
	mAllowUnlimitedVariablesInNegatedOrdinaryPredicates = allowUnlimitedVariablesInNegatedOrdinaryPredicates;
	mTernaryTargetsImplyLimited = ternaryTargetsImplyLimited;
	mAllowGuardedRules = allowGuardedRules;
    }

    public List<IRule> process(List<IRule> rules) throws RuleUnsafeException {
	List<IRule> processedRules = new ArrayList<IRule>();

	for (IRule rule : rules)
	    processedRules.add(process(rule));

	return processedRules;
    }

    public IRule process(IRule rule) throws RuleUnsafeException {
	RuleValidator validator = new RuleValidator(rule,
		mAllowUnlimitedVariablesInNegatedOrdinaryPredicates,
		mTernaryTargetsImplyLimited, mAllowGuardedRules);

	List<IVariable> unsafeVariables = validator.getAllUnlimitedVariables();	
	if (unsafeVariables.size() > 0) {
	    
	    StringBuilder buffer = new StringBuilder();
	    buffer.append(rule).append(
		    " is not guarded. The following variable(s): ");

	    boolean first = true;
	    for (IVariable variable : unsafeVariables) {
		if (first)
		    first = false;
		else
		    buffer.append(", ");
		buffer.append(variable);
	    }
	    buffer.append(" could be unlimited.");
	    throw new RuleUnsafeException(buffer.toString());
	}

	return rule;
    }

    private final boolean mAllowUnlimitedVariablesInNegatedOrdinaryPredicates;

    private final boolean mTernaryTargetsImplyLimited;

    private final boolean mAllowGuardedRules;

}