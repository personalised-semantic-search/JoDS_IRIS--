/*
 * Integrated Rule Inference System (IRIS+-):
 * An extensible rule inference system for datalog with extensions.
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
package org.deri.iris;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.performance.IRISPerformanceTest;
import org.deri.iris.performance.IRISTestCase;
import org.deri.iris.performance.TestConfiguration;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> 
 *	   ICT Institute - Politecnico di Milano.
 * @version 0.1b
 *
 */
public class PerformanceTester extends TestCase {

    private List<String> queries;
    private TestConfiguration config;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
	super.setUp();
	
	// Load the logging configuration
	PropertyConfigurator.configure("config/logging.properties");
	
	// Set the queries for the test
	queries = new LinkedList<String>();
	queries.add("Q4(?X) :- Publication(?X).");
	//queries.add("Q5(?X) :- ResearchGroup(?X), subOrganizationOf(?X,'http:www.University0.edu#University0').");
	//queries.add("Q10(?X) :- isFriendOf(?X,'http:www.Department0.University0.edu#FullProfessor0').");
	//queries.add("Q12(?X) :- Student(?X), takesCourse(?X,?Y), isTaughtBy(?Y,'http://www.Department0.University0.edu/FullProfessor0').");
	// Set the configuration for the test
	config = new TestConfiguration();
	config.setDataset("UOBM");
	config.setDBVendor("_ORACLE");
	config.setDBHost("pamir.dia.uniroma3.it");
	config.setDBName("Yaanii");
	config.setDBPort(1521);
	config.setDBProtocol("tcp");
	config.setDBUsername("UOBM2037");
	config.setDBPassword("!0_U03M_$!");
	config.setExpressiveness("OWL-QL");
	config.setReasoning(true);
	config.setTestHomePath("nyayaTest");
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
	super.tearDown();
    }
    
    public void testPerformance() {
	
	// Get the tester
	IRISPerformanceTest testSuite = new IRISPerformanceTest();
	
	// Execute the tests
	List<IRISTestCase> result = testSuite.executeTests(queries, config);
	
	// Print the results
	for (IRISTestCase t : result) {
	    System.out.println(t);
	}
	
    }

}
