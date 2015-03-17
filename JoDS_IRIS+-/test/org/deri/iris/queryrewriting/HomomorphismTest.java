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

import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.parser.parser.ParserException;
import org.deri.iris.queryrewriting.caching.CacheManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Test class for RewritingHelpers.mapsThroughRenaming(q1, q2) which is used by Query Rewriting.
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 * @see TermMatchingAndSubstitution.ismorphism( List<ILiteral> r1, List<ILiteral> r2 )
 */
public class HomomorphismTest extends TestCase {

	@Override
	public void setUp() {
		// setup caching
		CacheManager.setupCaching();
	}

	static {
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");

	}

	private static final IVariable VAR_U0 = Factory.TERM.createVariable("U_0");
	private static final IVariable VAR_U1 = Factory.TERM.createVariable("U_1");

	private static final IVariable VAR_B = Factory.TERM.createVariable("B");

	private static final IVariable VAR_A = Factory.TERM.createVariable("A");

	private static final IVariable VAR_Y = Factory.TERM.createVariable("Y");

	private static final IVariable VAR_X = Factory.TERM.createVariable("X");

	private static final IVariable VAR_Z = Factory.TERM.createVariable("Z");

	private static final IVariable VAR_T = Factory.TERM.createVariable("T");

	private final ITuple tupleXY = Factory.BASIC.createTuple(VAR_X, VAR_Y);

	private final ITuple tupleYZ = Factory.BASIC.createTuple(VAR_Y, VAR_Z);

	private final ITuple tupleYT = Factory.BASIC.createTuple(VAR_Y, VAR_T);

	private final ITuple tupleBT = Factory.BASIC.createTuple(VAR_B, VAR_T);

	private final ITuple tupleTB = Factory.BASIC.createTuple(VAR_T, VAR_B);

	private final ITuple tupleXX = Factory.BASIC.createTuple(VAR_X, VAR_X);

	private final ITuple tupleAA = Factory.BASIC.createTuple(VAR_A, VAR_A);

	private final ITuple tupleYX = Factory.BASIC.createTuple(VAR_Y, VAR_X);

	private final ITuple tupleZY = Factory.BASIC.createTuple(VAR_Z, VAR_Y);

	private final ITuple tupleAB = Factory.BASIC.createTuple(VAR_A, VAR_B);

	private final ITuple tupleU0 = Factory.BASIC.createTuple(VAR_U0);
	private final ITuple tupleU0U1 = Factory.BASIC.createTuple(VAR_U0, VAR_U1);
	private final ITuple tupleU1 = Factory.BASIC.createTuple(VAR_U1);

	/*
	 * True Outcome
	 */

	/**
	 * p(?X, ?Y), p(?Y, ?X) | p(?X, ?X).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySharedVariablesDifferentSize() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleYX);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, predicate, tupleXX);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);

		final boolean actual = RewritingUtils.mapsTo(l1, l2);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?Y) | p(?X, ?X).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySharedVariablesSameSize() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleXX);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit2);
		final boolean actual = RewritingUtils.mapsTo(l1, l2);

		assertEquals(true, actual);
	}

	/*
	 * False Outcome
	 */

	/**
	 * p(?X, ?X) . p(?X, ?Y), p(?Y, ?X).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySharedVariablesDifferentSizeReverse() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleYX);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, predicate, tupleXX);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(false, actual);
	}

	/**
	 * p(?X, ?X) . p(?X, ?Y).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySharedVariablesSameSizeReverse() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleXX);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit2);
		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(false, actual);
	}

	/**
	 * p(?X, ?Y), p(?Y, ?X) . p(?X, ?Y), p(?Y, ?Z)
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySharedVariablesSameSize2() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleYX);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, predicate, tupleYZ);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);
		final boolean actual = RewritingUtils.mapsTo(l1, l2);

		assertEquals(false, actual);
	}

	/**
	 * p(?X, ?Y), p(?Y, ?X) . p(?A, ?B), p(?B, ?T)
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySharedVariablesSameSize3() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleYX);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, predicate, tupleAB);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, predicate, tupleBT);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);
		final boolean actual = RewritingUtils.mapsTo(l1, l2);

		assertEquals(false, actual);
	}

	/**
	 * p(?X, ?X) . p(?Y, ?X).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySharedVariablesSameSizeReverse2() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleYX);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleXX);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit2);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(false, actual);
	}

	/**
	 * p(?X, ?Y), p(?Y, ?Z) . p(?X, ?Y), p(?Y, ?Z).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameAritySameVariables() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleYZ);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, predicate, tupleYZ);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?Y), p(?Y, ?Z) . p(?X, ?Y), p(?Y, ?T).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSharedVariables() throws ParserException {
		final IPredicate predicate = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, predicate, tupleYZ);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, predicate, tupleXY);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, predicate, tupleYT);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?Y), s(?Y, ?Z) . p(?A, ?B), s(?B, ?T).
	 * @throws ParserException
	 */
	public void testDifferentPredicateSameOrderSameArityDisjointVariables() throws ParserException {

		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 2);

		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, s, tupleYZ);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, p, tupleAB);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, s, tupleBT);

		final Set<ILiteral> l1 = ImmutableSet.of(lit1, lit2);
		final Set<ILiteral> l2 = ImmutableSet.of(lit3, lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?Y), s(?Y, ?Z) . p(?X, ?Y), s(?Y, ?T).
	 * @throws ParserException
	 */
	public void testDifferentPredicateSameOrderSameAritySharedVariables() throws ParserException {
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, s, tupleYZ);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, p, tupleXY);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, s, tupleYT);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?Y), s(?Y, ?Z) . s(?B, ?T), p(?A, ?B).
	 * @throws ParserException
	 */
	public void testDifferentPredicateDifferentOrderSameArityDifferentVariables() throws ParserException {
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, s, tupleYZ);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, s, tupleBT);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, p, tupleAB);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?Y), s(?Z, ?Y) . p(?A, ?B), s(?T, ?B).
	 * @throws ParserException
	 */
	public void testDifferentPredicateSameOrderSameArityDifferentVariablesRedundantQuery() throws ParserException {
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, s, tupleZY);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, p, tupleAB);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, s, tupleTB);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?X), s(?X, ?Y) . s(?A, ?B), p(?A, ?A).
	 * @throws ParserException
	 */
	public void testDifferentPredicateDifferentOrderSameArityRepeatedDifferentVariables() throws ParserException {
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXX);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, s, tupleXY);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, s, tupleAB);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, p, tupleAA);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?X), s(?X, ?X) . s(?A, ?A), p(?A, ?A).
	 * @throws ParserException
	 */
	public void testDifferentPredicateDifferentOrderSameArityRepeatedSharedVariables() throws ParserException {
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXX);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, s, tupleXX);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, s, tupleAA);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, p, tupleAA);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/*
	 * False Outcome
	 */

	/**
	 * p(?X, ?Y), s(?Y, ?Z) . p(?B, ?T), s(?A, ?B).
	 * @throws ParserException
	 */
	public void testDifferentPredicateSameOrderSameArityDifferentVariables() throws ParserException {
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, s, tupleYZ);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, p, tupleBT);
		final ILiteral lit4 = Factory.BASIC.createLiteral(true, s, tupleAB);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);
		l2.add(lit4);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(false, actual);
	}

	/**
	 * p(?X, ?Y), p(?Y, ?Z) . p(?X, ?X).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameArityRightUnifier() throws ParserException {
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, p, tupleYZ);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, p, tupleXX);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		l1.add(lit2);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit3);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(false, actual);
	}

	/**
	 * p(?X, ?X) . p(?X, ?Y), p(?Y, ?Z).
	 * @throws ParserException
	 */
	public void testSamePredicateSameOrderSameArityleftUnifier() throws ParserException {
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p, tupleXY);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, p, tupleYZ);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, p, tupleXX);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit3);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();

		l2.add(lit1);
		l2.add(lit2);

		final boolean actual = RewritingUtils.mapsTo(l2, l1);

		assertEquals(true, actual);
	}

	/**
	 * p(?X, ?X) . p(?X, ?Y), p(?Y, ?Z).
	 * @throws ParserException
	 */
	public void testAdolena() throws ParserException {
		final IPredicate p1 = Factory.BASIC.createPredicate("Motorised_Wheelchair", 1);
		final IPredicate p2 = Factory.BASIC.createPredicate("assistsWith", 2);
		final IPredicate p3 = Factory.BASIC.createPredicate("UpperLimbMobility", 1);

		final ILiteral lit1 = Factory.BASIC.createLiteral(true, p1, tupleU0);
		final ILiteral lit2 = Factory.BASIC.createLiteral(true, p2, tupleU0U1);
		final ILiteral lit3 = Factory.BASIC.createLiteral(true, p3, tupleU1);

		final Set<ILiteral> l1 = new LinkedHashSet<ILiteral>();
		l1.add(lit1);
		final Set<ILiteral> l2 = new LinkedHashSet<ILiteral>();
		l2.add(lit1);
		l2.add(lit2);
		l2.add(lit3);

		System.out.println(l1);
		System.out.println(l2);

		final boolean actual = RewritingUtils.mapsTo(l1, l2);

		assertEquals(true, actual);
	}

	public void testBoolean1() throws Exception {

		/*
		 * Queries:
		 * 
		 * q() <- t(X,Y), t(Y,Z) |-> q() <- t(A,B), t(B,C) ? q() <- t(A,B), t(B,C) |-> q() <- t(X,Y), t(Y,Z) ?
		 */

		final IPredicate q = Factory.BASIC.createPredicate("q", 0);
		final IPredicate t = Factory.BASIC.createPredicate("t", 2);

		final IVariable x = Factory.TERM.createVariable("X");
		final IVariable y = Factory.TERM.createVariable("Y");
		final IVariable z = Factory.TERM.createVariable("Z");

		final IVariable a = Factory.TERM.createVariable("A");
		final IVariable b = Factory.TERM.createVariable("B");
		final IVariable c = Factory.TERM.createVariable("C");

		final ILiteral q3 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple());
		final ILiteral txy = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(x, y));
		final ILiteral tyz = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(y, z));
		final ILiteral tab = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(a, b));
		final ILiteral tbc = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(b, c));

		final IRule q1 = Factory.BASIC.createRule(ImmutableList.of(q3), ImmutableList.of(txy, tyz));
		final IRule q2 = Factory.BASIC.createRule(ImmutableList.of(q3), ImmutableList.of(tab, tbc));

		// Do the test
		Assert.assertEquals(true, RewritingUtils.mapsTo(q1, q2));
		Assert.assertEquals(true, RewritingUtils.mapsTo(q2, q1));

	}

	public void testBoolean2() throws Exception {

		/*
		 * Queries:
		 * 
		 * q() <- t(X,Y) |-> q() <- t(A,B), t(B,C) ? q() <- t(A,B), t(B,C) |-> q() <- t(X,Y) ?
		 */

		final IPredicate q = Factory.BASIC.createPredicate("q", 0);
		final IPredicate t = Factory.BASIC.createPredicate("t", 2);

		final IVariable x = Factory.TERM.createVariable("X");
		final IVariable y = Factory.TERM.createVariable("Y");

		final IVariable a = Factory.TERM.createVariable("A");
		final IVariable b = Factory.TERM.createVariable("B");
		final IVariable c = Factory.TERM.createVariable("C");

		final ILiteral q3 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple());
		final ILiteral txy = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(x, y));
		final ILiteral tab = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(a, b));
		final ILiteral tbc = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(b, c));

		final IRule q1 = Factory.BASIC.createRule(ImmutableList.of(q3), ImmutableList.of(txy));
		final IRule q2 = Factory.BASIC.createRule(ImmutableList.of(q3), ImmutableList.of(tab, tbc));

		// Do the test
		Assert.assertEquals(true, RewritingUtils.mapsTo(q1, q2));
		Assert.assertEquals(false, RewritingUtils.mapsTo(q2, q1));

	}

	public void testBoolean3() throws Exception {

		/*
		 * Queries:
		 * 
		 * q() <- t(X,Y,Z), t(Z,V,W) |-> q() <- p(A,B), p(B,C), t(C,D,E) ?
		 */

		final IPredicate q = Factory.BASIC.createPredicate("q", 0);
		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate t = Factory.BASIC.createPredicate("t", 3);

		final IVariable x = Factory.TERM.createVariable("X");
		final IVariable y = Factory.TERM.createVariable("Y");
		final IVariable z = Factory.TERM.createVariable("Z");
		final IVariable v = Factory.TERM.createVariable("V");
		final IVariable w = Factory.TERM.createVariable("W");

		final IVariable a = Factory.TERM.createVariable("A");
		final IVariable b = Factory.TERM.createVariable("B");
		final IVariable c = Factory.TERM.createVariable("C");
		final IVariable d = Factory.TERM.createVariable("D");
		final IVariable e = Factory.TERM.createVariable("E");

		final ILiteral q3 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple());

		final ILiteral txyz = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(x, y, z));
		final ILiteral tzvw = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(z, v, w));

		final ILiteral pab = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(a, b));
		final ILiteral pbc = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(b, c));
		final ILiteral tcde = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(c, d, e));

		final IRule q1 = Factory.BASIC.createRule(ImmutableList.of(q3), ImmutableList.of(txyz, tzvw));
		final IRule q2 = Factory.BASIC.createRule(ImmutableList.of(q3), ImmutableList.of(pab, pbc, tcde));

		// Do the test
		Assert.assertEquals(false, RewritingUtils.mapsTo(q1, q2));
	}

	public void testBoolean4() throws Exception {

		// Queries:

		// Q() :- r2(?T0, ?T1, ?T2, ?T3, ?T4, ?T5, ?T6, ?T7, ?T8), r2(?T8, ?T9, ?T10, ?T11, ?T12, ?T13, ?T14, ?T15,
		// ?T16).
		// Q() :- r1(?U0, ?U1, ?U2, ?U3, ?U4), r1(?U4, ?U5, ?U6, ?U7, ?U8), r2(?U8, ?U9, ?U10, ?U11, ?U12, ?U13, ?U14,
		// ?U15, ?U16).

		final IPredicate q = Factory.BASIC.createPredicate("q", 0);
		final IPredicate r2 = Factory.BASIC.createPredicate("r2", 9);
		final IPredicate r1 = Factory.BASIC.createPredicate("r1", 5);

		final IVariable t0 = Factory.TERM.createVariable("T0");
		final IVariable t1 = Factory.TERM.createVariable("T1");
		final IVariable t2 = Factory.TERM.createVariable("T2");
		final IVariable t3 = Factory.TERM.createVariable("T3");
		final IVariable t4 = Factory.TERM.createVariable("T4");
		final IVariable t5 = Factory.TERM.createVariable("T5");
		final IVariable t6 = Factory.TERM.createVariable("T6");
		final IVariable t7 = Factory.TERM.createVariable("T7");
		final IVariable t8 = Factory.TERM.createVariable("T8");
		final IVariable t9 = Factory.TERM.createVariable("T9");
		final IVariable t10 = Factory.TERM.createVariable("T10");
		final IVariable t11 = Factory.TERM.createVariable("T11");
		final IVariable t12 = Factory.TERM.createVariable("T12");
		final IVariable t13 = Factory.TERM.createVariable("T13");
		final IVariable t14 = Factory.TERM.createVariable("T14");
		final IVariable t15 = Factory.TERM.createVariable("T15");
		final IVariable t16 = Factory.TERM.createVariable("T16");

		final IVariable u0 = Factory.TERM.createVariable("U0");
		final IVariable u1 = Factory.TERM.createVariable("U1");
		final IVariable u2 = Factory.TERM.createVariable("U2");
		final IVariable u3 = Factory.TERM.createVariable("U3");
		final IVariable u4 = Factory.TERM.createVariable("U4");
		final IVariable u5 = Factory.TERM.createVariable("U5");
		final IVariable u6 = Factory.TERM.createVariable("U6");
		final IVariable u7 = Factory.TERM.createVariable("U7");
		final IVariable u8 = Factory.TERM.createVariable("U8");
		final IVariable u9 = Factory.TERM.createVariable("U9");
		final IVariable u10 = Factory.TERM.createVariable("U10");
		final IVariable u11 = Factory.TERM.createVariable("U11");
		final IVariable u12 = Factory.TERM.createVariable("U12");
		final IVariable u13 = Factory.TERM.createVariable("U13");
		final IVariable u14 = Factory.TERM.createVariable("U14");
		final IVariable u15 = Factory.TERM.createVariable("U15");
		final IVariable u16 = Factory.TERM.createVariable("U16");

		final ILiteral q3 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple());

		final ILiteral r2t0t8 = Factory.BASIC.createLiteral(true, r2,
		        Factory.BASIC.createTuple(t0, t1, t2, t3, t4, t5, t6, t7, t8));
		final ILiteral r2t8t16 = Factory.BASIC.createLiteral(true, r2,
		        Factory.BASIC.createTuple(t8, t9, t10, t11, t12, t13, t14, t15, t16));

		final ILiteral r1u0u4 = Factory.BASIC.createLiteral(true, r1, Factory.BASIC.createTuple(u0, u1, u2, u3, u4));
		final ILiteral r1u4u8 = Factory.BASIC.createLiteral(true, r1, Factory.BASIC.createTuple(u4, u5, u6, u7, u8));
		final ILiteral r2u8u16 = Factory.BASIC.createLiteral(true, r2,
		        Factory.BASIC.createTuple(u8, u9, u10, u11, u12, u13, u14, u15, u16));

		final IRule q1 = Factory.BASIC.createRule(ImmutableList.of(q3), ImmutableList.of(r2t0t8, r2t8t16));
		final IRule q2 = Factory.BASIC.createRule(ImmutableList.of(q3), ImmutableList.of(r1u0u4, r1u4u8, r2u8u16));

		// Do the test
		Assert.assertEquals(false, RewritingUtils.mapsTo(q1, q2));
	}

	public void testSameSetAround() throws Exception {

		// Queries:

		// Q1(?T0) :- r(?T1, ?T0), r(?T2, ?T0), r(?T2, ?T1), s(?T0).
		// Q1(?U0) :- r(?U1, ?U0), r(?U2, ?U1), r(?U2, ?U0), s(?U0).

		final IPredicate q = Factory.BASIC.createPredicate("q", 1);
		final IPredicate r = Factory.BASIC.createPredicate("r", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 1);

		final IVariable t0 = Factory.TERM.createVariable("T0");
		final IVariable t1 = Factory.TERM.createVariable("T1");
		final IVariable t2 = Factory.TERM.createVariable("T2");

		final IVariable u0 = Factory.TERM.createVariable("U0");
		final IVariable u1 = Factory.TERM.createVariable("U1");
		final IVariable u2 = Factory.TERM.createVariable("U2");

		final ILiteral qt0 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple(t0));
		final ILiteral qu0 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple(u0));

		final ILiteral rt1t0 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(t1, t0));
		final ILiteral rt2t0 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(t2, t0));
		final ILiteral rt2t1 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(t2, t1));
		final ILiteral st0 = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(t0));

		final ILiteral ru1u0 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(u1, u0));
		final ILiteral ru2u0 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(u2, u0));
		final ILiteral ru2u1 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(u2, u1));
		final ILiteral su0 = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(u0));

		final IRule q1 = Factory.BASIC.createRule(ImmutableList.of(qt0), ImmutableList.of(rt1t0, rt2t0, rt2t1, st0));
		final IRule q2 = Factory.BASIC.createRule(ImmutableList.of(qu0), ImmutableList.of(ru1u0, ru2u1, ru2u0, su0));

		// Do the test
		Assert.assertEquals(true, RewritingUtils.mapsTo(q1, q2));
	}

	public void testSameSetAround2() throws Exception {

		// Queries:

		// q(?T0) :- p(?T0, ?T1, ?T2), p(?T3, ?T4, ?T5), r(?T0, ?T3), s(?T0), s(?T3).
		// q(?U0) :- p(?U0, ?U1, ?U2), p(?U3, ?U4, ?U5), r(?U3, ?U0), s(?U0), s(?U3).

		final IPredicate q = Factory.BASIC.createPredicate("q", 1);

		final IPredicate p = Factory.BASIC.createPredicate("p", 3);
		final IPredicate r = Factory.BASIC.createPredicate("r", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 1);

		final IVariable t0 = Factory.TERM.createVariable("T0");
		final IVariable t1 = Factory.TERM.createVariable("T1");
		final IVariable t2 = Factory.TERM.createVariable("T2");
		final IVariable t3 = Factory.TERM.createVariable("T3");
		final IVariable t4 = Factory.TERM.createVariable("T4");
		final IVariable t5 = Factory.TERM.createVariable("T5");

		final IVariable u0 = Factory.TERM.createVariable("U0");
		final IVariable u1 = Factory.TERM.createVariable("U1");
		final IVariable u2 = Factory.TERM.createVariable("U2");
		final IVariable u3 = Factory.TERM.createVariable("U3");
		final IVariable u4 = Factory.TERM.createVariable("U4");
		final IVariable u5 = Factory.TERM.createVariable("U5");

		final ILiteral qt0 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple(t0));
		final ILiteral qu0 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple(u0));

		final ILiteral pt0t1t2 = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(t0, t1, t2));
		final ILiteral pt3t4t5 = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(t3, t4, t5));

		final ILiteral rt0t3 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(t0, t3));
		final ILiteral st0 = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(t0));
		final ILiteral st3 = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(t3));

		final ILiteral pu0u1u2 = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(u0, u1, u2));
		final ILiteral pu3u4u5 = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(u3, u4, u5));

		final ILiteral ru3u0 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(u3, u0));
		final ILiteral su0 = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(u0));
		final ILiteral su3 = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(u3));

		final IRule q1 = Factory.BASIC.createRule(ImmutableList.of(qt0),
		        ImmutableList.of(pt0t1t2, pt3t4t5, rt0t3, st0, st3));
		final IRule q2 = Factory.BASIC.createRule(ImmutableList.of(qu0),
		        ImmutableList.of(pu0u1u2, pu3u4u5, ru3u0, su0, su3));

		// Do the test
		Assert.assertEquals(false, RewritingUtils.mapsTo(q1, q2));
		Assert.assertEquals(false, RewritingUtils.mapsTo(q2, q1));
	}

	public void testContainment() throws Exception {

		// Queries

		// Q1(?U0) :- AssistiveDevice(?U0), TactileReading(?U0).
		// Q1(?U0) :- TactileReading(?U0).

		final IPredicate q = Factory.BASIC.createPredicate("Q1", 1);

		final IPredicate ad = Factory.BASIC.createPredicate("AssistiveDevice", 1);
		final IPredicate tr = Factory.BASIC.createPredicate("TactileReading", 1);

		final IVariable u0 = Factory.TERM.createVariable("U0");

		final ILiteral qu0 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple(u0));

		final ILiteral adu0 = Factory.BASIC.createLiteral(true, ad, Factory.BASIC.createTuple(u0));
		final ILiteral tru0 = Factory.BASIC.createLiteral(true, tr, Factory.BASIC.createTuple(u0));

		final IRule q1 = Factory.BASIC.createRule(ImmutableList.of(qu0), ImmutableList.of(adu0, tru0));
		final IRule q2 = Factory.BASIC.createRule(ImmutableList.of(qu0), ImmutableList.of(tru0));

		// Do the test
		Assert.assertEquals(false, RewritingUtils.mapsTo(q1, q2));
		Assert.assertEquals(true, RewritingUtils.mapsTo(q2, q1));
	}
}