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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.deri.iris.api.basics.IRule;
import org.deri.iris.storage.IRelation;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> 
 *	   ICT Institute - Politecnico di Milano.
 * @version 0.1b
 *
 */
public class IRISTestCase {
    
    private IRule query;
    private List<Task> tasks;
    private IRelation answer;
    
    /**
     * Initializes the current test case with an empty list of tasks.
     */
    public IRISTestCase() {
	this.tasks = Collections.synchronizedList(new LinkedList<Task>());
    }
    
    /**
     * @return the query
     */
    public IRule getQuery() {
        return query;
    }
    /**
     * @param query the query to set
     */
    public void setQuery(IRule query) {
        this.query = query;
    }
    /**
     * @return the tasks
     */
    public List<Task> getTasks() {
        return tasks;
    }
    /**
     * @param tasks the tasks to set
     */
    public void setTasks(LinkedList<Task> tasks) {
        this.tasks = tasks;
    }
    /**
     * @return the answer
     */
    public IRelation getAnswer() {
        return answer;
    }
    /**
     * @param answer the answer to set
     */
    public void setAnswer(IRelation answer) {
        this.answer = answer;
    }
    
    public long getAnswerCount() {
	return (this.answer.size());
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
	String result = "==============\n";
	result += "IRIS TEST CASE\n";
	result += "==============\n\n";
	
	result += "+++++\n";
	result += "Query\n";
	result += "+++++\n";
	
	result += this.getQuery().toString() + "\n";
	result += "\n";
	
	result += "+++++\n";
	result += "TASKS\n";
	result += "+++++\n";
	
	for (Task t : this.getTasks())
	    result += t.toString() + "\n";
	result += "\n";
	
	result += "+++++++\n";
	result += "ANSWERS\n";
	result += "+++++++\n";
	
	result += "Count: " + this.getAnswerCount() + "\n";
	result += "Tuples: \n";
	
	IRelation r = this.getAnswer();
	for (int i=0; i<r.size(); i++) 
	    result += r.get(i).toString() + "\n";
	
	return (result);
    }
}
