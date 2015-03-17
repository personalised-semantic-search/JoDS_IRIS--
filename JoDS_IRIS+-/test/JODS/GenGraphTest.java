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
public class GenGraphTest extends TestCase {

	/** The unrecursive preference graph. */
	private PreferencesGraph pg1;
	private Map<String, Double> probModel;
	
	private static final ITuple a = BASIC.createTuple(TERM.createString("a"));
	private static final ITuple b = BASIC.createTuple(TERM.createString("b"));
	private static final ITuple c = BASIC.createTuple(TERM.createString("c"));
	private static final ITuple d = BASIC.createTuple(TERM.createString("d"));
	private static final ITuple e = BASIC.createTuple(TERM.createString("e"));
	private static final ITuple m1 = BASIC.createTuple(TERM.createString("m1"));
	private static final ITuple m2 = BASIC.createTuple(TERM.createString("m2"));
	private static final ITuple m3 = BASIC.createTuple(TERM.createString("m3"));
	private static final ITuple m4 = BASIC.createTuple(TERM.createString("m4"));
	private static final ITuple m5 = BASIC.createTuple(TERM.createString("m5"));
	private static final ITuple m6 = BASIC.createTuple(TERM.createString("m6"));

	public static Test suite() {
		return new TestSuite(GenGraphTest.class,
				GenGraphTest.class.getSimpleName());
	}

	public void testcombPrefsGen() throws NumberFormatException, IOException {
		// PreferencesGraph pg2 = CombinationAlgorithms.combPrefsGen(pg1,
		// probModel, 0);
		// System.out.println("---------------------");
		PreferencesGraph pg3 = CombinationAlgorithms.combPrefsGen(pg1,
				probModel, 0.3);
		// System.out.println("combPrefsGen 0.0 " + pg2);
		System.out.println("combPrefsGen 0.3 " + pg3);
		System.out
				.println("top3 " + TopKAlgorithms.getTopK(pg3, 3));
		System.out.println("top3 "
				+ TopKAlgorithms.topkPrefsGen(pg1, probModel, 0.3, 3));
		// Map<ITuple, Integer> pg2a = CombinationAlgorithms.combPrefsRank(pg1,
		// probModel, Function.Min);
		//
		// Map<ITuple, Integer> pg3b = CombinationAlgorithms.combPrefsRank(pg1,
		// probModel, Function.Max);
		// System.out.println("combPrefsRank Min" + pg2a);
		// System.out.println("combPrefsRank Max" + pg3b);

		// PreferencesGraph pt = CombinationAlgorithms.combPrefsPT(pg1,
		// probModel,
		// 0.7);
		// System.out.println("combPrefsPT " + pt);s
		// PreferencesGraph psort = CombinationAlgorithms.combPrefsSort(pg1,
		// probModel);
		// System.out.println("topkgen "
		// + TopKAlgorithms.topkPrefsGen(pg1, probModel, 0.3, 4));
		// System.out.println("topksort "
		// + TopKAlgorithms.topkPrefsSort(pg1, probModel, 4));
		// System.out
		// .println("topkrank Max "
		// + TopKAlgorithms.topkPrefsRank(pg1, probModel,
		// Function.Max, 4));
		// System.out.println("topkpt "
		// + TopKAlgorithms.topkPrefsPT(pg1, probModel, 0.75, 4));
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		result.add(a);
		result.add(b);
		result.add(c);
		result.add(d);
		final PreferencesGraph prefGraph = PreferenceGenerator
				.generatePreferenceGraph(1, result);
		System.out.println("generated " + prefGraph);

//		probModel = ProbabilisticModel
//				.get("examples/jods_test/input/reviews.txt");
//		System.out.print(probModel.size());

	}

	public void testcombPrefsRank() throws NumberFormatException, IOException {
		// Map<ITuple, Integer> pg2 = CombinationAlgorithms.combPrefsRank(pg1,
		// probModel, Function.Min);
		//
		// Map<ITuple, Integer> pg3 = CombinationAlgorithms.combPrefsRank(pg1,
		// probModel, Function.Max);
		// System.out.println("combPrefsRank Min" + pg2);
		// System.out.println("combPrefsRank Max" + pg3);

	}

	public void testcombPrefsPT() throws NumberFormatException, IOException {
		// PreferencesGraph pg2 = CombinationAlgorithms.combPrefsSort(pg1,
		// probModel);
		// PreferencesGraph pg2 = CombinationAlgorithms.combPrefsPT(pg1,
		// probModel, 0.3);
		// System.out.println("combPrefsPT " + pg2);

	}

	public void combPrefsSort() throws NumberFormatException, IOException {
		// PreferencesGraph pg2 = CombinationAlgorithms.combPrefsSort(pg1,
		// probModel);
		// System.out.println("combPrefsSort " + pg2);

	}

	public void setUp() {
		// probModel = new HashMap<String, Double>();
		// probModel.put("a", 0.1);
		// probModel.put("b", 0.9);
		// probModel.put("c", 0.5);
		// probModel.put("d", 0.6);
		// probModel.put("e", 0.4);
		//
		// pg1 = new PreferencesGraph();
		// pg1.addPreference(a, d);
		// pg1.addPreference(a, c);
		// pg1.addPreference(a, b);
		// pg1.addPreference(c, b);
		// pg1.addPreference(c, d);
		// pg1.addPreference(e, d);
		// pg1.addPreference(e, c);
		// pg1.addPreference(e, b);
		probModel = new HashMap<String, Double>();
		probModel.put("m1", 0.7);
		probModel.put("m2", 0.45);
		probModel.put("m3", 0.87);
		probModel.put("m4", 0.68);
		probModel.put("m5", 0.75);
		probModel.put("m6", 0.95);

		pg1 = new PreferencesGraph();
		pg1.addPreference(m2, m6);
		pg1.addPreference(m4, m6);
		pg1.addPreference(m6, m1);
		pg1.addPreference(m6, m3);
		pg1.addPreference(m1, m5);
	}
}
