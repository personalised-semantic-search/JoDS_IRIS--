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
package org.deri.iris.queryrewriting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.parser.parser.ParserException;
import org.deri.iris.queryrewriting.caching.CacheManager;
import org.deri.iris.utils.TermMatchingAndSubstitution;
import org.deri.iris.utils.UniqueList;

import com.google.common.collect.Iterators;

/**
 * Test class for TermMatchingAndSubstitution.isomorphism(r1, r2) which is used by Query Rewriting.
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 * @see TermMatchingAndSubstitution.ismorphism( List<ILiteral> r1, List<ILiteral> r2 )
 */
public class FactorizationTest extends TestCase {

	@Override
	public void setUp() {
		// setup caching
		CacheManager.setupCaching();
	}

	static {
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");

	}

	private static final IStringTerm STR_B = Factory.TERM.createString("b");

	private static final IStringTerm STR_A = Factory.TERM.createString("a");

	private static final IVariable VAR_B = Factory.TERM.createVariable("B");

	private static final IVariable VAR_A = Factory.TERM.createVariable("A");

	private static final IVariable VAR_Y = Factory.TERM.createVariable("Y");

	private static final IVariable VAR_X = Factory.TERM.createVariable("X");

	private static final IVariable VAR_Z = Factory.TERM.createVariable("Z");

	private static final IVariable VAR_T = Factory.TERM.createVariable("T");

	private static final IVariable VAR_W = Factory.TERM.createVariable("W");

	private final ITuple emptyTuple = Factory.BASIC.createTuple();

	private final ITuple tupleX = Factory.BASIC.createTuple(VAR_X);

	private final ITuple tupleY = Factory.BASIC.createTuple(VAR_Y);

	private final ITuple tupleT = Factory.BASIC.createTuple(VAR_T);

	private final ITuple tupleXY = Factory.BASIC.createTuple(VAR_X, VAR_Y);

	private final ITuple tupleYZ = Factory.BASIC.createTuple(VAR_Y, VAR_Z);

	private final ITuple tupleYT = Factory.BASIC.createTuple(VAR_Y, VAR_T);

	private final ITuple tupleZT = Factory.BASIC.createTuple(VAR_Z, VAR_T);

	private final ITuple tupleTW = Factory.BASIC.createTuple(VAR_T, VAR_W);

	private final ITuple tupleBT = Factory.BASIC.createTuple(VAR_B, VAR_T);

	private final ITuple tupleTB = Factory.BASIC.createTuple(VAR_T, VAR_B);

	private final ITuple tupleBA = Factory.BASIC.createTuple(VAR_B, VAR_A);

	private final ITuple tupleXX = Factory.BASIC.createTuple(VAR_X, VAR_X);

	private final ITuple tupleAA = Factory.BASIC.createTuple(VAR_A, VAR_A);

	private final ITuple tupleYX = Factory.BASIC.createTuple(VAR_Y, VAR_X);

	private final ITuple tupleZY = Factory.BASIC.createTuple(VAR_Z, VAR_Y);

	private final ITuple tupleAB = Factory.BASIC.createTuple(VAR_A, VAR_B);

	private final ITuple tupleXYX = Factory.BASIC.createTuple(VAR_X, VAR_Y, VAR_X);

	private final ITuple tupleABX = Factory.BASIC.createTuple(VAR_A, VAR_B, VAR_X);

	private final ITuple tupleab = Factory.BASIC.createTuple(STR_A, STR_B);

	private final ITuple tupleaB = Factory.BASIC.createTuple(STR_A, VAR_B);

	private final ITuple tuplebB = Factory.BASIC.createTuple(STR_B, VAR_B);

	private final ITuple tupleBa = Factory.BASIC.createTuple(VAR_B, STR_A);

	private final ITuple tupleaX = Factory.BASIC.createTuple(STR_A, VAR_X);

	private final ITuple tupleFX = Factory.BASIC.createTuple(Factory.TERM.createConstruct("f",
	        Factory.BASIC.createTuple(VAR_X)));

	private final ITuple tupleFa = Factory.BASIC.createTuple(Factory.TERM.createConstruct("f",
	        Factory.BASIC.createTuple(STR_A)));

	/*
	 * True Outcome
	 */

	/**
	 * p(?X, ?Y), p(?Y, ?X).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySharedVariablesDifferentOrder() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleYX);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, predicate, tupleXX);

		final List<ILiteral> l1 = new UniqueList<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);

		final List<ILiteral> l2 = new UniqueList<ILiteral>();
		l2.add(lit3);

		final IRule cmp = Factory.BASIC.createRule(new UniqueList<ILiteral>(), l2);
		final IRule r = Factory.BASIC.createRule(new UniqueList<ILiteral>(), l1);
		IRule actual = null;
		for (int i = 0; i < (r.getBody().size() - 1); i++) {
			for (int j = i + 1; j < r.getBody().size(); j++) {
				final IAtom a1 = Iterators.get(r.getBody().iterator(), i).getAtom();
				final IAtom a2 = Iterators.get(r.getBody().iterator(), j).getAtom();

				final Map<IVariable, ITerm> sbstMap = new HashMap<IVariable, ITerm>();
				if (TermMatchingAndSubstitution.unify(a1, a2, sbstMap)) {
					actual = RewritingUtils.factoriseQuery(r, sbstMap);
				}
			}
		}

	}
}