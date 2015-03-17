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

import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;
import org.deri.iris.Configuration;
import org.deri.iris.KnowledgeBaseFactory;
import org.deri.iris.evaluation.forewriting.FORewritingEvaluationStrategyFactory;
import org.deri.iris.evaluation.forewriting.GroupSQLRewritingEvaluationStrategyFactory;
import org.deri.iris.evaluation.forewriting.SQLRewritingEvaluationStrategyFactory;
import org.deri.iris.evaluation.stratifiedbottomup.StratifiedBottomUpEvaluationStrategyFactory;
import org.deri.iris.evaluation.stratifiedbottomup.guardednaive.GuardedNaiveEvaluatorFactory;
import org.deri.iris.evaluation.stratifiedbottomup.naive.NaiveEvaluatorFactory;
import org.deri.iris.evaluation.stratifiedbottomup.seminaive.SemiNaiveEvaluatorFactory;
import org.deri.iris.evaluation.wellfounded.WellFoundedEvaluationStrategyFactory;
import org.deri.iris.optimisations.magicsets.MagicSets;
import org.deri.iris.optimisations.rulefilter.RuleFilter;
import org.deri.iris.queryrewriting.caching.CacheManager;
import org.deri.iris.rules.safety.AugmentingRuleSafetyProcessor;
import org.deri.iris.rules.safety.GuardedRuleSafetyProcessor;
import org.deri.iris.rules.safety.LinearReducibleRuleSafetyProcessor;
import org.deri.iris.rules.safety.StandardRuleSafetyProcessor;
import org.deri.iris.rules.safety.WeaklyGuardedRuleSafetyProcessor;

/**
 * A command line demonstrator for IRIS.
 */
public class Demo {
	public static final String WELL_FOUNDED = "well-founded";

	public static final String NAIVE = "naive";

	public static final String SEMI_NAIVE = "semi-naive";

	public static final String FO_REWRITING = "fo-rewriting";

	public static final String SQL_REWRITING = "sql-rewriting";
	
	public static final String GROUP_SQL_REWRITING = "group-sql-rewriting";

	public static final String GUARDED_NAIVE = "guarded-naive";

	public static final String SAFE_RULES = "safe-rules";

	public static final String UNSAFE_RULES = "unsafe-rules";

	public static final String LINEAR_RULES = "linear-rules";

	public static final String GUARDED_RULES = "guarded-rules";

	public static final String WEAKLY_GUARDED_RULES = "weakly-guarded-rules";

	public static final String MAGIC_SETS = "magic-sets";

	public static final String TIMEOUT = "timeout";

	public static final String PROGRAM = "program";

	public static final String PROGRAM_FILE = "program-file";

	private static void usage() {
		final String space = "    ";

		System.out.println();
		System.out.println("Usage: java org.deri.iris.Demo <ARGUMENTS>");
		System.out.println();
		System.out.println("where <ARGUMENTS> is made up of:");
		System.out.println(space + PROGRAM + "=<datalog program>");
		System.out.println(space + PROGRAM_FILE + "=<filename containing datalog program>");
		System.out.println(space + TIMEOUT + "=<timeout in miliseconds> (default is to run forever)");
		System.out.println(space + WELL_FOUNDED + " (to use the well-founded evaluation strategy)");
		System.out.println(space + NAIVE + " (to use naive rule evaluation)");
		System.out.println(space + FO_REWRITING + " (to use first-order rewriting evaluation strategy)");
		System.out.println(space + SQL_REWRITING + " (to use first-order rewriting into SQL");
		System.out.println(space + GUARDED_NAIVE + " (to use guarded naive rule evaluation)");
		System.out.println(space + SEMI_NAIVE + "* (to use semi-naive rule evaluation)");
		System.out.println(space + SAFE_RULES + "* (to allow only safe rules)");
		System.out.println(space + UNSAFE_RULES + " (to process unsafe rules)");
		System.out.println(space + LINEAR_RULES + " (to process linear datalog+- rules)");
		System.out.println(space + GUARDED_RULES + " (to process guarded datalog+- rules)");
		System.out.println(space + WEAKLY_GUARDED_RULES + " (to process weakly-guarded datalog+- rules)");
		System.out.println(space + MAGIC_SETS + " (to use magic sets and rule-filtering optimisations)");
		System.out.println("(*=default)");

		System.exit(1);
	}

	private static boolean startsWith(final String argument, final String token) {
		if (argument.length() < token.length())
			return false;

		final String start = argument.substring(0, token.length());

		return start.equalsIgnoreCase(token);
	}

	private static String getParameter(final String argument) {
		final int equals = argument.indexOf('=');

		if (equals >= 0)
			return argument.substring(equals + 1);

		return null;
	}

	private static final String loadFile(final String filename) throws IOException {
		final FileReader r = new FileReader(filename);

		final StringBuilder builder = new StringBuilder();

		int ch = -1;
		while ((ch = r.read()) >= 0) {
			builder.append((char) ch);
		}
		r.close();
		return builder.toString();
	}

	/**
	 * Entry point.
	 * @param args program evaluation_method
	 * @throws Exception
	 */
	public static void main(final String[] args) {
		String program = null;

		final Configuration configuration = KnowledgeBaseFactory.getDefaultConfiguration();

		// Setup caching
		CacheManager.setupCaching();

		// Load the logging configuration
		PropertyConfigurator.configure("config/logging.properties");

		for (final String argument : args) {
			if (startsWith(argument, PROGRAM_FILE)) {
				final String filename = getParameter(argument);
				try {
					program = loadFile(filename);
				} catch (final Exception e) {
					System.out.println("Unable to load input file '" + filename + "': " + e.getMessage());
					System.exit(2);
				}
			} else if (startsWith(argument, PROGRAM)) {
				program = getParameter(argument);
			} else if (startsWith(argument, TIMEOUT)) {
				configuration.evaluationTimeoutMilliseconds = Integer.parseInt(getParameter(argument));
			} else if (startsWith(argument, WELL_FOUNDED)) {
				configuration.evaluationStrategyFactory = new WellFoundedEvaluationStrategyFactory();
			} else if (startsWith(argument, NAIVE)) {
				configuration.evaluationStrategyFactory = new StratifiedBottomUpEvaluationStrategyFactory(
				        new NaiveEvaluatorFactory());
			} else if (startsWith(argument, SEMI_NAIVE)) {
				configuration.evaluationStrategyFactory = new StratifiedBottomUpEvaluationStrategyFactory(
				        new SemiNaiveEvaluatorFactory());
			} else if (startsWith(argument, FO_REWRITING)) {
				configuration.evaluationStrategyFactory = new FORewritingEvaluationStrategyFactory();
			} else if (startsWith(argument, SQL_REWRITING)) {
				configuration.evaluationStrategyFactory = new SQLRewritingEvaluationStrategyFactory();
			}else if (startsWith(argument, GROUP_SQL_REWRITING)) {
					configuration.evaluationStrategyFactory = new GroupSQLRewritingEvaluationStrategyFactory();
			} else if (startsWith(argument, GUARDED_NAIVE)) {
				configuration.evaluationStrategyFactory = new StratifiedBottomUpEvaluationStrategyFactory(
				        new GuardedNaiveEvaluatorFactory());
			} else if (startsWith(argument, SAFE_RULES)) {
				configuration.ruleSafetyProcessor = new StandardRuleSafetyProcessor();
			} else if (startsWith(argument, UNSAFE_RULES)) {
				configuration.ruleSafetyProcessor = new AugmentingRuleSafetyProcessor();
			} else if (startsWith(argument, LINEAR_RULES)) {
				configuration.ruleSafetyProcessor = new LinearReducibleRuleSafetyProcessor();
			} else if (startsWith(argument, GUARDED_RULES)) {
				configuration.ruleSafetyProcessor = new GuardedRuleSafetyProcessor();
			} else if (startsWith(argument, WEAKLY_GUARDED_RULES)) {
				configuration.ruleSafetyProcessor = new WeaklyGuardedRuleSafetyProcessor();
			} else if (startsWith(argument, MAGIC_SETS)) {
				configuration.programOptmimisers.add(new RuleFilter());
				configuration.programOptmimisers.add(new MagicSets());
			} else {
				usage();
			}
		}

		if (program == null) {
			usage();
		}

		System.out.println();

		execute(program, configuration);
	}

	@SuppressWarnings("deprecation")
	public static void execute(final String program, final Configuration configuration) {
		final Thread t = new Thread(new ExecutionTask(program, configuration), "Evaluation task");

		t.setPriority(Thread.MIN_PRIORITY);
		t.start();

		try {
			t.join(configuration.evaluationTimeoutMilliseconds);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}

		if (t.isAlive()) {
			t.stop();
			System.out.println("Timeout exceeded: " + configuration.evaluationTimeoutMilliseconds + "ms");
		}
	}

	static class ExecutionTask implements Runnable {
		ExecutionTask(final String program, final Configuration configuration) {
			this.program = program;
			this.configuration = configuration;
		}

		// @Override
		@Override
		public void run() {
			final ProgramExecutor executor = new ProgramExecutor(program, configuration);
			System.out.println(executor.getOutput());
		}

		private final String program;

		private final Configuration configuration;
	}
}
