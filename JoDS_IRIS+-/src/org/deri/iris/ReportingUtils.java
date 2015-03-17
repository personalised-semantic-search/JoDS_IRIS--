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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */
package org.deri.iris;

/**
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 * @version 1.0
 */
public class ReportingUtils {

	public static String[] getSummaryRewritingSizeReportHeader() {

		final String[] header = { "ontology", "query", "size [#CQs]", "length [#atoms]", "width [#joins]",
		        "explored [#CQs]", "generated [#CQs]", "components [#CQs]", "ncs [#CQ]", "ncchk purge [#CQ]",
		        "subchk purge [#CQ]" };

		return (header);
	}

	public static String[] getSummaryRewritingTimeReportHeader() {

		final String[] header = { "ontology", "query", "depgraph [msec]", "total rewriting [msec]",
		        "backward rewriting [msec]", "factorisation [msec]", "atom coverage [msec]", "unfolding [msec]",
		        "renaming [msec]", "nccheck [msec]", "subcheck [msec]", "decomposition [msec]", "nc rewriting [msec]" };

		// return the header
		return header;
	}

	public static String[] getSummaryRewritingMemoryReportHeader() {

		final String[] header = { "ontology", "query", "depgraph [MB]", "rewriting [MB]" };

		// return the header
		return header;
	}

	public static String[] getSummaryCachingReportHeader() {

		final String[] header = { "ontology", "query", "factorisations [#]", "fact+ cache hits [%]",
		        "fact- cache hits [%]", "fact+ cache size [#]", "fact- cache size [#]", "cover checks [#]",
		        "cover+ cache hits [%]", "cover- cache hits [%]", "cover+ cache size [#]", "cover- cache size [#]",
		        "homomorphisms [#]", "homo+ cache hits [%]", "homo- cache hits [%]", "homo+ cache size [#]",
		        "homo- cache size [#]", "MGUs [#]", "MGU cache hits [%]", "MGU cache size [#]", "Renaming [#]",
		        "Renaming Cache hits [%]", "renaming Cache size [#]" };

		// return the header
		return header;
	}

	public static String[] getSummaryMemoryReportHeader() {
		final String[] header = { "ontology", "query", "rew mem [Kb]", "depgraph mem [Kb]" };
		return header;
	}

}
