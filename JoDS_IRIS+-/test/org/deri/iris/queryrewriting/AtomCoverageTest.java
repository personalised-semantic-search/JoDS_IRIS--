/**
 * 
 */
package org.deri.iris.queryrewriting;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;
import org.deri.iris.parser.parser.ParserException;
import org.deri.iris.queryrewriting.caching.CacheManager;
import org.deri.iris.utils.UniqueList;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * @author jd
 */
public class AtomCoverageTest extends TestCase {

	private final Logger LOGGER = Logger.getLogger(PositionDependenciesTest.class);

	private static final IVariable VAR_B = Factory.TERM.createVariable("B");

	private static final IVariable VAR_A = Factory.TERM.createVariable("A");

	private static final IVariable VAR_C = Factory.TERM.createVariable("C");

	private static final IVariable VAR_Y = Factory.TERM.createVariable("Y");

	private static final IVariable VAR_X = Factory.TERM.createVariable("X");

	private final ITuple emptyTuple = Factory.BASIC.createTuple();

	private final ITuple tupleX = Factory.BASIC.createTuple(VAR_X);

	private final ITuple tupleY = Factory.BASIC.createTuple(VAR_Y);

	private final ITuple tupleXY = Factory.BASIC.createTuple(VAR_X, VAR_Y);

	private final ITuple tupleYX = Factory.BASIC.createTuple(VAR_Y, VAR_X);

	private final ITuple tupleA = Factory.BASIC.createTuple(VAR_A);

	private final ITuple tupleB = Factory.BASIC.createTuple(VAR_B);

	private final ITuple tupleC = Factory.BASIC.createTuple(VAR_C);

	private final ITuple tupleBA = Factory.BASIC.createTuple(VAR_B, VAR_A);

	private final ITuple tupleAB = Factory.BASIC.createTuple(VAR_A, VAR_B);

	private final ITuple tupleBC = Factory.BASIC.createTuple(VAR_B, VAR_C);

	@Override
	public void setUp() {
		// setup caching
		CacheManager.setupCaching();
	}

	static {
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");

	}

	@Test
	public void testAtomCoverage() throws ParserException {

		final IPredicate p = Factory.BASIC.createPredicate("p", 1);
		final IPredicate t = Factory.BASIC.createPredicate("t", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 1);
		final IPredicate m = Factory.BASIC.createPredicate("m", 1);

		final ILiteral px = Factory.BASIC.createLiteral(true, p, tupleX);
		final ILiteral txy = Factory.BASIC.createLiteral(true, t, tupleXY);
		final ILiteral sx = Factory.BASIC.createLiteral(true, s, tupleX);
		final ILiteral sy = Factory.BASIC.createLiteral(true, s, tupleY);
		final ILiteral mx = Factory.BASIC.createLiteral(true, m, tupleX);

		final ILiteral tAB = Factory.BASIC.createLiteral(true, t, tupleAB);
		final ILiteral sA = Factory.BASIC.createLiteral(true, s, tupleA);
		final ILiteral sB = Factory.BASIC.createLiteral(true, s, tupleB);
		final ILiteral pA = Factory.BASIC.createLiteral(true, p, tupleA);
		final ILiteral pB = Factory.BASIC.createLiteral(true, p, tupleB);
		final ILiteral tBC = Factory.BASIC.createLiteral(true, t, tupleBC);
		final ILiteral mB = Factory.BASIC.createLiteral(true, m, tupleB);

		final IPredicate q = Factory.BASIC.createPredicate("q", 0);
		final ILiteral ql = Factory.BASIC.createLiteral(true, q, emptyTuple);

		final List<ILiteral> qh = ImmutableList.of(ql);
		// q() <- t(A,B), s(B).
		final IRule qtest1 = Factory.BASIC.createRule(qh, ImmutableList.of(tAB, sB));
		// q() <- t(A,B), m(B).
		final IRule qtest2 = Factory.BASIC.createRule(qh, ImmutableList.of(tAB, mB));
		// q() <- p(A), s(A).
		final IRule qtest3 = Factory.BASIC.createRule(qh, ImmutableList.of(pA, sA));
		// q() <- p(A), t(A,B).
		final IRule qtest4 = Factory.BASIC.createRule(qh, ImmutableList.of(pA, tAB));
		// q() <- t(A,B), t(B,C).
		final IRule qtest5 = Factory.BASIC.createRule(qh, ImmutableList.of(tAB, tBC));

		//
		// Theory:
		//
		// p(X) -> t(X,Y).
		// t(X,Y) -> s(Y).
		// p(X) -> m(X).
		// s(X) -> m(X).
		//

		final List<ILiteral> h1 = new UniqueList<ILiteral>();
		final List<ILiteral> b1 = new UniqueList<ILiteral>();
		h1.add(txy);
		b1.add(px);

		final List<ILiteral> h2 = new UniqueList<ILiteral>();
		final List<ILiteral> b2 = new UniqueList<ILiteral>();
		h2.add(sy);
		b2.add(txy);

		final List<ILiteral> h3 = new UniqueList<ILiteral>();
		final List<ILiteral> b3 = new UniqueList<ILiteral>();
		h3.add(mx);
		b3.add(px);

		final List<ILiteral> h4 = new UniqueList<ILiteral>();
		final List<ILiteral> b4 = new UniqueList<ILiteral>();
		h4.add(mx);
		b4.add(sx);

		final IRule r1 = Factory.BASIC.createRule(h1, b1);
		final IRule r2 = Factory.BASIC.createRule(h2, b2);
		final IRule r3 = Factory.BASIC.createRule(h3, b3);
		final IRule r4 = Factory.BASIC.createRule(h4, b4);

		final List<IRule> in = ImmutableList.of(r1, r2, r3, r4);

		final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils.computePositionDependencyGraph(in);

		assertEquals(true, RewritingUtils.covers(tAB, sB, deps, qtest1));
		assertEquals(true, RewritingUtils.covers(tAB, mB, deps, qtest2));
		assertEquals(false, RewritingUtils.covers(mB, tAB, deps, qtest3));
		assertEquals(false, RewritingUtils.covers(pA, sA, deps, qtest4));
		assertEquals(false, RewritingUtils.covers(pA, tAB, deps, qtest5));

	}

}
