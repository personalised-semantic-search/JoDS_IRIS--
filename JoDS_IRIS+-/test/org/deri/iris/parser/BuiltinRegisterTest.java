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
package org.deri.iris.parser;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deri.iris.compiler.BuiltinRegister;

/**
 * <p>
 * Tests for the <code>BuiltinRegister</code>.
 * </p>
 * <p>
 * $Id: BuiltinRegisterTest.java,v 1.3 2007-10-12 14:34:56 bazbishop237 Exp $
 * </p>
 * 
 * @author Richard Pöttler (richard dot poettler at deri dot at)
 * @version $Revision: 1.3 $
 */
public class BuiltinRegisterTest extends TestCase {

    /** The register on which to operate. */
    private BuiltinRegister reg;

    public static Test suite() {
	return new TestSuite(BuiltinRegisterTest.class,
		BuiltinRegisterTest.class.getSimpleName());
    }

    public void setUp() {
	reg = new BuiltinRegister();
    }

    /**
     * Checks whether the core builtins gets registered correctly.
     * 
     * @see <a
     *      href="http://sourceforge.net/tracker/index.php?func=detail&aid=1784947&group_id=167309&atid=985821">bug
     *      #1784947: simple possibility to make new core builtins parseable</a>
     */
    public void testRegisterCoreBuiltins() {
	checkRegisteredBuiltin("ADD", org.deri.iris.builtins.AddBuiltin.class,
		3);
	checkRegisteredBuiltin("SUBTRACT",
		org.deri.iris.builtins.SubtractBuiltin.class, 3);
	checkRegisteredBuiltin("MULTIPLY",
		org.deri.iris.builtins.MultiplyBuiltin.class, 3);
	checkRegisteredBuiltin("DIVIDE",
		org.deri.iris.builtins.DivideBuiltin.class, 3);
	checkRegisteredBuiltin("MODULUS",
		org.deri.iris.builtins.ModulusBuiltin.class, 3);

	checkRegisteredBuiltin("EQUAL",
		org.deri.iris.builtins.EqualBuiltin.class, 2);
	checkRegisteredBuiltin("NOT_EQUAL",
		org.deri.iris.builtins.NotEqualBuiltin.class, 2);

	checkRegisteredBuiltin("LESS",
		org.deri.iris.builtins.LessBuiltin.class, 2);
	checkRegisteredBuiltin("LESS_EQUAL",
		org.deri.iris.builtins.LessEqualBuiltin.class, 2);
	checkRegisteredBuiltin("GREATER",
		org.deri.iris.builtins.GreaterBuiltin.class, 2);
	checkRegisteredBuiltin("GREATER_EQUAL",
		org.deri.iris.builtins.GreaterEqualBuiltin.class, 2);

	checkRegisteredBuiltin("SAME_TYPE",
		org.deri.iris.builtins.SameTypeBuiltin.class, 2);
	checkRegisteredBuiltin("IS_NUMERIC",
		org.deri.iris.builtins.IsNumericBuiltin.class, 1);

	checkRegisteredBuiltin("IS_BASE64BINARY",
		org.deri.iris.builtins.IsBase64BinaryBuiltin.class, 1);
	checkRegisteredBuiltin("IS_BOOLEAN",
		org.deri.iris.builtins.IsBooleanBuiltin.class, 1);
	checkRegisteredBuiltin("IS_DATE",
		org.deri.iris.builtins.IsDateBuiltin.class, 1);
	checkRegisteredBuiltin("IS_DATETIME",
		org.deri.iris.builtins.IsDateTimeBuiltin.class, 1);
	checkRegisteredBuiltin("IS_DECIMAL",
		org.deri.iris.builtins.IsDecimalBuiltin.class, 1);
	checkRegisteredBuiltin("IS_DOUBLE",
		org.deri.iris.builtins.IsDoubleBuiltin.class, 1);
	checkRegisteredBuiltin("IS_DURATION",
		org.deri.iris.builtins.IsDurationBuiltin.class, 1);
	checkRegisteredBuiltin("IS_FLOAT",
		org.deri.iris.builtins.IsFloatBuiltin.class, 1);
	checkRegisteredBuiltin("IS_GDAY",
		org.deri.iris.builtins.IsGDayBuiltin.class, 1);
	checkRegisteredBuiltin("IS_GMONTH",
		org.deri.iris.builtins.IsGMonthBuiltin.class, 1);
	checkRegisteredBuiltin("IS_GMONTHDAY",
		org.deri.iris.builtins.IsGMonthDayBuiltin.class, 1);
	checkRegisteredBuiltin("IS_GYEAR",
		org.deri.iris.builtins.IsGYearBuiltin.class, 1);
	checkRegisteredBuiltin("IS_GYEARMONTH",
		org.deri.iris.builtins.IsGYearMonthBuiltin.class, 1);
	checkRegisteredBuiltin("IS_HEXBINARY",
		org.deri.iris.builtins.IsHexBinaryBuiltin.class, 1);
	checkRegisteredBuiltin("IS_INTEGER",
		org.deri.iris.builtins.IsIntegerBuiltin.class, 1);
	checkRegisteredBuiltin("IS_IRI",
		org.deri.iris.builtins.IsIriBuiltin.class, 1);
	checkRegisteredBuiltin("IS_SQNAME",
		org.deri.iris.builtins.IsSqNameBuiltin.class, 1);
	checkRegisteredBuiltin("IS_STRING",
		org.deri.iris.builtins.IsStringBuiltin.class, 1);
	checkRegisteredBuiltin("IS_TIME",
		org.deri.iris.builtins.IsTimeBuiltin.class, 1);
    }

    /**
     * Asserts whether a builtin was registered with the correct name, class and
     * arity.
     * 
     * @param name
     *            the name of the builtin to check (predicate symbol)
     * @param clazz
     *            the class of the builtin to check
     * @param arity
     *            the arity of the builtin to check
     */
    private void checkRegisteredBuiltin(final String name,
	    final Class<?> clazz, final int arity) {
	assert name != null : "The name must not be null";
	assert clazz != null : "The class must not be null";
	assert arity > 0 : "The arity must be greater than 0";

	assertEquals("Could not find the class of " + name, clazz, reg
		.getBuiltinClass(name));
	assertEquals("Could not find the arity for " + name, arity, reg
		.getBuiltinArity(name));
    }

}
