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
package com.braintribe.model.processing.accessory.test.cortex;

import static com.braintribe.model.generic.reflection.Model.modelGlobalId;

import com.braintribe.model.accessdeployment.CollaborativeAccess;
import com.braintribe.model.extensiondeployment.meta.StreamWith;
import com.braintribe.model.extensiondeployment.meta.UploadWith;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.components.AccessModelExtension;
import com.braintribe.model.meta.data.constraint.Mandatory;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.accessory.test.wire.space.MaTestSpace;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

/**
 * @author peter.gazdik
 */
public class MaInit_4A_EssentialAndNonEssentialMd extends SimplePersistenceInitializer implements MaTestConstants {

	/**
	 * This initializes essential and non-essential model MD, directly on cortex data model as well as via extension MD.
	 * <p>
	 * {@link Mandatory} and {@link Visible} are both essential.
	 * <p>
	 * {@link UploadWith} and {@link StreamWith} are non-essential.
	 * <p>
	 * The point is that using the persistence perspective those non-essential MD are cut off/
	 * 
	 * @see MaTestSpace#mdPerspectiveRegistry
	 */
	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		ManagedGmSession session = context.getSession();

		GmMetaModel cortexDataModel = session.getEntityByGlobalId(modelGlobalId(CORTEX_MODEL_NAME));
		GmMetaModel perspectiveRelatedMdModel = perspectiveRelatedMdModel(session);

		UseCaseSelector perspectiveSelector = perspectiveSelector(session);

		ModelMetaDataEditor mdEditor;

		mdEditor = BasicModelMetaDataEditor.create(perspectiveRelatedMdModel).withSession(session).done();
		mdEditor.addModelMetaData( //
				md(Mandatory.T, session, perspectiveSelector), // essential
				md(UploadWith.T, session, perspectiveSelector) // non-essential
		);

		mdEditor = BasicModelMetaDataEditor.create(cortexDataModel).withSession(session).done();

		mdEditor.addModelMetaData( //
				md(Visible.T, session, perspectiveSelector), // essential
				md(StreamWith.T, session, perspectiveSelector) // non-essential
		);

		// CollaborativeAccess gets a special BinaryRetrieval
		mdEditor.onEntityType(CollaborativeAccess.T) //
				.addMetaData(accessModelExtensions(session, perspectiveRelatedMdModel));

	}

	private GmMetaModel perspectiveRelatedMdModel(ManagedGmSession session) {
		GmMetaModel result = session.create(GmMetaModel.T);
		result.setName("synthetic:perspective-related-mdd-model");
		result.getDependencies().add(session.getEntityByGlobalId(GenericEntity.T.getModel().globalId()));

		return result;
	}

	private UseCaseSelector perspectiveSelector(ManagedGmSession session) {
		UseCaseSelector result = session.create(UseCaseSelector.T);
		result.setUseCase("perspective");
		return result;
	}

	private <T extends MetaData> T md(EntityType<T> mdType, ManagedGmSession session, UseCaseSelector selector) {
		T result = session.create(mdType);
		result.setSelector(selector);
		return result;

	}

	private AccessModelExtension accessModelExtensions(ManagedGmSession session, GmMetaModel model) {
		AccessModelExtension result = session.create(AccessModelExtension.T);
		result.getModels().add(model);

		return result;
	}

}
