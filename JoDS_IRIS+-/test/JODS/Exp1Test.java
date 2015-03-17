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
package JODS;

import static org.deri.iris.factory.Factory.BASIC;
import static org.deri.iris.factory.Factory.TERM;
import group.models.PreferenceStrategy;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.graph.LabeledEdge;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.jgrapht.graph.DefaultEdge;

import JODS.PreferencesGraph;

/**
 * <p>
 * Tests the JODS.
 * </p>
 * <p>
 * $Id$
 * </p>
 * 
 * @author Oana Tifrea-Marciuska
 * @version $Revision$
 */
public class Exp1Test extends TestCase {

	/** The unrecursive preference graph. */
	private PreferencesGraph pg1;
	private Map<String, Double> probModel;
	

	private static final ITuple m1 = BASIC.createTuple(TERM.createString("m1"));
	private static final ITuple m2 = BASIC.createTuple(TERM.createString("m2"));
	private static final ITuple m3 = BASIC.createTuple(TERM.createString("m3"));
	private static final ITuple m4 = BASIC.createTuple(TERM.createString("m4"));
	private static final ITuple m5 = BASIC.createTuple(TERM.createString("m5"));
	private static final ITuple m6 = BASIC.createTuple(TERM.createString("m6"));
	private static final ITuple m7 = BASIC.createTuple(TERM.createString("m7"));
	private static final ITuple m8 = BASIC.createTuple(TERM.createString("m8"));
	private static final ITuple m9 = BASIC.createTuple(TERM.createString("m9"));
	private static final ITuple m10 = BASIC.createTuple(TERM.createString("m10"));
	private static final ITuple m11 = BASIC.createTuple(TERM.createString("m11"));
	private static final ITuple m12 = BASIC.createTuple(TERM.createString("m12"));
	private static final ITuple m13 = BASIC.createTuple(TERM.createString("m13"));
	private static final ITuple m14 = BASIC.createTuple(TERM.createString("m14"));
	private static final ITuple m15 = BASIC.createTuple(TERM.createString("m15"));

	public static Test suite() {
		return new TestSuite(Exp1Test.class,
				Exp1Test.class.getSimpleName());
	}

	public void testcombPrefsGen() throws NumberFormatException, IOException {
		System.out.println("top 15 t=0.3"
				+ TopKAlgorithms.topkPrefsGen(pg1, probModel, 0.3, 15));
		System.out.println("top 15 t=0.15"
				+ TopKAlgorithms.topkPrefsGen(pg1, probModel, 0.15, 15));
		
		Map<ITuple, Integer> ranks = CombinationAlgorithms.combPrefsRank(pg1,
		 probModel, Function.Min);
		Map<ITuple, Integer> ranks2 = CombinationAlgorithms.combPrefsRank(pg1,
				 probModel, Function.Max);
		
		System.out.println("top 15 Rank Min"
				+ TopKAlgorithms.topkPrefsRank(ranks, 15));
		System.out.println("top 15 Rank Max"
				+ TopKAlgorithms.topkPrefsRank(ranks2, 15));
		
		
		System.out.println("top 15 p=0.5"
				+ TopKAlgorithms.topkPrefsPT(pg1, probModel, 0.5, 15));
		System.out.println("top 15 p=0.25"
				+ TopKAlgorithms.topkPrefsPT(pg1, probModel, 0.25, 15));
		
		System.out.println("top 15 Sort"
				+ TopKAlgorithms.topkPrefsSort(pg1, probModel, 15));
		
		
	}



	public void setUp() {
		probModel = new HashMap<String, Double>();
		probModel.put("m1", 0.90);
		probModel.put("m2", 0.39);
		probModel.put("m3", 0.96);
		probModel.put("m4", 0.03);
		probModel.put("m5", 0.90);
		probModel.put("m6", 0.39);
		probModel.put("m7", 0.64);
		probModel.put("m8", 0.69);
		probModel.put("m9", 0.90);
		probModel.put("m10", 0.69);
		probModel.put("m11", 0.98);
		probModel.put("m12", 0.39);
		probModel.put("m13", 0.98);
		probModel.put("m14", 0.39);
		probModel.put("m15", 0.98);
		
		
		pg1 = new PreferencesGraph();
		pg1.addPreference(m7, m1);
		pg1.addPreference(m7, m2);
		pg1.addPreference(m4, m1);
		pg1.addPreference(m4, m2);
		pg1.addPreference(m3, m1);
		pg1.addPreference(m3, m2);
		
		pg1.addPreference(m1, m14);
		pg1.addPreference(m1, m11);
		pg1.addPreference(m1, m9);
		pg1.addPreference(m1, m5);
		
		pg1.addPreference(m2, m14);
		pg1.addPreference(m2, m11);
		pg1.addPreference(m2, m9);
		pg1.addPreference(m2, m5);
		
		pg1.addPreference(m11, m12);
		pg1.addPreference(m5, m15);
		
		pg1.addPreference(m12, m13);
		pg1.addPreference(m12, m10);
		pg1.addPreference(m15, m13);
		pg1.addPreference(m15, m10);
		
		pg1.addPreference(m13, m6);
		pg1.addPreference(m10, m8);
	}
}
