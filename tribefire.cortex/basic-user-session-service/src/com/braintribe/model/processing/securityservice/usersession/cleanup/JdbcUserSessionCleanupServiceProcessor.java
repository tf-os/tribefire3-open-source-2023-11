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
package com.braintribe.model.processing.securityservice.usersession.cleanup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;

import javax.sql.DataSource;

import com.braintribe.cfg.Required;
import com.braintribe.gm.model.user_session_service.CleanupUserSessions;
import com.braintribe.gm.model.user_session_service.CleanupUserSessionsResponse;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.securityservice.api.exceptions.SecurityServiceException;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.utils.lcd.StopWatch;

public class JdbcUserSessionCleanupServiceProcessor implements ServiceProcessor<CleanupUserSessions, CleanupUserSessionsResponse> {

	private DataSource dataSource;

	// argument order value_false, cur_date, value_true, cur_date
	private static final String DELETE_EXPIRED_PERSISTENCE_USER_SESSIONS_STMT = "DELETE FROM TF_US_PERSISTENCE_USER_SESSION WHERE (BLOCKS_AUTHENTICATION_AFTER_LOGOUT = ? AND EXPIRY_DATE < ?) OR (BLOCKS_AUTHENTICATION_AFTER_LOGOUT = ? AND FIXED_EXPIRY_DATE < ?)";

	private static final Logger log = Logger.getLogger(UserSessionCleanupWorker.class);

	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public CleanupUserSessionsResponse process(ServiceRequestContext requestContext, CleanupUserSessions request) {
		StopWatch stopWatch = new StopWatch();
		int countOfSessionsDeleted = deleteExpiredPersistenceUserSessions();

		logDeletion(stopWatch, countOfSessionsDeleted);

		return CleanupUserSessionsResponse.T.create();
	}

	private void logDeletion(StopWatch stopWatch, int countOfSessionsDeleted) {
		if (!log.isDebugEnabled())
			return;

		if (countOfSessionsDeleted > 0) {
			log.debug("Cleanup of " + countOfSessionsDeleted + " user session(s) concluded in " + stopWatch.getElapsedTime()
					+ " ms, which amounts to " + ((double) stopWatch.getElapsedTime() / countOfSessionsDeleted) + " ms per session");
		} else {
			log.debug("Cleanup of user sessions concluded; No invalid user sessions found");
		}
	}

	private int deleteExpiredPersistenceUserSessions() {
		try (Connection conn = getJdbcConnection()) {
			PreparedStatement stmt = conn.prepareStatement(DELETE_EXPIRED_PERSISTENCE_USER_SESSIONS_STMT);
			Timestamp now = new Timestamp(new Date().getTime());
			stmt.setBoolean(1, false);
			stmt.setTimestamp(2, now);
			stmt.setBoolean(3, true);
			stmt.setTimestamp(4, now);
			return stmt.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException("Running user session delete query failed", e);
		}
	}

	private Connection getJdbcConnection() throws SecurityServiceException {
		try {
			return dataSource.getConnection();
		} catch (Exception e) {
			throw new RuntimeException("Failed to obtain the JDBC connection from the provider", e);
		}
	}
}
