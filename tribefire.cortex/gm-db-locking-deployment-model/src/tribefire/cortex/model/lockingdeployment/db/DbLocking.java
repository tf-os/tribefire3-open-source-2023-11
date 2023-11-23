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
package tribefire.cortex.model.lockingdeployment.db;

import java.util.concurrent.locks.Lock;

import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.lockingdeployment.Locking;

/**
 * 
 */
public interface DbLocking extends Locking {

	EntityType<DbLocking> T = EntityTypes.T(DbLocking.class);

	@Mandatory
	DatabaseConnectionPool getDatabaseConnection();
	void setDatabaseConnection(DatabaseConnectionPool databaseConnection);

	@Initializer("true")
	boolean getAutoUpdateSchema();
	void setAutoUpdateSchema(boolean autoUpdateSchema);

	/** Describes the re-try interval to acquire a lock in case the first try wasn't successful. Relevant for the {@link Lock#tryLock(long, java.util.concurrent.TimeUnit)} */
	@Initializer("100")
	int getPollIntervalInMillis();
	void setPollIntervalInMillis(int pollIntervalInMillis);

	/**
	 * Time period after which a lock expires and is automatically unlocked.
	 * <p>
	 * Typical lock is unlock explicitly (in the <tt>finally</tt> block), this is only relevant if a node dies (JVM is terminated). This is not a
	 * regular case and thus the value can be higher, e.g. the default of 5 minutes.
	 * <p>
	 * While the lock is being held, it's expiration is updated automatically, with a shorter interval (half of this value).
	 */
	@Initializer("60")
	int getLockExpirationInSecs();
	void setLockExpirationInSecs(int lockExpirationInSecs);

	@Initializer("5000L")
	long getTopicExpirationInMillis();
	void setTopicExpirationInMillis(long topicExpirationInMillis);

}
