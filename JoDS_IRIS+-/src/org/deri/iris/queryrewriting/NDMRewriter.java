/*
 * Integrated Rule Inference System (IRIS):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2009 ICT Institute - Dipartimento di Elettronica e Informazione (DEI), 
 * Politecnico di Milano, Via Ponzio 34/5, 20133 Milan, Italy.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.queryrewriting;

import it.uniroma3.dia.ndm.OIDBroker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deri.iris.EvaluationException;
import org.deri.iris.api.basics.IAtom;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.api.queryrewriting.IQueryRewriter;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.terms.TermFactory;
import org.deri.iris.utils.TermMatchingAndSubstitution;
import org.deri.iris.utils.UniqueList;

import com.google.common.collect.Iterators;

/**
 * A class to produce a NDM (Nyaya Data Model) Rewriting from a set of datalog queries.
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 */
public class NDMRewriter implements IQueryRewriter {
	private final List<IRule> mRules;
	static int freshQVarCount;
	static int freshRVarCount;
	static int previousCount;
	private final boolean nyayaOptimization;

	public NDMRewriter(final List<IRule> rules, final boolean nyayaOpt) {
		// Store the TGDs to be used for the rewriting
		mRules = rules;
		nyayaOptimization = nyayaOpt;
	}

	public NDMRewriter(final List<IRule> rules) {
		// Store the TGDs to be used for the rewriting
		mRules = rules;
		nyayaOptimization = false;
	}

	/**
	 * Returns all the conjunctive query rewritings w.r.t a set of TGDs.
	 * @param rules the set of linear TGDs
	 * @param queries the queries to be rewritten
	 * @return the list of rewritings corresponding to the queries and the set of TGDs
	 */
	@Override
	public List<Set<IRule>> getRewritings(final List<IRule> queries) throws EvaluationException {
		freshQVarCount = 0;
		freshRVarCount = 0;
		final List<Set<IRule>> rewritings = new UniqueList<Set<IRule>>();

		// Get each query and compute its rewriting in conjunctive queries
		for (final IRule q : queries) {
			final Set<IRule> curRew = getRewriting(q);
			if (!rewritings.contains(curRew)) {
				rewritings.add(curRew);
			}
		}

		return (rewritings);
	}

	@Override
	public Set<IRule> getRewriting(final IRule query) throws EvaluationException {

		// This set will always consist of a single query
		final Set<IRule> rewriting = new LinkedHashSet<IRule>();

		// Refresh the variables
		IRule qr = RenamingUtils.canonicalRenaming(query, "V");

		boolean expanded;
		// Repeat until no more atoms are expanded
		do {
			expanded = false;

			// for each atom in the query
			for (final ILiteral l : qr.getBody()) {
				// Get the atom a
				final IAtom a = l.getAtom();

				// if not already expanded
				if (!a.getPredicate().getPredicateSymbol().startsWith("@")) {

					// For each rule in the SBox
					final Iterator<IRule> srIt = mRules.iterator();
					while (srIt.hasNext() && !expanded) {
						final IRule qs = srIt.next();
						final Map<IVariable, ITerm> gamma = new HashMap<IVariable, ITerm>();
						// Check if the rule is applicable to the atom a
						if (isApplicable(qs, qr, a, gamma)) {
							// rewrite the atom a with the body of the rule qs
							qr = rewrite(qr, a, qs.getBody(), gamma);
							expanded = true;

						}
					}
					if (!expanded)
						throw new EvaluationException("The atom " + a.getPredicate().getPredicateSymbol()
						        + " is not defined in the SBox.");
					else {
						break;
					}
				}
			}
			// Refresh the variables
			qr = RenamingUtils.canonicalRenaming(qr, "V");
		} while (expanded);

		// Clean the storage prefix
		qr = CleaningUtils.cleanPrefix(qr, "@");

		if (nyayaOptimization) {
			// Optimize the rule
			qr = optimizeRule(qr);
		}

		// Add the rule to the set of rewritings
		rewriting.add(qr);

		return (rewriting);
	}

	private boolean isApplicable(final IRule r, final IRule q, final IAtom a, final Map<IVariable, ITerm> gamma) {

		// check if the head of the rule unifies with the atom a
		if (!TermMatchingAndSubstitution.unify(a, r.getHead().iterator().next().getAtom(), gamma))
			return (false);

		return (true);
	}

	private IRule rewrite(final IRule q, final IAtom a, final Set<ILiteral> body, final Map<IVariable, ITerm> gamma) {

		// The list containing the literals for q'
		final List<ILiteral> qPrimeBodyLiterals = new UniqueList<ILiteral>();
		final List<ILiteral> qPrimeHeadLiterals = new UniqueList<ILiteral>();

		final IBasicFactory bf = BasicFactory.getInstance();

		// Rewrite the atom a in the query q with the atoms in body producing a query q'
		// For each literal in the body of q
		for (final ILiteral curLit : q.getBody()) {
			// if the current atom is not the atom to be rewritten
			if (curLit.getAtom().compareTo(a) != 0) {
				// apply the MGU and add the current atom
				qPrimeBodyLiterals.add(bf.createLiteral(RewritingUtils.applyMGU(curLit.getAtom(), gamma)));
			} else {
				// apply the MGU and add the atoms of body to q'
				final Iterator<ILiteral> it = body.iterator();
				while (it.hasNext()) {
					qPrimeBodyLiterals.add(bf.createLiteral(RewritingUtils.applyMGU(it.next().getAtom(), gamma)));
				}
			}
		}

		// Apply the MGU also to the head of the query
		for (final ILiteral curLit : q.getHead()) {
			qPrimeHeadLiterals.add(bf.createLiteral(RewritingUtils.applyMGU(curLit.getAtom(), gamma)));
		}

		return (bf.createRule(qPrimeHeadLiterals, qPrimeBodyLiterals));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.deri.iris.api.queryrewriting.IQueryRewriter#optimizeRewriting(java.util.Set) Removes redundant atoms in
	 * the queries by assuming that the dependencies implied by the Core Nyaya Data-Model on the atoms i-class,
	 * i-objectproperty and i-datatype-property are satisfied. In particular we adopt two optimizations: 1) the
	 * following unifications are performed: 1a) i-class(X1,X2,X3,X4),i-class(X5,X6,X7,X8) : unified if X2=X6 1b)
	 * i-objectproperty(X1,X2,X3,X4,X5), i-objectproperty(X6,X7,X8,X9,X10): unified if X2=X7 and X3=X8 1c)
	 * i-dataproperty(X1,X2,X3,X4,X5), i-dataproperty(X6,X7,X8,X9,X10): unified if X2=X7 and X3=X8 2) The following
	 * variables are replaced with hash-functions 2a) i-class(X1,X2,X3,X4) replaced with i-class(X1,X2,f('Nc')) where
	 * 'Nc' is the named class 2a) i-objectproperty(X1,X2,X3,X4,X5) replaced with i-objectproperty(X1,X2,X3,f(Nr),X5)
	 * where 'Nr' is the name of the role 2a) i-dataproperty(X1,X2,X3,X4,X5) replaced with
	 * i-dataproperty(X1,X2,X3,f(Np),X5) where where 'Np' is the name of the property
	 */
	private IRule optimizeRule(final IRule r) {

		IRule optRule = substituteHashReferences(r);
		optRule = removeRedundantAtoms(optRule);

		return (optRule);
	}

	/**
	 * @param optRule The rule to be optimized
	 * @return an IRule where all the class and property references in i-class, i-objectproperty and i-dataproperty
	 *         atoms have been substituted with the corresponding hashes.
	 */
	private IRule substituteHashReferences(final IRule r) {
		final IBasicFactory bf = BasicFactory.getInstance();
		final ITermFactory tf = TermFactory.getInstance();

		final List<ILiteral> body = new UniqueList<ILiteral>();

		for (final ILiteral l1 : r.getBody()) {
			final IAtom a1 = l1.getAtom();
			if (a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_CLASS")
			        || a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_OBJECTPROPERTY")
			        || a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_DATAPROPERTY")) {
				for (final ILiteral l2 : r.getBody()) {
					final IAtom a2 = l2.getAtom();
					ITuple newTuple = a2.getTuple();
					/*
					 * test if there are redundant atoms and substitute the hash of the referenced entity inside the
					 * predicate
					 */
					if (a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_CLASS")
					        && a2.getPredicate().getPredicateSymbol().equalsIgnoreCase("CLASS")
					        && a1.getTuple().get(2).equals(a2.getTuple().get(0))) {
						final ITerm refTerm = tf.createString(OIDBroker.getOID(a2.getTuple().get(2).toString()
						        .replace("'", "")));
						newTuple = bf.createTuple(a1.getTuple().get(0), a1.getTuple().get(1), refTerm, a1.getTuple()
						        .get(3));
						body.add(bf.createLiteral(l1.isPositive(), a1.getPredicate(), newTuple));
					} else if (a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_OBJECTPROPERTY")
					        && a2.getPredicate().getPredicateSymbol().equalsIgnoreCase("OBJECTPROPERTY")
					        && a1.getTuple().get(4).equals(a2.getTuple().get(0))) {
						final ITerm refTerm = tf.createString(OIDBroker.getOID(a2.getTuple().get(4).toString()
						        .replace("'", "")));
						newTuple = bf.createTuple(a1.getTuple().get(0), a1.getTuple().get(1), a1.getTuple().get(2), a1
						        .getTuple().get(3), refTerm);
						body.add(bf.createLiteral(l1.isPositive(), a1.getPredicate(), newTuple));
					} else if (a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_DATAPROPERTY")
					        && a2.getPredicate().getPredicateSymbol().equalsIgnoreCase("DATAPROPERTY")
					        && a1.getTuple().get(3).equals(a2.getTuple().get(0))) {
						final ITerm refTerm = tf.createString(OIDBroker.getOID(a2.getTuple().get(4).toString()
						        .replace("'", "")));
						newTuple = bf.createTuple(a1.getTuple().get(0), a1.getTuple().get(1), a1.getTuple().get(2),
						        refTerm, a1.getTuple().get(4));
						body.add(bf.createLiteral(l1.isPositive(), a1.getPredicate(), newTuple));
					} else {
						body.add(bf.createLiteral(l1.isPositive(), a1));
					}
				}
			}
		}
		return (bf.createRule(r.getHead(), body));
	}

	/**
	 * @param r the rule to be tested against redundant atoms.
	 * @return an IRule without redundant storage atoms. Warning: This is not a general optimization. It only removes
	 *         redundant predicates based on the Nyaya Data-model semantics.
	 */
	private IRule removeRedundantAtoms(final IRule r) {
		final IBasicFactory bf = BasicFactory.getInstance();
		Set<ILiteral> head, body;

		// Enforce foreign key between I_OBJECT/I_DATAPROPERTY atoms and the referenced I_CLASS atom
		IRule optRule = bf.createRule(r.getHead(), r.getBody());

		// Until no new unifications are made
		boolean unified;
		do {
			unified = false;

			// For each literal in body(r)
			for (int i = 0; (i < (optRule.getBody().size() - 1)) && !unified; i++) {
				final IAtom a1 = Iterators.get(optRule.getBody().iterator(), i).getAtom();
				for (int j = i + 1; (j < optRule.getBody().size()) && !unified; j++) {
					final IAtom a2 = Iterators.get(optRule.getBody().iterator(), j).getAtom();
					// test if there are redundant atoms
					if ((a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_CLASS")
					        && a2.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_CLASS") && a1.getTuple()
					        .get(3).equals(a2.getTuple().get(3)))
					        || (a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_OBJECTPROPERTY")
					                && a2.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_OBJECTPROPERTY")
					                && a1.getTuple().get(2).equals(a2.getTuple().get(2)) && a1.getTuple().get(3)
					                .equals(a2.getTuple().get(3)))
					        || (a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_DATAPROPERTY")
					                && a2.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_DATAPROPERTY")
					                && a1.getTuple().get(2).equals(a2.getTuple().get(2)) && a1.getTuple().get(4)
					                .equals(a2.getTuple().get(4)))) {
						// Unify the redundant atoms
						final Map<IVariable, ITerm> sbstMap = new HashMap<IVariable, ITerm>();
						if (TermMatchingAndSubstitution.unify(a1, a2, sbstMap)) {
							unified = true;
							body = RewritingUtils.applyMGU(optRule.getBody(), sbstMap);
							head = RewritingUtils.applyMGU(optRule.getHead(), sbstMap);
							optRule = bf.createRule(head, body);
						}
					}
				}
			}
		} while (unified);

		body = new LinkedHashSet<ILiteral>();

		// For each literal in body(r)
		for (final ILiteral l : optRule.getBody()) {
			final IAtom a1 = l.getAtom();
			if (a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_OBJECTPROPERTY")
			        || a1.getPredicate().getPredicateSymbol().equalsIgnoreCase("I_DATAPROPERTY")) {
				body.add(l);
			} else if (a1.getTuple().get(2).isGround() || a1.getTuple().get(3).isGround()
			        || optRule.getHeadVariables().contains(a1.getTuple().get(3))) {
				body.add(l);
			}
		}
		optRule = bf.createRule(optRule.getHead(), body);

		return (optRule);
	}
}