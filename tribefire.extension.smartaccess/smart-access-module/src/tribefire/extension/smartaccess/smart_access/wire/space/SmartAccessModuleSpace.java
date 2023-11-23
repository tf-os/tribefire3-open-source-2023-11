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
package tribefire.extension.smartaccess.smart_access.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;

import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.smart.SmartAccess;
import com.braintribe.model.accessdeployment.smart.meta.PropertyAsIs;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileUploadSource;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.scripting.module.wire.contract.ScriptingContract;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.ModelApiContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * This module's javadoc is yet to be written.
 */
@Managed
public class SmartAccessModuleSpace implements TribefireModuleContract {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModelApiContract modelApi;

	@Import
	private ScriptingContract scripting;

	//
	// Deployables
	//

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		// NOTE this also ensures smart-access-deployment-model is part of cortex-model
		bindings.bind(com.braintribe.model.accessdeployment.smart.SmartAccess.T) //
				.component(tfPlatform.binders().incrementalAccess()) //
				.expertFactory(this::access);
	}

	@Managed
	public SmartAccess access(ExpertContext<com.braintribe.model.accessdeployment.smart.SmartAccess> context) {
		com.braintribe.model.accessdeployment.smart.SmartAccess deployable = context.getDeployable();

		SmartAccess bean = new SmartAccess();
		bean.setAccessMapping(accessMapping(context));
		bean.setSmartDenotation(deployable);
		bean.setAccessId(deployable.getExternalId());
		bean.setMetaModel(deployable.getMetaModel());
		bean.setScriptingEngineResolver(scripting.scriptingEngineResolver());

		return bean;

	}

	protected Map<com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess> accessMapping(
			ExpertContext<com.braintribe.model.accessdeployment.smart.SmartAccess> context) {

		com.braintribe.model.accessdeployment.smart.SmartAccess deployable = context.getDeployable();

		Map<com.braintribe.model.accessdeployment.IncrementalAccess, IncrementalAccess> accessMapping = new IdentityHashMap<>();
		for (com.braintribe.model.accessdeployment.IncrementalAccess incrementalAccess : deployable.getDelegates()) {
			IncrementalAccess access = context.resolve(incrementalAccess, com.braintribe.model.accessdeployment.IncrementalAccess.T);
			accessMapping.put(incrementalAccess, access);
		}

		return accessMapping;
	}

	//
	// Initializers
	//

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind(this::initialize);
	}

	private void initialize(PersistenceInitializationContext ctx) {
		new SmartAccessIntialization(ctx).run();
	}

	private class SmartAccessIntialization {

		private final ManagedGmSession session;

		public SmartAccessIntialization(PersistenceInitializationContext ctx) {
			this.session = ctx.getSession();
		}

		public void run() {
			mapPartitionAsIs();
			configureIcon();
		}

		private void mapPartitionAsIs() {
			GmMetaModel rootModel = session.getEntityByGlobalId(GenericEntity.T.getModel().globalId());
			ModelMetaDataEditor mdEditor = modelApi.newMetaDataEditor(rootModel).done();

			PropertyAsIs propertyAsIsMd = session.create(PropertyAsIs.T, "540502f8-6f5b-4873-af07-0be3259dca30");

			mdEditor.onEntityType(GenericEntity.T).addPropertyMetaData(GenericEntity.partition, propertyAsIsMd);
		}

		private void configureIcon() {
			String cortexModelName = "tribefire.cortex:tribefire-cortex-model";
			GmMetaModel cortexModel = session.getEntityByGlobalId(Model.modelGlobalId(cortexModelName));
			cortexModel.getDependencies().add(configModel());
		}

		private GmMetaModel configModel() {
			String configModelName = "tribefire.extension.smartaccess:synthetic-smart-access-configuration-model";

			GmMetaModel deploymentModel = session
					.getEntityByGlobalId(com.braintribe.model.accessdeployment.smart.SmartAccess.T.getModel().globalId());

			GmMetaModel configModel = session.create(GmMetaModel.T, Model.modelGlobalId(configModelName));
			configModel.setName(configModelName);
			configModel.setVersion("1.0");
			configModel.getDependencies().add(deploymentModel);

			ModelMetaDataEditor mdEditor = modelApi.newMetaDataEditor(configModel).done();
			mdEditor.onEntityType(com.braintribe.model.accessdeployment.smart.SmartAccess.T) //
					.addMetaData(entityIcon());
			return configModel;
		}

		private Icon entityIcon() {
			Icon bean = session.create(Icon.T, "smart:meta:icon");
			bean.setIcon(adaptiveIcon());
			return bean;
		}

		private AdaptiveIcon adaptiveIcon() {
			AdaptiveIcon result = session.create(AdaptiveIcon.T, "smart:adaptive-icon");
			result.setName("SmartAccess Icon");
			result.setRepresentations(asSet( //
					icon("SmartAccess_16x16.png"), //
					icon("SmartAccess_24x24.png"), //
					icon("SmartAccess_32x32.png"), //
					icon("SmartAccess_64x64.png") //
			));

			return result;
		}

		private Resource icon(String fileName) {
			Path path = tfPlatform.resources().publicResources("smartaccess/" + fileName).asPath();

			FileUploadSource source = session.create(FileUploadSource.T, "smart:icon-resource-source:" + fileName);
			source.setLocalFilePath(path.toString());

			Resource result = session.create(Resource.T, "smart:icon-resource:" + fileName);
			result.setResourceSource(source);

			return result;
		}

	}

}
