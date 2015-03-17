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
package org.deri.iris.basics;

import static org.deri.iris.factory.Factory.BASIC;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deri.iris.MiscHelper;
import org.deri.iris.ObjectTests;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;

/**
 * <p>
 * Tests for the atom.
 * </p>
 * <p>
 * $Id$
 * </p>
 * @author Richard Pöttler (richard dot poettler at deri dot at)
 * @version $Revision$
 */
public class AtomTest extends TestCase {

	private static final int ARITY = 3;

	private static final int ARITYMORE = 4;

	private static final String SYMBOL = "date";

	private static final ITuple TUPLE = MiscHelper.createTuple("a", "b", "c");

	private static final ITuple TUPLEMORE = MiscHelper.createTuple("a", "b", "d");

	private static final ITuple TUPLEEQTYPE = Factory.BASIC.createTuple(Factory.TERM.createVariable("A"),
	        Factory.TERM.createVariable("B"), Factory.TERM.createVariable("A"), Factory.TERM.createString("a"));

	private static final Set<EqualityTypeItem> eqType = new HashSet<EqualityTypeItem>();

	private static final IPredicate PREDICATE = BASIC.createPredicate(SYMBOL, ARITY);

	private static final IPredicate PREDICATEMORE = BASIC.createPredicate(SYMBOL, ARITYMORE);

	private static final IPredicate PREDICATEPLUS = BASIC.createPredicate(SYMBOL + 1, ARITY);

	public static Test suite() {
		return new TestSuite(AtomTest.class, AtomTest.class.getSimpleName());
	}

	public void testIsGround() {
		final Atom REF = new Atom(PREDICATE, TUPLE);
		assertEquals("The isGround method doesn't work properly", true, REF.isGround());
	}

	public void testIsBuiltin() {
		final Atom REF = new Atom(PREDICATE, TUPLE);
		assertEquals("The isBuiltin method doesn't work properly", false, REF.isBuiltin());
	}

	public void testGetTuple() {
		final Atom REF = new Atom(PREDICATE, TUPLE);
		assertEquals("The getTuple method doesn't work properly", TUPLE, REF.getTuple());
	}

	public void testGetPredicate() {
		final Atom REF = new Atom(PREDICATE, TUPLE);
		assertEquals("The getPredicate method doesn't work properly", PREDICATE, REF.getPredicate());
	}

	public void testEquals() {
		ObjectTests.runTestEquals(new Atom(PREDICATE, TUPLE), new Atom(PREDICATE, TUPLE),
		        new Atom(PREDICATE, TUPLEMORE));
	}

	public void testHashCode() {
		ObjectTests.runTestHashCode(new Atom(PREDICATE, TUPLE), new Atom(PREDICATE, TUPLE));
	}

	public void testCompareTo() {
		ObjectTests.runTestCompareTo(new Atom(PREDICATE, TUPLE), new Atom(PREDICATE, TUPLE), new Atom(PREDICATE,
		        TUPLEMORE), new Atom(PREDICATEPLUS, TUPLE));
	}

	public void testEqualityType() {
		final IAtom a = new Atom(PREDICATEMORE, TUPLEEQTYPE);
		eqType.add(new EqualityTypeItem(new Position(SYMBOL, 1), new Position(SYMBOL, 3)));
		eqType.add(new EqualityTypeItem(new Position(SYMBOL, 4), Factory.TERM.createString("a")));
		System.out.println(a.getEqualityType());
		System.out.println(eqType);
		assertEquals(a.getEqualityType(), eqType);

	}
}
