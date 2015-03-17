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
package org.deri.iris.demo;
import group.models.PreferenceStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.terms.IVariable;
import org.deri.iris.compiler.Parser;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.StorageManager;

import com.google.gson.JsonArray;

/**
 * Helper for the demo applications.
 */
public class GroupProgramExecutor {
	/** The new line separator to use when formatting output. */
	public static final String NEW_LINE = System.getProperty("line.separator");

	/** Output helper. */
	public static final String BAR = "----------------------------------";

	/** Flag for how to format the output. */
	public static final boolean SHOW_VARIABLE_BINDINGS = true;

	/** Flag for how to format the output. */
	public static final boolean SHOW_QUERY_TIME = true;

	/** Flag for how to format the output. */
	public static final boolean SHOW_ROW_COUNT = true;

	/**
	 * Constructor. This is where the program is actually evaluated.
	 * 
	 * @param program
	 *            The Datalog program to evaluate.
	 * @param configuration
	 *            The configuration object.
	 */
	public GroupProgramExecutor(final String program,
			final Configuration configuration,
			final JsonArray preferences, int k, PreferenceStrategy strategy) {
		try {

			// Get the parser
			final Parser parser = new Parser();
			// Parse the program
			parser.parse(program);

			/*
			 * Ask the parser to retrieve the facts explicitly added in the
			 * program
			 */
			final Map<IPredicate, IRelation> facts = parser.getFacts();

			/*
			 * Get the rules in the program: A rule has the form: head :- body.
			 */
			final List<IRule> rules = parser.getRules();

			/*
			 * Get the queries in the program: A query has the form ?- body.
			 */
			final List<IQuery> queries = parser.getQueries();

			/*
			 * Get the directives in the program: A directive has the form
			 * #dirName().
			 */
			final Map<IPredicate, IRelation> conf = parser.getDirectives();


			if (!conf.isEmpty()) {
				StorageManager.getInstance();
				StorageManager.configure(conf);
			}

			// A string that will be used as output buffer.
			final StringBuilder output = new StringBuilder();

			/*
			 * Create the knowledge base
			 */
			float duration = -System.currentTimeMillis();
			final IKnowledgeBase knowledgeBase = KnowledgeBaseFactory
					.createKnowledgeBase(facts, rules, queries, configuration,preferences);
			duration += System.currentTimeMillis();

			if (SHOW_QUERY_TIME) {
				output.append("Init time: ").append(duration).append("ms")
						.append(NEW_LINE);
			}

			final List<IVariable> variableBindings = new ArrayList<IVariable>();
             
			for (final IQuery query : queries) {
				// Execute the query
				duration = -System.nanoTime();
				final IRelation results = knowledgeBase.execute(query,
						variableBindings, k, strategy);
				duration = ((duration + System.nanoTime()) / 1000000);

				output.append(BAR).append(NEW_LINE);
				output.append("Query:      ").append(query);
				if (SHOW_ROW_COUNT) {
					output.append(" ==>> ").append(results.size());
					if (results.size() == 1) {
						output.append(" row");
					} else {
						output.append(" rows");
					}
				}
				if (SHOW_QUERY_TIME) {
					output.append(" in ").append(duration).append("ms");
				}

				output.append(NEW_LINE);

				// if (SHOW_VARIABLE_BINDINGS) {
				// output.append("Variables:  ");
				// boolean first = true;
				// for (IVariable variable : variableBindings) {
				// if (first)
				// first = false;
				// else
				// output.append(", ");
				// output.append(variable);
				// }
				// output.append(NEW_LINE);
				// }

				formatResults(output, results);
			}

			mOutput = output.toString();
		} catch (final Exception e) {
			e.printStackTrace();
			mOutput = e.toString();
		}
	}

	/**
	 * Get the formatted program output.
	 * 
	 * @return The formatted program output.
	 */
	public String getOutput() {
		return mOutput;
	}

	/**
	 * Format the actual query results (tuples).
	 * 
	 * @param builder
	 * @param m
	 */
	private void formatResults(final StringBuilder builder, final IRelation m) {
		for (int i = 0; i < m.size(); i++) {
			builder.append(m.get(i).toString()).append(NEW_LINE);
		}
	}

	/** The output (or error) from the program execution. */
	private String mOutput;
}
