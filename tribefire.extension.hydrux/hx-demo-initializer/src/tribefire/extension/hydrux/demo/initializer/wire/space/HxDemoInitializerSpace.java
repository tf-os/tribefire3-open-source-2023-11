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
package tribefire.extension.hydrux.demo.initializer.wire.space;

import com.braintribe.model.accessdeployment.smood.CollaborativeSmoodAccess;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.hydrux.demo.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.hydrux.demo.initializer.wire.contract.HxDemoInitializerContract;
import tribefire.extension.hydrux.demo.model.data.HxDemoEntity;
import tribefire.extension.hydrux.demo.model.ux.deployment.HxDemoAccessView;
import tribefire.extension.hydrux.demo.model.ux.deployment.HxDemoDefaultView;
import tribefire.extension.hydrux.demo.model.ux.deployment.HxDemoErrorsView;
import tribefire.extension.hydrux.demo.model.ux.deployment.HxDemoModalDialogView;
import tribefire.extension.hydrux.demo.model.ux.deployment.HxDemoPushNotificationsView;
import tribefire.extension.hydrux.demo.model.ux.deployment.HxDemoServiceProcessorView;
import tribefire.extension.hydrux.demo.model.ux.deployment.HxDemoStaticPageView;
import tribefire.extension.hydrux.model.deployment.HxApplication;
import tribefire.extension.hydrux.model.deployment.HxScope;
import tribefire.module.wire.contract.ModelApiContract;

@Managed
public class HxDemoInitializerSpace extends AbstractInitializerSpace implements HxDemoInitializerContract {

	private static final String HX_DEMO_ACCESS_ID = "access.hx-demo";

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private ModelApiContract modelApi;

	@Managed
	@Override
	public CollaborativeSmoodAccess access() {
		CollaborativeSmoodAccess bean = create(CollaborativeSmoodAccess.T);
		bean.setExternalId(HX_DEMO_ACCESS_ID);
		bean.setName("Hx Demo Access");
		bean.setMetaModel(configuredDataModel());
		bean.setServiceModel(configuredServiceModel());

		return bean;
	}

	@Managed
	private GmMetaModel configuredDataModel() {
		GmMetaModel bean = create(GmMetaModel.T);
		bean.setName("tribefire.extension.hydrux:hx-demo-configured-data-model");
		bean.getDependencies().add(modelOf(HxDemoEntity.T));

		return bean;
	}

	@Managed
	private GmMetaModel configuredServiceModel() {
		GmMetaModel bean = create(GmMetaModel.T);
		bean.setName("tribefire.extension.hydrux:hx-demo-configured-service-model");
		bean.getDependencies().add(serviceApiModel());

		configureHxMetaData(bean);

		return bean;
	}

	private GmMetaModel serviceApiModel() {
		return modelOf(ServiceRequest.T);
	}

	private void configureHxMetaData(GmMetaModel model) {
		ModelMetaDataEditor mdEditor = modelApi.newMetaDataEditor(model).done();
		mdEditor.addModelMetaData( //
				defaultHxApplication(), //
				staticHxApplication(), //
				serviceProcessorHxApplication(), //
				accessHxApplication(), //
				errorsHxApplication(), //
				pushNotificationsHxApplication(), //
				modalDialogHxApplication() //
		);
	}

	@Managed
	private HxApplication defaultHxApplication() {
		HxApplication bean = createHxApplicationWithDefaultScope();
		bean.setTitle("Hx Demo Default App");
		bean.setApplicationId("hx-demo-default");
		bean.setView(hxDefaultView());
		bean.setConflictPriority(-100d);

		return bean;
	}

	@Managed
	private HxDemoDefaultView hxDefaultView() {
		HxDemoDefaultView bean = create(HxDemoDefaultView.T);
		bean.setModule(existingInstances.hxComponentModule());

		return bean;
	}

	@Managed
	private HxApplication staticHxApplication() {
		HxApplication bean = createHxApplicationWithDefaultScope();
		bean.setTitle("Hx Demo Static App");
		bean.setApplicationId("hx-demo-static");
		bean.setView(hxStaticView());
		bean.setSelector(staticUseCase());

		return bean;
	}

	@Managed
	private HxDemoStaticPageView hxStaticView() {
		HxDemoStaticPageView bean = create(HxDemoStaticPageView.T);
		bean.setModule(existingInstances.hxComponentModule());
		bean.setTextColor("black");
		bean.setBackgroundColor("orange");

		return bean;
	}

	@Managed
	private UseCaseSelector staticUseCase() {
		UseCaseSelector bean = create(UseCaseSelector.T);
		bean.setUseCase("static");

		return bean;
	}

	// ServiceProcessor

	@Managed
	private HxApplication serviceProcessorHxApplication() {
		HxApplication bean = createHxApplicationWithDefaultScope();
		bean.setTitle("Hx Demo Service Processor App");
		bean.setApplicationId("hx-demo-service-processing");
		bean.setView(hxServiceProcessorView());
		bean.setSelector(serviceProcessorUseCase());

		return bean;
	}

	@Managed
	private HxDemoServiceProcessorView hxServiceProcessorView() {
		HxDemoServiceProcessorView bean = create(HxDemoServiceProcessorView.T);
		bean.setModule(existingInstances.hxComponentModule());

		return bean;
	}

	@Managed
	private UseCaseSelector serviceProcessorUseCase() {
		UseCaseSelector bean = create(UseCaseSelector.T);
		bean.setUseCase("service");

		return bean;
	}

	// Access

	@Managed
	private HxApplication accessHxApplication() {
		HxApplication bean = createHxApplicationWithDefaultScope();
		bean.setTitle("Hx Demo Access App");
		bean.setApplicationId("hx-demo-access");
		bean.setView(hxAccessView());
		bean.setSelector(accessUseCase());

		return bean;
	}

	@Managed
	private HxDemoAccessView hxAccessView() {
		HxDemoAccessView bean = create(HxDemoAccessView.T);
		bean.setModule(existingInstances.hxComponentModule());
		bean.setMaxRows(5);

		return bean;
	}

	@Managed
	private UseCaseSelector accessUseCase() {
		UseCaseSelector bean = create(UseCaseSelector.T);
		bean.setUseCase("access");

		return bean;
	}

	private MetaData errorsHxApplication() {
		HxApplication bean = createHxApplicationWithDefaultScope();
		bean.setTitle("Hx Demo Errors App");
		bean.setApplicationId("hx-demo-errors");
		bean.setView(hxErrorsView());
		bean.setSelector(errorsUseCase());

		return bean;
	}

	@Managed
	private HxDemoErrorsView hxErrorsView() {
		HxDemoErrorsView bean = create(HxDemoErrorsView.T);
		bean.setModule(existingInstances.hxComponentModule());

		return bean;
	}

	@Managed
	private UseCaseSelector errorsUseCase() {
		UseCaseSelector bean = create(UseCaseSelector.T);
		bean.setUseCase("errors");

		return bean;
	}

	private MetaData pushNotificationsHxApplication() {
		HxApplication bean = createHxApplicationWithDefaultScope();
		bean.setTitle("Hx Demo PushNotifications App");
		bean.setApplicationId("hx-demo-push-notifications");
		bean.setView(hxPushNotificationsView());
		bean.setSelector(pushNotificationsUseCase());

		return bean;
	}

	@Managed
	private HxDemoPushNotificationsView hxPushNotificationsView() {
		HxDemoPushNotificationsView bean = create(HxDemoPushNotificationsView.T);
		bean.setModule(existingInstances.hxComponentModule());

		return bean;
	}

	@Managed
	private UseCaseSelector pushNotificationsUseCase() {
		UseCaseSelector bean = create(UseCaseSelector.T);
		bean.setUseCase("pushNotifications");

		return bean;
	}

	private MetaData modalDialogHxApplication() {
		HxApplication bean = createHxApplicationWithDefaultScope();
		bean.setTitle("Hx Demo ModalDialog App");
		bean.setApplicationId("hx-demo-modal-dialog");
		bean.setView(modalDialogView());
		bean.setSelector(modalDialogUseCase());

		return bean;
	}

	@Managed
	private HxDemoModalDialogView modalDialogView() {
		HxDemoModalDialogView bean = create(HxDemoModalDialogView.T);
		bean.setModule(existingInstances.hxComponentModule());

		return bean;
	}

	@Managed
	private UseCaseSelector modalDialogUseCase() {
		UseCaseSelector bean = create(UseCaseSelector.T);
		bean.setUseCase("modalDialog");

		return bean;
	}

	private HxApplication createHxApplicationWithDefaultScope() {
		HxApplication bean = create(HxApplication.T);
		bean.setRootScope(hxDefaultScope());

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
