/**
 * 
 */
package JODS;
import group.models.PreferenceStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.deri.iris.Reporter;

public class JoDSReporter {

	private final Logger LOGGER = Logger.getLogger(Reporter.class);

	private static Map<URewMetric, Long> metrics;
	private static JoDSReporter _INSTANCE;
	private static String test;
	private static String query;
	private static Integer k;
	private static PreferenceStrategy strategy;
	

	public static JoDSReporter getInstance() {
		return getInstance(false);
	}

	public static JoDSReporter getInstance(final boolean clean) {
		if ((_INSTANCE == null) || clean) {
			_INSTANCE = new JoDSReporter();
			setupReporting();
		}
		return _INSTANCE;
	}

	private JoDSReporter() {
		super();
		metrics = new ConcurrentHashMap<URewMetric, Long>();
	}

	public static void setTest(final String test) {
		JoDSReporter.test = test;
	}

	public static void setQuery(final String query) {
		JoDSReporter.query = query;
	}

	public String getQuery() {
		if ((query == null) || query.isEmpty()) {
			LOGGER.warn("No value set for the query.");
		}
		return query;
	}

	public String getTest() {
		if ((test == null) || test.isEmpty()) {
			LOGGER.warn("No value set for the test.");
		}
		return test;
	}

	public void setValue(final URewMetric metric, final Long value) {
		metrics.put(metric, value);
	}

	public void addToValue(final URewMetric metric, final Long value) {
		metrics.put(metric, metrics.get(metric) + value);
	}

	public void incrementValue(final URewMetric metric) {
		addToValue(metric, (long) 1);
	}

	public Long getValue(final URewMetric metric) {
		return metrics.get(metric);
	}

	public static void setupReporting() {

	
		_INSTANCE.setValue(URewMetric.REW_TIME, (long) 0);

		_INSTANCE.setValue(URewMetric.REW_CNS_TIME, (long) 0);

		_INSTANCE.setValue(URewMetric.PREFGRAPH_CONST_TIME, (long) 0);
		_INSTANCE.setValue(URewMetric.PREFGRAPH_MERGE_TIME, (long) 0);
		// _INSTANCE.setValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_TIME, (long) 0);
		_INSTANCE.setValue(URewMetric.PREFGRAPH_TOPK_TIME, (long) 0);

		// Memory
		_INSTANCE.setValue(URewMetric.REW_MEM, (long) 0);

		_INSTANCE.setValue(URewMetric.DEPGRAPH_MEM, (long) 0);

		_INSTANCE.setValue(URewMetric.REW_CNS_MEM, (long) 0);

		_INSTANCE.setValue(URewMetric.PREFGRAPH_CONST_MEM, (long) 0);
		_INSTANCE.setValue(URewMetric.PREFGRAPH_MERGE_MEM, (long) 0);
		// _INSTANCE.setValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_MEM, (long) 0);
		_INSTANCE.setValue(URewMetric.PREFGRAPH_TOPK_MEM, (long) 0);

		// size
		_INSTANCE.setValue(URewMetric.PREFGRAPH_CONST_SIZE_E, (long) 0);
		_INSTANCE.setValue(URewMetric.PREFGRAPH_CONST_SIZE_V, (long) 0);
		_INSTANCE.setValue(URewMetric.PREFGRAPH_TRA_SIZE_E, (long) 0);
		_INSTANCE.setValue(URewMetric.PREFGRAPH_TRA_SIZE_V, (long) 0);
		_INSTANCE.setValue(URewMetric.TRANSITIVE_CLOSURE_TIME, (long) 0);
		// _INSTANCE.setValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_SIZE_E, (long)
		// 0);
		_INSTANCE.setValue(URewMetric.PREFGRAPH_TOPK_SIZE_V, (long) 0);

	}

	public String getReport() {
		final StringBuffer sb = new StringBuffer();

		sb.append("/// ---------- METRICS ----------");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- SIZE -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of queries: ");
		sb.append(getValue(URewMetric.REW_SIZE));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- TIME -----");

		// sb.append(IOUtils.LINE_SEPARATOR);
		// sb.append("/// Total: ");
		// sb.append(getValue(GRewMetric.OVERALL_TIME));
		// sb.append(" msec.");
		// sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Backward rewriting: ");
		sb.append(getValue(URewMetric.REW_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Dependency graph: ");
		sb.append(getValue(URewMetric.DEPGRAPH_TIME));
		sb.append(" msec (constant).");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Constraints rewriting time: ");
		sb.append(getValue(URewMetric.REW_CNS_TIME));
		sb.append(" msec (constant).");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- OTHER -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- MEMORY -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # Rewriting Memory: ");
		sb.append(getValue(URewMetric.REW_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # DepGraph Memory: ");
		sb.append(getValue(URewMetric.DEPGRAPH_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # NC rewriting Memory: ");
		sb.append(getValue(URewMetric.REW_CNS_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		return (sb.toString());
	}

	public String[] getSummaryMemoryMetrics() {
		// test, query,strategy, k, groupID,nbUsers,Scenario,City,nbBuss,rew mem
		// [Kb], depgraph [Kb],prefgraph [Kb] .
		final String[] line = { getTest(), getQuery(),
				getStrategy().toString(), Integer.toString(getK()),
				Long.toString(getValue(URewMetric.REW_MEM)),
				Long.toString(getValue(URewMetric.DEPGRAPH_MEM)),
				Long.toString(getValue(URewMetric.PREFGRAPH_CONST_MEM)),
				Long.toString(getValue(URewMetric.PREFGRAPH_MERGE_MEM)),
				Long.toString(getValue(URewMetric.PREFGRAPH_TOPK_MEM)),
		// Long.toString(getValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_MEM))
		};
		return line;
	}

	public String[] getSummarySizeMetrics() {
		// test, query, strategy, k,groupID,nbUsers,Scenario,City,nbBuss, size
		// [#CQs], prefGraph[#vertices],
		// prefGraph[#edges],prefGraphTRANS[#vertices],
		// prefGraphTRANS[#edges],prefGraphCYCLES[#vertices],
		// prefGraphCYCLES[#edges]
		final String[] line = { getTest(), getQuery(),
				getStrategy().toString(), 
				Integer.toString(getK()),
				Long.toString(getValue(URewMetric.REW_SIZE)),
				Long.toString(getValue(URewMetric.PREFGRAPH_CONST_SIZE_V)),
				Long.toString(getValue(URewMetric.PREFGRAPH_CONST_SIZE_E)),
				Long.toString(getValue(URewMetric.PREFGRAPH_TRA_SIZE_V)),
				Long.toString(getValue(URewMetric.PREFGRAPH_TRA_SIZE_E)),
				Long.toString(getValue(URewMetric.PREFGRAPH_TOPK_SIZE_V)),
				Long.toString(getValue(URewMetric.PREFGRAPH_TOPK_SIZE_E)),
				Long.toString(getValue(URewMetric.ANSWER_SIZE))
				
		// Long.toString(getValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_SIZE_V)),
		// Long.toString(getValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_SIZE_E))
		};
		return line;
	}

	public String[] getSummaryTimeMetrics() {
		// test, query, strategy, k,groupID,nbUsers,Scenario,City,nbBuss,
		// depgraph [msec], total rewriting [msec],
		// construct preference graph time[msec]
		final String[] line = { getTest(), getQuery(),
				getStrategy().toString(), Integer.toString(getK()),
				Long.toString(getValue(URewMetric.DEPGRAPH_TIME)),
				// Long.toString(getValue(GRewMetric.OVERALL_TIME)),
				Long.toString(getValue(URewMetric.REW_TIME)),
				Long.toString(getValue(URewMetric.PREFGRAPH_CONST_TIME)),
				Long.toString(getValue(URewMetric.TRANSITIVE_CLOSURE_TIME)),
				Long.toString(getValue(URewMetric.PREFGRAPH_MERGE_TIME)),
				// Long.toString(getValue(GRewMetric.PREFGRAPH_REMOVE_CYCLE_TIME)),
				Long.toString(getValue(URewMetric.PREFGRAPH_TOPK_TIME)), };
		return line;
	}

	public String[] getSummaryCacheMetrics() {
		// ontology, query, strategy,k,groupID,nbUsers,Scenario,City,nbBuss,

		final String[] line = { getTest(), getQuery(),
				getStrategy().toString(), Integer.toString(getK()), };
		return line;
	}

	/**
	 * @return the k
	 */
	public static int getK() {
		return k;
	}

	/**
	 * @param k
	 *            the k to set
	 */
	public static void setK(int k) {
		JoDSReporter.k = k;
	}

	/**
	 * @return the strategy
	 */
	public static PreferenceStrategy getStrategy() {
		return strategy;
	}

	/**
	 * @param strategy
	 *            the strategy to set
	 */
	public static void setStrategy(PreferenceStrategy strategy) {
		JoDSReporter.strategy = strategy;
	}

	


	


	
}
