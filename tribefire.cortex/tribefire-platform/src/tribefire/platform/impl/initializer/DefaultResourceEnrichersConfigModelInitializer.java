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
package tribefire.platform.impl.initializer;

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static tribefire.module.wire.contract.HardwiredResourceProcessorsContract.defaultResourceEnrichersConfigModel;

import com.braintribe.model.extensiondeployment.ResourceEnricher;
import com.braintribe.model.extensiondeployment.meta.PreEnrichResourceWith;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.ManipulationPersistenceException;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.collaboration.SimplePersistenceInitializer;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.resource.specification.ImageSpecification;

import tribefire.module.wire.contract.HardwiredResourceProcessorsContract;
import tribefire.platform.impl.streaming.MimeBasedDispatchingResourceEnricher;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;

/**
 * This creates the {@link HardwiredResourceProcessorsContract#defaultResourceEnrichersConfigModel} model, adds
 * basic-resource-model as it's dependency and places {@link PreEnrichResourceWith} MD with standard
 * pre-persistence-enricher (which among other things detects mime types) and a
 * {@link MimeBasedDispatchingResourceEnricher} for the custom enrichers bound from modules.
 * <p>
 * This initializer is internal usage, can be extracted at any time!
 * 
 * @author peter.gazdik
 */
public class DefaultResourceEnrichersConfigModelInitializer extends SimplePersistenceInitializer {

	@Override
	public void initializeData(PersistenceInitializationContext context) throws ManipulationPersistenceException {
		ManagedGmSession session = context.getSession();

		GmMetaModel configModel = createModel(session);

		configureDefaultEnrichers(session, configModel);
	}

	private GmMetaModel createModel(ManagedGmSession session) {
		GmMetaModel configModel = session.create(GmMetaModel.T, Model.modelGlobalId(defaultResourceEnrichersConfigModel));
		configModel.setName(defaultResourceEnrichersConfigModel);
		configModel.setVersion("1.0");
		configModel.getDependencies().add(basicResourceModel(session));

		return configModel;
	}

	private GmMetaModel basicResourceModel(ManagedGmSession session) {
		return session.getEntityByGlobalId(ImageSpecification.T.getModel().globalId());
	}

	private void configureDefaultEnrichers(ManagedGmSession session, GmMetaModel configModel) {
		ResourceEnricher mimeTypeDetector = session.getEntityByGlobalId(ResourceAccessSpace.mimeTypeDetectingEnricherGlobalId);
		ResourceEnricher mimeBasedEnricher = session.getEntityByGlobalId(MimeBasedDispatchingResourceEnricher.globalId);
		ResourceEnricher standardSpecificationEnricher = session.getEntityByGlobalId(ResourceAccessSpace.standardSpecificationEnricherGlobalId);

		PreEnrichResourceWith enrichWith = session.create(PreEnrichResourceWith.T, "md:default-resource-pre-enriche-with");
		enrichWith.setImportant(true);
		enrichWith.setConflictPriority(100d);
		enrichWith.getPrePersistenceEnrichers().addAll(//
				asList( //
						mimeTypeDetector, //
						mimeBasedEnricher, //
						standardSpecificationEnricher
				));

		ModelMetaDataEditor mdEditor = BasicModelMetaDataEditor.create(configModel).withSession(session).done();
		mdEditor.onEntityType(ResourceSource.T).addMetaData(enrichWith);
	}

}
