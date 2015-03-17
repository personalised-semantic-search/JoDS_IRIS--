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
package org.deri.iris.queryrewriting;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.deri.iris.EvaluationException;
import org.deri.iris.Expressivity;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;

/**
 * @author jd
 */
public class QueryRewriterFactory {

	private final Logger LOGGER = Logger.getLogger(QueryRewriterFactory.class);

	private static QueryRewriterFactory _INSTANCE;

	public static QueryRewriterFactory getInstance() {

		if ((_INSTANCE == null)) {
			_INSTANCE = new QueryRewriterFactory();
		}

		return _INSTANCE;
	}

	private QueryRewriterFactory() {
		super();
	}

	public QueryRewriter getRewriter(final IRule query, final List<IRule> rules, final Set<IRule> constraints,
	        final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> posDeps, final Expressivity expr,
	        final SubCheckStrategy subchkStrategy, final NCCheck ncCheckStrategy) throws EvaluationException {
		switch (expr) {
		case LINEAR:
			return new LinearRewriter(query, rules, constraints, posDeps, subchkStrategy, ncCheckStrategy);
		case STICKY:
			return new FORewriter(query, rules, constraints, posDeps, subchkStrategy, ncCheckStrategy);
		default:
			final String message = "Unable to rewrite a ".concat(expr.name()).concat("theory");
			LOGGER.error(message);
			throw new EvaluationException(message);
		}
	}
}
