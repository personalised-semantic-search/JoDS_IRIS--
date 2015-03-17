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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
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
import org.jgraph.graph.DefaultEdge;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedMultigraph;


import au.com.bytecode.opencsv.CSVWriter;

import com.google.common.collect.Sets;

public class ExperimentAll {

	
		// TODO Auto-generated method stub

		private final Logger LOGGER = Logger.getLogger(ExperimentAll.class);
		
		private final String _DEFAULT_OUTPUT_PATH = ".";
		private final String _DEFAULT_SUMMARY_DIR = "results";
		private final String _DEFAULT_INPUT_PATH = "examples/jods_test/input/";
		private final File _WORKING_DIR = FileUtils.getFile(System
				.getProperty("user.dir"));
	

		
		public static void main(String[] args) {
			// TODO Auto-generated method stub
			//1 4 0.2 4 true true true true 
			//args[0] number of run
			//args[1] number of nodes
			//args[2] seeds density
			// args[3] k
			//args[4] if true run gen
			//args[5] if true run alg2
			//args[6] if true run alg3
			//args[7] if true run alg4
			//args[8] value for t (if run gen)
			//args[9] value for p (if run alg 3)
			
			int runNumber = Integer.parseInt(args[0]);  //id of the run we are performing
			int numberNodes = Integer.parseInt(args[1]); //number of nodes in the graph
			Double seed = Double.parseDouble(args[2]);  //seed for density
			int k = Integer.parseInt(args[3]); //topk
			boolean runGen = Boolean.parseBoolean(args[4]); //true if we are running ComPrefsGen
			boolean runRank = Boolean.parseBoolean(args[5]); //true if we are running ComPrefRank_max
			boolean runPT = Boolean.parseBoolean(args[6]); //true if we are running ComPrefPT
			boolean runSort = Boolean.parseBoolean(args[7]); //true if we are running ComPrefSort
			Double t = null;
			Double p = null;
			if(runGen){
				t = Double.parseDouble(args[8]);
			}
			if(runPT){
				p = Double.parseDouble(args[9]);
			}
					
			
			ExperimentAll myexp = new ExperimentAll();
			try{
				myexp.runExp(runNumber,numberNodes, seed, k, runGen, runRank,runPT, runSort, t,p);
			}
			catch (final Exception e) {
						e.printStackTrace();}
		}

		public void runExp(int runNumber, int numberNodes, Double seedDen, int k, 
				boolean runGen, boolean runRank, boolean runPT,boolean runSort, Double t, Double p) throws Exception {
		/**
		 * Performs one run with the following parameters
		 * int runNumber - id of this run
		 * int numberNodes - number of nodes the preference graph should have
		 * Double seedDen - density percentage of the preference graph
		 * int k - topk answers to be returned
		 * boolean runGen - true if we run algorithm ComPrefsGen
		 * boolean runRank - true if we run algorithm ComPrefsRank
		 * boolean runPT - true if we run algorithm ComPrefsPT
		 * boolean runSort - true if we run ComPrefsSort
		 * Double t - value t for algorithm ComPrefsGen
		 * Double p - value of p for algorithm ComPresPT
		 */
			
			
			
			// Configuration.
				final DecompositionStrategy decomposition = DecompositionStrategy.DECOMPOSE;
				final RewritingLanguage rewLang = RewritingLanguage.UCQ;
				final SubCheckStrategy subchkStrategy = SubCheckStrategy.INTRADEC;
				final NCCheck ncCheckStrategy = NCCheck.NONE;

				LOGGER.info("Decomposition: " + decomposition.name());
				System.out.println("Decomposition: " + decomposition.name());
				LOGGER.info("Rewriting Language: " + rewLang.name());
				System.out.println("Rewriting Language: " + rewLang.name());
				LOGGER.info("Subsumption Check Strategy: " + subchkStrategy.name());
				System.out.println("Subsumption Check Strategy: " + subchkStrategy.name());
				LOGGER.info("Negative Constraints Check Strategy "+ ncCheckStrategy.name());
						System.out.println("Negative Constraints Check Strategy "
								+ ncCheckStrategy.name());
						
				

				
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
				System.out.println("Computing position dependencies.");
				long posDepTime = System.currentTimeMillis();
				Map<Pair<IPosition, IPosition>, Set<List<IRule>>> deps = DepGraphUtils
						.computePositionDependencyGraph(tgds);
				posDepTime = System.currentTimeMillis() - posDepTime;
				CacheManager.setupCaching();

				// if linear TGDs, compute the atom coverage graph.
				LOGGER.debug("Computing atom coverage graph.");
				System.out.println("Computing atom coverage graph.");
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
				System.out.println("Finished rewriting constraints.");
				
				Map<String, Double> probModel = ProbabilisticModel
						.get(_DEFAULT_INPUT_PATH + "reviews.txt");
				// Compute the Rewriting
				final ParallelRewriter rewriter = new ParallelRewriter(decomposition,
						rewLang, subchkStrategy, ncCheckStrategy);

				// List<Integer> ks = new ArrayList<Integer>();
				// ks.add(5);

				List<PreferenceStrategy> str = new ArrayList<PreferenceStrategy>();
				str.add(PreferenceStrategy.PREFS_GEN);
				str.add(PreferenceStrategy.PREFS_PT);
				str.add(PreferenceStrategy.PREFS_RANK);
				str.add(PreferenceStrategy.PREFS_SORT);

				LOGGER.trace("Starting...");
				System.out.println("Starting...");
				
				//creating output files
				final String summaryPrefix;
				if(runGen){
					 summaryPrefix = StringUtils.join("Run"+runNumber+" - k"+k+" - Nodes"+numberNodes+" - Den "+seedDen+" - Trans");

				}
				else  					 summaryPrefix = StringUtils.join("Run"+runNumber+" - k"+k+" - Nodes"+numberNodes+" - Den "+seedDen+" - NoTrans");


				final File exp2cSummaryFile = FileUtils.getFile(_WORKING_DIR,
						FilenameUtils
								.separatorsToSystem(_DEFAULT_OUTPUT_PATH + "/"),
						FilenameUtils.separatorsToSystem(_DEFAULT_SUMMARY_DIR),
						StringUtils.join(summaryPrefix,".csv"));
				final CSVWriter expSummaryWriter = new CSVWriter(new FileWriter(
						exp2cSummaryFile), ',');
				
				expSummaryWriter.writeNext(UReportingUtils
						.getSummaryRewritingExpReportHeader());
						
				//setting parameters for the preference graph
				PrefParameters parameters = new PrefParameters(numberNodes,seedDen);
				
				IRule q = query;

				CacheManager.setupCaching();

				final String queryPredicate = q.getHead().iterator().next()
								.getAtom().getPredicate().getPredicateSymbol();

				// Setup reporting
				final JoDSReporter rep = JoDSReporter.getInstance(true);
				JoDSReporter.setupReporting();
				JoDSReporter.setQuery(queryPredicate);
				JoDSReporter.setTest("test" + runNumber);

				rep.setValue(URewMetric.DEPGRAPH_TIME, posDepTime);
				LOGGER.info("Processing query: ".concat(q.toString()));
				System.out.println("Processing query: ".concat(q.toString()));
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
				System.out.println("Creating the Graph...");
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
				if(runGen || runPT){
					System.out.println("Doing transitive closure");
					TransitiveClosure c = TransitiveClosure.INSTANCE;
					c.closeSimpleDirectedGraph(prefGraph.g);
					transitiveClosureTime = System.currentTimeMillis()
						- transitiveClosureTime;
					rep.setValue(URewMetric.TRANSITIVE_CLOSURE_TIME,
							transitiveClosureTime);
					rep.setValue(URewMetric.PREFGRAPH_TRA_SIZE_V,
							(long) prefGraph.getVertexesSize());
					rep.setValue(URewMetric.PREFGRAPH_TRA_SIZE_E,
							(long) prefGraph.getEdgesSize());
					System.out.println("Trans" + "Num edges in closed graph: "+prefGraph.getEdgesSize() + "-"
							+ "Time for closure: "+ transitiveClosureTime);
				}
				else {
					System.out.println("Not Doing transitive closure");
				
					transitiveClosureTime = 0;
					rep.setValue(URewMetric.TRANSITIVE_CLOSURE_TIME,
							transitiveClosureTime);
					rep.setValue(URewMetric.PREFGRAPH_TRA_SIZE_V,
							(long) prefGraph.getVertexesSize());
					rep.setValue(URewMetric.PREFGRAPH_TRA_SIZE_E,
							(long) prefGraph.getEdgesSize());
				}
				
				PreferencesGraph prefMerged = null;
				Map<ITuple, Integer> ranks = null;
				// Merge Graph graph
				PreferenceStrategy strategyQA;
				long mergeOperatorTime = System.currentTimeMillis();
				
				//if runGen then run ComPrefsGen
				if(runGen){

					System.out.println("calling garbagge collector?");
					System.gc();  
					System.out.println("Garbagge collector finished?");
					  
					strategyQA = PreferenceStrategy.PREFS_GEN;
					System.out.println("Combining preferences");
					mergeOperatorTime = System.currentTimeMillis();
					prefMerged = CombinationAlgorithms.combPrefsGen(
							prefGraph, probModel, t);
					mergeOperatorTime = System.currentTimeMillis()
							- mergeOperatorTime;
					
					if (prefMerged!=null){
						rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
								(long) prefMerged.getVertexesSize());
						rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
								(long) prefMerged.getEdgesSize());}
					else{rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
										(long)0);
								rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
										(long)0);}
					System.out.print("test"+1+strategyQA+"\n");
					rep.setValue(URewMetric.PREFGRAPH_MERGE_TIME,
							(long) mergeOperatorTime);
					
					JoDSReporter.setK(k);
					JoDSReporter.setStrategy(strategyQA);
					
					//compute top-k
					System.out.println("Computing top-k ");
					long topKTime = System.currentTimeMillis();
					List<ITuple> topk = null;
					topk = TopKAlgorithms.getTopK(prefMerged, k);
					topKTime = System.currentTimeMillis() - topKTime;
					rep.setValue(URewMetric.PREFGRAPH_TOPK_TIME, topKTime);
				
					int sizeAnswer = (topk != null) ? topk.size() : 0;
					rep.setValue(URewMetric.ANSWER_SIZE, (long) sizeAnswer);
					
					String[] resultsize = rep.getSummarySizeMetrics();
					String[] resulttime = rep.getSummaryTimeMetrics();
					String[] both = ArrayUtils.addAll(resultsize, resulttime);
					
					expSummaryWriter.writeNext(both);

						
				}
				
				//Run ComPrefsRank
				if(runRank){

					System.out.println("calling garbagge collector?");
					System.gc();  
					System.out.println("Garbagge collector finished?");
					  
					strategyQA = PreferenceStrategy.PREFS_RANK;
					JoDSReporter.setK(k);
					JoDSReporter.setStrategy(strategyQA);
					System.out.println("Combining preferences");
					mergeOperatorTime = System.currentTimeMillis();
						ranks = CombinationAlgorithms.combPrefsRank(
								prefGraph, probModel, Function.Max);
						mergeOperatorTime = System.currentTimeMillis()
								- mergeOperatorTime;
						if (ranks!=null){
							rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
									(long) prefGraph.getVertexesSize());
							rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
									(long) prefGraph.getEdgesSize());}
						else{rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
											(long)0);
									rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
											(long)0);}
						System.out.print("test"+1+strategyQA+"\n");
						rep.setValue(URewMetric.PREFGRAPH_MERGE_TIME,
								(long) mergeOperatorTime);
						
						System.out.println("Computing top-k ");
						long topKTime = System.currentTimeMillis();
						List<ITuple> topk = null;
						topk = TopKAlgorithms.topkPrefsRank(ranks, k);
						
						topKTime = System.currentTimeMillis() - topKTime;
						rep.setValue(URewMetric.PREFGRAPH_TOPK_TIME, topKTime);
					
						int sizeAnswer = (topk != null) ? topk.size() : 0;
						rep.setValue(URewMetric.ANSWER_SIZE, (long) sizeAnswer);
						
						String[] resultsize = rep.getSummarySizeMetrics();
						String[] resulttime = rep.getSummaryTimeMetrics();
						String[] both = ArrayUtils.addAll(resultsize, resulttime);
						
						expSummaryWriter.writeNext(both);

				}
				
				//Run ComPrefsPT
				if(runPT){
					System.out.println("calling garbagge collector?");
					System.gc();  
					System.out.println("Garbagge collector finished?");
					  
					strategyQA = PreferenceStrategy.PREFS_PT;
					JoDSReporter.setK(k);
					JoDSReporter.setStrategy(strategyQA);
					System.out.println("Combining preferences");
					mergeOperatorTime = System.currentTimeMillis();
					prefMerged = CombinationAlgorithms.combPrefsPT(
							prefGraph, probModel, p);
					mergeOperatorTime = System.currentTimeMillis()
							- mergeOperatorTime;
					
					if (prefMerged!=null){
						rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
								(long) prefMerged.getVertexesSize());
						rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
								(long) prefMerged.getEdgesSize());}
					else{rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
										(long)0);
								rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
										(long)0);}
					System.out.print("test"+1+strategyQA+"\n");
					rep.setValue(URewMetric.PREFGRAPH_MERGE_TIME,
							(long) mergeOperatorTime);
					
					System.out.println("Computing top-k ");
					long topKTime = System.currentTimeMillis();
					List<ITuple> topk = null;
					topk = TopKAlgorithms.getTopK(prefMerged, k);
					topKTime = System.currentTimeMillis() - topKTime;
					rep.setValue(URewMetric.PREFGRAPH_TOPK_TIME, topKTime);
				
					int sizeAnswer = (topk != null) ? topk.size() : 0;
					rep.setValue(URewMetric.ANSWER_SIZE, (long) sizeAnswer);
					
					String[] resultsize = rep.getSummarySizeMetrics();
					String[] resulttime = rep.getSummaryTimeMetrics();
					String[] both = ArrayUtils.addAll(resultsize, resulttime);
					
					expSummaryWriter.writeNext(both);

					
				}
				
				//run ComPrefsSort
				if(runSort){
					System.out.println("calling garbagge collector?");
					System.gc();  
					System.out.println("Garbagge collector finished?");
					
					strategyQA = PreferenceStrategy.PREFS_SORT;
					JoDSReporter.setK(k);
					JoDSReporter.setStrategy(strategyQA);
					System.out.println("Combining preferences");
					mergeOperatorTime = System
							.currentTimeMillis();
					prefMerged = CombinationAlgorithms
							.combPrefsSort(prefGraph, probModel);
					mergeOperatorTime = System
							.currentTimeMillis()
							- mergeOperatorTime;
					
					if (prefMerged!=null){
						rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
								(long) prefMerged.getVertexesSize());
						rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
								(long) prefMerged.getEdgesSize());}
					else{rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V,
										(long)0);
								rep.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_E,
										(long)0);}
					System.out.print("test"+1+strategyQA+"\n");
					rep.setValue(URewMetric.PREFGRAPH_MERGE_TIME,
							(long) mergeOperatorTime);
					
					System.out.println("Computing top-k ");
					long topKTime = System.currentTimeMillis();
					List<ITuple> topk = null;
					topk = TopKAlgorithms.getTopK(prefMerged, k);
					topKTime = System.currentTimeMillis() - topKTime;
					rep.setValue(URewMetric.PREFGRAPH_TOPK_TIME, topKTime);
				
					int sizeAnswer = (topk != null) ? topk.size() : 0;
					rep.setValue(URewMetric.ANSWER_SIZE, (long) sizeAnswer);
					
					String[] resultsize = rep.getSummarySizeMetrics();
					String[] resulttime = rep.getSummaryTimeMetrics();
					String[] both = ArrayUtils.addAll(resultsize, resulttime);
					
					expSummaryWriter.writeNext(both);
					
					
					
				}
						
					
			
				expSummaryWriter.flush();
				expSummaryWriter.close();
				

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
		private void extracted() {
			throw new EvaluationException(
					"Only Linear and Sticky TGDs are supported for rewriting.");
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


		
	}
