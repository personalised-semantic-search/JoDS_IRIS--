/**
 * 
 */
package org.deri.iris.queryrewriting;

import java.util.ArrayList;
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

/**
 * @author jd
 */
public class QueryReductionTest extends TestCase {

	@Override
	public void setUp() {
		// setup caching
		CacheManager.setupCaching();
	}

	static {
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");

	}

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

	@Test
	public void testQueryReduce() throws ParserException {

		// TGDs
		final IPredicate p = Factory.BASIC.createPredicate("p", 1);
		final IPredicate t = Factory.BASIC.createPredicate("t", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 1);
		final IPredicate m = Factory.BASIC.createPredicate("m", 1);

		final ILiteral px = Factory.BASIC.createLiteral(true, p, tupleX);
		final ILiteral txy = Factory.BASIC.createLiteral(true, t, tupleXY);
		final ILiteral sx = Factory.BASIC.createLiteral(true, s, tupleX);
		final ILiteral sy = Factory.BASIC.createLiteral(true, s, tupleY);
		final ILiteral mx = Factory.BASIC.createLiteral(true, m, tupleX);

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

		final List<IRule> in = new ArrayList<IRule>();
		in.add(r1);
		in.add(r2);
		in.add(r3);
		in.add(r4);

		// Queries

		final ILiteral tAB = Factory.BASIC.createLiteral(true, t, tupleAB);
		final ILiteral sA = Factory.BASIC.createLiteral(true, s, tupleA);
		final ILiteral sB = Factory.BASIC.createLiteral(true, s, tupleB);
		final ILiteral pA = Factory.BASIC.createLiteral(true, p, tupleA);
		final ILiteral pB = Factory.BASIC.createLiteral(true, p, tupleB);
		final ILiteral tBC = Factory.BASIC.createLiteral(true, t, tupleBC);
		final ILiteral tBA = Factory.BASIC.createLiteral(true, t, tupleBA);
		final ILiteral mA = Factory.BASIC.createLiteral(true, m, tupleA);
		final ILiteral mB = Factory.BASIC.createLiteral(true, m, tupleB);
		final ILiteral mC = Factory.BASIC.createLiteral(true, m, tupleC);

		final IPredicate q = Factory.BASIC.createPredicate("q", 0);
		final ILiteral ql = Factory.BASIC.createLiteral(true, q, emptyTuple);
		final List<ILiteral> qh = new UniqueList<ILiteral>();
		qh.add(ql);

		Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils.computePositionDependencyGraph(in);
		deps = DepGraphUtils.computeAtomCoverageGraph(deps);

		// Query 1
		final List<ILiteral> q1b = new UniqueList<ILiteral>();
		q1b.add(tAB);
		q1b.add(sB);

		final IRule q1 = Factory.BASIC.createRule(qh, q1b);

		final List<ILiteral> q1Compb = new UniqueList<ILiteral>();
		q1Compb.add(tAB);

		final IRule q1Comp = Factory.BASIC.createRule(qh, q1Compb);

		// Query 2
		final List<ILiteral> q2b = new UniqueList<ILiteral>();
		q2b.add(mB);
		q2b.add(tAB);

		final IRule q2 = Factory.BASIC.createRule(qh, q2b);

		final List<ILiteral> q2Compb = new UniqueList<ILiteral>();
		q2Compb.add(tAB);

		final IRule q2Comp = Factory.BASIC.createRule(qh, q2Compb);

		// Query 3
		final List<ILiteral> q3b = new UniqueList<ILiteral>();
		q3b.add(mA);
		q3b.add(tAB);

		final IRule q3 = Factory.BASIC.createRule(qh, q3b);

		final List<ILiteral> q3Compb = new UniqueList<ILiteral>();
		q3Compb.add(mA);
		q3Compb.add(tAB);

		final IRule q3Comp = Factory.BASIC.createRule(qh, q3Compb);

		// Query 4
		final List<ILiteral> q4b = new UniqueList<ILiteral>();
		q4b.add(mA);
		q4b.add(tAB);
		q4b.add(tBA);
		q4b.add(tBC);
		q4b.add(sB);
		q4b.add(mB);
		q4b.add(mC);
		q4b.add(sA);

		final IRule q4 = Factory.BASIC.createRule(qh, q4b);

		final List<ILiteral> q4Compb = new UniqueList<ILiteral>();
		q4Compb.add(tAB);
		q4Compb.add(tBA);
		q4Compb.add(tBC);

		final IRule q4Comp = Factory.BASIC.createRule(qh, q4Compb);

		// Tests
		final IRule q1Red = RewritingUtils.reduceQuery(q1, deps);
		final IRule q2Red = RewritingUtils.reduceQuery(q2, deps);
		final IRule q3Red = RewritingUtils.reduceQuery(q3, deps);
		final IRule q4Red = RewritingUtils.reduceQuery(q4, deps);

		assertEquals(RenamingUtils.canonicalRenaming(q1Comp, "U_"), RenamingUtils.canonicalRenaming(q1Red, "U_"));
		assertEquals(RenamingUtils.canonicalRenaming(q2Comp, "U_"), RenamingUtils.canonicalRenaming(q2Red, "U_"));
		assertEquals(RenamingUtils.canonicalRenaming(q3Comp, "U_"), RenamingUtils.canonicalRenaming(q3Red, "U_"));
		assertEquals(RenamingUtils.canonicalRenaming(q4Comp, "U_"), RenamingUtils.canonicalRenaming(q4Red, "U_"));

	}
}
