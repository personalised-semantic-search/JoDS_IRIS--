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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.StorageManager;
import org.deri.iris.terms.StringTerm;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;

/**
 * A class to produce a SQL Rewriting from a set of datalog queries.
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 */
public class SQLRewriter {

	private final Logger LOGGER = Logger.getLogger(SQLRewriter.class);

	private final Set<IRule> mRules;

	public SQLRewriter(final Set<IRule> rules) {
		// Store the TGDs to be used for the rewriting
		mRules = rules;
	}

	public String getSQLRewriting(IRule query,final String sqlConstraint, final int nbNodes, final int startedFrom) throws SQLException {
		//LOGGER.debug("Translating " + query);
		final IBasicFactory bf = Factory.BASIC;

		final Map<String, String> aliasMap = new HashMap<String, String>();
		final Set<ITerm> processedTargetTerms = new HashSet<ITerm>();
		final List<String> targetList = new ArrayList<String>();
		final List<String> fromList = new ArrayList<String>();
		final List<String> whereList = new ArrayList<String>();

		// Disambiguate same predicates in the query body
		final Set<ILiteral> body = new LinkedHashSet<ILiteral>();
		int dis = 1;
		for (final ILiteral l : query.getBody()) {
			final String p = l.getAtom().getPredicate().toString();
			String p_alias;
			if (aliasMap.containsValue(p)) {
				p_alias = Joiner.on("").join(p, "_", dis++);
			} else {
				p_alias = p;
			}
			aliasMap.put(p_alias, p);
			body.add(bf.createLiteral(l.isPositive(),
			        bf.createPredicate(p_alias, l.getAtom().getPredicate().getArity()), l.getAtom().getTuple()));
		}
		query = bf.createRule(query.getHead(), body);

		try {
			final StringBuffer out = new StringBuffer();

			// Translate the Query
			for (int i = 0; i < query.getBody().size(); i++) {
				final ILiteral l = Iterators.get(query.getBody().iterator(), i);
				final String p = l.getAtom().getPredicate().getPredicateSymbol();
				fromList.add(p);

				int pos = 0;
				for (final ITerm t : l.getAtom().getTuple()) {
					pos++;
					if (query.getHeadVariables().contains(t) && !processedTargetTerms.contains(t)) {
						// This is a head variable
						targetList.add(Joiner.on("").join(p, ".",
						        StorageManager.getFields(aliasMap.get(p)).get(pos - 1)));
						processedTargetTerms.add(t);
					}
					if (t instanceof StringTerm) {
						if (aliasMap.containsKey(p)) {
							whereList.add(Joiner.on("").join(p, ".",
							        StorageManager.getFields(aliasMap.get(p)).get(pos - 1), "=", t, ""));
						}
					}
					for (int j = i + 1; j < query.getBody().size(); j++) {
						final ILiteral lj = Iterators.get(query.getBody().iterator(), j);
						final String pj = lj.getAtom().getPredicate().toString();
						int posj = 0;
						for (final ITerm jt : lj.getAtom().getTuple()) {
							posj++;
							if (jt.equals(t)) {
								if (p.equalsIgnoreCase("I_CLASS")) {
									final String whereAtom = Joiner.on("").join(p, ".",
									        StorageManager.getFields(aliasMap.get(p)).get(pos - 1), "=", pj, ".",
									        StorageManager.getFields(aliasMap.get(pj)).get(posj - 1));
									whereList.add(whereAtom);
								} else {
									final String whereAtom = Joiner.on("").join(pj, ".",
									        StorageManager.getFields(aliasMap.get(pj)).get(posj - 1), "=", p, ".",
									        StorageManager.getFields(aliasMap.get(p)).get(pos - 1));
									whereList.add(whereAtom);
								}
							}
						}
					}
				}
			}

			// Building the target list
			if (targetList.size() == 0) {
				out.append("SELECT DISTINCT 'true'");
			} else {
				out.append("SELECT DISTINCT ");
				for (int i = 0; i < (targetList.size() - 1); i++) {
					out.append(targetList.get(i)).append(", ");
				}
				out.append(targetList.get(targetList.size() - 1));
			}

			// Building the from list
			out.append(" FROM ");
			final String vendor = StorageManager.getVendor();
			for (int i = 0; i < (fromList.size() - 1); i++) {
				if (aliasMap.get(fromList.get(i)).compareTo(fromList.get(i)) != 0) {
					if (vendor.compareTo("_ORACLE") == 0) {
						out.append(aliasMap.get(fromList.get(i))).append(" ").append(fromList.get(i)).append(", ");
					} else if (vendor.compareTo("_MYSQL") == 0) {
						out.append(aliasMap.get(fromList.get(i))).append(" AS ").append(fromList.get(i)).append(", ");
					} else if (vendor.compareTo("_POSTGRES") == 0) {
						out.append(StorageManager.getSchemaName()).append(".").append(aliasMap.get(fromList.get(i)))
						        .append(" AS ").append(fromList.get(i)).append(", ");
					} else
						throw new SQLException("Unsupported Vendor: " + vendor);
				} else {
					if (vendor.equals("_POSTGRES")) {
						out.append(StorageManager.getSchemaName()).append(".").append(fromList.get(i)).append(", ");
					} else {
						out.append(fromList.get(i)).append(", ");
					}
				}
			}
			if (aliasMap.get(fromList.get(fromList.size() - 1)).compareTo(fromList.get(fromList.size() - 1)) != 0) {
				if (vendor.equals("_ORACLE")) {
					out.append(aliasMap.get(fromList.get(fromList.size() - 1))).append(" ")
					        .append(fromList.get(fromList.size() - 1));
				} else if (vendor.compareTo("_MYSQL") == 0) {
					out.append(aliasMap.get(fromList.get(fromList.size() - 1))).append(" AS ")
					        .append(fromList.get(fromList.size() - 1));
				} else if (vendor.compareTo("_POSTGRES") == 0) {
					out.append(StorageManager.getSchemaName()).append(".")
					        .append(aliasMap.get(fromList.get(fromList.size() - 1))).append(" AS ")
					        .append(fromList.get(fromList.size() - 1));
				} else
					throw new SQLException("Unsupported Vendor: " + vendor);
			} else {
				if (vendor.equals("_POSTGRES")) {
					out.append(StorageManager.getSchemaName()).append(".").append(fromList.get(fromList.size() - 1));
					//out.append(",preferences.business");
				} else {
					out.append(fromList.get(fromList.size() - 1));
				}
			}

			// Building the where list
			if (whereList.size() > 0) {
				out.append(" WHERE ");
				for (int i = 0; i < (whereList.size() - 1); i++) {
					out.append(whereList.get(i)).append(" AND ");
				}
				out.append(whereList.get(whereList.size() - 1));
			//	out.append("and business_categories.bs_id = business.bs_id "+sqlConstraint);
				
				
			}
			out.append("  limit "+nbNodes+" offset "+startedFrom); 
			//LOGGER.debug("Q " + out.toString());
			return (out.toString());

		} catch (final SQLException e) {
			e.printStackTrace();
		}
		return (null);
	}

	public List<String> getSQLRewritings(final String sqlConstraint, final int nbNodes, final int startedFrom) throws SQLException {
		final List<String> rewritings = new ArrayList<String>();

		// Get each query and compute its rewriting in SQL
		for (final IRule q : mRules) {
			rewritings.add(getSQLRewriting(q,  sqlConstraint,   nbNodes,   startedFrom));
		}

		return (rewritings);
	}

	public String getUCQSQLRewriting(final String sqlConstraint, final int nbNodes, final int startedFrom) throws SQLException {

		final StringBuffer sb = new StringBuffer();
		final List<String> partialRewritings = getSQLRewritings( sqlConstraint,   nbNodes,   startedFrom);

		// Get each query and compute a SQL UCQ
		if (partialRewritings.size() > 0) {
			for (int i = 0; i < (partialRewritings.size() - 1); i++) {
				sb.append(partialRewritings.get(i).toString());
				sb.append(IOUtils.LINE_SEPARATOR);
				sb.append(" UNION ");
				sb.append(IOUtils.LINE_SEPARATOR);
			}
			sb.append(partialRewritings.get(partialRewritings.size() - 1));
		}
		return (sb.toString());

	}

}
