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
package com.braintribe.model.processing.smood;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.access.impl.XmlAccess;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.provider.Holder;

public class Smood_Cortex_Test {

	protected Smood cortexSmood = null;

	@Ignore
	public long initialize() throws Exception {

		File file = new File("/Applications/Braintribe/tribefire/storage/databases/cortex/data/initial.xml");

		if (!file.exists()) {
			this.cortexSmood = null;
			return -1;
		}

		GmMetaModel cortexModel = new NewMetaModelGeneration().buildMetaModel("cortex", Collections.emptySet());

		XmlAccess initialStorage = new XmlAccess();
		initialStorage.setFilePath(file);
		initialStorage.setModelProvider(new Holder<GmMetaModel>(cortexModel));

		Object genericModelValue = initialStorage.loadModel();

		cortexSmood = new Smood(EmptyReadWriteLock.INSTANCE);
		cortexSmood.setAccessId("cortex");
		long start = System.currentTimeMillis();
		cortexSmood.initialize(genericModelValue);
		// cortexSmood.initializePopulation((Set<GenericEntity>) genericModelValue, true);
		long stop = System.currentTimeMillis();
		cortexSmood.setMetaModel(cortexModel);

		return (stop - start);

	}

	@Test
	public void testCortexDb() throws Exception {
		int count = 100;
		int queryCount = 100;

		long totalInitTime = 0L;
		long totalQueryTime = 0L;

		this.initialize();
		if (this.cortexSmood == null) {
			return;
		}
		int size = 0;

		for (int i = 0; i < count; ++i) {

			totalInitTime += this.initialize();

			EntityQuery eq = EntityQueryBuilder.from(GenericEntity.class).done();
			for (int j = 0; j < queryCount; ++j) {
				long start = System.currentTimeMillis();
				List<GenericEntity> list = cortexSmood.queryEntities(eq).getEntities();
				long stop = System.currentTimeMillis();
				size = list.size();
				totalQueryTime += (stop - start);
			}
		}
		System.out.println("Initialization times " + count + " took " + totalInitTime + " ms");
		System.out.println("Query times " + (count * queryCount) + " took " + totalQueryTime + " ms (result size: " + size + ")");

	}

	@Ignore
	public static void main(String[] args) throws Exception {
		Smood_Cortex_Test app = new Smood_Cortex_Test();
		app.testCortexDb();
	}
}
