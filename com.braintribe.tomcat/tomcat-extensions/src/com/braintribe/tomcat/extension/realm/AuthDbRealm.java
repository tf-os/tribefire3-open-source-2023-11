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
package com.braintribe.tomcat.extension.realm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.catalina.CredentialHandler;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.tomcat.util.ExceptionUtils;

public class AuthDbRealm extends JDBCRealm {

	protected CredentialHandler cryptorCredentialsHandler;

	protected PreparedStatement preparedRolesDirect = null;
	protected PreparedStatement preparedRolesIndirect = null;

	protected String user2RoleTable = "TF_ID_USER_ROLES";
	protected String user2RoleUserId = "USER_ID";
	protected String user2RoleRoleId = "ROLE_ID";

	protected String user2GroupTable = "TF_ID_USER_GROUPS";
	protected String user2GroupUserId = "USER_ID";
	protected String user2GroupGroupId = "GROUP_ID";

	protected String group2RoleTable = "TF_ID_GROUP_ROLES";
	protected String group2RoleGroupId = "GROUP_ID";
	protected String group2RoleRoleId = "ROLE_ID";

	protected String fullAccessAlias = "tf-admin";

	protected String saltLength = "16";

	protected ReentrantLock lock = new ReentrantLock();

	@Override
	public CredentialHandler getCredentialHandler() {
		if (cryptorCredentialsHandler == null) {
			int saltLengthInt = Integer.parseInt(saltLength);
			cryptorCredentialsHandler = new CryptorCredentialHandler(saltLengthInt);
		}
		return cryptorCredentialsHandler;
	}

	@Override
	protected ArrayList<String> getRoles(String username) {

		// Number of tries is the number of attempts to connect to the database
		// during this login attempt (if we need to open the database)
		// This needs rewritten wuth better pooling support, the existing code
		// needs signature changes since the Prepared statements needs cached
		// with the connections.
		// The code below will try twice if there is a SQLException so the
		// connection may try to be opened again. On normal conditions (including
		// invalid login - the above is only used once.
		int numberOfTries = 2;
		while (numberOfTries > 0) {
			try {
				// Ensure that we have an open database connection
				open();

				// Accumulate the user's roles
				Set<String> roleList = new HashSet<>();

				PreparedStatement stmt = rolesDirect(dbConnection, username);
				try (ResultSet rs = stmt.executeQuery()) {

					while (rs.next()) {
						String role = rs.getString(1);
						if (null != role) {
							roleList.add(role.trim());
						}
					}

				} finally {
					dbConnection.commit();
				}

				stmt = rolesIndirect(dbConnection, username);
				try (ResultSet rs = stmt.executeQuery()) {

					while (rs.next()) {
						String role = rs.getString(1);
						if (null != role) {
							roleList.add(role.trim());
						}
					}

				} finally {
					dbConnection.commit();
				}

				if (fullAccessAlias != null && fullAccessAlias.length() > 0 && roleList.contains(fullAccessAlias)) {
					roleList.add("tomcat");
					roleList.add("manager-gui");
					roleList.add("manager-script");
				}

				return new ArrayList<>(roleList);

			} catch (SQLException e) {
				// Log the problem for posterity
				containerLog.error(sm.getString("jdbcRealm.exception"), e);

				// Close the connection so that it gets reopened next time
				if (dbConnection != null) {
					close(dbConnection);
				}
			}

			numberOfTries--;
		}

		return null;
	}

	protected PreparedStatement rolesDirect(Connection dbConnection, String username) throws SQLException {

		// select r.ROLE_NAME from TF_ID_USERS u, TF_ID_USER_ROLES u2r, TF_ID_ROLES r where u.id = u2r.user_id and u2r.role_id =
		// r.id and u.user_name = 'cortex';
		if (preparedRolesDirect == null) {
			lock.lock();
			try {
				if (preparedRolesDirect == null) {
					StringBuilder sb = new StringBuilder("SELECT r.");
					sb.append(roleNameCol);
					sb.append(" FROM ");
					sb.append(userTable);
					sb.append(" u, ");
					sb.append(user2RoleTable);
					sb.append(" u2r, ");
					sb.append(userRoleTable);
					sb.append(" r  WHERE u.ID = u2r.");
					sb.append(user2RoleUserId);
					sb.append(" AND u2r.");
					sb.append(user2RoleRoleId);
					sb.append(" = r.ID AND u.");
					sb.append(userNameCol);
					sb.append(" = ?");
					preparedRolesDirect = dbConnection.prepareStatement(sb.toString());
				}
			} finally {
				lock.unlock();
			}
		}

		preparedRolesDirect.setString(1, username);
		return preparedRolesDirect;

	}

	protected PreparedStatement rolesIndirect(Connection dbConnection, String username) throws SQLException {

		// select r.ROLE_NAME from TF_ID_USERS u, TF_ID_USER_GROUPS u2g, TF_ID_GROUP_ROLES g2r, TF_ID_ROLES r where u.ID =
		// u2g.USER_ID and u2g.GROUP_ID = g2r.GROUP_ID and g2r.ROLE_ID = R.ID and u.USER_NAME = 'cortex';
		if (preparedRolesIndirect == null) {
			lock.lock();
			try {
				if (preparedRolesIndirect == null) {
					StringBuilder sb = new StringBuilder("SELECT r.");
					sb.append(roleNameCol);
					sb.append(" FROM ");
					sb.append(userTable);
					sb.append(" u, ");
					sb.append(user2GroupTable);
					sb.append(" u2g, ");
					sb.append(group2RoleTable);
					sb.append(" g2r, ");
					sb.append(userRoleTable);
					sb.append(" r  WHERE u.ID = u2g.");
					sb.append(user2GroupUserId);
					sb.append(" AND u2g.");
					sb.append(user2GroupGroupId);
					sb.append(" = g2r.");
					sb.append(group2RoleGroupId);
					sb.append(" AND g2r.");
					sb.append(group2RoleRoleId);
					sb.append(" = r.ID AND u.");
					sb.append(userNameCol);
					sb.append(" = ?");
					preparedRolesIndirect = dbConnection.prepareStatement(sb.toString());
				}
			} finally {
				lock.unlock();
			}
		}

		preparedRolesIndirect.setString(1, username);
		return preparedRolesIndirect;

	}

	@Override
	protected void close(Connection dbConnection) {
		try {
			preparedRolesDirect.close();
		} catch (Throwable f) {
			ExceptionUtils.handleThrowable(f);
		}
		this.preparedRolesDirect = null;

		super.close(dbConnection);
	}

	public void setUser2RoleTable(String user2RoleTable) {
		this.user2RoleTable = user2RoleTable;
	}
	public void setUser2RoleUserId(String user2RoleUserId) {
		this.user2RoleUserId = user2RoleUserId;
	}
	public void setUser2RoleRoleId(String user2RoleRoleId) {
		this.user2RoleRoleId = user2RoleRoleId;
	}

	public void setUser2GroupTable(String user2GroupTable) {
		this.user2GroupTable = user2GroupTable;
	}
	public void setUser2GroupUserId(String user2GroupUserId) {
		this.user2GroupUserId = user2GroupUserId;
	}
	public void setUser2GroupGroupId(String user2GroupGroupId) {
		this.user2GroupGroupId = user2GroupGroupId;
	}

	public void setGroup2RoleTable(String group2RoleTable) {
		this.group2RoleTable = group2RoleTable;
	}
	public void setGroup2RoleGroupId(String group2RoleGroupId) {
		this.group2RoleGroupId = group2RoleGroupId;
	}
	public void setGroup2RoleRoleId(String group2RoleRoleId) {
		this.group2RoleRoleId = group2RoleRoleId;
	}
	public void setFullAccessAlias(String fullAccessAlias) {
		this.fullAccessAlias = fullAccessAlias;
	}
	public void setSaltLength(String saltLength) {
		this.saltLength = saltLength;
	}
}
