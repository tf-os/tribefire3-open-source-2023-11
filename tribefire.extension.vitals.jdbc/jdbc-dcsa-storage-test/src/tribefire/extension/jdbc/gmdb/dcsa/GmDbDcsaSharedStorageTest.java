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
package tribefire.extension.jdbc.gmdb.dcsa;

import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.braintribe.common.db.DbVendor;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.resource.Resource;

/**
 * Tests for {@link GmDbDcsaSharedStorage}
 */
public class GmDbDcsaSharedStorageTest extends AbstractGmDbDcsaSharedStorageTest {

	public GmDbDcsaSharedStorageTest(DbVendor vendor) {
		super(vendor);
	}

	@Override
	protected boolean supportsReadResources() {
		return true;
	}

	// @formatter:off
	@Override @Test	public void readNothingFromEmptyStorage() throws Exception {
		super.readNothingFromEmptyStorage();
	}

	@Override @Test	public void storeSingleOperation() throws Exception {
		super.storeSingleOperation();
	}

	@Override @Test	public void storeMultipleOperations() throws Exception {
		super.storeMultipleOperations();
	}

	@Override @Test	public void storeAndReadResource() throws Exception {
		super.storeAndReadResource();
	}
	// @formatter:on

	@Test
	public void storeAndReadResourceOnSamePath() throws Exception {
		final String content1 = "DUMMY RESOURCE 1";
		final String content2 = "DUMMY RESOURCE 2";
		final String content3 = "DUMMY RESOURCE 3";

		final String path = "res/path";

		// Create binary resources
		storeResource(path, content1);
		assertResource(path, content1);

		// These two also log a warning that the path is already used.
		storeResource(path, content2);
		assertResource(path, content2);

		storeResource(path, content3);
		assertResource(path, content3);
	}

	private void assertResource(String path, String content) throws Exception {
		Map<String, Resource> resources;
		resources = readResouces(path);
		assertResourceContent(resources.get(path), content);
	}

	private String gmml;

	@Override
	@Test
	public void storeResourceBasedOperations() throws Exception {
		this.gmml = "SHORT GMML";
		super.storeResourceBasedOperations();
	}

	@Test
	public void storeResourceBasedOperations_LongGmml() throws Exception {
		this.gmml = str128K();
		super.storeResourceBasedOperations();
	}

	@Override
	protected String storeResourceBasedOperationsFileContent() {
		return gmml;
	}

	@Override
	protected DcsaSharedStorage newDcsaSharedStorage() {
		GmDbDcsaSharedStorage storage = new GmDbDcsaSharedStorage();

		storage.setProjectId("storage-test-" + UUID.randomUUID().toString());
		storage.setGmDb(gmDb);
		storage.setLockManager(lockManager);

		storage.postConstruct();

		return storage;
	}

	// Helpers

	protected String str128K() {
		return doubleNTimes(str1K(), 7);
	}

	protected String str1K() {
		String s = str8Chars();
		s = doubleNTimes(s, 7);
		return s.substring(0, 1000);
	}

	protected String str8Chars() {
		return "HÆllo!!!";
	}

	protected String doubleNTimes(String s, int n) {
		while (n-- > 0)
			s += s;
		return s;
	}

}
