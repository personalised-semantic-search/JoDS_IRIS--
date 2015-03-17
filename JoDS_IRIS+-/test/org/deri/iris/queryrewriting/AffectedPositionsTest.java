/**
 * 
 */
package org.deri.iris.queryrewriting;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.Position;
import org.deri.iris.factory.Factory;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * @author
 */
public class AffectedPositionsTest extends TestCase {

	private final Logger LOGGER = Logger.getLogger(AffectedPositionsTest.class);

	{
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");
	}

	@Test
	public void testAffectedPositions1() throws Exception {

		//
		// Theory:
		//
		// p(X) -> t(X,Y).
		// t(X,Y) -> s(Y).
		//

		final IVariable x = Factory.TERM.createVariable("X");
		final IVariable y = Factory.TERM.createVariable("Y");

		final IPredicate p = Factory.BASIC.createPredicate("p", 1);
		final IPredicate t = Factory.BASIC.createPredicate("t", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 1);

		final ILiteral px = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(x));
		final ILiteral txy = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(x, y));
		final ILiteral sy = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(y));

		final IRule r1 = Factory.BASIC.createRule(ImmutableList.of(txy), ImmutableList.of(px));
		final IRule r2 = Factory.BASIC.createRule(ImmutableList.of(sy), ImmutableList.of(txy));

		final List<IRule> theory = ImmutableList.of(r1, r2);

		LOGGER.debug("Computing dependency graph.");
		final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> posDeps = DepGraphUtils
		        .computePositionDependencyGraph(theory);

		//
		// Comparison Structure:
		//
		// {t[2], s[1]}
		//
		//
		final Set<IPosition> cmp = ImmutableSet.of((IPosition) new Position("t", 2), (IPosition) new Position("s", 1));
		LOGGER.debug("Expected: " + cmp.toString());
		//
		// test
		//
		final Map<IPosition, Set<IRule>> affected = DepGraphUtils.computeAffectedPositions(theory, posDeps);
		LOGGER.debug("Actual: " + affected.toString());

		Assert.assertEquals(cmp, affected);
	}
}
