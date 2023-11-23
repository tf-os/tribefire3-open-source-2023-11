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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.integration.etcd.supplier.ClientSupplier;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaAppendDataManipulation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaOperation;
import com.braintribe.testing.category.SpecialEnvironment;

@Category(SpecialEnvironment.class)
public class EtcdDcsaSharedStorageTest {

	protected final static List<String> endpointUrls = List.of("http://localhost:2379");

	@Test
	public void testDcsaStorage() throws Exception {

		EtcdDcsaSharedStorage storage = new EtcdDcsaSharedStorage();
		storage.setProject("storage-test-" + UUID.randomUUID().toString());
		// TODO: add authentication case here if needed
		storage.setClientSupplier(new ClientSupplier(endpointUrls, null, null));
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		storage.setMarshaller(marshaller);
		storage.setTtlInSeconds(60);

		String accessId = UUID.randomUUID().toString();

		CsaAppendDataManipulation csaOperation = CsaAppendDataManipulation.T.create();
		csaOperation.setId(UUID.randomUUID().toString());

		String revisionAfterFirstWrite = storage.storeOperation(accessId, csaOperation);

		assertThat(revisionAfterFirstWrite).isNotNull();
		assertThat(revisionAfterFirstWrite).isNotEmpty();
		assertThat(revisionAfterFirstWrite).isEqualTo("0000000000000001");

		System.out.println("Revision after 1st write: " + revisionAfterFirstWrite);

		CsaAppendDataManipulation csaOperation2 = CsaAppendDataManipulation.T.create();
		csaOperation2.setId(UUID.randomUUID().toString());
		String revisionAfterSecondWrite = storage.storeOperation(accessId, csaOperation2);

		assertThat(revisionAfterSecondWrite).isNotNull();
		assertThat(revisionAfterSecondWrite).isNotEmpty();
		assertThat(revisionAfterSecondWrite).isEqualTo("0000000000000002");

		assertThat(getNumericalPartOfKey(revisionAfterSecondWrite)).isGreaterThan(getNumericalPartOfKey(revisionAfterFirstWrite));

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

	protected long getNumericalPartOfKey(String key) {
		int index = key.lastIndexOf('/');
		return Long.parseLong(key.substring(index + 1));
	}
}
