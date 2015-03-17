/**
 * 
 */
package org.deri.iris.queryrewriting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.deri.iris.Reporter;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.queryrewriting.caching.RenamingCache;
import org.deri.iris.terms.TermFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author jd
 */
public class RenamingUtils {

	public static Logger LOGGER = Logger.getLogger(RenamingUtils.class);
	public static Reporter rep = Reporter.getInstance();

	public static Set<IRule> canonicalRenaming(final String var, final Set<IRule> s) {
		final Set<IRule> out = Sets.newHashSet();
		for (final IRule r : s) {
			out.add(canonicalRenaming(r, var));
		}
		return s;
	}

	public static IRule canonicalRenaming(final IRule r, final String var) {

		// Reporting and cache check.
		rep.incrementValue(RewMetric.RENAMING_COUNT);
		if (RenamingCache.inCache(r, var)) {
			rep.incrementValue(RewMetric.RENAMING_CACHE_HITS);
			return RenamingCache.getRenamed(r, var);
		}

		final long renamingTime = System.currentTimeMillis();

		final IBasicFactory bf = BasicFactory.getInstance();

		final Map<IVariable, IVariable> map = new HashMap<IVariable, IVariable>();
		final Collection<ILiteral> freshHeadLiterals = canonicalRenaming(r.getHead(), var, map);
		final Collection<ILiteral> freshBodyLiterals = canonicalRenaming(r.getBody(), var, map);

		final IRule renamed = bf.createRule(freshHeadLiterals, freshBodyLiterals);

		rep.addToValue(RewMetric.RENAMING_TIME, System.currentTimeMillis() - renamingTime);

		// Reporting and caching.
		RenamingCache.cache(r, var, renamed);

		return (renamed);
	}

	public static Collection<ILiteral> canonicalRenaming(final Set<ILiteral> literals, final String var) {
		return canonicalRenaming(literals, var, new HashMap<IVariable, IVariable>());
	}

	public static Collection<ILiteral> canonicalRenaming(final Set<ILiteral> literals, final String var,
	        final Map<IVariable, IVariable> map) {

		final IBasicFactory bf = BasicFactory.getInstance();
		final ITermFactory tf = TermFactory.getInstance();

		final List<ILiteral> lits = Lists.newArrayList(literals);
		Collections.sort(lits);
		final List<ILiteral> renamedLiterals = Lists.newArrayList();

		for (final ILiteral l : lits) {
			final List<ITerm> freshTerms = new ArrayList<ITerm>();
			for (final ITerm t : l.getAtom().getTuple()) {
				if (t instanceof IVariable)
					if (map.containsKey(t)) {
						// ordinately substitute the variable with the corresponding fresh variable.
						freshTerms.add(map.get(t));
					} else {
						// substitute the current variable with a fresh variable
						final IVariable v = tf.createVariable(var + map.size());
						freshTerms.add(v);
						map.put((IVariable) t, v);
					}
				else {
					freshTerms.add(t);
				}
			}
			renamedLiterals
			        .add(bf.createLiteral(l.isPositive(), l.getAtom().getPredicate(), bf.createTuple(freshTerms)));
		}

		return renamedLiterals;
	}
}
