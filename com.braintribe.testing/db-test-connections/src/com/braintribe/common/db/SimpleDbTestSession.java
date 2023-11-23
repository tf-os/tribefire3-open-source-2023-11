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
package com.braintribe.common.db;

import java.io.File;

import javax.sql.DataSource;

import com.braintribe.common.db.wire.DbTestConnectionsWireModule;
import com.braintribe.common.db.wire.contract.DbTestDataSourcesContract;
import com.braintribe.utils.FileTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * Represents an access point to the {@link DbTestDataSourcesContract}, i.e. the contract which provides the {@link DataSource} instances for all the
 * supported {@link DbVendor vendors}.
 * <p>
 * Typically this test-session is acquired via {@link #startDbTest()}, and stored in a static field. It is then cleaned-up via
 * {@link #shutdownDbTest()}.
 * <p>
 * For an example see <tt>AbstractGmDbTestBase</tt> in <tt>jdbc-gm-support-test</tt> artifact.
 *
 * @author peter.gazdik
 */
public class SimpleDbTestSession {

	public WireContext<DbTestDataSourcesContract> context;
	public DbTestDataSourcesContract contract;

	private SimpleDbTestSession() {
		DbTestSupport.startDerby();
		context = Wire.context(DbTestConnectionsWireModule.INSTANCE);
		contract = context.contract();
	}

	public static SimpleDbTestSession startDbTest() {
		deleteResFolderWithDerbyData();

		return new SimpleDbTestSession();
	}

	/** @see DbTestConstants#derbyUrl */
	private static void deleteResFolderWithDerbyData() {
		File res = new File("res/db/dbtest");
		if (res.exists()) {
			FileTools.deleteDirectoryRecursivelyUnchecked(res);
		}
	}

	public void shutdownDbTest() throws Exception {
		if (context != null) {
			context.shutdown();
		}

		DbTestSupport.shutdownDerby();
	}

}
