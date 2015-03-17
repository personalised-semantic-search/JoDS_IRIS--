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
package org.deri.iris.evaluation.stratifiedbottomup;

import group.models.PreferenceStrategy;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.ProgramNotStratifiedException;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.facts.FiniteUniverseFacts;
import org.deri.iris.facts.IFacts;
import org.deri.iris.rules.compiler.ICompiledRule;
import org.deri.iris.rules.compiler.RuleCompiler;
import org.deri.iris.rules.safety.AugmentingRuleSafetyProcessor;
import org.deri.iris.rules.safety.GuardedRuleSafetyProcessor;
import org.deri.iris.rules.safety.SafetyUtils;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;

import com.google.gson.JsonArray;

/**
 * A strategy that uses bottom up evaluation on a stratified rule set.
 */
public class StratifiedBottomUpEvaluationStrategy implements IEvaluationStrategy {
    
    StratifiedBottomUpEvaluationStrategy( IFacts facts, List<IRule> rules, 
	    				  IRuleEvaluatorFactory ruleEvaluatorFactory, 
	    				  Configuration configuration) throws EvaluationException {
	mConfiguration = configuration;

	mRuleEvaluatorFactory = ruleEvaluatorFactory;

	mFacts = facts;
	
	mUtils = new EvaluationUtilities(mConfiguration);

	if (mConfiguration.ruleSafetyProcessor instanceof GuardedRuleSafetyProcessor)
	    mAffectedPositions = SafetyUtils.computeAffectedPositions(rules);
	else
	    mAffectedPositions = new ArrayList<IPosition>();

	if (mConfiguration.ruleSafetyProcessor instanceof AugmentingRuleSafetyProcessor)
	    facts = new FiniteUniverseFacts(facts, rules);

	// Rule safety processing
	List<IRule> safeRules = mUtils.applyRuleSafetyProcessor(rules);

	// Stratify
	List<List<IRule>> stratifiedRules = mUtils.stratify(safeRules);

	// Compile
	RuleCompiler rc = new RuleCompiler(facts, mConfiguration);

	for (List<IRule> stratum : stratifiedRules) {
	    // Re-order stratum
	    List<IRule> reorderedRules = mUtils.reOrderRules(stratum);

	    // Rule optimisation
	    List<IRule> optimisedRules = mUtils
		    .applyRuleOptimisers(reorderedRules);

	    // Compile
	    List<ICompiledRule> compiledRules = new ArrayList<ICompiledRule>();

	    for (IRule rule : optimisedRules)
		compiledRules.add(rc.compile(rule));

	    IRuleEvaluator eval = mRuleEvaluatorFactory.createEvaluator();
	    eval.evaluateRules(compiledRules, facts, configuration);
	}

    }

    public IRelation evaluateQuery(IQuery query, List<IVariable> outputVariables) throws EvaluationException {
	if (query == null)
	    throw new IllegalArgumentException(
		    "StratifiedBottomUpEvaluationStrategy.evaluateQuery() - query must not be null.");

	if (outputVariables == null)
	    throw new IllegalArgumentException(
		    "StratifiedBottomUpEvaluationStrategy.evaluateQuery() - outputVariables must not be null.");

	RuleCompiler compiler = new RuleCompiler(mFacts, mConfiguration);

	ICompiledRule compiledQuery = compiler.compile(query);

	IRelation evaluation = compiledQuery.evaluate();
	
	// selects away the tuples with freshly generated terms.
	IRelation result = getGroundFacts (evaluation);
	
	outputVariables.clear();
	outputVariables.addAll(compiledQuery.getVariablesBindings());

	return result;
    }
    
    public IRelation getGroundFacts (IRelation r) {
	IRelationFactory rf = new RelationFactory();
	IRelation res = rf.createRelation(); 
	
	for ( int i=0; i<r.size(); i++ )
	    if ( r.get( i ).getFreshTerms().size() == 0 )
		res.add( r.get( i ) );
	
    	return ( res );
    }

    public static List<IPosition> getAffectedPositions() {
	return (mAffectedPositions);
    }
    
    protected final EvaluationUtilities mUtils;

    protected final Configuration mConfiguration;

    protected final IFacts mFacts;

    protected final IRuleEvaluatorFactory mRuleEvaluatorFactory;

    protected static List<IPosition> mAffectedPositions;

	@Override
	public IRelation evaluateQuery(IQuery query,
			List<IVariable> outputVariables, JsonArray pg, int k, PreferenceStrategy strategy)
			throws ProgramNotStratifiedException, RuleUnsafeException,
			EvaluationException {
		// TODO Auto-generated method stub
		return null;
	}

}
