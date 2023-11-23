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
package com.braintribe.model.access.collaboration.distributed.api;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.model.access.smood.collaboration.distributed.api.sharedstorage.AbstractSharedStorageTest;
import com.braintribe.testing.category.SpecialEnvironment;

@Category(SpecialEnvironment.class)
public class JdbcSharedStorageTest extends AbstractSharedStorageTest {

	protected static DbHandler dbHandler;

	@BeforeClass
	public static void beforeClass() throws Exception {
		dbHandler = new DerbyDbHandler();
		dbHandler.initialize();
	}

	@AfterClass
	public static void destroyDatabase() throws Exception {
		if (dbHandler != null) {
			dbHandler.destroy();
		}
	}

	@Override
	protected DcsaSharedStorage newDcsaSharedStorage() {

		JdbcDcsaStorage storage = new JdbcDcsaStorage();
		storage.setProjectId("storage-test-" + UUID.randomUUID().toString());
		storage.setDataSource(dbHandler.dataSource());
		storage.setLockManager(dbHandler.lockManager());
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		storage.setMarshaller(marshaller);
		storage.postConstruct();

		return storage;
	}

}
