/**
 * 
 */
package org.deri.iris;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.queryrewriting.DepGraphUtils;
import org.deri.iris.queryrewriting.PositionJoin;

import com.google.common.collect.Sets;

/**
 * @author jd
 */
public class ExpressivityChecker {

	@SuppressWarnings("unused")
	private final static Logger LOGGER = Logger.getLogger(ExpressivityChecker.class);

	public static boolean isSticky(final List<IRule> tgds) {

		// initial marking
		final Set<IPosition> mPos = new LinkedHashSet<IPosition>();
		for (final IRule r : tgds) {
			final Set<IVariable> mVars = Sets.difference(r.getBodyVariables(), r.getFrontierVariables());
			for (final IVariable v : mVars) {
				mPos.addAll(r.getTermBodyPositions(v));
			}
		}

		if (mPos.isEmpty())
			return true;
		else {
			// iterative marking
			boolean newMarkings;
			do {
				newMarkings = false;
				for (final IRule r : tgds) {
					final Set<IPosition> mHeadPos = Sets.intersection(r.getHeadPositions(), mPos);
					if (mHeadPos.size() > 0) {
						// we have marked positions in the head.
						final Set<IVariable> mHeadVars = r.getHeadVariables(mHeadPos);
						final Set<IPosition> mBodyPos = r.getBodyPositions(mHeadVars);
						if (mBodyPos.size() > 0) {
							final int curSize = mPos.size();
							mPos.addAll(mBodyPos);
							if (mPos.size() > curSize) {
								newMarkings = true;
							}

						}
					}
				}
			} while (newMarkings);

			// stickiness check
			for (final IRule r : tgds) {
				final Set<PositionJoin> joins = DepGraphUtils.computePositionJoins(r);
				for (final PositionJoin j : joins) {
					if (mPos.contains(j.getLeftPosition()) && mPos.contains(j.getRightPosition()))
						return false;
				}
			}
			return true;
		}
	}

	public static boolean isGuarded(final List<IRule> tgds) {

		for (final IRule r : tgds) {
			final Set<IVariable> uVars = r.getBodyVariables();
			boolean foundGuard = false;
			for (final ILiteral l : r.getBody()) {
				if (l.getAtom().getTuple().containsAll(uVars)) {
					foundGuard = true;
					break;
				}
			}
			if (!foundGuard)
				return false;
		}
		return true;
	}

	public static boolean isLinear(final List<IRule> tgds) {

		for (final IRule r : tgds) {
			if (r.getBody().size() > 1)
				return false;
		}
		return true;
	}

}
