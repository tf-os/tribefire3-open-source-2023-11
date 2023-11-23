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
package com.braintribe.model.access.smood.distributed.test.concurrent.tester;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.smood.distributed.test.concurrent.entities.MultiExtensionType;
import com.braintribe.model.access.smood.distributed.test.concurrent.entities.SingleExtensionType;
import com.braintribe.model.access.smood.distributed.test.concurrent.worker.WorkerFactory;
import com.braintribe.model.access.smood.distributed.test.wire.contract.DistributedSmoodAccessTestContract;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.user.User;

public abstract class AbstractSmoodDbAccessTest {

	protected WorkerFactory factory = null;
	protected DistributedSmoodAccessTestContract space = null;
	protected int workerCount = 1;

	public void executeTest() throws Exception {
		
		space.utils().clearTables();

		IncrementalAccess access = space.concurrentAccess(); 
		PersistenceGmSession session = space.utils().openSession(access);
		
		int iterations = this.factory.getIterations();
		int expectedCount = workerCount * iterations;

		long start = System.currentTimeMillis();
		this.executeWorkers(workerCount, iterations);
		long duration = System.currentTimeMillis() - start;

		List<User> users = space.utils().getUsers(session, "Mustermann");
		int finalCount = users.size();
		Assert.assertEquals(expectedCount, finalCount);
		System.out.println("Test was OK ("+finalCount+" equals "+expectedCount+") after "+duration+" ms");
		
		// Test ClassDataStorage interface
		
		Set<String> qualifiedNames = space.utils().getClassDataStorage().getQualifiedNamesOfStoredClasses();
		System.out.println("Known types:");
		System.out.println(qualifiedNames);
		Assert.assertEquals(workerCount+1, qualifiedNames.size());
		
		String entityTypeSignatureSingle = SingleExtensionType.class.getName()+"_runtime";
		EntityType<GenericEntity> entityTypeSingle = GMF.getTypeReflection().getEntityType(entityTypeSignatureSingle);
		
		List<GenericEntity> list = session.query().entities(EntityQueryBuilder.from(entityTypeSingle).done()).list();
		Assert.assertEquals(workerCount, list.size());
		
		for (GenericEntity ge : list) {
			Object value = entityTypeSingle.getProperty("name").get(ge);
			System.out.println(value);
		}
		this.assertSingleEntityListValidity(list, entityTypeSingle);
		
		for (int i=0; i<workerCount; ++i) {
			String entityTypeSignatureMulti = MultiExtensionType.class.getName()+"_runtime_"+i;
			EntityType<GenericEntity> entityTypeMulti = GMF.getTypeReflection().getEntityType(entityTypeSignatureMulti);

			list = session.query().entities(EntityQueryBuilder.from(entityTypeMulti).done()).list();
			Assert.assertEquals(1, list.size());
			GenericEntity ge = list.get(0);
			Object value = entityTypeMulti.getProperty("name").get(ge);
			Assert.assertEquals(""+i, value.toString());
		}
	}
	
	protected void assertSingleEntityListValidity(List<GenericEntity> list, EntityType<GenericEntity> entityType) throws AssertionError {
		
		Set<String> expectedSet = new HashSet<String>();
		for (int i=0; i<workerCount; ++i) {
			expectedSet.add(""+i);
		}
		for (GenericEntity ge : list) {
			Object value = entityType.getProperty("name").get(ge);
			String valueString = (String) value;
			Assert.assertEquals(true, expectedSet.remove(valueString));
		}
		Assert.assertEquals(0, expectedSet.size());
	}

	protected abstract void executeWorkers(int workerCountParam, int iterations) throws Exception;

	@Required
	public void setFactory(WorkerFactory factory) {
		this.factory = factory;
	}
	
	public int getWorkerCount() {
		return workerCount;
	}
	@Configurable
	public void setWorkerCount(int workerCount) {
		this.workerCount = workerCount;
	}
	public DistributedSmoodAccessTestContract getSpace() {
		return space;
	}
	@Configurable
	@Required
	public void setSpace(DistributedSmoodAccessTestContract space) {
		this.space = space;
	}

}
