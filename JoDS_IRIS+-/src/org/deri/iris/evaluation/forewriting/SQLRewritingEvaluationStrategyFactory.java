/*
 * <Project Name>
 * <Project Description>
 * 
 * Copyright (C) 2010 ICT Institute - Politecnico di Milano, Via Ponzio 34/5, 20133 Milan, Italy.
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
package org.deri.iris.evaluation.forewriting;

import java.util.ArrayList;
import java.util.List;

import org.deri.iris.Configuration;
import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.evaluation.IEvaluationStrategy;
import org.deri.iris.evaluation.IEvaluationStrategyFactory;
import org.deri.iris.facts.IFacts;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> 
 *	   ICT Institute - Politecnico di Milano.
 * @version 0.1b
 *
 */
public class SQLRewritingEvaluationStrategyFactory extends
	FORewritingEvaluationStrategyFactory implements
	IEvaluationStrategyFactory {

    public IEvaluationStrategy createEvaluator(IFacts facts, List<IRule> rules,
	    Configuration configuration) throws EvaluationException {
	return createEvaluator(facts, rules, new ArrayList<IQuery>(), configuration);
	
    }

    /* (non-Javadoc)
     * @see org.deri.iris.evaluation.IEvaluationStrategyFactory#createEvaluator(org.deri.iris.facts.IFacts, java.util.List, java.util.List, org.deri.iris.Configuration)
     */
    @Override
    public IEvaluationStrategy createEvaluator(IFacts facts, List<IRule> rules,
	    List<IQuery> queries, Configuration configuration)
	    throws EvaluationException {
	
	return new SQLRewritingEvaluationStrategy(facts, rules, queries, configuration);
    }
}
