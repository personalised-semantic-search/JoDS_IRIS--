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
package org.deri.iris.performance;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.storage.StorageManager;
import org.deri.iris.terms.TermFactory;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> 
 *	   ICT Institute - Politecnico di Milano.
 * @version 0.1b
 *
 */
public class RunnableQuery implements Runnable {

    private String query;
    private Set<ITuple> answer; 
    private List<Task> tasks;
    
    public RunnableQuery(String q, Set<ITuple> ans, List<Task> tl) {
	this.query = q;
	
	/*
	 *  Synchronized structure for storing the answers of
	 *  concurrent Runnable Queries
	 */
	this.answer = ans;
	
	this.tasks = tl;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
	IBasicFactory bf = BasicFactory.getInstance();
	ITermFactory tf = TermFactory.getInstance();
	ResultSet rs = null;
	try {
	    // Execute the query
	    System.out.println("Executing: " + query);
	    Connection conn = StorageManager.getDataSource().getConnection();
	    Statement st = conn.createStatement();
	    float cqExec = (float)-System.nanoTime();
	    float initTime = -cqExec;
	    rs = st.executeQuery(query);
	    float finalTime = (float)System.nanoTime();
	    cqExec = ((float)(cqExec + finalTime)/1000000);
	    tasks.add(new Task(tasks.size()+1, "Execution: " + query + ".", cqExec, initTime, finalTime,"ms"));
	    
	    // Construct the result
	    System.out.println("Constructing: " + query);
	    float ansConstruct = (float)-System.nanoTime();
	    initTime = -ansConstruct;
	    int columnCount = rs.getMetaData().getColumnCount();
	    while (rs.next()) {
		List<ITerm> terms = new ArrayList<ITerm>();
		for (int i=1; i <= columnCount; i++)
		    terms.add(tf.createString(rs.getString(i)));
		answer.add(bf.createTuple(terms));
	    }
	    finalTime = (float)System.nanoTime();
	    ansConstruct = ((float)(ansConstruct + finalTime)/1000000);
	    System.out.println("Construction Terminated for: " + query);
	    tasks.add(new Task(tasks.size()+1, "Construction: " + query + ".", ansConstruct, initTime, finalTime,"ms"));
	    rs.close();
	    st.close();
	    conn.close();
	} catch (SQLSyntaxErrorException se) {
	    System.out.println("Responsible: " + query);
	    se.printStackTrace();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

}
