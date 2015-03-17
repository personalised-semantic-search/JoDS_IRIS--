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
package org.deri.iris.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.apache.log4j.Logger;
import org.deri.iris.ConfigurationException;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.api.factory.IBasicFactory;
import org.deri.iris.api.factory.ITermFactory;
import org.deri.iris.api.terms.ITerm;
import org.deri.iris.basics.BasicFactory;
import org.deri.iris.terms.TermFactory;
import org.postgresql.ds.PGPoolingDataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

/**
 * @author Giorgio Orsi <orsi@elet.polimi.it> - Politecnico di Milano
 */
public class StorageManager {

	private static String dbVendorName;
	private static String owner;
	private static String schema;
	private static DataSource ds;
	private static Map<String, List<String>> dbIndexes = new HashMap<String, List<String>>();
	private static Map<String, List<ITuple>> storedRelations = new HashMap<String, List<ITuple>>();
	private static Map<String, List<String>> tableFieldsCache = new HashMap<String, List<String>>();

	private final static Logger LOGGER = Logger.getLogger(StorageManager.class);

	private StorageManager() {
	}

	/**
	 * @return the Storage Manager
	 */
	public static StorageManager getInstance() {
		return Holder.instance;
	}

	private static class Holder {
		private final static StorageManager instance = new StorageManager();
	}

	public static void configure(final Map<IPredicate, IRelation> conf) throws ConfigurationException {
		// Parse the configuration
		for (final IPredicate p : conf.keySet()) {
			if (p.getPredicateSymbol().compareTo("DBConnection") == 0) {
				// Get the information about the DB Connection
				final IRelation r = conf.get(p);
				if ((r.size() == 0) || (r.size() > 1)) {
					LOGGER.error("Multiple definition for the DB Connection. Please check the configuration.");

					throw new ConfigurationException(
					        "Multiple definition for the DB Connection. Please check the configuration.");
				} else {
					// Configure the connection
					final ITuple t = r.get(0);
					if (t.size() != 8) {
						LOGGER.error("Predicate DBConnection is a reserved Predicate with arity 7, please check your program.");
						throw new ConfigurationException(
						        "Predicate DBConnection is a reserved Predicate with arity 7, please check your program.");
					} else {
						try {
							StorageManager.connect(t.get(0).toString().replace("'", ""),
							        t.get(1).toString().replace("'", ""), t.get(2).toString().replace("'", ""),
							        new Integer(t.get(3).toString().replace("'", "")),
							        t.get(4).toString().replace("'", ""), t.get(5).toString().replace("'", ""), t
							                .get(6).toString().replace("'", ""), t.get(7).toString().replace("'", ""));
						} catch (final SQLException sqle) {
							sqle.printStackTrace();
						}
					}
				}
			}
		}
	}

	public static void connect(final String vendor, final String protocol, final String host, final int port,
	        final String dbName, final String schemaName, final String usr, final String pwd) throws SQLException {

		if (ds == null) {
			// Instantiate a DataSource object
			dbVendorName = vendor;
			owner = usr;
			schema = schemaName;
			if (vendor.compareTo("_ORACLE") == 0) {
				ds = new SimpleDataSource(usr, pwd);
				ds = new OracleDataSource();
				// Set the JDBC Configuration
				((OracleDataSource) ds).setDriverType("thin");
				((OracleDataSource) ds).setDataSourceName("jdbc/nyayaDB");
				((OracleDataSource) ds).setDatabaseName(dbName);
				((OracleDataSource) ds).setConnectionCacheName("cache");
				((OracleDataSource) ds).setNetworkProtocol(protocol);
				((OracleDataSource) ds).setServerName(host);
				((OracleDataSource) ds).setPortNumber(port);
				((OracleDataSource) ds).setUser(usr);
				((OracleDataSource) ds).setPassword(pwd);

				// Set the cache properties
				final Properties cacheProps = new Properties();
				cacheProps.setProperty("ConnectionWaitTimeout", "60");
				cacheProps.setProperty("MinLimit", "1");
				cacheProps.setProperty("MaxLimit", String.valueOf(Runtime.getRuntime().availableProcessors() * 5));
				cacheProps.setProperty("InitialLimit", "1");
				((OracleDataSource) ds).setConnectionProperties(cacheProps);

			} else if (vendor.compareTo("_MYSQL") == 0) {
				ds = new MysqlConnectionPoolDataSource();
				((MysqlConnectionPoolDataSource) ds).setAllowMultiQueries(true);
				((MysqlConnectionPoolDataSource) ds).setDatabaseName(dbName);
				((MysqlConnectionPoolDataSource) ds).setServerName(host);
				((MysqlConnectionPoolDataSource) ds).setPortNumber(port);
				((MysqlConnectionPoolDataSource) ds).setUser(usr);
				((MysqlConnectionPoolDataSource) ds).setPassword(pwd);
			} else if (vendor.compareTo("_POSTGRES") == 0) {
				ds = new PGPoolingDataSource();
				((PGPoolingDataSource) ds).setDataSourceName("A Data Source");
				((PGPoolingDataSource) ds).setServerName(host);
				((PGPoolingDataSource) ds).setDatabaseName(dbName);
				((PGPoolingDataSource) ds).setUser(usr);
				((PGPoolingDataSource) ds).setPassword(pwd);
				((PGPoolingDataSource) ds).setMaxConnections(300);
			} else
				throw new SQLException("Unsupported vendor: " + vendor);
		}
		try {
			final Connection conn = ds.getConnection();
			if (conn != null) {
				LOGGER.info("Connection to: " + dbName + " granted.");
			}
		} catch (final SQLException e) {
			LOGGER.error(e.getMessage());
			throw new SQLException(e);
		}

	}

	public static DataSource getDataSource() throws SQLException {
		if (ds == null)
			throw new SQLException("Data Source Unavailable, try connect() first...");
		else
			return (ds);
	}

	public static String getSchemaName() {
		return schema;
	}

	public static List<String> getFields(final String predicate) {
		final List<String> fields = new ArrayList<String>();
		Statement st = null;
		ResultSet rs = null;
		Connection conn = null;

		if (tableFieldsCache.containsKey(predicate))
			return (tableFieldsCache.get(predicate));
		else {
			String isQuery = "";
			if (dbVendorName.compareTo("_MYSQL") == 0) {
				// MySQL Database Information Schema Query
				isQuery = "SHOW COLUMNS FROM " + predicate;
			} else if (dbVendorName.compareTo("_ORACLE") == 0) {
				// Oracle Database Information Schema Query
				isQuery = "select distinct COLUMN_NAME, COLUMN_ID from ALL_TAB_COLUMNS where TABLE_NAME = '"
				        + predicate.toUpperCase() + "' and OWNER='" + owner.toUpperCase() + "' order by COLUMN_ID";
			} else if (dbVendorName.compareTo("_POSTGRES") == 0) {
				// Oracle Database Information Schema Query
				isQuery = "SELECT column_name FROM information_schema.columns WHERE table_schema='" + schema
				        + "' and table_name='" + predicate + "' order by ordinal_position";
			}
			try {
				conn = getDataSource().getConnection();
				st = conn.createStatement();
				rs = st.executeQuery(isQuery);
				while (rs.next()) {
					if (dbVendorName.compareTo("_MYSQL") == 0) {
						fields.add(rs.getString("Field"));
					} else if (dbVendorName.compareTo("_ORACLE") == 0) {
						fields.add(rs.getString("COLUMN_NAME"));
					} else if (dbVendorName.compareTo("_POSTGRES") == 0) {
						final String relationName = rs.getString("column_name");

						fields.add(relationName);
					}
				}
			} catch (final SQLException e) {
				e.printStackTrace();
			} finally {
				close(st, rs, conn);
			}
			tableFieldsCache.put(predicate, fields);
			return (fields);
		}

	}

	public static void close(final Statement st, final ResultSet rs, final Connection con) {
		try {
			if (st != null) {
				st.close();
			}
			if (rs != null) {
				rs.close();
			}
			if (con != null) {
				con.close();
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}
	public static void close( final Connection con) {
		try {
			if (con != null) {
				con.close();
			}
		} catch (final SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<String> getDBIndex(final String predicate) {
		if (dbIndexes.containsKey(predicate))
			return (dbIndexes.get(predicate));
		else {
			final List<String> idx = createDBIndex(predicate);
			dbIndexes.put(predicate, idx);
			return (idx);
		}
	}

	public static List<ITuple> getStoredRelation(final String predicate) {
		if (storedRelations.containsKey(predicate))
			return (storedRelations.get(predicate));
		else {
			final List<ITuple> sr = loadStoredRelation(predicate);
			storedRelations.put(predicate, sr);
			return (sr);
		}
	}

	private static List<String> createDBIndex(final String pred) {
		final List<String> idx = new ArrayList<String>();
		Statement st = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			st = conn.createStatement();

			rs = st.executeQuery("SELECT COUNT(*) from " + pred);
			rs.next();
			final int dbSize = rs.getInt(1);

			System.out.println("Creating the Index for " + pred + " on " + dbSize + " rows.");
			rs = st.executeQuery("SELECT * from " + pred);

			int i = 0;
			while (rs.next()) {
				idx.add(rs.getString("oid"));
				i++;
				if ((i % 1000) == 0) {
					System.out.print(".");
				}
			}
			System.out.println("\ndone.\n");
		} catch (final SQLException sqle) {
			sqle.printStackTrace();
		} finally {
			close(st, rs, conn);
		}
		return (idx);
	}

	public static List<ITuple> loadStoredRelation(final String pred) {

		final List<ITuple> res = new ArrayList<ITuple>();
		Statement st = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = getDataSource().getConnection();
			st = conn.createStatement();
			rs = st.executeQuery("SELECT COUNT(*) from " + pred);
			rs.next();

			final int dbSize = rs.getInt(1);
			System.out.println("Loading " + dbSize + " rows from table: " + pred);
			rs = st.executeQuery("SELECT * from " + pred);

			final IBasicFactory bf = BasicFactory.getInstance();
			final ITermFactory tf = TermFactory.getInstance();
			int idx = 0;
			while (rs.next()) {
				final List<ITerm> terms = new ArrayList<ITerm>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					terms.add(tf.createString(rs.getString(i)));
				}
				res.add(bf.createTuple(terms));
				idx++;
				if ((idx % 1000) == 0) {
					System.out.print(".");
				}
			}
			rs.close();
			st.close();
			conn.close();
			System.out.println("\ndone.\n");
		} catch (final SQLException e) {
			e.printStackTrace();
		} finally {
			close(st, rs, conn);
		}
		return (res);
	}

	public static String getVendor() {
		return (dbVendorName);
	}

	public static IRelation constructAnswer(final ResultSet rs) {
		final IRelationFactory rf = new RelationFactory();
		final IRelation result = rf.createRelation();
		final IBasicFactory bf = BasicFactory.getInstance();
		final ITermFactory tf = TermFactory.getInstance();

		try {
			final int columnCount = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				final List<ITerm> terms = new ArrayList<ITerm>();
				for (int i = 1; i <= columnCount; i++) {
					terms.add(tf.createString(rs.getString(i)));
				}
				result.add(bf.createTuple(terms));
			}
			rs.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return (result);
	}

	public static IRelation executeQuery(final String query) {
		final RelationFactory rf = new RelationFactory();
		IRelation result = rf.createRelation();

		try {
			final Connection conn = getDataSource().getConnection();
			final Statement st = conn.createStatement();
			final ResultSet rs = st.executeQuery(query);
			result = constructAnswer(rs);
			st.close();
			rs.close();
			conn.close();
		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return (result);
	}

	

	
	/**
     * 
     */
	public static void disconnect() throws SQLException {
		if (dbVendorName.compareTo("_ORACLE") == 0) {
			// ((SimpleDataSource)ds).close();
		} else if (dbVendorName.compareTo("_MYSQL") == 0) {
			// DO Nothing
		} else
			throw new SQLException("Unsupported Vendor");
	}

}
