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
package org.deri.iris.terms.concrete;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deri.iris.ObjectTests;
import org.deri.iris.TermTests;

/**
 * <p>
 * Tests the functionality of the <code>DoubleTerm</code>.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Richard Pöttler, richard dot poettler at deri dot org
 * @version $Revision$
 */
public class DoubleTest extends TestCase {

    private final static double BASIC = 0.1d;

    private final static double MORE = 0.2d;

    private final static double MORE1 = 0.3d;

    public static Test suite() {
	return new TestSuite(DoubleTest.class, DoubleTest.class.getSimpleName());
    }

    public void testBasic() {
	DoubleTerm basic = new DoubleTerm(BASIC);

	assertEquals("object not initialized correctly", BASIC, basic
		.getValue());
    }

    public void testEquals() {
	ObjectTests.runTestEquals(new DoubleTerm(BASIC), new DoubleTerm(BASIC),
		new DoubleTerm(MORE));
    }

    public void testCompare() {
	ObjectTests.runTestCompareTo(new DoubleTerm(BASIC), new DoubleTerm(
		BASIC), new DoubleTerm(MORE), new DoubleTerm(MORE1));
    }

    public void testEqualsPositiveNegativeZero() {
	ObjectTests.runTestCompareTo(new DoubleTerm(+0.0),
		new DoubleTerm(-0.0), new DoubleTerm(0.000000001),
		new DoubleTerm(0.000000002));
    }

    public void testHashCode() {
	ObjectTests.runTestHashCode(new DoubleTerm(BASIC),
		new DoubleTerm(BASIC));
    }

    public void testGetMinValue() {
	TermTests.runTestGetMinValue(new DoubleTerm(Double.MIN_VALUE + 0.0001));
    }
}
