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
package com.braintribe.model.processing.locking.db.test.wire.contract;

import com.braintribe.common.db.DbVendor;
import com.braintribe.model.processing.locking.db.impl.DbLocking;
import com.braintribe.wire.api.space.WireSpace;

public interface DbLockingTestContract extends WireSpace {

	int LOCK_EXPIRATION_SEC = 2;

	int ACTIVEMQ_PORT = 61636;

	// switch to false to see if any test fails; just have a way to verify the refresher is being tested
	boolean REFRESHER_ENABLED = true;

	DbLocking locking(DbVendor vendor);

}
