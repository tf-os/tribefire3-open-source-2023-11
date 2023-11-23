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
package com.braintribe.model.access.smood.distributed.test.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.Assert;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.ClassDataStorage;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.user.User;
import com.braintribe.utils.FileTools;

public class TestUtilities {

	protected DataSource dataSource = null;
	protected ClassDataStorage classDataStorage = null;
	protected IncrementalAccess storage = null;

	public IncrementalAccess getStorage() {
		return storage;
	}
	@Configurable
	@Required
	public void setStorage(IncrementalAccess storage) {
		this.storage = storage;
	}
	public ClassDataStorage getClassDataStorage() {
		return classDataStorage;
	}
	@Configurable
	@Required
	public void setClassDataStorage(ClassDataStorage classDataStorage) {
		this.classDataStorage = classDataStorage;
	}

	public void clearTables() throws Exception {
		this.clearLockTables();
		this.clearSmoodStorageTables();
	}

	public void clearLockTables() throws Exception {

		Connection c = null;
		Statement s = null; 
		try {
			c = this.dataSource.getConnection();
			c.setAutoCommit(false);
			s = c.createStatement();
			Set<String> existingTables = this.getExistingTableNames(c);
			this.deleteFromTable(s, existingTables, "tf_dstlck");
			this.deleteFromTable(s, existingTables, "tf_lockholder");
			this.deleteFromTable(s, existingTables, "tf_intlock");
			this.deleteFromTable(s, existingTables, "tf_applock");
			c.commit();
		} finally {
			closeSilently(c, s, null);
		}

	}
	
	protected void deleteFromTable(Statement s, Set<String> existingTables, String tableName) throws Exception {
		if (existingTables.contains(tableName.toLowerCase())) {
			s.executeUpdate("delete from "+tableName);
		}
	}
	protected void dropTable(Statement s, Set<String> existingTables, String tableName) throws Exception {
		if (existingTables.contains(tableName.toLowerCase())) {
			s.executeUpdate("drop table "+tableName);
		}
	}
	
	protected Set<String> getExistingTableNames(Connection connection) throws Exception {
		DatabaseMetaData md = connection.getMetaData();
		ResultSet rs = null;
		try {
			rs = md.getTables(null, null, "%", null);
			Set<String> result = new HashSet<String>();
			while (rs.next()) {
				result.add(rs.getString(3).toLowerCase());
			}
			return result;
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	public void clearSmoodStorageTables() throws Exception {

		Connection c = null;
		Statement s = null; 
		try {
			c = this.dataSource.getConnection();
			c.setAutoCommit(false);
			s = c.createStatement();
			Set<String> existingTables = this.getExistingTableNames(c);
			this.deleteFromTable(s, existingTables, "tf_smoodstoragebufferedmanip");
			this.deleteFromTable(s, existingTables, "tf_bufferedmanipulation");
			this.deleteFromTable(s, existingTables, "tf_smoodstorageclassdepend");
			this.deleteFromTable(s, existingTables, "tf_smoodstorage");
			this.deleteFromTable(s, existingTables, "tf_javaclass");
			c.commit();
		} finally {
			closeSilently(c, s, null);
		}

		File storageFolder = new File("storage");
		if (storageFolder.exists()) {
			FileTools.deleteDirectoryRecursively(storageFolder);
		}
	}

	public void removeSmoodStorageTables() throws Exception {

		Connection c = null;
		Statement s = null; 
		try {
			c = this.dataSource.getConnection();
			c.setAutoCommit(false);
			s = c.createStatement();
			Set<String> existingTables = this.getExistingTableNames(c);
			this.dropTable(s, existingTables, "tf_smoodstoragebufferedmanip");
			this.dropTable(s, existingTables, "tf_bufferedmanipulation");
			this.dropTable(s, existingTables, "tf_smoodstorageclassdepend");
			this.dropTable(s, existingTables, "tf_smoodstorage");
			this.dropTable(s, existingTables, "tf_javaclass");
			c.commit();
		} finally {
			closeSilently(c, s, null);
		}

	}

	public void removeDbLockTables() throws Exception {

		Connection c = null;
		Statement s = null; 
		try {
			c = this.dataSource.getConnection();
			c.setAutoCommit(false);
			s = c.createStatement();
			Set<String> existingTables = this.getExistingTableNames(c);
			this.dropTable(s, existingTables, "tf_dstlck");
			c.commit();
		} finally {
			closeSilently(c, s, null);
		}

	}
	protected void closeSilently(Connection c, Statement statement, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch(Exception e) {
				//ignore
			}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch(Exception e) {
				//ignore
			}
		}
		if (c != null) {
			try {
				c.close();
			} catch(Exception e) {
				//ignore
			}
		}
	}

	public void checkUserList(PersistenceGmSession session, String[][] expectedUserPairs) throws AssertionError {
		SelectQuery query = new SelectQueryBuilder().from(User.class, "u").done();
		List<Object> objectList = null;
		try {
			objectList = session.query().select(query).list();
		} catch (Exception e) {
			throw new AssertionError("Error while seraching for Users.", e);
		}
		if (objectList != null) {
			Assert.assertEquals(expectedUserPairs.length, objectList.size());
			if (expectedUserPairs.length == 0) {
				return;
			}
			this.checkUserList(objectList, expectedUserPairs);
		} else {
			throw new AssertionError("Could not get a user list.");
		}
	}

	public void checkUserList(List<Object> userList, String[][] expectedUserPairs) throws AssertionError {
		Set<String[]> userSet = new HashSet<String[]>();
		for (String[] userPair : expectedUserPairs) {
			userSet.add(userPair);
		}
		for (Object o : userList) {
			User user = (User) o;
			String firstName = user.getFirstName();
			String lastName = user.getLastName();
			boolean found = false;
			for (Iterator<String[]> it = userSet.iterator(); it.hasNext(); ) {
				String[] comp = it.next();
				if (comp[0].equals(firstName) && comp[1].equals(lastName)) {
					it.remove();
					found = true;
					break;
				}
			}
			if (!found) {
				throw new AssertionError("Could not find user "+firstName+" "+lastName);
			}
		}
		if (!userSet.isEmpty()) {
			throw new AssertionError("Too many users: "+userSet);
		}
	}

	public void checkUserCount(PersistenceGmSession session, int expectedCount) throws AssertionError {
		SelectQuery query = new SelectQueryBuilder().from(User.class, "u").orderBy().property("u", User.id).done();
		List<User> objectList = null;
		try {
			objectList = session.query().select(query).list();
		} catch (Exception e) {
			throw new AssertionError("Could not execute User query", e);
		}
		if (objectList != null) {
			if (expectedCount != objectList.size()) {
				for (User user : objectList) {
					System.out.println("  "+user);
				}
			}
			String accessId = session.getAccessId();
			Assert.assertEquals("Dump count: "+getDumpCount(accessId)+"\nManipulation Buffer count: "+getManipulationBuffers(accessId), expectedCount, objectList.size());
		} else {
			throw new AssertionError("Did not get a search result");
		}
	}

	public void checkManipulationBuffers(String accessId, int expectedCount) throws AssertionError {
		int realValue = getManipulationBuffers(accessId);
		Assert.assertEquals(expectedCount, realValue);
	}
	public int getManipulationBuffers(String accessId) throws AssertionError {

		Connection c = null;
		PreparedStatement s = null;
		PreparedStatement s2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		try {
			c = this.dataSource.getConnection();

			s = c.prepareStatement("select id from tf_smoodstorage where accessId = ? order by sequenceNumber desc");
			s.setString(1, accessId);
			rs = s.executeQuery();
			String id = null;
			if (rs.next()) {
				id = rs.getString(1);
			} else {
				return 0;
			}
			Assert.assertNotNull(id);

			s2 = c.prepareStatement("select count(*) as count from tf_smoodstoragebufferedmanip where SmoodStorageId = ?");
			s2.setString(1, id);
			rs2 = s2.executeQuery();

			if (rs2.next()) {
				int realValue = rs2.getInt(1);
				return realValue;
			} else {
				return 0;
			}

		} catch(Exception e) {
			throw new AssertionError("Could not get number of manipulation buffers.", e);
		} finally {
			closeSilently(null, s2, rs2);
			closeSilently(c, s, rs);
		}

	}
	
	public void checkDumpCount(String accessId, int expectedCount) throws AssertionError {
		int count = getDumpCount(accessId);
		Assert.assertEquals(expectedCount, count);
	}
	
	public int getDumpCount(String accessId) throws AssertionError {

		Connection c = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try {
			c = this.dataSource.getConnection();

			s = c.prepareStatement("select count(*) as count from tf_smoodstorage where accessId = ?");
			s.setString(1, accessId);
			rs = s.executeQuery();
			if (rs.next()) {
				int count = rs.getInt(1);
				return count;
			} else {
				throw new AssertionError("Could not get number of dumps.");
			}

		} catch(Exception e) {
			throw new AssertionError("Could not get number of manipulation buffers.", e);
		} finally {
			closeSilently(c, s, rs);
		}

	}

	public int getCurrentBufferSize(String accessId) throws AssertionError {

		Connection c = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try {
			c = this.dataSource.getConnection();

			s = c.prepareStatement("select size_ from tf_smoodstorage where accessId = ? order by sequencenumber desc");
			s.setString(1, accessId);
			rs = s.executeQuery();
			if (rs.next()) {
				int size = rs.getInt(1);
				return size;
			} else {
				return -1;
			}

		} catch(Exception e) {
			throw new AssertionError("Could not get the size of the current buffer for accessId "+accessId, e);
		} finally {
			closeSilently(c, s, rs);
		}

	}
	public int getBufferedManipulationsSize(String accessId) throws AssertionError {

		Connection c = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try {
			c = this.dataSource.getConnection();

			s = c.prepareStatement("select bufferedManipulationsSize from tf_smoodstorage where accessId = ? order by sequencenumber desc");
			s.setString(1, accessId);
			rs = s.executeQuery();
			if (rs.next()) {
				int size = rs.getInt(1);
				return size;
			} else {
				return -1;
			}

		} catch(Exception e) {
			throw new AssertionError("Could not get the size of the current buffer manipulation size for accessId "+accessId, e);
		} finally {
			closeSilently(c, s, rs);
		}

	}
	
	
	public String getEncodedDataOfCurrentBuffer(String accessId) throws AssertionError {

		Connection c = null;
		PreparedStatement s = null;
		ResultSet rs = null;
		try {
			c = this.dataSource.getConnection();

			s = c.prepareStatement("select encodedData from tf_smoodstorage where accessId = ? order by sequencenumber desc");
			s.setString(1, accessId);
			rs = s.executeQuery();
			if (rs.next()) {
				String encodedData = rs.getString(1);
				return encodedData;
			} else {
				throw new AssertionError("Could not get the size of the current buffer for accessId "+accessId);
			}

		} catch(Exception e) {
			throw new AssertionError("Could not get the size of the current buffer for accessId "+accessId, e);
		} finally {
			closeSilently(c, s, rs);
		}

	}
	
	public PersistenceGmSession openSession(IncrementalAccess access) {
		BasicPersistenceGmSession session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(access);
		return session;
	}

	public User getUser(PersistenceGmSession session, String firstName, String lastName) throws Exception {
		SelectQuery query = new SelectQueryBuilder().from(User.class, "u")
				.where()
				.conjunction()
				.property("firstName").eq(firstName)
				.property("lastName").eq(lastName)
				.close()
				.done();
		List<Object> objectList = session.query().select(query).list();
		if (objectList != null) {
			Assert.assertEquals(1, objectList.size());
			User user = (User) objectList.get(0);
			return user;
		} else {
			throw new AssertionError("Could not get user "+firstName+" "+lastName);
		}		
	}

	public List<User> getUsers(PersistenceGmSession session, String lastName) throws Exception {
		SelectQuery query = new SelectQueryBuilder().from(User.class, "u")
				.where()
				.conjunction()
				.property("lastName").eq(lastName)
				.close()
				.done();
		List<Object> objectList = session.query().select(query).list();
		if (objectList != null) {
			List<User> result = new ArrayList<User>();
			for (Object o : objectList) {
				result.add((User) o);
			}
			return result;
		} else {
			throw new AssertionError("Could not get a single user with lastName "+lastName);
		}		
	}

	public DataSource getDataSource() {
		return dataSource;
	}
	@Required
	@Configurable
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
