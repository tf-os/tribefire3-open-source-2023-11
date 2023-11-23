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
package com.braintribe.model.processing.locking.db.impl;

import java.io.File;

import javax.sql.DataSource;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.braintribe.common.db.DbVendor;
import com.braintribe.common.db.SimpleDbTestSession;
import com.braintribe.common.db.wire.contract.DbTestDataSourcesContract;
import com.braintribe.model.processing.locking.db.test.wire.contract.DbLockingTestContract;
import com.braintribe.util.jdbc.JdbcTools;
import com.braintribe.util.network.NetworkTools;
import com.braintribe.utils.FileTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * @author peter.gazdik
 */
@RunWith(Parameterized.class)
public abstract class AbstractDbLockingTestBase {

	// ###############################################
	// ## . . . . . . . . Static . . . . . . . . . .##
	// ###############################################

	private static SimpleDbTestSession dbTestSession;

	private static WireContext<DbLockingTestContract> lockingWireContext;

	@BeforeClass
	public static void beforeClass() throws Exception {
		FileTools.deleteDirectoryRecursively(new File("res"));

		dbTestSession = SimpleDbTestSession.startDbTest();

		lockingWireContext = Wire.context(DbLockingTestContract.class) //
				.bindContracts(DbLockingTestContract.class) //
				.bindContract(DbTestDataSourcesContract.class, dbTestSession.contract) //
				.build();

		// Call this on startup so that the result is cached and does not interfere with other tests that rely on timing
		// TODO find out if needed
		NetworkTools.getNetworkAddress().getHostAddress();
	}

	@Before
	public void setup() throws Exception {
		JdbcTools.withStatement(dataSource, () -> "Cleaning up TF_LOCKS table", ps -> {
			ps.executeUpdate("delete from TF_LOCKS");
		});
	}

	@AfterClass
	public static void afterClass() throws Exception {
		dbTestSession.shutdownDbTest();
		lockingWireContext.shutdown();
	}

	// ###############################################
	// ## . . . . . . . . . Tests . . . . . . . . . ##
	// ###############################################

	@Parameters
	public static Object[][] params() {
		return new Object[][] { //
				{ DbVendor.derby }, //
				// { DbVendor.postgres }, //
				// { DbVendor.mysql }, //
				// { DbVendor.mssql }, //
				// { DbVendor.oracle }, //
		};
	}

	protected final DbVendor vendor;
	protected final DbLocking locking;
	protected final DataSource dataSource;

	public AbstractDbLockingTestBase(DbVendor vendor) {
		this.vendor = vendor;
		this.dataSource = dbTestSession.contract.dataSource(vendor);
		this.locking = lockingWireContext.contract().locking(vendor);
	}

}
