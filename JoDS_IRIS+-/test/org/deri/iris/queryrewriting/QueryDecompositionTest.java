/**
 * 
 */
package org.deri.iris.queryrewriting;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.Position;
import org.deri.iris.factory.Factory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author jd
 */
public class QueryDecompositionTest extends TestCase {

	static {
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");

	}

	public static Logger LOGGER = Logger.getLogger(QueryDecompositionTest.class);

	public void testQueryDecomposition1() throws Exception {

		// Queries:

		// q(?T0) :- p(?T0, ?T1, ?T2), p(?T3, ?T4, ?T5), r(?T0, ?T3), s(?T0), s(?T3).

		final IPredicate q = Factory.BASIC.createPredicate("q", 1);

		final IPredicate p = Factory.BASIC.createPredicate("p", 3);
		final IPredicate r = Factory.BASIC.createPredicate("r", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 1);

		final IVariable t0 = Factory.TERM.createVariable("T0");
		final IVariable t1 = Factory.TERM.createVariable("T1");
		final IVariable t2 = Factory.TERM.createVariable("T2");
		final IVariable t3 = Factory.TERM.createVariable("T3");
		final IVariable t4 = Factory.TERM.createVariable("T4");
		final IVariable t5 = Factory.TERM.createVariable("T5");

		final ILiteral qt0 = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple(t0));

		final ILiteral pt0t1t2 = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(t0, t1, t2));
		final ILiteral pt3t4t5 = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(t3, t4, t5));

		final ILiteral rt0t3 = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(t0, t3));
		final ILiteral st0 = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(t0));
		final ILiteral st3 = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(t3));

		final IRule q1 = Factory.BASIC.createRule(ImmutableList.of(qt0),
		        ImmutableList.of(pt0t1t2, pt3t4t5, rt0t3, st0, st3));

		final Map<IPosition, Set<IRule>> exPos = Maps.newHashMap();
		// the actual rule is not meaningful in this case
		final IRule tgd = Factory.BASIC.createRule(Lists.newArrayList(st0), Lists.newArrayList(st0));
		exPos.put(new Position("r", 1), Sets.newHashSet(tgd));

		final Set<Set<ILiteral>> decomposition = Sets.newLinkedHashSet();
		decomposition.add(ImmutableSet.of(pt0t1t2));
		decomposition.add(ImmutableSet.of(pt3t4t5));
		decomposition.add(ImmutableSet.of(rt0t3, st0, st3));

		// Do the test
		final Set<IRule> components = RewritingUtils.constructQueryComponents(q1, exPos, decomposition);
		LOGGER.info(components);
	}

	public void testDecomposeNonExJoins() {
		/*
		 * r1: p(X,Y) -> r(X,Y,Z). r2: p(X,Y) -> r(Z,Y,X).
		 * 
		 * Query: q(A,B,C,D,E) <- r(A,B,C), r(C,D,E)
		 * 
		 * is decomposable
		 */
		final IPredicate q = Factory.BASIC.createPredicate("q", 5);

		final IPredicate p = Factory.BASIC.createPredicate("p", 2);
		final IPredicate r = Factory.BASIC.createPredicate("r", 3);

		final IVariable X = Factory.TERM.createVariable("X");
		final IVariable Y = Factory.TERM.createVariable("Y");
		final IVariable Z = Factory.TERM.createVariable("Z");

		final IVariable A = Factory.TERM.createVariable("A");
		final IVariable B = Factory.TERM.createVariable("B");
		final IVariable C = Factory.TERM.createVariable("C");
		final IVariable D = Factory.TERM.createVariable("D");
		final IVariable E = Factory.TERM.createVariable("E");

		final ILiteral qABCDE = Factory.BASIC.createLiteral(true, q, Factory.BASIC.createTuple(A, B, C, D, E));

		final ILiteral pXY = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(X, Y));
		final ILiteral rXYZ = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(X, Y, Z));
		final ILiteral rZYX = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(Z, Y, X));

		final ILiteral rABC = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(A, B, C));
		final ILiteral rCDE = Factory.BASIC.createLiteral(true, r, Factory.BASIC.createTuple(C, D, E));

		final IRule r1 = Factory.BASIC.createRule(Sets.newHashSet(rXYZ), Sets.newHashSet(pXY));
		final IRule r2 = Factory.BASIC.createRule(Sets.newHashSet(rZYX), Sets.newHashSet(pXY));

		final IRule query = Factory.BASIC.createRule(Sets.newHashSet(qABCDE), Sets.newHashSet(rABC, rCDE));

		final List<IRule> tgds = Lists.newArrayList(r1, r2);

		final Set<IRule> components = RewritingUtils.queryDecomposition(query, tgds,
		        DepGraphUtils.computePositionDependencyGraph(tgds));

		// Comparison Structure
		final IPredicate q1 = Factory.BASIC.createPredicate("q_1", 3);
		final IPredicate q2 = Factory.BASIC.createPredicate("q_2", 3);

		final ILiteral qcABC = Factory.BASIC.createLiteral(true, q1, Factory.BASIC.createTuple(A, B, C));
		final ILiteral qcCDE = Factory.BASIC.createLiteral(true, q2, Factory.BASIC.createTuple(C, D, E));

		final IRule c1 = Factory.BASIC.createRule(Sets.newHashSet(qcABC), Sets.newHashSet(rABC));
		final IRule c2 = Factory.BASIC.createRule(Sets.newHashSet(qcCDE), Sets.newHashSet(rCDE));

		Assert.assertEquals(Sets.newHashSet(c1, c2), components);

	}
}
