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
package org.deri.iris.evaluation.topdown;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;

public class RecursivePredicateTagger implements IPredicateTagger {

	private List<IRule> mRules;

	private Set<IPredicate> mMemoPredicates;

	private RecursivePredicateTagger() {
	}

	public RecursivePredicateTagger(final List<IRule> rules, final IQuery query) {
		this();
		mRules = rules;
		mMemoPredicates = tagMemoPredicates(query);
	}

	private Set<IPredicate> tagMemoPredicates(final IQuery query) {
		final Set<IPredicate> headPredicates = new HashSet<IPredicate>();
		final Set<IPredicate> memoPredicates = new HashSet<IPredicate>();

		for (final ILiteral queryLiteral : query.getLiterals()) {
			headPredicates.add(queryLiteral.getAtom().getPredicate());
		}

		for (final IPredicate hp : headPredicates) {
			final Set<IPredicate> allPredicatesForThisRule = new HashSet<IPredicate>();
			getBodyPredicates(hp, allPredicatesForThisRule, memoPredicates);
		}

		return memoPredicates;
	}

	/**
	 * Get body predicates of all rules that match the given predicate.
	 * @param headPredicate predicate
	 * @return list of predicates that depend on <code>headPredicate</code>
	 */
	private Set<IPredicate> getBodyPredicates(final IPredicate headPredicate) {
		final HashSet<IPredicate> bodyPredicates = new HashSet<IPredicate>();
		for (final IRule rule : mRules) {
			if (getHeadPredicate(rule).equals(headPredicate)) { // Matching rule
				for (final ILiteral bodyLiteral : rule.getBody()) { // Add predicates
					bodyPredicates.add(bodyLiteral.getAtom().getPredicate());
				}
			}
		}
		return bodyPredicates;
	}

	/**
	 * Get body predicates recursively, until an already expanded node is visited
	 * @param headPredicate predicate
	 * @param visitedPredicates set of already expanded predicates
	 * @param memoPredicates set that holds 'memo predicates' (predicates with circular dependencies)
	 * @return set of expanded predicates
	 */
	private Set<IPredicate> getBodyPredicates(final IPredicate headPredicate, final Set<IPredicate> visitedPredicates,
	        final Set<IPredicate> memoPredicates) {
		final Set<IPredicate> bodyPredicates = getBodyPredicates(headPredicate);

		visitedPredicates.add(headPredicate);

		for (final IPredicate p : bodyPredicates) {
			if (visitedPredicates.contains(p)) { // predicate is somewhere in
				// the tree
				memoPredicates.add(p); // add it to memo
			}
		}

		// TODO gigi: correctly implement recursive predicate tagger

		for (final IPredicate p : bodyPredicates) {
			if (!visitedPredicates.contains(p)) {
				getBodyPredicates(p, visitedPredicates, memoPredicates); // keep
				// expanding
			}
		}

		return visitedPredicates;
	}

	private IPredicate getHeadPredicate(final IRule rule) {
		return rule.getHead().iterator().next().getAtom().getPredicate();
	}

	public List<IRule> getRules() {
		return mRules;
	}

	@Override
	public Set<IPredicate> getMemoPredicates() {
		return mMemoPredicates;
	}

}