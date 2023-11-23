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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.GmSessionFactories;
import com.braintribe.model.processing.session.api.managed.SelectQueryExecution;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.record.ListRecord;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.User;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.IOTools;

// TODO review tests and add comparisons of SQL results to dummy or GMQL results
@Category(SpecialEnvironment.class)
public class TfStatementTest {

	private static PersistenceGmSessionFactory sessionFactory;
	private static PersistenceGmSession authSession;
	private static Connection conAuth;

	@BeforeClass
	public static void initialize() throws Exception {
		Class.forName("com.braintribe.tribefire.jdbc.TfDriver");
		sessionFactory = GmSessionFactories.remote("http://localhost:8080/tribefire-services")
				.authentication("cortex", "cortex").done();
		authSession = sessionFactory.newSession("auth");
		conAuth = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth",
				"cortex", "cortex");
	}

	@Test
	@Ignore
	public void testStrangeUrl() throws Exception {

		Connection con = DriverManager.getConnection(
				"jdbc:tribefire://cloud-beta.tribefire.com:80/v1/adidas/adidas-demo/services/?accessId=impala.clickstream.access&ssl=false",
				"cortex", "cortex");

		String sql = "select top 20 * from Clickstreamsamplecsv where browser = 'Edge'";
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String brand = rs.getString("brand");
				String browser = rs.getString("browser");
				System.out.println("brand: " + brand + ", browser: " + browser);

			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		} finally {
			IOTools.closeCloseable(rs, null);
			IOTools.closeCloseable(st, null);
		}

		sql = "select top 20  * from Clickstreamsamplecsv where browser = 'Firefox'";
		st = null;
		rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String brand = rs.getString("brand");
				String browser = rs.getString("browser");
				System.out.println("brand: " + brand + ", browser: " + browser);

			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		} finally {
			IOTools.closeCloseable(rs, null);
			IOTools.closeCloseable(st, null);
		}
	}

	@Test
	public void testSimpleQueryWithSetAndFkId() throws SQLException {

		String sql = "select name, groups, description.id from User";
		Statement st = conAuth.createStatement();
		ResultSet rs = st.executeQuery(sql);
		boolean hadResults = false;
		while (rs.next()) {
			hadResults = true;
			String name = rs.getString(1);
			@SuppressWarnings("unchecked")
			Set<Group> groups = (Set<Group>) rs.getObject("groups");
			String descriptionId = rs.getString("description.id");
			assertNotNull(descriptionId);
			System.out.println("Name: " + name + ", groups: " + groups);
		}
		assertTrue(hadResults);
		System.out.println();
	}

	@Test
	public void testSimpleTopQueryWithFkColumnMapping() throws Exception {

		String sql = "select top 1 * from User";
		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int results = 0;
			while (rs.next()) {
				results++;
				String firstName = rs.getString("firstName");
				String lastName = rs.getString("lastName");
				String description = rs.getString("description.id");
				assertNotNull(description);

				System.out.println(
						"First name: " + firstName + ", Last name: " + lastName + ", description.id: " + description);

			}
			assertEquals(results, 1);
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleQuery2() throws Exception {

		String sql = "select firstName,lastName from User where (firstName = 'Steven' or firstName = 'Robert') and lastName = 'Brown' order by firstName asc, lastName desc";
		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				String firstName = rs.getString(1);
				String lastName = rs.getString(2);
				System.out.println("First name: " + firstName + ", Last name: " + lastName);

			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleQuery() throws Exception {

		String sql = "select firstName as fn, lastName as ln from User where firstName = 'John' or lastName = 'Taylor'";
		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				String firstName = rs.getString(1);
				String lastName = rs.getString("ln");
				System.out.println("First name: " + firstName + ", Last name: " + lastName);

			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleQueryWithOrdering() throws Exception {

		String sql = "select firstName as fn, lastName as ln from User order by lastName asc, firstName desc";
		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			while (rs.next()) {
				String firstName = rs.getString(1);
				String lastName = rs.getString("ln");
				System.out.println("First name: " + firstName + ", Last name: " + lastName);

			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectPreparedStatements3() throws Exception {

		String sql = "SELECT email, firstName, lastName name FROM User";
		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();

			while (rs.next()) {
				System.out.println("");
				for (int i = 0; i < columnCount; ++i) {
					String o = rs.getString(i + 1);
					System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
				}
			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectPreparedStatementsForEntityFk() throws Exception {

		String sql = "SELECT * FROM User WHERE description.id=7";

		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();

			boolean hadrecords = false;
			while (rs.next()) {
				hadrecords = true;
				System.out.println("");
				for (int i = 0; i < columnCount; ++i) {
					String o = rs.getString(i + 1);
					System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
				}
			}

			assertTrue("Expected to get a record.", hadrecords);
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectPreparedStatementsForInnerJoin() throws Exception {

		SelectQuery sq = new SelectQueryBuilder().from(User.T, "u").from(LocalizedString.T, "l").where()
				.property("u", "description.id").eq().property("l", "id").done();
		SelectQueryExecution result = authSession.query().select(sq);
		assertNotNull(result.first());
		System.out.println(result.list());

		String sql = "SELECT u.name as username, l.partition as partitionString FROM User u, LocalizedString l WHERE u.description.id=l.id";

		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();

			boolean hadrecords = false;
			while (rs.next()) {
				hadrecords = true;
				System.out.println("");
				for (int i = 0; i < columnCount; ++i) {
					String o = rs.getString(i + 1);
					System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
				}
			}

			assertTrue("Expected to get a record.", hadrecords);
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectPreparedStatementsForJoinComparison() throws Exception {

		SelectQuery sq = new SelectQueryBuilder().from(User.T, "u").from(LocalizedString.T, "l").where()
				.property("u", "description.id").eq().property("l", "id").where().property("u", "description.id").ge(7L)
				.done();
		SelectQueryExecution result = authSession.query().select(sq);
		assertNotNull(result.first());
		System.out.println(result.list());

		String sql = "SELECT u.name as username, l.partition as partitionString FROM User u, LocalizedString l WHERE u.description.id=l.id AND u.description.id > 7";

		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();

			boolean hadrecords = false;
			while (rs.next()) {
				hadrecords = true;
				System.out.println("");
				for (int i = 0; i < columnCount; ++i) {
					String o = rs.getString(i + 1);
					System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
				}
			}

			assertTrue("Expected to get a record.", hadrecords);
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectPreparedStatementsForVirtualTables() throws Exception {

		SelectQuery sq;
		SelectQueryExecution result;
		sq = new SelectQueryBuilder().select("l", "id").select("r", "id").from(User.T, "l").join("l", "roles", "r")
				.done();

		result = authSession.query().select(sq);
		System.out.println(result.list());

		for (Object record : result.list()) {
			System.out.println((ListRecord) record);
		}

		String sql = "SELECT * FROM UserRolesToRole;";

		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();

			boolean hadrecords = false;

			while (rs.next()) {
				hadrecords = true;
				System.out.println("");
				for (int i = 0; i < columnCount; ++i) {
					String o = rs.getString(i + 1);
					System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
				}
			}

			assertTrue("Expected to get a record.", hadrecords);
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectPreparedStatementsForPrimitiveVirtualTables() throws Exception {
		String sql = "SELECT * FROM LocalizedStringLocalizedValues;";

		try {
			SelectQuery sq;
			SelectQueryExecution result;
			sq = new SelectQueryBuilder().select("ls", "id").select("ls", "localizedValues")
					.from(LocalizedString.T, "ls").done();

			result = authSession.query().select(sq);
			System.out.println(result.list());

			for (Object record : result.list()) {
				ListRecord lr = (ListRecord) record;
				for (Object value : lr.getValues()) {
					System.out.println(value);
				}
			}

			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();

			boolean hadrecords = false;

			while (rs.next()) {
				hadrecords = true;
				System.out.println("");
				for (int i = 0; i < columnCount; ++i) {
					String o = rs.getString(i + 1);
					System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
				}
			}

			assertTrue("Expected to get a record.", hadrecords);
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectPreparedStatementsForVirtualJoins() throws Exception {

		SelectQuery sq2 = new SelectQueryBuilder().from(User.T, "l").join("l", "roles", "r").where().property("r", "id")
				.eq("86eaff50-1720-4dfd-89fe-4aa5277379d4").done();
		SelectQueryExecution result2 = authSession.query().select(sq2);

		for (Object record : result2.list()) {
			ListRecord lr = (ListRecord) record;
			for (Object value : lr.getValues()) {
				System.out.println(value);
			}
		}

		String sql = "SELECT * FROM User u, UserRolesToRole r WHERE u.id = r.User.id AND r.Role.id = '86eaff50-1720-4dfd-89fe-4aa5277379d4';";

		try {
			Statement st = conAuth.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();
			assertTrue("Expected some columns", columnCount > 0);

			boolean hadrecords = false;
			while (rs.next()) {
				hadrecords = true;
				System.out.println("");
				for (int i = 0; i < columnCount; ++i) {
					String o = rs.getString(i + 1);
					System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
				}
			}

			assertTrue("Expected to get a record.", hadrecords);
			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test(expected = TfJdbcException.class)
	public void testSimpleSelectPreparedStatementsForVirtualBidirectionalJoins() throws SQLException {

		String sql = "SELECT * FROM User u, Role r, UserRolesToRole ur WHERE u.id = ur.User.id AND r.id = ur.Role.id;";

		Statement st = conAuth.createStatement();
		ResultSet rs = st.executeQuery(sql);
		int columnCount = rs.getMetaData().getColumnCount();
		assertTrue("Expected some columns", columnCount > 0);

		boolean hadrecords = false;
		while (rs.next()) {
			hadrecords = true;
			System.out.println("");
			for (int i = 0; i < columnCount; ++i) {
				String o = rs.getString(i + 1);
				System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
			}
		}

		assertTrue("Expected to get a record.", hadrecords);
		System.out.println();
	}

	@Test
	public void testSimpleSelectPreparedStatements2() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth",
				"cortex", "cortex");

		String sql = "SELECT User.description, User.email, User.firstName, User.groups, User.id, User.lastLogin, User.lastName, User.name, User.password, User.picture, User.roles FROM User User";
		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();
			System.out.println(sql);
			while (rs.next()) {
				System.out.println("");
				for (int i = 0; i < columnCount; ++i) {
					Object o = rs.getObject(i + 1);
					System.out.println(rs.getMetaData().getColumnName(i + 1) + ": " + o);
				}
			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectWithFixedValues() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth",
				"cortex", "cortex");

		String sql = "select 1 as fv, firstName as fn,lastName as ln from User where firstName = 'John' or lastName = 'Taylor'";
		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println(sql);
			while (rs.next()) {
				long fixedValue = rs.getLong(1);
				long fixedValue2 = rs.getLong("fv");
				String firstName = rs.getString(2);
				String lastName = rs.getString(3);
				System.out.println("  Fixed value: " + fixedValue + ", Fixed value2: " + fixedValue2 + ", First name: "
						+ firstName + ", Last name: " + lastName);
			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

	@Test
	public void testSimpleSelectStatements() throws Exception {

		Connection con = DriverManager.getConnection("jdbc:tribefire://localhost:8443/tribefire-services?accessId=auth",
				"cortex", "cortex");

		testUserQuerySt(con,
				"select firstName as fn,lastName as ln from User where firstName = 'John' or lastName = 'Taylor'", null,
				null);
		testUserQuerySt(con, "select firstName,lastName from User where firstName = 'John' or lastName = 'Taylor'",
				null, null);
		testUserQuerySt(con, "select firstName,lastName from User where firstName = 'John' or lastName = 'Taylor'",
				null, "lastName");
		testUserQuerySt(con,
				"select firstName as fn,lastName as ln from User where firstName = 'John' or lastName = 'Taylor'", null,
				"ln");
		testUserQuerySt(con, "select firstName,lastName from User where firstName = 'John' and lastName = 'Taylor'",
				null, null);
		testUserQuerySt(con,
				"select firstName,lastName from User where (firstName = 'John' and lastName = 'Smith') or lastName = 'Smith'",
				null, null);
		testUserQuerySt(con,
				"select firstName,lastName from User where (firstName = 'John' and lastName = 'Smith') or lastName = 'Taylor'",
				null, null);
		testUserQuerySt(con,
				"select firstName,lastName from User where (firstName = 'John' or firstName = 'Robert') or lastName = 'Williams'",
				null, null);
		testUserQuerySt(con,
				"select firstName,lastName from User where (firstName = 'John' or firstName = 'Robert') or lastName = 'Williams' order by lastName",
				null, null);
		testUserQuerySt(con,
				"select firstName,lastName from User where (firstName = 'Steven' or firstName = 'Robert') order by firstName asc, lastName desc",
				null, null);
		testUserQuerySt(con,
				"select firstName as fn,lastName as ln from User where (firstName = 'Steven' or firstName = 'Robert') order by firstName asc, lastName desc",
				"fn", "ln");
		testUserQuerySt(con,
				"select firstName,lastName from User where (firstName = 'Steven' or firstName = 'Robert') order by lastName asc, firstName desc",
				null, null);

	}

	protected void testUserQuerySt(Connection con, String sql, String firstNameAlias, String lastNameAlias)
			throws Exception {
		try {
			Statement st = con.createStatement();
			ResultSet rs = st.executeQuery(sql);
			System.out.println(sql);
			while (rs.next()) {
				String firstName = firstNameAlias == null ? rs.getString(1) : rs.getString(firstNameAlias);
				String lastName = lastNameAlias == null ? rs.getString(2) : rs.getString(lastNameAlias);
				System.out.println("  First name: " + firstName + ", Last name: " + lastName);
			}
			System.out.println();
		} catch (Exception e) {
			throw new Exception("Error while executing prepared statement query: " + sql, e);
		}
	}

}
