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
package org.deri.iris.storage;

import org.deri.iris.storage.relational.StoredRelation;
import org.deri.iris.storage.simple.SimpleRelation;

/**
 * Factory for simple relations.
 */
public class RelationFactory implements IRelationFactory {
    
    public IRelation createRelation() {
	return new SimpleRelation();
    }
    
    public IRelation createRelation(boolean storage, String predicate) {
	if (storage)
	    return new StoredRelation(predicate);
	else
	    return createRelation();
    }
}
