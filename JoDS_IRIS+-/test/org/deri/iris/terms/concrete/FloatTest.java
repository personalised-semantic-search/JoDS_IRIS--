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
 * Tests the functionality of the <code>FloatTerm</code>.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Richard Pöttler, richard dot poettler at deri dot org
 * @version $Revision$
 */
public class FloatTest extends TestCase {

    private final static float BASIC = 0.1f;

    private final static float MORE = 0.2f;

    private final static float MORE1 = 0.3f;

    public static Test suite() {
	return new TestSuite(FloatTest.class, FloatTest.class.getSimpleName());
    }

    public void testBasic() {
	FloatTerm basic = new FloatTerm(BASIC);

	assertEquals("object not initialized correctly", BASIC, basic
		.getValue());
    }

    public void testEquals() {
	ObjectTests.runTestEquals(new FloatTerm(BASIC), new FloatTerm(BASIC),
		new FloatTerm(MORE));
    }

    public void testCompare() {
	ObjectTests
		.runTestCompareTo(new FloatTerm(BASIC), new FloatTerm(BASIC),
			new FloatTerm(MORE), new FloatTerm(MORE1));
    }

    public void testEqualsPositiveNegativeZero() {
	ObjectTests.runTestCompareTo(new FloatTerm(+0.0f),
		new FloatTerm(-0.0f), new FloatTerm(0.0001f), new FloatTerm(
			0.0002f));
    }

    public void testHashCode() {
	ObjectTests.runTestHashCode(new FloatTerm(BASIC), new FloatTerm(BASIC));
    }

    public void testGetMinValue() {
	TermTests.runTestGetMinValue(new FloatTerm(Float.MIN_VALUE + 0.0001f));
    }
}
