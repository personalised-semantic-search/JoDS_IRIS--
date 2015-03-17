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
package JODS;

public class UReportingUtils {

	public static String[] getSummaryRewritingSizeReportHeader() {

		final String[] header = { "test", "query", "strategy", "k",
				"size [#CQs]", "prefGraph[#vertices]", "prefGraph[#edges]",
				"prefGraphTransitive[#vertices]", "prefGraphTransitive[#edges]",
				"prefGraph-TOPk[#vertices]", "prefGraph-TOPk[#edges]",
				"[#answers]" };

		return (header);
	}

	public static String[] getSummaryRewritingTimeReportHeader() {

		final String[] header = { "test", "query", "strategy", "k",
				"depgraph [msec]", "total rewriting [msec]","generate pref [msec]",
				"transitive pref [msec]", "merging operator [msec]",
				"TOPk[msec]" };
		return header;
	}

	public static String[] getSummaryRewritingExpReportHeader() {

		final String[] header = { "test", "query", "strategy", "k",
				"size [#CQs]", "prefGraph[#vertices]", "prefGraph[#edges]",
				"prefGraphTransitive[#vertices]", "prefGraphTransitive[#edges]",
				"prefGraph-TOPk[#vertices]", "prefGraph-TOPk[#edges]",
				"[#answers]", "test", "query", "strategy", "k",
				"depgraph [msec]", "total rewriting [msec]","generate pref [msec]",
				"transitive pref [msec]", "merging operator [msec]",
				"TOPk[msec]" };

		return (header);
	}

	public static String[] getSummaryCachingReportHeader() {

		final String[] header = { "test", "query", "strategy", "k" };

		return header;
	}

	public static String[] getSummaryMemoryReportHeader() {
		final String[] header = { "test", "query", "strategy", "k",
				" rew mem [Kb]", "depgraph [Kb]", "prefgraph [Kb]",
				"prefgraph Merge [Kb]", "TOPk[Kb]" };
		return header;
	}
}
