// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.access.sql.main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.braintribe.model.access.sql.tools.JdbcTools;

/**
 * @author peter.gazdik
 */
public class DerbyDemo {

	private static final String derbyUrl = "jdbc:derby:res/db/DerbyDemo;create=true";

	public static void main(String[] args) throws Exception {
		System.out.println("Starting DerbyDemo");

		new DerbyDemo().run();

		System.out.println("#################################");
		System.out.println("DerbyDemo finished!");
	}

	private void run() throws Exception {
		Connection connection = getDriverManagetConnection();

		connection.setAutoCommit(false);

		JdbcTools.tryDoStatement(connection, this::runItemExample);

		// connection.commit();
	}

	protected Connection getDriverManagetConnection() throws SQLException {
		return DriverManager.getConnection(derbyUrl);
	}


	// #############################################
	// ## . . . . . . . . Queries . . . . . . . . ##
	// #############################################

	private void runItemExample(Statement s) throws Exception {
		createItemTable(s);
		// showTables(s);
//		addItems(s);
		addItemsPs(s.getConnection());
		// throwException();
		queryItems(s);
//		 dropItemTable(s);
	}

	protected void createItemTable(Statement s) throws SQLException {
		try {
			int res = s.executeUpdate("CREATE TABLE item (id bigint primary key, name varchar(255), description clob(64k))");
			// int res = s.executeUpdate("CREATE TABLE item (id bigint, name varchar(255), description clob(64k),
			// primary key(id))");
			// int res = s.executeUpdate("CREATE TABLE item (id bigint, primary key(id), name varchar(255), description
			// clob(64k))");
			System.out.println("\nCreating table 'item' finished with result: " + res);

		} catch (SQLException e) {
			if (e.getSQLState().equals("X0Y32"))
				return; // Table already exists
			throw e;
		}
	}

	protected void showTables(Statement s) throws SQLException {
		System.out.println("\nShowing all tables:");
		try (ResultSet rs = s.executeQuery("select * from sys.systables where tabletype = 'T'")) {
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnsNumber; i++) {
					if (i > 1)
						System.out.print(",  ");
					String columnValue = rs.getString(i);
					System.out.print(columnValue + " " + rsmd.getColumnName(i));
				}
				System.out.println("");
			}
		}
	}

	protected void addItems(Statement s) throws SQLException {
		System.out.println("");

		addItem(s, 1, "Hammer", "Smashes everything");
		addItem(s, 2, "Sickle", "Cuts everything");
		addItem(s, 3, "Bottle", "Contains liquid");
	}

	private void addItem(Statement s, int id, String name, String description) throws SQLException {
		String query = String.format("INSERT INTO item (id, name, description) VALUES (%d, '%s', '%s')", id, name, description);
		int res = s.executeUpdate(query);
		System.out.println("Creating item '" + name + "' finished with result: " + res);
	}

	protected void addItemsPs(Connection c) throws Exception {
		JdbcTools.tryDoPreparedStatement(c, "INSERT INTO item (id, name, description) VALUES (?, ?, ?)", this::addItemsPs);
	}

	protected void addItemsPs(PreparedStatement ps) throws SQLException {
		System.out.println("");

		addItemPs(ps, 1, "Hammer", "Smashes everything");
		addItemPs(ps, 2, "Sickle", "Cuts everything");
		addItemPs(ps, 3, "Bottle", "Contains liquid");
	}

	private void addItemPs(PreparedStatement ps, int id, String name, String description) throws SQLException {
		ps.setObject(1, id);
		ps.setObject(2, name);
		ps.setObject(3, description);
		
		ps.execute();
		int res = ps.getUpdateCount();
		System.out.println("Creating item '" + name + "' finished with result: " + res);
	}

	
	protected void queryItems(Statement s) throws SQLException {
		try (ResultSet rs = s.executeQuery("SELECT * FROM item")) {
			while (rs.next()) {
				// Retrieve by column name
				int id = rs.getInt("id");
				String name = rs.getString("name");
				String description = rs.getString("description");

				System.out.println("ID: " + id);
				System.out.println("name: " + name);
				System.out.println("description: " + description);
				System.out.println();
			}
		}

	}

	protected void dropItemTable(Statement s) throws SQLException {
		int res = s.executeUpdate("DROP TABLE item");
		System.out.println("\nDropping table 'item' finished with result: " + res);
	}

	public static void throwException() {
		throw new RuntimeException("RE");
	}

}
