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
package com.braintribe.model.access.smood.collaboration.basic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.processing.session.api.collaboration.CollaborativeAccess;

/**
 * @see CollaborativeAccess
 * 
 * @author peter.gazdik
 */
public class CortexCsaTest extends AbstractCollaborativePersistenceTest {

	private static final String MODEL_NAME = "test:CsaTestModel";

	@Test
	public void creatingModelOnlyStoresInModelFile() {
		GmMetaModel model = session.create(GmMetaModel.T);
		model.setName(MODEL_NAME);
		session.commit();

		// @formatter:off
		baseFolderFsAssert
			.sub("config.json").isExistingFile_()
			.sub(trunkStageName).isDirectory()
				.sub("model.man").isExistingFile_()
				.sub("data.man").notExists_();
		// @formatter:on
	}

	@Test
	public void creatingModelWithMetaDataStoresInBothFiles() {
		Visible visible = session.create(Visible.T);

		GmMetaModel model = session.create(GmMetaModel.T);
		model.setName(MODEL_NAME);
		model.getMetaData().add(visible);
		session.commit();

		// @formatter:off
		baseFolderFsAssert
			.sub("config.json").isExistingFile_()
			.sub(trunkStageName).isDirectory()
				.sub("model.man").isExistingFile_()
				.sub("data.man").isExistingFile();
		// @formatter:on
	}

	@Test
	public void deletingModelCleansNonSkeletonProperties() {
		Visible visible = session.create(Visible.T);

		GmMetaModel model = session.create(GmMetaModel.T);
		model.setGlobalId(MODEL_NAME);
		model.getMetaData().add(visible);
		session.commit();

		session.deleteEntity(model);
		session.commit();

		model = session.create(GmMetaModel.T);
		model.setGlobalId(MODEL_NAME);
		session.commit();

		redeploy();

		model = session.findEntityByGlobalId(MODEL_NAME);

		assertThat(model).isNotNull();
		assertThat(model.getMetaData()).isEmpty();
	}
}
