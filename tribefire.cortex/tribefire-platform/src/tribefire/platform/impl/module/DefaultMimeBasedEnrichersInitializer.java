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
package tribefire.platform.impl.module;

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.requireNonEmpty;
import static tribefire.module.wire.contract.HardwiredResourceProcessorsContract.defaultResourceEnrichersConfigModel;

import java.util.Set;

import com.braintribe.model.extensiondeployment.HardwiredResourceEnricher;
import com.braintribe.model.extensiondeployment.meta.MimeBasedResourceEnriching;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.session.api.collaboration.DataInitializer;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;

import tribefire.cortex.initializer.tools.ModelInitializingTools;
import tribefire.platform.impl.initializer.DefaultResourceEnrichersConfigModelInitializer;

/**
 * This initializer adds additional configuration - {@link MimeBasedResourceEnriching} for given hardwired mime-based enricher - to the default
 * resource enriching configuration model, defined by {@link DefaultResourceEnrichersConfigModelInitializer}.
 * 
 * @author peter.gazdik
 */
/* package */ class DefaultMimeBasedEnrichersInitializer implements DataInitializer {

	private final HardwiredResourceEnricher deployable;
	private final Set<String> mimeTypes;
	private final Set<Model> models;

	public DefaultMimeBasedEnrichersInitializer(HardwiredResourceEnricher deployable, Set<String> mimeTypes, Set<Model> models) {
		this.deployable = deployable;
		this.mimeTypes = requireNonEmpty(mimeTypes, "You need to specify mime-types for a mime based enricher.");
		this.models = models;
	}

	@Override
	public void initialize(PersistenceInitializationContext context) {
		ManagedGmSession session = context.getSession();

		GmMetaModel configModel = session.getEntityByGlobalId(Model.modelGlobalId(defaultResourceEnrichersConfigModel));

		if (!isEmpty(models))
			ModelInitializingTools.extendModelToCoverModels(session, configModel, models);

		HardwiredResourceEnricher hwEnricher = session.getEntityByGlobalId(deployable.getGlobalId());

		MimeBasedResourceEnriching md = session.create( //
				MimeBasedResourceEnriching.T, "md:hardwired-resource-enricher/" + deployable.getExternalId());

		md.setMimeTypes(mimeTypes);
		md.setResourceEnricher(hwEnricher);

		configModel.getMetaData().add(md);
	}

}
