/*
 * IRIS+/- Engine:
 * An extensible rule inference system for Datalog with extensions.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris.queryrewriting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.EvaluationException;
import org.deri.iris.Expressivity;
import org.deri.iris.Reporter;
import org.deri.iris.ReportingUtils;
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.compiler.Parser;
import org.deri.iris.queryrewriting.caching.CacheManager;
import org.deri.iris.queryrewriting.caching.CoveringCache;
import org.deri.iris.queryrewriting.caching.CoveringCache.CacheType;
import org.deri.iris.queryrewriting.caching.MGUCache;
import org.deri.iris.queryrewriting.caching.MapsToCache;
import org.deri.iris.queryrewriting.caching.RenamingCache;
import org.deri.iris.queryrewriting.configuration.DecompositionStrategy;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.RewritingLanguage;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Sets;

/**
 * @author Giorgio Orsi <giorgio.orsi@cs.ox.ac.uk> - Department of Computer Science, University of Oxford.
 * @version 1.0
 */
public class QueryRewritingTest extends TestCase {

	private final Logger LOGGER = Logger.getLogger(QueryRewritingTest.class);

	private final String _DEFAULT_OUTPUT_PATH = "/examples/rewriting_test/output/";
	private final String _DEFAULT_SUMMARY_DIR = "summary";
	private final String _DEFAULT_INPUT_PATH = "/examples/rewriting_test/input/";
	private final File _WORKING_DIR = FileUtils.getFile(System.getProperty("user.dir"));
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'-'HH:mm:ss");

	static {
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");
	}

	public static Test suite() {

		return new TestSuite(QueryRewritingTest.class, QueryRewritingTest.class.getSimpleName());
	}

	public void testFORewriting() throws Exception {

		// Configuration.
		final DecompositionStrategy decomposition = DecompositionStrategy.DECOMPOSE;
		final RewritingLanguage rewLang = RewritingLanguage.UCQ;
		final SubCheckStrategy subchkStrategy = SubCheckStrategy.INTRADEC;
		final NCCheck ncCheckStrategy = NCCheck.NONE;

		LOGGER.info("Decomposition: " + decomposition.name());
		LOGGER.info("Rewriting Language: " + rewLang.name());
		LOGGER.info("Subsumption Check Strategy: " + subchkStrategy.name());
		LOGGER.info("Negative Constraints Check Strategy " + ncCheckStrategy.name());

		// Read the test-cases file

		final File testSuiteFile = FileUtils.getFile(_WORKING_DIR,
		        FilenameUtils.separatorsToSystem(_DEFAULT_INPUT_PATH), "test-cases.txt");

		final List<String> tests = IOUtils.readLines(new FileReader(testSuiteFile));

		final String creationDate = dateFormat.format(new Date());

		// Summary reporting
		final String summaryPrefix = StringUtils.join(creationDate, "-", decomposition.name(), "-", rewLang.name(),
		        "-", subchkStrategy.name(), "-", ncCheckStrategy.name());

		final File sizeSummaryFile = FileUtils.getFile(_WORKING_DIR,
		        FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH),
		        FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
		        StringUtils.join(summaryPrefix, "-", "size-summary.csv"));
		final CSVWriter sizeSummaryWriter = new CSVWriter(new FileWriter(sizeSummaryFile), ',');

		final File timeSummaryFile = FileUtils.getFile(_WORKING_DIR,
		        FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH),
		        FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
		        StringUtils.join(summaryPrefix, "-", "time-summary.csv"));
		final CSVWriter timeSummaryWriter = new CSVWriter(new FileWriter(timeSummaryFile), ',');

		final File cacheSummaryFile = FileUtils.getFile(_WORKING_DIR,
		        FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH),
		        FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
		        StringUtils.join(summaryPrefix, "-", "cache-summary.csv"));
		final CSVWriter cacheSummaryWriter = new CSVWriter(new FileWriter(cacheSummaryFile), ',');

		final File memorySummaryFile = FileUtils.getFile(_WORKING_DIR,
		        FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH),
		        FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
		        StringUtils.join(summaryPrefix, "-", "memory-summary.csv"));
		final CSVWriter memorySummaryWriter = new CSVWriter(new FileWriter(memorySummaryFile), ',');

		sizeSummaryWriter.writeNext(ReportingUtils.getSummaryRewritingSizeReportHeader());
		timeSummaryWriter.writeNext(ReportingUtils.getSummaryRewritingTimeReportHeader());
		cacheSummaryWriter.writeNext(ReportingUtils.getSummaryCachingReportHeader());
		memorySummaryWriter.writeNext(ReportingUtils.getSummaryMemoryReportHeader());

		// Compute the rewriting for each test ontology.
		for (final String testName : tests) {

			// Read the next test case on the list
			final File testFile = FileUtils.getFile(_WORKING_DIR,
			        FilenameUtils.separatorsToSystem(_DEFAULT_INPUT_PATH), testName + ".dtg");

			// Create the Directory where to store the test results
			final File outTestDir = FileUtils.getFile(_WORKING_DIR,
			        FilenameUtils.separatorsToSystem(_DEFAULT_OUTPUT_PATH), testName);
			if (!outTestDir.exists()) {
				if (outTestDir.mkdir()) {
					LOGGER.info("Created output directory: " + testName);
				} else {
					LOGGER.fatal("Error creating output directory");
				}
			}

			LOGGER.info("Processing file: " + testName);

			// Read the content of the current program
			final FileReader fr = new FileReader(testFile);
			final StringBuilder sb = new StringBuilder();
			int ch = -1;
			while ((ch = fr.read()) >= 0) {
				sb.append((char) ch);
			}
			final String program = sb.toString();
			fr.close();

			// Parse the program
			final Parser parser = new Parser();
			parser.parse(program);

			// Get the rules
			final List<IRule> rules = parser.getRules();

			// Get the queries
			final List<IQuery> queryHeads = parser.getQueries();

			// Get the TGDs from the set of rules
			final List<IRule> tgds = RewritingUtils.getTGDs(rules, queryHeads);

			// Convert the query bodies in rules
			final List<IRule> bodies = new LinkedList<IRule>(rules);
			bodies.removeAll(tgds);

			final List<IRule> queries = RewritingUtils.getQueries(bodies, queryHeads);

			// get the constraints from the set of rules
			final Set<IRule> constraints = RewritingUtils.getConstraints(rules, queryHeads);

			final Set<Expressivity> exprs = RewritingUtils.getExpressivity(tgds);
			LOGGER.info("Expressivity: " + exprs.toString());

			if (!exprs.contains(Expressivity.LINEAR) && !exprs.contains(Expressivity.STICKY))
				throw new EvaluationException("Only Linear and Sticky TGDs are supported for rewriting.");

			// compute the dependency graph

			LOGGER.debug("Computing position dependencies.");
			// long depGraphMem = MonitoringUtils.getHeapUsage();
			long posDepTime = System.currentTimeMillis();
			Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils.computePositionDependencyGraph(tgds);
			posDepTime = System.currentTimeMillis() - posDepTime;

			// Setup caching
			CacheManager.setupCaching();

			// if linear TGDs, compute the atom coverage graph.
			LOGGER.debug("Computing atom coverage graph.");
			long atomCoverGraphTime = System.currentTimeMillis();
			if (exprs.contains(Expressivity.LINEAR)) {
				deps = DepGraphUtils.computeAtomCoverageGraph(deps);
			}
			atomCoverGraphTime = System.currentTimeMillis() - atomCoverGraphTime;
			// depGraphMem = MonitoringUtils.getHeapUsage() - depGraphMem;

			// rewriting constraints
			// long ncRewMem = MonitoringUtils.getHeapUsage();
			final ParallelRewriter cnsRewriter = new ParallelRewriter(DecompositionStrategy.MONOLITIC,
			        RewritingLanguage.UCQ, SubCheckStrategy.NONE, NCCheck.NONE);
			long ncRewTime = System.currentTimeMillis();
			final Set<IRule> rewrittenConstraints = Sets.newHashSet();
			if (!ncCheckStrategy.equals(NCCheck.NONE)) {
				for (final IRule c : constraints) {
					rewrittenConstraints.addAll(cnsRewriter.getRewriting(c, tgds, new HashSet<IRule>(), deps, exprs));
				}
			}
			ncRewTime = System.currentTimeMillis() - ncRewTime;
			// ncRewMem = ncRewMem - MonitoringUtils.getHeapUsage();
			LOGGER.debug("Finished rewriting constraints.");

			// dump the rewritten constraints:
			File outFile = FileUtils.getFile(outTestDir, testName.concat("_cns.dtg"));
			final FileWriter cnsFW = new FileWriter(outFile);
			IOUtils.writeLines(rewrittenConstraints, IOUtils.LINE_SEPARATOR, cnsFW);
			cnsFW.close();

			// Compute the Rewriting
			final ParallelRewriter rewriter = new ParallelRewriter(decomposition, rewLang, subchkStrategy,
			        ncCheckStrategy);
			for (final IRule q : queries) {

				// Setup caching
				CacheManager.setupCaching();

				final String queryPredicate = q.getHead().iterator().next().getAtom().getPredicate()
				        .getPredicateSymbol();

				// Setup reporting
				final Reporter rep = Reporter.getInstance(true);
				Reporter.setupReporting();
				Reporter.setQuery(queryPredicate);
				Reporter.setOntology(testName);
				rep.setValue(RewMetric.DEPGRAPH_TIME, posDepTime);

				LOGGER.info("Processing query: ".concat(q.toString()));
				// final long rewMem = MonitoringUtils.getHeapUsage();
				final long overallTime = System.currentTimeMillis();
				final Set<IRule> rewriting = rewriter.getRewriting(q, tgds, rewrittenConstraints, deps, exprs);
				rep.setValue(RewMetric.OVERALL_TIME, System.currentTimeMillis() - overallTime);
				// rep.setValue(RewMetric.REW_MEM, MonitoringUtils.getHeapUsage() - rewMem);
				// rep.setValue(RewMetric.DEPGRAPH_MEM, depGraphMem);
				rep.setValue(RewMetric.REW_SIZE, (long) rewriting.size());
				rep.setValue(RewMetric.JOIN_COUNT, RewritingUtils.joinCount(rewriting));
				rep.setValue(RewMetric.ATOM_COUNT, RewritingUtils.atomsCount(rewriting));
				rep.setValue(RewMetric.REW_CNS_COUNT, (long) rewrittenConstraints.size());
				rep.setValue(RewMetric.REW_CNS_TIME, ncRewTime);
				// rep.setValue(RewMetric.REW_CNS_MEM, ncRewMem);

				// Other metrics
				rep.setValue(RewMetric.OVERHEAD_TIME,
				        rep.getValue(RewMetric.OVERALL_TIME) - rep.getValue(RewMetric.REW_TIME));

				// Caching size metrics
				rep.setValue(RewMetric.MAX_COVERING_CACHE_SIZE, CoveringCache.getCache().size(CacheType.COVERING));
				rep.setValue(RewMetric.MAX_NON_COVERING_CACHE_SIZE,
				        CoveringCache.getCache().size(CacheType.NOT_COVERING));
				rep.setValue(RewMetric.MAX_MAPSTO_CACHE_SIZE, MapsToCache.size(MapsToCache.CacheType.MAPSTO));
				rep.setValue(RewMetric.MAX_NOT_MAPSTO_CACHE_SIZE, MapsToCache.size(MapsToCache.CacheType.NOT_MAPSTO));
				rep.setValue(RewMetric.MAX_FACTOR_CACHE_SIZE, (long) 0);
				rep.setValue(RewMetric.MAX_NON_FACTOR_CACHE_SIZE, (long) 0);
				rep.setValue(RewMetric.MAX_RENAMING_CACHE_SIZE, RenamingCache.size());
				rep.setValue(RewMetric.MAX_MGU_CACHE_SIZE, MGUCache.size());

				// Create a file to store the rewriting results.

				outFile = FileUtils.getFile(outTestDir, queryPredicate.concat("_rew.dtg"));
				final FileWriter rewFW = new FileWriter(outFile);

				rewFW.write("/// Query: " + q + "///\n");
				rewFW.write("/// Ontology: " + testName + "///");
				rewFW.write("/// Created on: " + creationDate + " ///\n");
				rewFW.write("/// Rules in the program: " + rules.size() + " ///\n");
				rewFW.write("/// TGDs in the program: " + tgds.size() + " ///\n");
				rewFW.write("/// Constraints in the program: " + constraints.size() + " ///\n");
				rewFW.write("/// Theory expressivity: " + exprs.toString() + " ///\n");
				rewFW.write("/// Decomposition: " + decomposition.name() + " ///\n");
				rewFW.write("/// Subsumption Check Strategy: " + subchkStrategy.name() + " ///\n");
				rewFW.write("/// Negative Constraints Check Strategy: " + ncCheckStrategy.name() + " ///\n");
				rewFW.write(IOUtils.LINE_SEPARATOR);

				LOGGER.info("Writing the output at: " + outFile.getAbsolutePath());

				// dump metrics for individual queries.
				rewFW.write(rep.getReport());

				rewFW.write(IOUtils.LINE_SEPARATOR);
				rewFW.write(IOUtils.LINE_SEPARATOR);

				rewFW.write("/// Rewritten Program ///\n");
				final Set<ILiteral> newHeads = new HashSet<ILiteral>();
				for (final IRule qr : rewriting) {
					newHeads.add(qr.getHead().iterator().next());
					rewFW.write(qr + "\n");
				}
				rewFW.write("\n");
				for (final ILiteral h : newHeads) {
					rewFW.write("?- " + h + ".\n");
				}
				rewFW.write("\n");
				rewFW.flush();
				rewFW.close();

				// dump summary metrics.
				sizeSummaryWriter.writeNext(rep.getSummarySizeMetrics());
				timeSummaryWriter.writeNext(rep.getSummaryTimeMetrics());
				cacheSummaryWriter.writeNext(rep.getSummaryCacheMetrics());
				memorySummaryWriter.writeNext(rep.getSummaryMemoryMetrics());
				sizeSummaryWriter.flush();
				timeSummaryWriter.flush();
				cacheSummaryWriter.flush();
				memorySummaryWriter.flush();
			}
		}
		sizeSummaryWriter.close();
		timeSummaryWriter.close();
		cacheSummaryWriter.close();
		memorySummaryWriter.close();
	}
}
