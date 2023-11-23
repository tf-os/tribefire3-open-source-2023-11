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
package com.braintribe.model.access.smood.collaboration.distributed.api.sharedstorage;

import static com.braintribe.model.access.smood.collaboration.tools.CollaborativePersistenceRequestBuilder.renameStageRequest;
import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import com.braintribe.model.access.collaboration.distributed.DistributedCollaborativeSmoodAccess;
import com.braintribe.model.access.collaboration.distributed.api.DcsaIterable;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaAppendDataManipulation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaManagePersistence;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.access.smood.collaboration.deployment.InMemoryDcsaSharedStorage;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.session.InputStreamProvider;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

/**
 * @author peter.gazdik
 */
public abstract class AbstractSharedStorageTest {

	private static final String ACCESS_ID = "access.test.dcsa";

	protected boolean supportsReadResources() {
		return false;
	}

	@Test
	public void readNothingFromEmptyStorage() throws Exception {
		DcsaIterable dcsaIterable = read(null);

		assertThat(dcsaIterable.getLastReadMarker()).isNull();
	}

	@Test
	public void storeSingleOperation() throws Exception {
		CsaManagePersistence operation = managePersistenceOp(renameStageRequest("stage1", "stage2"));

		String marker = store(operation);

		DcsaIterable dcsaIterable = read(null);
		assertThat(dcsaIterable.getLastReadMarker()).isEqualTo(marker);

		Iterator<CsaOperation> it = dcsaIterable.iterator();
		assertThat(it).isNotNull();

		assertThat(it.hasNext()).isTrue();
		CsaOperation storedOperation = it.next();

		assertEqualCsaOperations_IgnoreIds(storedOperation, operation);
	}

	@Test
	public void storeMultipleOperations() throws Exception {
		// Create two rename operations
		CsaManagePersistence renameOp1 = managePersistenceOp(renameStageRequest("stage1", "stage2"));
		CsaManagePersistence renameOp2 = managePersistenceOp(renameStageRequest("stage2", "stage3"));

		// Store the operations
		String firstMarker = store(renameOp1);
		String lastMarker = store(renameOp2);

		DcsaIterable dcsaIterable;
		Iterator<CsaOperation> it;

		// Check correct operations are read with marker

		dcsaIterable = read(firstMarker);

		assertThat(dcsaIterable.getLastReadMarker()).isEqualTo(lastMarker);

		it = dcsaIterable.iterator();
		assertThat(it).isNotNull();
		assertThat(it.hasNext()).isTrue();
		assertEqualCsaOperations_IgnoreIds(it.next(), renameOp2);

		// Check correct operations are read with marker

		dcsaIterable = read(null);
		assertThat(dcsaIterable.getLastReadMarker()).isEqualTo(lastMarker);

		it = dcsaIterable.iterator();
		assertThat(it).isNotNull();

		CsaManagePersistence[] originalRenameOps = { renameOp1, renameOp2 };
		for (int i = 0; i < 2; i++) {
			assertThat(it.hasNext()).isTrue();
			CsaOperation storedOperation = it.next();

			assertEqualCsaOperations_IgnoreIds(storedOperation, originalRenameOps[i]);
		}
	}

	@Test
	public void storeResourceBasedOperations() throws Exception {
		final String gmmlFileContent = storeResourceBasedOperationsFileContent();

		// Create GMML file resource
		Resource gmmlFileResource = Resource.createTransient(inputStreamProvider(gmmlFileContent));
		gmmlFileResource.setMimeType(DistributedCollaborativeSmoodAccess.TEXT_PLAIN_MIME_TYPE);
		gmmlFileResource.setFileSize((long) gmmlFileContent.length());

		CsaAppendDataManipulation operation = appendDataManipulation(gmmlFileResource);

		// Store GMML file resource
		String marker = store(operation);

		DcsaIterable dcsaIterable = read(null);
		assertThat(dcsaIterable.getLastReadMarker()).isEqualTo(marker);

		// Check correct operation is read

		Iterator<CsaOperation> it = dcsaIterable.iterator();
		assertThat(it).isNotNull();

		assertThat(it.hasNext()).isTrue();
		CsaOperation storedOperation = it.next();
		assertThat(storedOperation).isInstanceOf(CsaAppendDataManipulation.class);

		CsaAppendDataManipulation appendDataManipulation = (CsaAppendDataManipulation) storedOperation;
		Resource storedGmmlFileResource = appendDataManipulation.getPayload();
		assertThat(storedGmmlFileResource).isNotNull();

		assertThat(toString(storedGmmlFileResource)).isEqualTo(gmmlFileContent);
	}

	protected String storeResourceBasedOperationsFileContent() {
		return "DUMMY FILE CONTENT";
	}

	@Test
	public void storeAndReadResource() throws Exception {
		final String content1 = "DUMMY RESOURCE 1";
		final String content2 = "DUMMY RESOURCE 2";

		final String path1 = "res/res1";
		final String path2 = "res/res2";

		// Store binary resources
		String lastMarker;
		lastMarker = storeResource(path1, content1);
		lastMarker = storeResource(path2, content2);

		DcsaIterable dcsaIterable = read(null);
		assertThat(dcsaIterable.getLastReadMarker()).isEqualTo(lastMarker);

		// Check correct operation is read through iterator

		Iterator<CsaOperation> it = dcsaIterable.iterator();
		assertThat(it).isNotNull();

		assertNextIsStoreResource(it, path1, content1);
		assertNextIsStoreResource(it, path2, content2);

		// Check correct resource is retrieved by marker
		if (supportsReadResources()) {
			// read all the resources
			Map<String, Resource> resources = readResouces(path1, path2);
			assertResourceContent(resources.get(path1), content1);
			assertResourceContent(resources.get(path2), content2);

			// read a sub-set of all resources
			Map<String, Resource> single = readResouces(path1);
			assertResourceContent(single.remove(path1), content1);
			assertThat(single.keySet()).isEmpty();
		}
	}

	protected String storeResource(String path, String content) {
		Resource resource = Resource.createTransient(inputStreamProvider(content));
		CsaStoreResource op = storeResource(path, resource);
		return store(op);
	}

	// ##################################################################################
	// ## . . . . . . . . . . . . . . . . Asserts . . . . . . . . . . . . . . . . . . .##
	// ##################################################################################

	protected void assertNextIsStoreResource(Iterator<CsaOperation> it, String expectedPath, String expectedFileContent) throws Exception {
		assertThat(it.hasNext()).isTrue();
		CsaOperation storedOperation = it.next();
		assertThat(storedOperation).isInstanceOf(CsaStoreResource.class);

		CsaStoreResource storeResource = (CsaStoreResource) storedOperation;
		assertThat(storeResource.getResourceRelativePath()).isEqualTo(expectedPath);

		Resource storedResource = storeResource.getPayload();
		if (!InMemoryDcsaSharedStorage.TMP_ENABLE_LAZY_LOADING)
			assertResourceContent(storedResource, expectedFileContent);
	}

	protected void assertResourceContent(Resource storedResource, String expectedFileContent) throws Exception {
		assertThat(storedResource).isNotNull();
		assertThat(toString(storedResource)).isEqualTo(expectedFileContent);
	}

	// ##################################################################################
	// ## . . . . . . . . . . . . . . . . . Helpers . . . . . . . . . . . . . . . . . .##
	// ##################################################################################

	protected InputStreamProvider inputStreamProvider(String content) {
		byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
		return () -> new ByteArrayInputStream(bytes);
	}

	protected void assertEqualCsaOperations_IgnoreIds(CsaOperation storedOp, CsaOperation op) {
		assertIsNotNull(op, "operation cannot be null");
		assertIsNotNull(storedOp, "storedOperation cannot be null");

		assertAreEqual(storedOp.operationType(), op.operationType(), "operations have different type");

		assertEqualSimpleProperties(storedOp, op, "storedOpertion and operation");

		if (storedOp instanceof CsaManagePersistence) {
			assertEqualSimpleProperties(((CsaManagePersistence) storedOp).getPersistenceRequest(),
					((CsaManagePersistence) op).getPersistenceRequest(), "storedOpertion and operation's persistenceRequest");
			return;
		}

		if (storedOp instanceof CsaResourceBasedOperation) {
			assertEqualSimpleProperties(((CsaResourceBasedOperation) storedOp).getPayload(), ((CsaResourceBasedOperation) op).getPayload(),
					"storedOpertion and operation's persistenceRequest");
			return;
		}
	}

	private void assertEqualSimpleProperties(GenericEntity ge1, GenericEntity ge2, String desc) {
		for (Property p : ge1.entityType().getProperties())
			if (p.getType().isScalar())
				assertThat(p.<Object> get(ge1)).as(desc + " have different value for property: " + p.getName()).isEqualTo(p.get(ge2));
	}

	protected void assertIsNotNull(Object o, String desc) {
		assertThat(o).as(desc).isNotNull();
	}

	protected void assertAreEqual(Object o1, Object o2, String desc) {
		assertThat(o1).as(desc).isEqualTo(o2);
	}

	protected CsaManagePersistence managePersistenceOp(CollaborativePersistenceRequest persistenceRequest) {
		CsaManagePersistence result = CsaManagePersistence.T.create();
		result.setPersistenceRequest(persistenceRequest);

		return result;
	}

	protected CsaAppendDataManipulation appendDataManipulation(Resource payload) {
		CsaAppendDataManipulation result = CsaAppendDataManipulation.T.create();
		result.setPayload(payload);

		return result;
	}

	protected CsaStoreResource storeResource(String path, Resource payload) {
		CsaStoreResource result = CsaStoreResource.T.create();
		result.setResourceRelativePath(path);
		result.setPayload(payload);

		return result;
	}

	private String toString(Resource resource) throws IOException {
		try (InputStream inputStream = resource.openStream()) {
			return IOTools.slurp(inputStream, "UTF-8");
		}
	}

	// ##################################################################################
	// ## . . . . . . . . Read / Write methods using DcsaSharedStorage . . . . . . . . ##
	// ##################################################################################

	protected DcsaIterable read(String lastReadMarker) {
		DcsaIterable result = sharedStorage().readOperations(ACCESS_ID, lastReadMarker);
		assertThat(result).isNotNull();

		return result;
	}

	protected Map<String, Resource> readResouces(String... paths) {
		Map<String, Resource> resouceMap = sharedStorage().readResource(ACCESS_ID, asList(paths));

		for (String path : paths) {
			assertThat(resouceMap).containsKey(path);
			assertThat(resouceMap.get(path)).isNotNull();
		}

		return resouceMap;
	}

	protected String store(CsaOperation operation) {
		requireNonNull(operation, "Error in test. Attempting to write null operation.");
		return sharedStorage().storeOperation(ACCESS_ID, operation);
	}

	// ##################################################################################
	// ## . . . . . . . . . . . . . Acquiring shared storage . . . . . . . . . . . . . ##
	// ##################################################################################

	private DcsaSharedStorage sharedStorage;

	protected DcsaSharedStorage sharedStorage() {
		if (sharedStorage == null)
			sharedStorage = newDcsaSharedStorage();

		return sharedStorage;
	}

	protected abstract DcsaSharedStorage newDcsaSharedStorage();

}
