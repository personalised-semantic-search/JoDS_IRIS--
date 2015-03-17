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
package org.deri.iris.queryrewriting;

import it.uniroma3.dia.ndm.OIDBroker;

import java.util.List;

import junit.framework.TestCase;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IStringTerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.parser.parser.ParserException;
import org.deri.iris.utils.UniqueList;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> 
 *	   ICT Institute - Politecnico di Milano.
 * @version 0.1b
 *
 */
public class NyayaOptimizationTest extends TestCase {

    private static final IStringTerm STR_NC = Factory.TERM.createString("c");
    
    private static final IStringTerm STR_NC_RES = Factory.TERM.createString(OIDBroker.getOID("c"));
    
    private static final IStringTerm STR_NP = Factory.TERM.createString("dp");
    
    private static final IStringTerm STR_NP_RES = Factory.TERM.createString(OIDBroker.getOID("dp"));
    
    private static final IStringTerm STR_NR = Factory.TERM.createString("op");
    
    private static final IStringTerm STR_NR_RES = Factory.TERM.createString(OIDBroker.getOID("op"));
    
    private static final IStringTerm STR_V = Factory.TERM.createString("v");
    
    private static final IVariable VAR_X1 = Factory.TERM.createVariable("X1");

    private static final IVariable VAR_X2 = Factory.TERM.createVariable("X2");

    private static final IVariable VAR_X3 = Factory.TERM.createVariable("X3");

    private static final IVariable VAR_X4 = Factory.TERM.createVariable("X4");
    
    private static final IVariable VAR_A = Factory.TERM.createVariable("A");
    
    private static final IVariable VAR_B = Factory.TERM.createVariable("B");
    
    private ITuple iclassTuple = Factory.BASIC.createTuple(VAR_X1, VAR_A, VAR_X2, VAR_X3);
    
    private ITuple classTuple = Factory.BASIC.createTuple(VAR_X2, VAR_A, VAR_X4);
    
    private ITuple iclassTupleResolved = Factory.BASIC.createTuple(VAR_X1, VAR_A, VAR_X2, STR_NC_RES);

    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
	super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
	super.tearDown();
    }
    
    /**
     * I_CLASS(?X1, A, ?X2, ?X3), CLASS(?X2, 'NC', ?X4).
     * 
     * @throws ParserException
     */
    public void testClassIClassReferenceSubstitution1() throws ParserException {
	IPredicate p1 = Factory.BASIC.createPredicate("I_CLASS", 4);
	IPredicate p2 = Factory.BASIC.createPredicate("CLASS", 3);
	ILiteral lit11 = Factory.BASIC.createLiteral(true, p1, iclassTuple);
	ILiteral lit12 = Factory.BASIC.createLiteral(true, p2, classTuple);
	ILiteral lit21 = Factory.BASIC.createLiteral(true, p1, iclassTupleResolved);

	List<ILiteral> l1 = new UniqueList<ILiteral>();
	l1.add(lit11);
	l1.add(lit12);
	
	List<ILiteral> l2 = new UniqueList<ILiteral>();
	l2.add(lit21);
	
	IRule cmp = Factory.BASIC.createRule(new UniqueList<ILiteral>(), l2);
	IRule r = Factory.BASIC.createRule(new UniqueList<ILiteral>(), l1);
	IRule actual = null;
	
    }

}
