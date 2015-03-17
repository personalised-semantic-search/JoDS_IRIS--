/**
 * 
 */
package org.deri.iris;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.deri.iris.queryrewriting.RewMetric;

/**
 * @author Giorgio Orsi <giorgio.orsi@cs.ox.ac.uk> - Department of Computer Science, University of Oxford.
 * @version 1.0
 */
public class Reporter {

	private final Logger LOGGER = Logger.getLogger(Reporter.class);

	private static Map<RewMetric, Long> metrics;
	private static Reporter _INSTANCE;
	private static String ontology;
	private static String query;

	public static Reporter getInstance() {
		return getInstance(false);
	}

	public static Reporter getInstance(final boolean clean) {
		if ((_INSTANCE == null) || clean) {
			_INSTANCE = new Reporter();
			setupReporting();
		}
		return _INSTANCE;
	}

	private Reporter() {
		super();
		metrics = new ConcurrentHashMap<RewMetric, Long>();
	}

	public static void setOntology(final String ontology) {
		Reporter.ontology = ontology;
	}

	public static void setQuery(final String query) {
		Reporter.query = query;
	}

	public String getQuery() {
		if ((query == null) || query.isEmpty()) {
			LOGGER.warn("No value set for the query.");
		}
		return query;
	}

	public String getOntology() {
		if ((ontology == null) || ontology.isEmpty()) {
			LOGGER.warn("No value set for the ontology.");
		}
		return ontology;
	}

	public void setValue(final RewMetric metric, final Long value) {
		metrics.put(metric, value);
	}

	public void addToValue(final RewMetric metric, final Long value) {
		metrics.put(metric, metrics.get(metric) + value);
	}

	public void incrementValue(final RewMetric metric) {
		addToValue(metric, (long) 1);
	}

	public Long getValue(final RewMetric metric) {
		return metrics.get(metric);
	}

	public static void setupReporting() {

		// Counters
		_INSTANCE.setValue(RewMetric.RENAMING_COUNT, (long) 0);

		_INSTANCE.setValue(RewMetric.FACTOR_COUNT, (long) 0);

		_INSTANCE.setValue(RewMetric.ELIM_ATOM_COUNT, (long) 0);

		_INSTANCE.setValue(RewMetric.COVER_CHECK_COUNT, (long) 0);

		_INSTANCE.setValue(RewMetric.MAPSTO_CHECK_COUNT, (long) 0);

		_INSTANCE.setValue(RewMetric.MGU_COUNT, (long) 0);

		_INSTANCE.setValue(RewMetric.GENERATED_QUERIES, (long) 0);

		_INSTANCE.setValue(RewMetric.EXPLORED_QUERIES, (long) 0);

		_INSTANCE.setValue(RewMetric.DECOMPOSITION_SIZE, (long) 0);

		_INSTANCE.setValue(RewMetric.SUBCHECKPURGE_COUNT, (long) 0);

		_INSTANCE.setValue(RewMetric.NCPURGE_COUNT, (long) 0);

		_INSTANCE.setValue(RewMetric.REW_CNS_COUNT, (long) 0);

		// Caching
		_INSTANCE.setValue(RewMetric.RENAMING_CACHE_HITS, (long) 0);

		_INSTANCE.setValue(RewMetric.COVERING_CACHE_HITS, (long) 0);

		_INSTANCE.setValue(RewMetric.NON_COVERING_CACHE_HITS, (long) 0);

		_INSTANCE.setValue(RewMetric.FACTOR_CACHE_HITS, (long) 0);

		_INSTANCE.setValue(RewMetric.NON_FACTOR_CACHE_HITS, (long) 0);

		_INSTANCE.setValue(RewMetric.MAPSTO_CACHE_HITS, (long) 0);

		_INSTANCE.setValue(RewMetric.NOT_MAPSTO_CACHE_HITS, (long) 0);

		_INSTANCE.setValue(RewMetric.CARTESIAN_CACHE_HITS, (long) 0);

		_INSTANCE.setValue(RewMetric.MGU_CACHE_HITS, (long) 0);

		// Timing
		_INSTANCE.setValue(RewMetric.QELIM_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.RENAMING_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.AUX_CLEANING_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.SUBCHECK_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.FACTOR_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.CNS_VIOLATION_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.UNFOLD_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.REW_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.DECOMPOSITION_TIME, (long) 0);

		_INSTANCE.setValue(RewMetric.REW_CNS_TIME, (long) 0);

		// Memory
		_INSTANCE.setValue(RewMetric.REW_MEM, (long) 0);

		_INSTANCE.setValue(RewMetric.DEPGRAPH_MEM, (long) 0);

		_INSTANCE.setValue(RewMetric.REW_CNS_MEM, (long) 0);
	}

	public String getReport() {
		final StringBuffer sb = new StringBuffer();

		sb.append("/// ---------- METRICS ----------");
		sb.append(IOUtils.LINE_SEPARATOR);
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- SIZE -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Decomposition size: ");
		sb.append(getValue(RewMetric.DECOMPOSITION_SIZE));
		sb.append(" queries.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of queries: ");
		sb.append(getValue(RewMetric.REW_SIZE));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of atoms: ");
		sb.append(getValue(RewMetric.ATOM_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of joins: ");
		sb.append(getValue(RewMetric.JOIN_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of generated queries: ");
		sb.append(getValue(RewMetric.GENERATED_QUERIES));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of explored queries: ");
		sb.append(getValue(RewMetric.EXPLORED_QUERIES));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Constraints rewriting count: ");
		sb.append(getValue(RewMetric.REW_CNS_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of queries purged via subcheck: ");
		sb.append(getValue(RewMetric.SUBCHECKPURGE_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of queries purged via nccheck: ");
		sb.append(getValue(RewMetric.NCPURGE_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- TIME -----");

		sb.append(IOUtils.LINE_SEPARATOR);
		sb.append("/// Total: ");
		sb.append(getValue(RewMetric.OVERALL_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Backward rewriting: ");
		sb.append(getValue(RewMetric.REW_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Factorisation: ");
		sb.append(getValue(RewMetric.FACTOR_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Query elimination: ");
		sb.append(getValue(RewMetric.QELIM_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Negative contraints violation check: ");
		sb.append(getValue(RewMetric.CNS_VIOLATION_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Subsumption Check: ");
		sb.append(getValue(RewMetric.SUBCHECK_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Query Decomposition: ");
		sb.append(getValue(RewMetric.DECOMPOSITION_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Rewriting Unfolding: ");
		sb.append(getValue(RewMetric.UNFOLD_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Canonical Renaming: ");
		sb.append(getValue(RewMetric.RENAMING_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Dependency graph: ");
		sb.append(getValue(RewMetric.DEPGRAPH_TIME));
		sb.append(" msec (constant).");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Cleaning (aux predicates): ");
		sb.append(getValue(RewMetric.AUX_CLEANING_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Constraints rewriting time: ");
		sb.append(getValue(RewMetric.REW_CNS_TIME));
		sb.append(" msec (constant).");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Overhead: ");
		sb.append(getValue(RewMetric.OVERHEAD_TIME));
		sb.append(" msec.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- OTHER -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # Cover checks: ");
		sb.append(getValue(RewMetric.COVER_CHECK_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Covering cache hits: ");
		sb.append(getValue(RewMetric.COVERING_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Non-Covering cache hits: ");
		sb.append(getValue(RewMetric.NON_COVERING_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of eliminated atoms: ");
		sb.append(getValue(RewMetric.ELIM_ATOM_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Max covering cache size: ");
		sb.append(getValue(RewMetric.MAX_COVERING_CACHE_SIZE));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Max non covering cache size: ");
		sb.append(getValue(RewMetric.MAX_NON_COVERING_CACHE_SIZE));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # of subsumption checks: ");
		sb.append(getValue(RewMetric.MAPSTO_CHECK_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Subsumed cache hits: ");
		sb.append(getValue(RewMetric.MAPSTO_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Not Subsumed cache hits: ");
		sb.append(getValue(RewMetric.NOT_MAPSTO_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Max subsumption cache size: ");
		sb.append(getValue(RewMetric.MAX_MAPSTO_CACHE_SIZE));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Max not subsumed cache size: ");
		sb.append(getValue(RewMetric.MAX_NOT_MAPSTO_CACHE_SIZE));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Renaming count: ");
		sb.append(getValue(RewMetric.RENAMING_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Renaming cache hits: ");
		sb.append(getValue(RewMetric.RENAMING_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Cartesian cache hits: ");
		sb.append(getValue(RewMetric.CARTESIAN_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// MGU applications: ");
		sb.append(getValue(RewMetric.MGU_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// MGU cache hits: ");
		sb.append(getValue(RewMetric.MGU_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Factor count: ");
		sb.append(getValue(RewMetric.FACTOR_COUNT));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Factor cache hits: ");
		sb.append(getValue(RewMetric.FACTOR_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// Non Factor cache hits: ");
		sb.append(getValue(RewMetric.NON_FACTOR_CACHE_HITS));
		sb.append(".");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// ----- MEMORY -----");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # Rewriting Memory: ");
		sb.append(getValue(RewMetric.REW_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # DepGraph Memory: ");
		sb.append(getValue(RewMetric.DEPGRAPH_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		sb.append("/// # NC rewriting Memory: ");
		sb.append(getValue(RewMetric.REW_CNS_MEM));
		sb.append(" Kb.");
		sb.append(IOUtils.LINE_SEPARATOR);

		return (sb.toString());
	}

	public String[] getSummaryMemoryMetrics() {
		// ontology, query, rew mem [Kb], depgraph [Kb].
		final String[] line = { getOntology(), getQuery(), Long.toString(getValue(RewMetric.REW_MEM)),
		        Long.toString(getValue(RewMetric.DEPGRAPH_MEM)) };
		return line;
	}

	public String[] getSummarySizeMetrics() {
		// ontology, query, size [#CQs], length [#atoms], width [#joins], explored [#CQs], generated [#CQs], components
		// [#CQs], ncchk purge [#CQ], subchk purge [#CQ].
		final String[] line = { getOntology(), getQuery(), Long.toString(getValue(RewMetric.REW_SIZE)),
		        Long.toString(getValue(RewMetric.ATOM_COUNT)), Long.toString(getValue(RewMetric.JOIN_COUNT)),
		        Long.toString(getValue(RewMetric.EXPLORED_QUERIES)),
		        Long.toString(getValue(RewMetric.GENERATED_QUERIES)),
		        Long.toString(getValue(RewMetric.DECOMPOSITION_SIZE)),
		        Long.toString(getValue(RewMetric.REW_CNS_COUNT)), Long.toString(getValue(RewMetric.NCPURGE_COUNT)),
		        Long.toString(getValue(RewMetric.SUBCHECKPURGE_COUNT)) };
		return line;
	}

	public String[] getSummaryTimeMetrics() {
		// ontology, query, depgraph [msec], total rewriting [msec], backward rewriting [msec], factorisation [msec],
		// atom coverage [msec], unfolding [msec], renaming [msec], NCs violation [msec], subsumption check [msec],
		// decomposition time [msec], nc rewriting [msec].
		final String[] line = { getOntology(), getQuery(), Long.toString(getValue(RewMetric.DEPGRAPH_TIME)),
		        Long.toString(getValue(RewMetric.OVERALL_TIME)), Long.toString(getValue(RewMetric.REW_TIME)),
		        Long.toString(getValue(RewMetric.FACTOR_TIME)), Long.toString(getValue(RewMetric.QELIM_TIME)),
		        Long.toString(getValue(RewMetric.UNFOLD_TIME)), Long.toString(getValue(RewMetric.RENAMING_TIME)),
		        Long.toString(getValue(RewMetric.CNS_VIOLATION_TIME)),
		        Long.toString(getValue(RewMetric.SUBCHECK_TIME)),
		        Long.toString(getValue(RewMetric.DECOMPOSITION_TIME)), Long.toString(getValue(RewMetric.REW_CNS_TIME)) };
		return line;
	}

	public String[] getSummaryCacheMetrics() {
		// ontology, query, factorisations [#], fact+ cache hits [%], fact- cache hits [%], cover checks [#], cover+
		// cache hits [%], cover- cache hits [%], homomorphisms [#], homo+ cache hits [%], homo- cache hits [%], MGUs
		// [#], MGU cache hits [%], renaming [#], renaming cache hits [%].

		final int factHitsPerc;
		final int notFactHitsPerc;
		if (getValue(RewMetric.FACTOR_COUNT) > 0) {
			factHitsPerc = (int) ((getValue(RewMetric.FACTOR_CACHE_HITS) * 100) / getValue(RewMetric.FACTOR_COUNT));
			notFactHitsPerc = (int) ((getValue(RewMetric.NON_FACTOR_CACHE_HITS) * 100) / getValue(RewMetric.FACTOR_COUNT));
		} else {
			factHitsPerc = 0;
			notFactHitsPerc = 0;
		}

		final int coverHitsPerc;
		final int notCoverHitsPerc;
		if (getValue(RewMetric.COVER_CHECK_COUNT) > 0) {
			coverHitsPerc = (int) ((getValue(RewMetric.COVERING_CACHE_HITS) * 100) / getValue(RewMetric.COVER_CHECK_COUNT));
			notCoverHitsPerc = (int) ((getValue(RewMetric.NON_COVERING_CACHE_HITS) * 100) / getValue(RewMetric.COVER_CHECK_COUNT));
		} else {
			coverHitsPerc = 0;
			notCoverHitsPerc = 0;
		}

		final int mapsToHitsPerc;
		final int notMapsToHitsPerc;
		if (getValue(RewMetric.MAPSTO_CHECK_COUNT) > 0) {
			mapsToHitsPerc = (int) ((getValue(RewMetric.MAPSTO_CACHE_HITS) * 100) / getValue(RewMetric.MAPSTO_CHECK_COUNT));
			notMapsToHitsPerc = (int) ((getValue(RewMetric.NOT_MAPSTO_CACHE_HITS) * 100) / getValue(RewMetric.MAPSTO_CHECK_COUNT));
		} else {
			mapsToHitsPerc = 0;
			notMapsToHitsPerc = 0;
		}

		final int mguHitsPerc;
		if (getValue(RewMetric.MGU_COUNT) > 0) {
			mguHitsPerc = (int) ((getValue(RewMetric.MGU_CACHE_HITS) * 100) / getValue(RewMetric.MGU_COUNT));
		} else {
			mguHitsPerc = 0;
		}

		final int renHitsPerc;
		if (getValue(RewMetric.RENAMING_COUNT) > 0) {
			renHitsPerc = (int) ((getValue(RewMetric.RENAMING_CACHE_HITS) * 100) / getValue(RewMetric.RENAMING_COUNT));
		} else {
			renHitsPerc = 0;
		}

		final String[] line = { getOntology(), getQuery(), Long.toString(getValue(RewMetric.FACTOR_COUNT)),
		        Integer.toString(factHitsPerc), Integer.toString(notFactHitsPerc),
		        Long.toString(getValue(RewMetric.MAX_FACTOR_CACHE_SIZE)),
		        Long.toString(getValue(RewMetric.MAX_NON_FACTOR_CACHE_SIZE)),
		        Long.toString(getValue(RewMetric.COVER_CHECK_COUNT)), Integer.toString(coverHitsPerc),
		        Integer.toString(notCoverHitsPerc), Long.toString(getValue(RewMetric.MAX_COVERING_CACHE_SIZE)),
		        Long.toString(getValue(RewMetric.MAX_NON_COVERING_CACHE_SIZE)),
		        Long.toString(getValue(RewMetric.MAPSTO_CHECK_COUNT)), Integer.toString(mapsToHitsPerc),
		        Integer.toString(notMapsToHitsPerc), Long.toString(getValue(RewMetric.MAX_MAPSTO_CACHE_SIZE)),
		        Long.toString(getValue(RewMetric.MAX_NOT_MAPSTO_CACHE_SIZE)),
		        Long.toString(getValue(RewMetric.MGU_COUNT)), Integer.toString(mguHitsPerc),
		        Long.toString(getValue(RewMetric.MAX_MGU_CACHE_SIZE)),
		        Long.toString(getValue(RewMetric.RENAMING_COUNT)), Integer.toString(renHitsPerc),
		        Long.toString(getValue(RewMetric.MAX_RENAMING_CACHE_SIZE)) };
		return line;
	}
}
