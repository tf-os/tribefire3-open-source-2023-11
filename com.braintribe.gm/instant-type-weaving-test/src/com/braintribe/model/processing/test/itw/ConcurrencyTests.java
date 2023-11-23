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
package com.braintribe.model.processing.test.itw;

import static com.braintribe.model.generic.builder.meta.MetaModelBuilder.metaModel;

import java.util.concurrent.CountDownLatch;

import org.junit.Rule;
import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.test.itw.build.GmEntityBuilder;
import com.braintribe.utils.junit.core.rules.ConcurrentRule;

/**
 * 
 */
public class ConcurrencyTests extends ImportantItwTestSuperType {

	private static final int THREADS = 30;

	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule(THREADS);

	private final CountDownLatch cdl = new CountDownLatch(THREADS);
	private int counter = 1;

	@Test
	public void testConcurrency() throws Exception {
		GmMetaModel gmMetaModel = getMetaModel();

		cdl.countDown();
		cdl.await();

		gmMetaModel.deploy();
	}

	private GmEntityBuilder sub;
	private GmEntityBuilder supper; // super is keyword...

	private synchronized GmMetaModel getMetaModel() {
		GmEntityBuilder ge = new GmEntityBuilder(GenericEntity.class.getName());
		ge.setIsAbstract(true);

		GmEntityBuilder sge = new GmEntityBuilder(GenericEntity.class.getName() + "Sub");
		sge.setIsAbstract(false).addSuper(ge);

		GmMetaModel metaModel = metaModel();

		int entityId = this.counter++;

		supper = new GmEntityBuilder("com.braintribe.model.processing.test.itw.concurrent.Entity" + entityId);
		sub = new GmEntityBuilder("com.braintribe.model.processing.test.itw.concurrent.SubEntity" + entityId);

		supper.addSuper(sge);
		supper.addSuper(ge);
		supper.addProperty("myself", supper);
		supper.addProperty("sub", sub);

		sub.addSuper(supper.gmEntityType());

		supper.addToMetaModel(metaModel);
		sub.addToMetaModel(metaModel);

		return metaModel;
	}

	protected <T extends GenericEntity> T instantiate(Class<T> beanClass) {
		EntityType<GenericEntity> entityType = GMF.getTypeReflection().getEntityType(beanClass);
		return beanClass.cast(entityType.create());
	}

}
