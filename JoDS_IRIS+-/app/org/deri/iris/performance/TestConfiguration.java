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

import java.util.HashMap;

/**
 * @author Giorgio Orsi <orsi AT elet DOT polimi DOT it> ICT Institute - Politecnico di Milano.
 * @version 0.1b
 */
public class TestConfiguration {

	private final HashMap<String, String> config;

	/**
	 * Initializes the HashMap representing the configuration.
	 */
	public TestConfiguration() {
		config = new HashMap<String, String>();
	}

	// Getters and Setters for the Test-case Configuration

	/**
	 * Sets the home directory for the tests
	 * @param homePath
	 */
	public void setTestHomePath(final String homePath) {
		config.put("_HOME", homePath);
	}

	/**
	 * Gets the home directory for the tests
	 * @return the Test Home Directory
	 */
	public String getTestHomePath() {
		return (config.get("_HOME"));
	}

	// Getters and Setters for the Persistence Configuration

	public String getDBUsername() {
		return (config.get("_DB_USERNAME"));
	}

	public void setDBUsername(final String username) {
		config.put("_DB_USERNAME", username);
	}

	public void setDBPassword(final String pwd) {
		config.put("_DB_PWD", pwd);
	}

	public String getDBPassword() {
		return (config.get("_DB_PWD"));
	}

	public void setDBVendor(final String vendor) {
		config.put("_VENDOR", vendor);
	}

	public String getDBVendor() {
		return (config.get("_VENDOR"));
	}

	public void setDBProtocol(final String protocol) {
		config.put("_PROTOCOL", protocol);
	}

	public String getDBProtocol() {
		return (config.get("_PROTOCOL"));
	}

	public void setDBHost(final String host) {
		config.put("_HOST", host);
	}

	public String getDBHost() {
		return (config.get("_HOST"));
	}

	public void setDBPort(final int port) {
		config.put("_PORT", String.valueOf(port));
	}

	public int getDBPort() {
		return (Integer.parseInt(config.get("_PORT")));
	}

	public void setDBName(final String dbName) {
		config.put("_DB_NAME", dbName);
	}

	public String getDBName() {
		return (config.get("_DB_NAME"));
	}

	public void setSchemaName(final String schemaName) {
		config.put("_SCHEMA_NAME", schemaName);
	}

	public String getSchemaName() {
		return (config.get("_SCHEMA_NAME"));
	}

	// Getters and Setters for the Reasoning Configuration

	public void setExpressiveness(final String expr) {
		if (expr.compareTo("RDFS") == 0) {
			config.put("_EXPRESSIVENESS", "RDFS");
		}
		if (expr.compareTo("OWL-QL") == 0) {
			config.put("_EXPRESSIVENESS", "OWL-QL");
		}
	}

	public String getExpressiveness() {
		return (config.get("_EXPRESSIVENESS"));
	}

	public void setReasoning(final boolean reasoning) {
		if (reasoning) {
			config.put("_REASONING", "true");
		} else {
			config.put("_REASONING", "false");
		}
	}

	public boolean getReasoning() {
		return (config.get("_REASONING").equalsIgnoreCase("true"));
	}

	// Getters and Setters for the Dataset Configuration

	public void setDataset(final String dataset) {
		config.put("_DATASET", dataset);
	}

	public String getDataset() {
		return (config.get("_DATASET"));
	}

}
