/*
 * <Project Name>
 * <Project Description>
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
package org.deri.iris.queryrewriting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import org.deri.iris.api.basics.ILiteral;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.factory.Factory;
import org.deri.iris.utils.UniqueList;

import com.google.common.collect.Sets;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> ICT Institute - Politecnico di Milano.
 * @version 0.1b Simple symbols counter for the REQUIEM output format.
 */
public class RequiemSymbolsCounter {

	public static void main(final String[] args) {
		try {
			// Parse the REQUIEM input File
			final BufferedReader br = new BufferedReader(new FileReader(args[0]));
			while (br.ready()) {
				final String curFile = br.readLine();
				System.out.println("Processing Test: " + curFile);

				// Read the file
				final BufferedReader tr = new BufferedReader(new FileReader(curFile));

				final IBasicFactory bf = Factory.BASIC;
				final List<IRule> rules = new UniqueList<IRule>();

				String line, queryFragment, headFragment, bodyFragment;

				// Skip 9 lines (position the cursor to the first query)
				for (int i = 0; i < 9; i++) {
					tr.readLine();
				}

				/*
				 * Extract the query from this line. Each query line has the format: "N: head(X) <- body(X,Y)\n"
				 */
				int queries = 0;
				while (tr.ready()) {
					line = tr.readLine();
					if (!line.startsWith("==")) {
						queries++;
						queryFragment = line.substring(line.indexOf(':') + 1).replace(" ", "");
						System.out.println("Processing Query: " + queryFragment);
						headFragment = queryFragment.substring(0, queryFragment.lastIndexOf("<-"));
						bodyFragment = queryFragment.substring(queryFragment.lastIndexOf("<-") + 2,
						        queryFragment.length());

						// Generate IRIS Rule Format
						final List<ILiteral> head = new UniqueList<ILiteral>();
						final List<ITerm> hTerms = extractTerms(headFragment);
						final String hSym = extractPredicateSymbol(headFragment);
						final ILiteral headLit = bf.createLiteral(true, bf.createPredicate(hSym, hTerms.size()),
						        bf.createTuple(hTerms));
						head.add(headLit);

						final List<ILiteral> body = new UniqueList<ILiteral>();
						final StringTokenizer st = new StringTokenizer(bodyFragment, "^");

						while (st.hasMoreElements()) {
							final String elem = st.nextToken();
							final String bSym = extractPredicateSymbol(elem);
							final List<ITerm> bTerms = extractTerms(elem);
							body.add(bf.createLiteral(true, bf.createPredicate(bSym, bTerms.size()),
							        bf.createTuple(bTerms)));
						}

						rules.add(bf.createRule(head, body));
					}
				}

				// Count the Joins in the rules
				final long joins = RewritingUtils.joinCount(Sets.newHashSet(rules));
				final long symbols = RewritingUtils.atomsCount(Sets.newHashSet(rules));

				// Output the number of symbols
				final BufferedWriter bw = new BufferedWriter(new FileWriter(curFile.replace(".txt", "_count.txt")));
				bw.write("Rewriting Size (queries): " + queries + "\n");
				bw.write("Rewriting Size (atoms): " + symbols + "\n");
				bw.write("Rewriting Size (joins): " + joins + "\n");
				bw.flush();
				bw.close();
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

	private static List<ITerm> extractTerms(final String atom) {
		final String[] termList = atom.substring(atom.indexOf('(') + 1, atom.lastIndexOf(')')).split(",");
		final ITermFactory tf = Factory.TERM;

		final List<ITerm> out = new UniqueList<ITerm>();
		for (final String element : termList) {
			out.add(tf.createVariable(element));
		}

		return (out);

	}

	private static String extractPredicateSymbol(final String atom) {
		return (atom.substring(0, atom.indexOf('(')));
	}

}
