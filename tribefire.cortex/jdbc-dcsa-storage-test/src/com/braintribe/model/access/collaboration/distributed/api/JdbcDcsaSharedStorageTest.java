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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaAppendDataManipulation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.lcd.CollectionTools2;

@Category(SpecialEnvironment.class)
public class JdbcDcsaSharedStorageTest {

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

	@Test
	public void testAccessIds() throws Exception {

		String[] accessIdsPrefixes = new String[] { "normal", "looooooooooooooooooooooooooooooong",
				"MoreThan300Chars123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890" };

		for (String accessIdsPrefix : accessIdsPrefixes) {

			System.out.println("Trying accessId prefix: " + accessIdsPrefix);

			JdbcDcsaStorage storage = new JdbcDcsaStorage();
			storage.setProjectId("storage-test-" + UUID.randomUUID().toString());
			storage.setDataSource(dbHandler.dataSource());
			storage.setLockManager(dbHandler.lockManager());
			JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
			storage.setMarshaller(marshaller);
			storage.postConstruct();

			String accessId = accessIdsPrefix + "-" + UUID.randomUUID().toString();

			CsaAppendDataManipulation csaOperation = CsaAppendDataManipulation.T.create();
			csaOperation.setId(UUID.randomUUID().toString());

			String revisionAfterFirstWrite = storage.storeOperation(accessId, csaOperation);

			assertThat(revisionAfterFirstWrite).isNotNull();
			assertThat(revisionAfterFirstWrite).isNotEmpty();

			System.out.println("Revision after 1st write: " + revisionAfterFirstWrite);

			CsaAppendDataManipulation csaOperation2 = CsaAppendDataManipulation.T.create();
			csaOperation2.setId(UUID.randomUUID().toString());
			String revisionAfterSecondWrite = storage.storeOperation(accessId, csaOperation2);

			assertThat(revisionAfterSecondWrite).isNotNull();
			assertThat(revisionAfterSecondWrite).isNotEmpty();

			assertThat(revisionAfterSecondWrite.compareTo(revisionAfterFirstWrite)).isGreaterThan(0);

			System.out.println("Revision after 2nd write: " + revisionAfterSecondWrite);

			DcsaIterable iterable = storage.readOperations(accessId, null);
			assertThat(iterable.getLastReadMarker()).isEqualTo(revisionAfterSecondWrite);

			int count = 0;
			for (CsaOperation op : iterable) {
				count++;
			}

			assertThat(count).isEqualTo(2);

			iterable = storage.readOperations(accessId, revisionAfterFirstWrite);
			count = 0;
			CsaOperation resultOp = null;
			for (CsaOperation op : iterable) {
				resultOp = op;
				count++;
			}

			assertThat(count).isEqualTo(1);
			assertThat(resultOp.getId().toString()).isEqualTo(csaOperation2.getId().toString());
		}
	}

	@Test
	public void testDcsaStorage() throws Exception {

		JdbcDcsaStorage storage = new JdbcDcsaStorage();
		storage.setProjectId("storage-test-" + UUID.randomUUID().toString());
		storage.setDataSource(dbHandler.dataSource());
		storage.setLockManager(dbHandler.lockManager());
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		storage.setMarshaller(marshaller);
		storage.postConstruct();

		String accessId = UUID.randomUUID().toString();

		CsaAppendDataManipulation csaOperation = CsaAppendDataManipulation.T.create();
		csaOperation.setId(UUID.randomUUID().toString());

		String revisionAfterFirstWrite = storage.storeOperation(accessId, csaOperation);

		assertThat(revisionAfterFirstWrite).isNotNull();
		assertThat(revisionAfterFirstWrite).isNotEmpty();

		System.out.println("Revision after 1st write: " + revisionAfterFirstWrite);

		CsaAppendDataManipulation csaOperation2 = CsaAppendDataManipulation.T.create();
		csaOperation2.setId(UUID.randomUUID().toString());
		String revisionAfterSecondWrite = storage.storeOperation(accessId, csaOperation2);

		assertThat(revisionAfterSecondWrite).isNotNull();
		assertThat(revisionAfterSecondWrite).isNotEmpty();

		assertThat(revisionAfterSecondWrite.compareTo(revisionAfterFirstWrite)).isGreaterThan(0);

		System.out.println("Revision after 2nd write: " + revisionAfterSecondWrite);

		DcsaIterable iterable = storage.readOperations(accessId, null);
		assertThat(iterable.getLastReadMarker()).isEqualTo(revisionAfterSecondWrite);

		int count = 0;
		for (CsaOperation op : iterable) {
			count++;
		}

		assertThat(count).isEqualTo(2);

		iterable = storage.readOperations(accessId, revisionAfterFirstWrite);
		count = 0;
		CsaOperation resultOp = null;
		for (CsaOperation op : iterable) {
			resultOp = op;
			count++;
		}

		assertThat(count).isEqualTo(1);
		assertThat(resultOp.getId().toString()).isEqualTo(csaOperation2.getId().toString());
	}

	@Test
	public void testResource() throws Exception {

		JdbcDcsaStorage storage = new JdbcDcsaStorage();
		storage.setProjectId("storage-resource-test-" + UUID.randomUUID().toString());
		storage.setDataSource(dbHandler.dataSource());
		storage.setLockManager(dbHandler.lockManager());
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		storage.setMarshaller(marshaller);
		storage.postConstruct();

		String accessId = UUID.randomUUID().toString();

		byte[] data = new byte[10];
		new Random().nextBytes(data);

		Resource resource = Resource.createTransient(() -> new ByteArrayInputStream(data));
		resource.setMimeType("image/png");
		resource.setName("test.png");
		CsaAppendDataManipulation csaOperation = CsaAppendDataManipulation.T.create();
		csaOperation.setId(UUID.randomUUID().toString());
		csaOperation.setPayload(resource);

		String revisionAfterFirstWrite = storage.storeOperation(accessId, csaOperation);

		DcsaIterable iterable = storage.readOperations(accessId, null);
		assertThat(iterable.getLastReadMarker()).isEqualTo(revisionAfterFirstWrite);

		int count = 0;
		for (CsaOperation op : iterable) {
			if (count == 0) {
				assertThat(op).isInstanceOf(CsaAppendDataManipulation.class);
				CsaAppendDataManipulation o = (CsaAppendDataManipulation) op;
				Resource testPayload = o.getPayload();
				assertThat(testPayload.getMimeType()).isEqualTo("image/png");
				assertThat(testPayload.getName()).isEqualTo("test.png");
			}
			count++;
		}
		assertThat(count).isEqualTo(1);

	}

	@Test
	public void testResourceLoading() throws Exception {

		JdbcDcsaStorage storage = new JdbcDcsaStorage();
		storage.setProjectId("storage-resource-test-" + UUID.randomUUID().toString());
		storage.setDataSource(dbHandler.dataSource());
		storage.setLockManager(dbHandler.lockManager());
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		storage.setMarshaller(marshaller);
		storage.postConstruct();

		String accessId = UUID.randomUUID().toString();

		byte[] data = new byte[10];
		new Random().nextBytes(data);
		String path = UUID.randomUUID().toString();

		Resource resource = Resource.createTransient(() -> new ByteArrayInputStream(data));
		resource.setMimeType("image/png");
		resource.setName("test.png");
		CsaStoreResource csaOperation = CsaStoreResource.T.create();
		csaOperation.setResourceRelativePath(path);
		csaOperation.setId(UUID.randomUUID().toString());
		csaOperation.setPayload(resource);

		String revisionAfterFirstWrite = storage.storeOperation(accessId, csaOperation);

		DcsaIterable iterable = storage.readOperations(accessId, null);
		assertThat(iterable.getLastReadMarker()).isEqualTo(revisionAfterFirstWrite);

		Map<String, Resource> resourceMap = storage.readResource(accessId, CollectionTools2.asList(path));
		assertThat(resourceMap).isNotNull();
		assertThat(resourceMap).hasSize(1);

		Resource testPayload = resourceMap.get(path);
		assertThat(testPayload.getMimeType()).isEqualTo("image/png");
		assertThat(testPayload.getName()).isEqualTo("test.png");

	}

	@Test
	public void testResourceDefault() throws Exception {

		JdbcDcsaStorage storage = new JdbcDcsaStorage();
		storage.setProjectId("storage-resource-test-" + UUID.randomUUID().toString());
		storage.setDataSource(dbHandler.dataSource());
		storage.setLockManager(dbHandler.lockManager());
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		storage.setMarshaller(marshaller);
		storage.postConstruct();

		String accessId = UUID.randomUUID().toString();

		byte[] data = new byte[10];
		new Random().nextBytes(data);

		Resource resource = Resource.createTransient(() -> new ByteArrayInputStream(data));
		CsaAppendDataManipulation csaOperation = CsaAppendDataManipulation.T.create();
		csaOperation.setId(UUID.randomUUID().toString());
		csaOperation.setPayload(resource);

		String revisionAfterFirstWrite = storage.storeOperation(accessId, csaOperation);

		DcsaIterable iterable = storage.readOperations(accessId, null);
		assertThat(iterable.getLastReadMarker()).isEqualTo(revisionAfterFirstWrite);

		int count = 0;
		for (CsaOperation op : iterable) {
			if (count == 0) {
				assertThat(op).isInstanceOf(CsaAppendDataManipulation.class);
				CsaAppendDataManipulation o = (CsaAppendDataManipulation) op;
				Resource testPayload = o.getPayload();
				assertThat(testPayload.getMimeType()).isEqualTo("application/json");
				assertThat(testPayload.getName()).isNotEmpty();
			}
			count++;
		}
		assertThat(count).isEqualTo(1);

	}

}
