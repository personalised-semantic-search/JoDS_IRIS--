/**
 * 
 */
package org.deri.iris.performance;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.ExpressivityChecker;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.factory.Factory;

import com.google.common.collect.ImmutableList;

/**
 * @author
 */
public class ExpressivityCheckerTest extends TestCase {

	private final Logger LOGGER = Logger.getLogger(ExpressivityCheckerTest.class);

	{
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");
	}

	public void testExpressivityChecker1() throws Exception {

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

		Assert.assertTrue(ExpressivityChecker.isLinear(theory));
		Assert.assertTrue(ExpressivityChecker.isGuarded(theory));
		Assert.assertTrue(ExpressivityChecker.isSticky(theory));
	}

	public void testExpressivityChecker2() throws Exception {

		//
		// Theory:
		//
		// t(Y,X), p(X) -> t(X,Y).
		// t(X,Y) -> s(Y).
		//

		final IVariable x = Factory.TERM.createVariable("X");
		final IVariable y = Factory.TERM.createVariable("Y");

		final IPredicate p = Factory.BASIC.createPredicate("p", 1);
		final IPredicate t = Factory.BASIC.createPredicate("t", 2);
		final IPredicate s = Factory.BASIC.createPredicate("s", 1);

		final ILiteral px = Factory.BASIC.createLiteral(true, p, Factory.BASIC.createTuple(x));
		final ILiteral txy = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(x, y));
		final ILiteral tyx = Factory.BASIC.createLiteral(true, t, Factory.BASIC.createTuple(y, x));
		final ILiteral sy = Factory.BASIC.createLiteral(true, s, Factory.BASIC.createTuple(y));

		final IRule r1 = Factory.BASIC.createRule(ImmutableList.of(txy), ImmutableList.of(tyx, px));
		final IRule r2 = Factory.BASIC.createRule(ImmutableList.of(sy), ImmutableList.of(txy));

		final List<IRule> theory = ImmutableList.of(r1, r2);

		Assert.assertEquals(true, ExpressivityChecker.isGuarded(theory));
		Assert.assertEquals(false, ExpressivityChecker.isLinear(theory));
		Assert.assertEquals(false, ExpressivityChecker.isSticky(theory));
	}
}
