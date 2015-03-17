/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2009 ICT Institute - Dipartimento di Elettronica e Informazione (DEI), 
 * Politecnico di Milano, Via Ponzio 34/5, 20133 Milan, Italy.
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
package org.deri.iris.storage.relational;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.StorageManager;

/**
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 * @version 1.0
 */
public class StoredRelation implements IRelation{
    
    /** The array list of tuples. */
    private final List<ITuple> mTuples;
	
    public StoredRelation (String predicate) {
	mTuples = StorageManager.getStoredRelation(predicate);
    }
    
    @Override
    public boolean add(ITuple tuple) {
	assert mTuples.isEmpty() || (mTuples.get(0).size() == tuple.size());
	
	if (!this.contains(tuple)) {
	    mTuples.add(tuple);
	    return ( true );
	} else
	    return ( false );
    }

    @Override
    public boolean addAll(IRelation relation) {
	boolean added = false;

	for (int i = 0; i < relation.size(); ++i)
	    if (add(relation.get(i)))
		added = true;

	return added;
    }

    @Override
    public boolean contains(ITuple tuple) {
	
	if (mTuples.contains(tuple))
	    return (true);
	else
	    return false;
    }

    @Override
    public ITuple get(int index) {	
	return (mTuples.get(index));
    }

    @Override
    public int size() {
	return (mTuples.size());
    }
    
    @Override
    public String toString() {
	String output = "[";
	
	if (mTuples.size() > 0) {
	    for (int i=0; i<mTuples.size()-1; i++)
		output+= "(" + mTuples.get(i).toString() + ")" + ",";
	    output += "(" + mTuples.get(mTuples.size()-1) + ")";
	}
	output += "]";
	return (output);
    }
    
    public Set<ITuple> tuples() {
	return (new HashSet<ITuple>(mTuples));
    }
}