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
package com.braintribe.tribefire.jdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.testing.category.SpecialEnvironment;

@Category(SpecialEnvironment.class)
public class TfPreparedStatementTest {

	@BeforeClass
	public static void initialize() throws Exception {
		Class.forName("com.braintribe.tribefire.jdbc.TfDriver");
	}
	
	@Test
	public void testPreparedStatementWithParams2() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth", "cortex", "cortex");

		String sql = "select * from User where (name = ? or name = ?) and (firstName = ?)";
		try {
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setString(1, "cortex");
			ps.setString(2, "locksmith");
			ps.setString(3, "C.");
			
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString("name");
				Date lastLogin = rs.getDate("lastLogin");
				System.out.println("Name: "+name+", last login: "+lastLogin);
			}
			System.out.println();
		} catch(Exception e) {
			throw new Exception("Error while executing prepared statement query: "+sql, e);
		}
	}
	
	@Test
	public void testPreparedStatementWithParams1() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth", "cortex", "cortex");

		String sql = "select * from User where lastLogin is null or lastLogin < ?";
		try {
			PreparedStatement ps = con.prepareStatement(sql);
			
			ps.setDate(1, new java.sql.Date((new java.util.Date()).getTime()));
			
			System.out.println(ps.toString());

			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				String name = rs.getString("name");
				Date lastLogin = rs.getDate("lastLogin");
				System.out.println("Name: "+name+", last login: "+lastLogin);
			}
			System.out.println();
		} catch(Exception e) {
			throw new Exception("Error while executing prepared statement query: "+sql, e);
		}
	}
	
	@Test
	public void testSimpleSelectPreparedStatements3() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth", "cortex", "cortex");

		String sql = "SELECT email, firstName, lastName name FROM User";
		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();

			while(rs.next()) {
				System.out.println("");
				for (int i=0; i<columnCount; ++i) {
					String o = rs.getString(i+1);
					System.out.println(rs.getMetaData().getColumnName(i+1)+": "+o);
				}
			}
			System.out.println();
		} catch(Exception e) {
			throw new Exception("Error while executing prepared statement query: "+sql, e);
		}
	}
	
	@Test
	public void testSimpleSelectPreparedStatements2() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth", "cortex", "cortex");

		String sql = "SELECT User.description, User.email, User.firstName, User.groups, User.id, User.lastLogin, User.lastName, User.name, User.password, User.picture, User.roles FROM User User";
		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();
			System.out.println(sql);
			while(rs.next()) {
				System.out.println("");
				for (int i=0; i<columnCount; ++i) {
					Object o = rs.getObject(i+1);
					System.out.println(rs.getMetaData().getColumnName(i+1)+": "+o);
				}
			}
			System.out.println();
		} catch(Exception e) {
			throw new Exception("Error while executing prepared statement query: "+sql, e);
		}
	}

	@Test
	public void testSimpleSelectPreparedStatements1() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth", "cortex", "cortex");

		testUserQueryPs(con, "select firstName as fn,lastName as ln from User where firstName = 'John' or lastName = 'Taylor'", null, null);
	}

	@Test
	public void testSimpleSelectPreparedStatements() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth", "cortex", "cortex");

		testUserQueryPs(con, "select firstName as fn,lastName as ln from User where firstName = 'John' or lastName = 'Taylor'", null, null);
		testUserQueryPs(con, "select firstName,lastName from User where firstName = 'John' or lastName = 'Taylor'", null, null);
		testUserQueryPs(con, "select firstName,lastName from User where firstName = 'John' or lastName = 'Taylor'", null, "lastName");
		testUserQueryPs(con, "select firstName as fn,lastName as ln from User where firstName = 'John' or lastName = 'Taylor'", null, "ln");
		testUserQueryPs(con, "select firstName,lastName from User where firstName = 'John' and lastName = 'Taylor'", null, null);
		testUserQueryPs(con, "select firstName,lastName from User where (firstName = 'John' and lastName = 'Smith') or lastName = 'Smith'", null, null);
		testUserQueryPs(con, "select firstName,lastName from User where (firstName = 'John' and lastName = 'Smith') or lastName = 'Taylor'", null, null);
		testUserQueryPs(con, "select firstName,lastName from User where (firstName = 'John' or firstName = 'Robert') or lastName = 'Williams'", null, null);
		testUserQueryPs(con, "select firstName,lastName from User where (firstName = 'John' or firstName = 'Robert') or lastName = 'Williams' order by lastName", null, null);
		testUserQueryPs(con, "select firstName,lastName from User where (firstName = 'Steven' or firstName = 'Robert') order by firstName asc, lastName desc", null, null);

	}

	protected void testUserQueryPs(Connection con, String sql, String firstNameAlias, String lastNameAlias) throws Exception {
		try {
			PreparedStatement ps = con.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();
			System.out.println(sql);
			while(rs.next()) {
				String firstName = firstNameAlias == null ? rs.getString(1) : rs.getString(firstNameAlias);
				String lastName = lastNameAlias == null ? rs.getString(2) : rs.getString(lastNameAlias);
				System.out.println("  First name: "+firstName+", Last name: "+lastName);
			}
			System.out.println();
		} catch(Exception e) {
			throw new Exception("Error while executing prepared statement query: "+sql, e);
		}
	}

}
