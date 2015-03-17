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
package JODS;

import group.models.PreferenceStrategy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IPosition;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.queryrewriting.IQueryRewriter;
import org.deri.iris.compiler.Parser;
import org.deri.iris.queryrewriting.DepGraphUtils;
import org.deri.iris.queryrewriting.NDMRewriter;
import org.deri.iris.queryrewriting.ParallelRewriter;
import org.deri.iris.queryrewriting.RewritingUtils;
import org.deri.iris.queryrewriting.SQLRewriter;
import org.deri.iris.queryrewriting.caching.CacheManager;
import org.deri.iris.queryrewriting.configuration.DecompositionStrategy;
import org.deri.iris.queryrewriting.configuration.NCCheck;
import org.deri.iris.queryrewriting.configuration.RewritingLanguage;
import org.deri.iris.queryrewriting.configuration.SubCheckStrategy;
import org.deri.iris.rules.IRuleSafetyProcessor;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.RelationFactory;
import org.deri.iris.storage.StorageManager;

import JODS.PrefParameters;
import JODS.PreferencesGraph;
import JODS.UReportingUtils;
import JODS.URewMetric;
import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Sets;

/**
 * @author Giorgio Orsi <giorgio.orsi@cs.ox.ac.uk> - Department of Computer
 *         Science, University of Oxford.
 * @version 1.0
 */
public class PreferencesTestA extends TestCase {

	private final Logger LOGGER = Logger.getLogger(PreferencesTestA.class);
	private double t = 0.7;
	private final String _DEFAULT_OUTPUT_PATH = "examples/jods_test/output/";
	private final String _DEFAULT_SUMMARY_DIR = "summary";
	private final String _DEFAULT_INPUT_PATH = "examples/jods_test/input/";
	private final File _WORKING_DIR = FileUtils.getFile(System
			.getProperty("user.dir"));
	private final DateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'-'HH:mm:ss");

	static {
		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");
	}

	public static Test suite() {

		return new TestSuite(PreferencesTestA.class,
				PreferencesTestA.class.getSimpleName());
	}

	public String getStringFile(String input) throws IOException {
		// Read the content of the current program
		final FileReader fr = new FileReader(input);
		final StringBuilder sb = new StringBuilder();
		int ch = -1;
		while ((ch = fr.read()) >= 0) {
			sb.append((char) ch);
		}
		final String program = sb.toString();
		fr.close();
		return program;
	}
	public static int randInt(int min, int max) {

		// Usually this can be a field rather than a method variable
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}
	private IRelation getRelation(Set<IRule> rewriting,
			PrefParameters parameters, IQueryRewriter ndmRewriter) {
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		// rewFW.write("/// Rewritten Program ///\n");
		final Set<ILiteral> newHeads = new HashSet<ILiteral>();
		Map<IPredicate, IRelation> results = new HashMap<IPredicate, IRelation>();
		for (final IRule qr : rewriting) {
			newHeads.add(qr.getHead().iterator().next());
			final Set<IRule> sboxRewriting = new LinkedHashSet<IRule>();

			Set<IRule> rrules = ndmRewriter.getRewriting(qr);
			sboxRewriting.addAll(rrules);
			final SQLRewriter sqlRewriter = new SQLRewriter(sboxRewriting);
			try {
				long duration = -System.nanoTime();
				final List<String> ucqSQLRewriting = sqlRewriter
						.getSQLRewritings(" ", parameters.getNbNodes(),
								parameters.getStartFromRes());

				duration = ((duration + System.nanoTime()) / 1000000);
				IRelation resultAux = rf.createRelation();
				for (final String qu : ucqSQLRewriting) {
					IRelation r = StorageManager.executeQuery(qu);
					resultAux.addAll(r);
				}
				for (IPredicate predicate : qr.getBodyPredicates()) {
					results.put(predicate, resultAux);
				}

				for (int i = 0; i < resultAux.size(); i++) {
					if (result.size() == parameters.getNbNodes())
						return result;
					result.add(resultAux.get(i));
				}
			} catch (final SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public void testMerge() throws Exception {

		// Configuration.
		final DecompositionStrategy decomposition = DecompositionStrategy.DECOMPOSE;
		final RewritingLanguage rewLang = RewritingLanguage.UCQ;
		final SubCheckStrategy subchkStrategy = SubCheckStrategy.INTRADEC;
		final NCCheck ncCheckStrategy = NCCheck.NONE;

		LOGGER.info("Decomposition: " + decomposition.name());
		LOGGER.info("Rewriting Language: " + rewLang.name());
		LOGGER.info("Subsumption Check Strategy: " + subchkStrategy.name());
		LOGGER.info("Negative Constraints Check Strategy "
				+ ncCheckStrategy.name());

		final String creationDate = dateFormat.format(new Date());

		// Parse the program
		final Parser parser = new Parser();
		parser.parse(getStringFile(_DEFAULT_INPUT_PATH + "prefDB-ontology.dtg"));

		// Get the rules
		final List<IRule> rules = parser.getRules();

		// Get the queries
		final List<IQuery> queryHeads = parser.getQueries();
		final Map<IPredicate, IRelation> conf = parser.getDirectives();
		if (!conf.isEmpty()) {
			StorageManager.getInstance();
			StorageManager.configure(conf);
		}

		// Get the TGDs from the set of rules
		final List<IRule> tgds = RewritingUtils.getTGDs(rules, queryHeads);

		final List<IRule> mSBox = RewritingUtils
				.getSBoxRules(rules, queryHeads);
		final IRuleSafetyProcessor ruleProc = new StandardRuleSafetyProcessor();
		ruleProc.process(mSBox);
		final IQueryRewriter ndmRewriter = new NDMRewriter(mSBox);

		// Convert the query bodies in rules
		final List<IRule> bodies = new LinkedList<IRule>(rules);
		bodies.removeAll(tgds);

		final IRule query = RewritingUtils.getQueries(bodies, queryHeads)
				.get(0);

		// get the constraints from the set of rules
		final Set<IRule> constraints = RewritingUtils.getConstraints(rules,
				queryHeads);

		final Set<Expressivity> exprs = RewritingUtils.getExpressivity(tgds);
		LOGGER.info("Expressivity: " + exprs.toString());

		if (!exprs.contains(Expressivity.LINEAR)
				&& !exprs.contains(Expressivity.STICKY)) {
			extracted();
		}

		// compute the dependency graph
		LOGGER.debug("Computing position dependencies.");
		long posDepTime = System.currentTimeMillis();
		Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils
				.computePositionDependencyGraph(tgds);
		posDepTime = System.currentTimeMillis() - posDepTime;
		CacheManager.setupCaching();

		// if linear TGDs, compute the atom coverage graph.
		LOGGER.debug("Computing atom coverage graph.");
		long atomCoverGraphTime = System.currentTimeMillis();
		if (exprs.contains(Expressivity.LINEAR)) {
			deps = DepGraphUtils.computeAtomCoverageGraph(deps);
		}
		atomCoverGraphTime = System.currentTimeMillis() - atomCoverGraphTime;

		final ParallelRewriter cnsRewriter = new ParallelRewriter(
				DecompositionStrategy.MONOLITIC, RewritingLanguage.UCQ,
				SubCheckStrategy.NONE, NCCheck.NONE);
		long ncRewTime = System.currentTimeMillis();
		final Set<IRule> rewrittenConstraints = Sets.newHashSet();
		if (!ncCheckStrategy.equals(NCCheck.NONE)) {
			for (final IRule c : constraints) {
				rewrittenConstraints.addAll(cnsRewriter.getRewriting(c, tgds,
						new HashSet<IRule>(), deps, exprs));
			}
		}
		ncRewTime = System.currentTimeMillis() - ncRewTime;

		LOGGER.debug("Finished rewriting constraints.");

		Map<String, Double> probModel = ProbabilisticModel
				.get(_DEFAULT_INPUT_PATH + "reviews.txt");
		// Compute the Rewriting
		final ParallelRewriter rewriter = new ParallelRewriter(decomposition,
				rewLang, subchkStrategy, ncCheckStrategy);

		// List<Integer> ks = new ArrayList<Integer>();
		// ks.add(5);

		List<PreferenceStrategy> str = new ArrayList<PreferenceStrategy>();
		//str.add(PreferenceStrategy.PREFS_GEN);
		str.add(PreferenceStrategy.PREFS_PT);
		str.add(PreferenceStrategy.PREFS_RANK);
		str.add(PreferenceStrategy.PREFS_SORT);

		LOGGER.trace("start the things.");
		final String summaryPrefix = StringUtils.join(creationDate, "-");

		final File sizeSummaryFile = FileUtils.getFile(_WORKING_DIR,
				FilenameUtils
						.separatorsToSystem(_DEFAULT_OUTPUT_PATH + "/"),
				FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
				StringUtils.join(summaryPrefix, "-", "size-summary.csv"));
		final CSVWriter sizeSummaryWriter = new CSVWriter(new FileWriter(
				sizeSummaryFile), ',');

		final File timeSummaryFile = FileUtils.getFile(_WORKING_DIR,
				FilenameUtils
						.separatorsToSystem(_DEFAULT_OUTPUT_PATH + "/"),
				FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
				StringUtils.join(summaryPrefix, "-", "time-summary.csv"));
		final CSVWriter timeSummaryWriter = new CSVWriter(new FileWriter(
				timeSummaryFile), ',');
		sizeSummaryWriter.writeNext(UReportingUtils
				.getSummaryRewritingSizeReportHeader());
		timeSummaryWriter.writeNext(UReportingUtils
				.getSummaryRewritingTimeReportHeader());

		
		// for (int nbNodes = 500; nbNodes < 1000; nbNodes += 500) {
		// for (int nbNodes = 10; nbNodes < 20; nbNodes += 10) {
		for (int nbNodes = 1000; nbNodes < 13000; nbNodes += 1000) {

			double sparisity = 0.15;
            //double sparsity = 0.15 and  experirement no  gen and no transitve closure, expeirment 1000> 13.0000
			// for (Integer k : ks) {
			for (int con = 0; con < 10; con++) {

				PrefParameters parameters = new PrefParameters(nbNodes,
						sparisity);
				IRule q = query;

				CacheManager.setupCaching();

				final String queryPredicate = q.getHead().iterator().next()
						.getAtom().getPredicate().getPredicateSymbol();

				// Setup reporting
				final JoDSReporter rep = JoDSReporter.getInstance(true);
				JoDSReporter.setupReporting();
				JoDSReporter.setQuery(queryPredicate);
				JoDSReporter.setTest("test" + con);

				rep.setValue(URewMetric.DEPGRAPH_TIME, posDepTime);
				LOGGER.info("Processing query: ".concat(q.toString()));
				final long overallTime = System.currentTimeMillis();
				final Set<IRule> rewriting = rewriter.getRewriting(q, tgds,
						rewrittenConstraints, deps, exprs);
				rep.setValue(URewMetric.REW_TIME, System.currentTimeMillis()
						- overallTime);
				rep.setValue(URewMetric.REW_SIZE, (long) rewriting.size());

				rep.setValue(URewMetric.REW_CNS_TIME, ncRewTime);

				IRelation result = getRelation(rewriting, parameters,
						ndmRewriter);
				// CONSTRUCT graph
				long constPrefGraphTime = System.currentTimeMillis();
				final PreferencesGraph prefGraph = PreferenceGenerator
						.generatePreferenceGraph(parameters.getSparsity(),
								result);
				System.out.println("Gen" + prefGraph.getEdgesSize());
				rep.setValue(URewMetric.PREFGRAPH_CONST_SIZE_V,
						(long) prefGraph.getVertexesSize());
				rep.setValue(URewMetric.PREFGRAPH_CONST_SIZE_E,
						(long) prefGraph.getEdgesSize());
				constPrefGraphTime = System.currentTimeMillis()
						- constPrefGraphTime;
				rep.setValue(URewMetric.PREFGRAPH_CONST_TIME,
						constPrefGraphTime);

				// TRANSITIVE graph
				long transitiveClosureTime = System.currentTimeMillis();
				//TransitiveClosure c = TransitiveClosure.INSTANCE;
				//c.closeSimpleDirectedGraph(prefGraph.g);
				transitiveClosureTime = System.currentTimeMillis()
						- transitiveClosureTime;
				rep.setValue(URewMetric.TRANSITIVE_CLOSURE_TIME,
						transitiveClosureTime);
				rep.setValue(URewMetric.PREFGRAPH_TRA_SIZE_V,
						(long) prefGraph.getVertexesSize());
				rep.setValue(URewMetric.PREFGRAPH_TRA_SIZE_E,
						(long) prefGraph.getEdgesSize());
				System.out.println("Trans" + prefGraph.getEdgesSize() + "-"
						+ transitiveClosureTime);

				PreferencesGraph prefMerged = null;
				Map<ITuple, Integer> ranks = null;
				// Merge Graph graph
				for (PreferenceStrategy strategyQA : str) {
					JoDSReporter.setStrategy(strategyQA);
					long mergeOperatorTime = System.currentTimeMillis();
					if (strategyQA == PreferenceStrategy.PREFS_GEN) {
						double t = 0.3;//randInt(509, 761)/100.0;
						mergeOperatorTime = System.currentTimeMillis();
						prefMerged = CombinationAlgorithms.combPrefsGen(
								prefGraph, probModel, t);
						mergeOperatorTime = System.currentTimeMillis()
								- mergeOperatorTime;
					} else {
						if (strategyQA == PreferenceStrategy.PREFS_PT) {
							mergeOperatorTime = System.currentTimeMillis();
							double p = 0.5;//randInt(383, 887)/100.0;
							prefMerged = CombinationAlgorithms.combPrefsPT(
									prefGraph, probModel, p);
							mergeOperatorTime = System.currentTimeMillis()
									- mergeOperatorTime;
						} else {
							if (strategyQA == PreferenceStrategy.PREFS_RANK) {
								mergeOperatorTime = System.currentTimeMillis();
								ranks = CombinationAlgorithms.combPrefsRank(
										prefGraph, probModel, Function.Max);
								mergeOperatorTime = System.currentTimeMillis()
										- mergeOperatorTime;
							} else {
								if (strategyQA == PreferenceStrategy.PREFS_SORT) {
									mergeOperatorTime = System
											.currentTimeMillis();
									prefMerged = CombinationAlgorithms
											.combPrefsSort(prefGraph, probModel);
									mergeOperatorTime = System
											.currentTimeMillis()
											- mergeOperatorTime;
									
								}
							}
						}
					}
					if (prefMerged!=null){
					rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
							(long) prefMerged.getVertexesSize());
					rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
							(long) prefMerged.getEdgesSize());}else
							{rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
									(long)0);
							rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
									(long)0);}
					System.out.print("test"+con+strategyQA+"\n");
					for (int k = 5; k < 10; k = k + 5) {
						JoDSReporter.setK(k);
						rep.setValue(URewMetric.PREFGRAPH_MERGE_TIME,
								(long) mergeOperatorTime);
						long topKTime = System.currentTimeMillis();
						List<ITuple> topk = null;
						if (strategyQA == PreferenceStrategy.PREFS_RANK) {

							topk = TopKAlgorithms.topkPrefsRank(ranks, k);
						} else {
							topk = TopKAlgorithms.getTopK(prefMerged, k);
						}
						topKTime = System.currentTimeMillis() - topKTime;
						rep.setValue(URewMetric.PREFGRAPH_TOPK_TIME, topKTime);
					
						int sizeAnswer = (topk != null) ? topk.size() : 0;
						rep.setValue(URewMetric.ANSWER_SIZE, (long) sizeAnswer);
						sizeSummaryWriter
								.writeNext(rep.getSummarySizeMetrics());
						timeSummaryWriter
								.writeNext(rep.getSummaryTimeMetrics());
						sizeSummaryWriter.flush();
						timeSummaryWriter.flush();
					}
				}
			}

		}

		sizeSummaryWriter.close();
		timeSummaryWriter.close();

	}

	private void extracted() {
		throw new EvaluationException(
				"Only Linear and Sticky TGDs are supported for rewriting.");
	}
}
