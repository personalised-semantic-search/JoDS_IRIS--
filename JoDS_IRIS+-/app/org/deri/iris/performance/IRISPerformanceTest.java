/*
 * Integrated Rule Inference System (IRIS+-):
 * An extensible rule inference system for datalog with extensions.
 * 
 * Copyright (C) 2010 ICT Institute - Politecnico di Milano, Via Ponzio 34/5, 20133 Milan, Italy.
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
package org.deri.iris.performance;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.deri.iris.EvaluationException;
import org.deri.iris.Expressivity;
import org.deri.iris.RuleUnsafeException;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.queryrewriting.IQueryRewriter;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.compiler.Parser;
import org.deri.iris.compiler.ParserException;
import org.deri.iris.queryrewriting.DepGraphUtils;
import org.deri.iris.queryrewriting.NDMRewriter;
import org.deri.iris.queryrewriting.ParallelRewriter;
import org.deri.iris.queryrewriting.RewritingUtils;
import org.deri.iris.queryrewriting.SQLRewriter;
import org.deri.iris.queryrewriting.configuration.DecompositionStrategy;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.RewritingLanguage;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.rules.safety.LinearReducibleRuleSafetyProcessor;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.storage.StorageManager;
import org.deri.iris.utils.UniqueList;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> ICT Institute - Politecnico di Milano.
 * @version 0.1b
 */
public class IRISPerformanceTest {

	private static Logger LOGGER;

	/**
	 * Executes a set of datalog queries using the given configuration
	 * @param queries The set of Datalog queries
	 * @param config The configuration for the test suite
	 * @return a list of IRISTestCase objects with the result of the test campaign
	 */
	public List<IRISTestCase> executeTests(final List<String> queries, final TestConfiguration config) {

		// Get the logger
		LOGGER = Logger.getLogger(IRISPerformanceTest.class.getName());

		// Construct a valid IRIS+- program using the queries and the configuration file
		String program = "";

		// add the query and its IRIS execution command to the program
		program += "/// Query ///\n";
		for (final String s : queries) {
			program += s + "\n";
			program += "?-" + s.substring(0, s.indexOf(":-")) + ".\n";
		}
		program += "\n";

		// If reasoning is enabled, add the TBOX to the program
		program += "/// TBox ///\n";
		if (config.getReasoning()) {
			String tboxPath = config.getTestHomePath() + "/" + config.getDataset() + "/tbox";
			if (config.getExpressiveness().compareTo("RDFS") == 0) {
				tboxPath += "/rdfs";
			}
			if (config.getExpressiveness().compareTo("OWL-QL") == 0) {
				tboxPath += "/owlql";
			}
			final String tbox = loadFile(tboxPath + "/" + config.getDataset() + ".dtg");
			program += tbox + "\n";
		} else {
			program += "/// EMPTY ///\n";
		}

		// Add the SBox
		program += "/// SBox ///\n";
		String sboxPath = config.getTestHomePath() + "/" + config.getDataset() + "/sbox";
		if (config.getExpressiveness().compareTo("RDFS") == 0) {
			sboxPath += "/rdfs";
		}
		if (config.getExpressiveness().compareTo("OWL-QL") == 0) {
			sboxPath += "/owlql";
		}
		final String sbox = loadFile(sboxPath + "/" + config.getDataset() + ".dtg");
		program += sbox + "\n\n";

		LOGGER.debug(program);

		// Get the parser
		final Parser parser = new Parser();

		// Parse the program
		try {
			parser.parse(program);
		} catch (final ParserException e) {
			e.printStackTrace();
		}

		// Get the TGDs from the set of rules
		final List<IRule> tgds = RewritingUtils.getTGDs(parser.getRules(), parser.getQueries());

		// Get the query bodies
		final List<IRule> bodies = new ArrayList<IRule>(parser.getRules());
		final List<IRule> datalogQueries = RewritingUtils.getQueries(bodies, parser.getQueries());

		// Get the constraints from the set of rules
		final Set<IRule> constraints = RewritingUtils.getConstraints(parser.getRules(), parser.getQueries());

		// Get the SBox rules from the set of rules
		final List<IRule> storageRules = RewritingUtils.getSBoxRules(parser.getRules(), parser.getQueries());

		// Check that the TBox is FO-reducible
		IRuleSafetyProcessor ruleProc = new LinearReducibleRuleSafetyProcessor();
		try {
			ruleProc.process(tgds);
		} catch (final RuleUnsafeException e) {
			e.printStackTrace();
		}

		// Check that the SBox rules are Safe Datalog
		ruleProc = new StandardRuleSafetyProcessor();
		try {
			ruleProc.process(storageRules);
		} catch (final RuleUnsafeException e) {
			e.printStackTrace();
		}

		// Connect to the storage
		StorageManager.getInstance();
		try {
			StorageManager.connect(config.getDBVendor(), config.getDBProtocol(), config.getDBHost(),
			        config.getDBPort(), config.getDBName(), config.getSchemaName(), config.getDBUsername(),
			        config.getDBPassword());
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		// Evaluate the queries
		final List<IRISTestCase> output = new LinkedList<IRISTestCase>();
		for (final IQuery q : parser.getQueries()) {
			// Generate a new test-case
			final IRISTestCase currentTest = new IRISTestCase();
			int nTask = -10;

			// Get the Factories
			final IRelationFactory rf = new RelationFactory();

			// Get the Rewriter Engine
			final ParallelRewriter rewriter = new ParallelRewriter(DecompositionStrategy.DECOMPOSE,
			        RewritingLanguage.UCQ, SubCheckStrategy.TAIL, NCCheck.TAIL);

			// Get and log the rule corresponding to the query
			final IRule ruleQuery = getRuleQuery(q, datalogQueries);
			currentTest.setQuery(ruleQuery);

			final Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils
			        .computePositionDependencyGraph(tgds);

			final Set<Expressivity> exprs = RewritingUtils.getExpressivity(tgds);

			// Compute and log the FO-Rewriting
			LOGGER.info("Computing TBox Rewriting");
			float duration = -System.nanoTime();
			final Set<IRule> rewriting = rewriter.getRewriting(ruleQuery, tgds, constraints, deps, exprs);
			duration = ((duration + System.nanoTime()) / 1000000);
			currentTest.getTasks().add(new Task(nTask++, "TBox Rewriting", duration, 0, 0, "ms", rewriting.toString()));
			LOGGER.info("done.");
			int count = 0;
			for (final IRule r : rewriting) {
				LOGGER.debug("(Qr" + ++count + ")" + r);
			}

			// Produce the rewriting according to the Nyaya Data Model
			final IQueryRewriter ndmRewriter = new NDMRewriter(storageRules);

			// Create a buffer for the output
			final IRelation outRelation = rf.createRelation();

			// Get the SBox rewriting
			try {
				LOGGER.info("Computing SBox Rewriting");
				final Set<IRule> sboxRewriting = new LinkedHashSet<IRule>();
				duration = -System.nanoTime();
				for (final IRule pr : rewriting) {
					sboxRewriting.addAll(ndmRewriter.getRewriting(pr));
				}
				duration = ((duration + System.nanoTime()) / 1000000);
				currentTest.getTasks().add(
				        new Task(nTask++, "SBox Rewriting", duration, 0, 0, "ms", sboxRewriting.toString()));
				LOGGER.info("done.");
				count = 0;
				for (final IRule n : sboxRewriting) {
					LOGGER.debug("(Qn" + ++count + ")" + n);
				}

				// Produce the SQL rewriting for each query in the program
				final SQLRewriter sqlRewriter = new SQLRewriter(sboxRewriting);

				// Get the SQL rewriting as Union of Conjunctive Queries (UCQ)
				LOGGER.info("Computing SQL Rewriting");
				duration = -System.nanoTime();
				final List<String> ucqSQLRewriting = new LinkedList<String>();
				ucqSQLRewriting.add(sqlRewriter.getUCQSQLRewriting("",10000,0));
				duration = ((duration + System.nanoTime()) / 1000000);
				currentTest.getTasks().add(
				        new Task(nTask++, "SQL Rewriting", duration, 0, 0, "ms", ucqSQLRewriting.toString()));
				LOGGER.info("done.");
				count = 0;
				for (final String s : ucqSQLRewriting) {
					LOGGER.debug("(Qs" + ++count + ") " + s);
				}

				// Execute the UCQ
				LOGGER.info("Executing SQL");

				// float ansConstructOverall = 0;

				// The synchronized structure to store the output tuples
				final Set<ITuple> result = Collections.synchronizedSet(new HashSet<ITuple>());

				/*
				 * Prepare a set of runnable objects representing each partial rewriting to be executed in parallel
				 */
				final List<RunnableQuery> rql = new LinkedList<RunnableQuery>();
				for (final String cq : ucqSQLRewriting) {
					// Construct a Runnable Query
					rql.add(new RunnableQuery(cq, result, currentTest.getTasks()));
				}

				// Get an executor that allows a number of parallel threads equals to the number of available processors
				// ExecutorService queryExecutor =
				// Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*5);
				final ExecutorService queryExecutor = Executors.newSingleThreadScheduledExecutor();

				// Execute all the partial rewritings in parallel
				float ucqExecOverall = -System.nanoTime();
				for (final RunnableQuery rq : rql) {
					queryExecutor.execute(rq);
				}
				queryExecutor.shutdown();
				if (queryExecutor.awaitTermination(1, TimeUnit.DAYS)) {
					LOGGER.info("done.");
				} else
					throw new InterruptedException("Timeout Occured");
				ucqExecOverall = ((ucqExecOverall + System.nanoTime()) / 1000000);
				StorageManager.disconnect();

				// inizio aggiunta
				float minTime = System.nanoTime();
				float maxTime = 0;
				float avgTime = 0;
				int n = 0;
				for (final Task t : currentTest.getTasks()) {
					if (t.getName().contains("Execution")) {
						avgTime += (t.getFinalTime() - t.getInitTime()) / 1000000;
						n++;
						if (t.getFinalTime() > maxTime) {
							maxTime = t.getFinalTime();
						}
						if (t.getInitTime() < minTime) {
							minTime = t.getInitTime();
						}
					}
				}
				ucqExecOverall = (maxTime - minTime) / 1000000;
				// fine aggiunta

				currentTest.getTasks().add(new Task(nTask++, "UCQ Overall Execution Time", ucqExecOverall, 0, 0, "ms"));

				// inizio aggiunta
				avgTime = (avgTime / n);
				System.out.println(n);
				currentTest.getTasks().add(new Task(nTask++, "UCQ Average Execution Time", avgTime, 0, 0, "ms"));
				Collections.sort(currentTest.getTasks());
				// fine aggiunta

				for (final ITuple t : result) {
					outRelation.add(t);
				}

			} catch (final SQLException e) {
				e.printStackTrace();
			} catch (final EvaluationException e) {
				e.printStackTrace();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			currentTest.setAnswer(outRelation);
			output.add(currentTest);
		}
		return (output);
	}

	/**
	 * Loads the content of a file as a string.
	 * @param filename the file name.
	 * @return a string representing the file content.
	 */
	private String loadFile(final String filename) {
		FileReader r = null;
		StringBuilder builder = null;
		try {
			r = new FileReader(filename);
			builder = new StringBuilder();

			int ch = -1;
			while ((ch = r.read()) >= 0) {
				builder.append((char) ch);
			}
			r.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return builder.toString();
	}

	/**
	 * @param query the head of the query to be retrieved.
	 * @return the definition of the query whose head is given as input.
	 */
	private IRule getRuleQuery(final IQuery query, final List<IRule> rules) {
		final IBasicFactory bf = BasicFactory.getInstance();

		for (final IRule r : rules) {
			if (r.getHead().equals(query.getLiterals()))
				return (r);
		}

		// Return a Boolean Conjunctive Query (BCQ)
		return (bf.createRule(new UniqueList<ILiteral>(), query.getLiterals()));
	}

}
