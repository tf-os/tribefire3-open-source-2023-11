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
package tribefire.extension.hibernate.wire.space;

import static com.braintribe.wire.api.util.Sets.set;

import java.nio.file.Path;
import java.util.function.Supplier;

import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateSessionFactory;
import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.components.AccessModelExtension;
import com.braintribe.model.meta.data.components.ServiceModelExtension;
import com.braintribe.model.meta.data.display.Icon;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.persistence.NativePersistenceRequest;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.resource.AdaptiveIcon;
import com.braintribe.model.resource.FileResource;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.FileUploadSource;
import com.braintribe.model.service.api.callback.AsynchronousRequestProcessorCallback;
import com.braintribe.model.service.api.callback.AsynchronousRequestRestCallback;
import com.braintribe.model.service.persistence.ServiceRequestJob;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.ModelApiContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

/**
 * @author peter.gazdik
 */
@Managed
public class HibernateInitializerSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModelApiContract modelApi;

	public void bindInitializers(InitializerBindingBuilder bindings) {
		bindings.bind(this::initialize);
	}

	private void initialize(PersistenceInitializationContext ctx) {
		new HibernateAccessIntialization(ctx).run();
	}

	private class HibernateAccessIntialization {

		private final ManagedGmSession session;

		private final GmMetaModel cortexModel;

		public HibernateAccessIntialization(PersistenceInitializationContext ctx) {
			String cortexModelName = "tribefire.cortex:tribefire-cortex-model";

			this.session = ctx.getSession();
			this.cortexModel = session.getEntityByGlobalId(Model.modelGlobalId(cortexModelName));
		}

		public void run() {
			extendCortexModel();
			mapClobs();
			unmapFileResource();
		}

		private void extendCortexModel() {
			cortexModel.getDependencies().add(configModel());
		}

		private GmMetaModel configModel() {
			String configModelName = "tribefire.extension.hibernate:synthetic-hibernate-access-configuration-model";

			GmMetaModel deploymentModel = session.getEntityByGlobalId(HibernateAccess.T.getModel().globalId());

			GmMetaModel configModel = session.create(GmMetaModel.T, Model.modelGlobalId(configModelName));
			configModel.setName(configModelName);
			configModel.setVersion("1.0");
			configModel.getDependencies().add(deploymentModel);

			ModelMetaDataEditor mdEditor = modelApi.newMetaDataEditor(configModel).done();
			mdEditor.onEntityType(HibernateAccess.T) //
					.addMetaData(entityIcon()) //
					.addMetaData(extendDataModelWith_BasicHbmConfigurationModel()) // un-map partition/globalId
					.addMetaData(extendServiceModelWith_NativePersistenceApiModel());

			mdEditor.onEntityType(HibernateSessionFactory.T) //
					.addMetaData(hiddenGmeGlobal(session));

			return configModel;
		}

		private Icon entityIcon() {
			Icon bean = session.create(Icon.T, "hibernate-access:meta:icon");
			bean.setIcon(adaptiveIcon());
			return bean;
		}

		private AdaptiveIcon adaptiveIcon() {
			AdaptiveIcon result = session.create(AdaptiveIcon.T, "hibernate-access:adaptive-icon");
			result.setName("DatabaseAccess Icon");
			// @formatter:off
			result.setRepresentations(
				set(
					icon("DatabaseAccess_16x16.png"),
					icon("DatabaseAccess_24x24.png"),
					icon("DatabaseAccess_32x32.png"),
					icon("DatabaseAccess_64x64.png")
				)
			);
			// @formatter:on
			return result;
		}

		private Resource icon(String fileName) {
			Path path = tfPlatform.resources().publicResources("hibernateaccess/" + fileName).asPath();

			FileUploadSource source = session.create(FileUploadSource.T, "hibernate-access:icon-resource-source:" + fileName);
			source.setLocalFilePath(path.toString());

			Resource result = session.create(Resource.T, "hibernate-access:icon-resource:" + fileName);
			result.setResourceSource(source);

			return result;
		}

		/** Add tribefire.extension.hibernate:basic-hbm-configuration-model to every {@link HibernateAccess} */
		private AccessModelExtension extendDataModelWith_BasicHbmConfigurationModel() {
			GmMetaModel basicHbmConfigModel = session.getEntityByGlobalId("model:tribefire.extension.hibernate:basic-hbm-configuration-model");

			AccessModelExtension result = session.create(AccessModelExtension.T, "access-model-extension:hibernate/basic-hbm");
			result.getModels().add(basicHbmConfigModel);
			result.setAllowTypeExtension(true); // should not play a role, but just in case 

			return result;
		}

		/** Add <tt>native-persistence-api-model</tt> to every {@code HibernateAccess} */
		private ServiceModelExtension extendServiceModelWith_NativePersistenceApiModel() {
			GmMetaModel nativePersistenceApiModel = session.getEntityByGlobalId(NativePersistenceRequest.T.getModel().globalId());

			ServiceModelExtension result = session.create(ServiceModelExtension.T, "service-model-extension:hibernate/native-persistence-api");
			result.getModels().add(nativePersistenceApiModel);

			return result;
		}

		private MetaData hiddenGmeGlobal(ManagedGmSession session) {
			Hidden md = session.create(Hidden.T, "hidden:hibernate/gme/global");
			md.setSelector(gmeGlobalSelector(session));
			return md;
		}

		private UseCaseSelector gmeGlobalSelector(ManagedGmSession session) {
			return acquireGmeUseCaseSelector(session, KnownUseCase.gmeGlobalUseCase);
		}

		private UseCaseSelector acquireGmeUseCaseSelector(ManagedGmSession session, KnownUseCase useCase) {
			String globalId = "selector:useCase/gme." + useCase.name();
			return acquireEntity(session, globalId, () -> {
				UseCaseSelector result = session.create(UseCaseSelector.T, globalId);
				result.setUseCase(useCase.getDefaultValue());

				return result;
			});
		}

		private <T extends GenericEntity> T acquireEntity(ManagedGmSession session, String globalId, Supplier<T> factory) {
			T bean = session.query().findEntity(globalId);
			if (bean == null)
				bean = factory.get();
			return bean;
		}

		// CLOB

		private void mapClobs() {
			PropertyMapping clobMapping = clobMapping();

			mdEditorFor(ServiceRequestJob.T).onEntityType(ServiceRequestJob.T) //
					.addPropertyMetaData(ServiceRequestJob.serializedRequest, clobMapping) //
					.addPropertyMetaData(ServiceRequestJob.serializedResult, clobMapping)//
					.addPropertyMetaData(ServiceRequestJob.errorMessage, clobMapping) //
					.addPropertyMetaData(ServiceRequestJob.stackTrace, clobMapping);

			ModelMetaDataEditor serviceApiModelMdEditor = mdEditorFor(AsynchronousRequestRestCallback.T);
			serviceApiModelMdEditor.onEntityType(AsynchronousRequestRestCallback.T) //
					.addPropertyMetaData(AsynchronousRequestRestCallback.url, clobMapping) //
					.addPropertyMetaData(AsynchronousRequestRestCallback.customData, clobMapping);

			serviceApiModelMdEditor.onEntityType(AsynchronousRequestProcessorCallback.T) //
					.addPropertyMetaData(AsynchronousRequestProcessorCallback.callbackProcessorCustomData, clobMapping);
		}

		private PropertyMapping clobMapping() {
			PropertyMapping md = session.create(PropertyMapping.T, "eedcc6cc-8265-4bd0-9c58-2eac010e01a4");
			md.setType("materialized_clob");
			return md;
		}

		// Unmap File Resource

		private void unmapFileResource() {
			mdEditorFor(FileResource.T).onEntityType(FileResource.T) //
					.addMetaData(unmappedEntity());
		}

		private EntityMapping unmappedEntity() {
			return session.getEntityByGlobalId("hbm:unmapped-entity");
		}

		private ModelMetaDataEditor mdEditorFor(EntityType<?> type) {
			GmMetaModel model = session.getEntityByGlobalId(type.getModel().globalId());
			return modelApi.newMetaDataEditor(model).done();
		}

	}
}
