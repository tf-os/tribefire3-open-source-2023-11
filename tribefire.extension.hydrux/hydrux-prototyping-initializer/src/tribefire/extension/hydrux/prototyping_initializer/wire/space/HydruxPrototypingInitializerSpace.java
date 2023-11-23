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
package tribefire.extension.hydrux.prototyping_initializer.wire.space;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.hydrux.model.data.HxNamedEntity;
import tribefire.extension.hydrux.model.deployment.HxApplication;
import tribefire.extension.hydrux.model.deployment.HxScope;
import tribefire.extension.hydrux.model.deployment.prototyping.HxMainView;
import tribefire.extension.hydrux.prototyping_initializer.wire.contract.HydruxPrototypingInitializerContract;
import tribefire.module.wire.contract.ModelApiContract;

@Managed
public class HydruxPrototypingInitializerSpace extends AbstractInitializerSpace implements HydruxPrototypingInitializerContract {

	private static final String PROTOTYPING_ACCESS_ID = HxMainView.PROTOTYPING_DOMAIN_ID;

	@Import
	private ModelApiContract modelApi;

	@Managed
	@Override
	public CollaborativeSmoodAccess access() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		bean.setExternalId(PROTOTYPING_ACCESS_ID);
		bean.setName("Hydrux Prototyping Access");
		bean.setMetaModel(configuredDataModel());
		bean.setServiceModel(configuredServiceModel());

		return bean;
	}

	@Managed
	private GmMetaModel configuredDataModel() {
		GmMetaModel bean = create(GmMetaModel.T);
		bean.setName("tribefire.extension.hydrux:hydrux-prototyping-configured-data-model");
		bean.getDependencies().add(modelOf(HxNamedEntity.T));

		return bean;
	}

	@Managed
	private GmMetaModel configuredServiceModel() {
		GmMetaModel bean = create(GmMetaModel.T);
		bean.setName("tribefire.extension.hydrux:hydrux-prototyping-configured-service-model");
		bean.getDependencies().add(serviceApiModel());

		configureHxMetaData(bean);

		return bean;
	}

	private GmMetaModel serviceApiModel() {
		return modelOf(ServiceRequest.T);
	}

	private void configureHxMetaData(GmMetaModel model) {
		ModelMetaDataEditor mdEditor = modelApi.newMetaDataEditor(model).done();
		mdEditor.addModelMetaData(defaultHxApplication());
	}

	@Managed
	private HxApplication defaultHxApplication() {
		HxApplication bean = create(HxApplication.T);
		bean.setRootScope(hxDefaultScope());
		bean.setTitle("Hydrux Prototyping Demo Application");
		bean.setApplicationId("hx-demo-default");
		bean.setView(hxMainView());
		bean.setConflictPriority(-100d);

		return bean;
	}

	@Managed
	private HxMainView hxMainView() {
		HxMainView bean = create(HxMainView.T);
		// we do not set the module, that must be set dynamically, because this must work with any module

		return bean;
	}

	@Managed
	private HxScope hxDefaultScope() {
		HxScope bean = create(HxScope.T);
		bean.setName("root");

		return bean;
	}

	private GmMetaModel modelOf(EntityType<?> type) {
		return initializerSupport.session().getEntityByGlobalId(type.getModel().globalId());
	}

}
