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
package tribefire.extension.wopi.templates.wire.space;

import com.braintribe.logging.Logger;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.extensiondeployment.meta.BinaryProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.selector.AccessSelector;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.resource.configuration.ExternalResourcesContext;
import com.braintribe.model.resource.source.BlobSource;
import com.braintribe.model.resource.source.FileSystemSource;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.model.wopi.WopiSession;
import com.braintribe.model.wopi.service.WopiServiceProcessor;
import com.braintribe.model.wopi.service.integration.WopiRequest;
import com.braintribe.utils.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.scope.InstanceConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.wopi.WopiConstants;
import tribefire.extension.wopi.processing.StorageType;
import tribefire.extension.wopi.templates.api.WopiTemplateContext;
import tribefire.extension.wopi.templates.util.WopiTemplateUtil;
import tribefire.extension.wopi.templates.wire.contract.WopiMetaDataContract;
import tribefire.extension.wopi.templates.wire.contract.WopiTemplatesContract;

@Managed
public class WopiMetaDataSpace implements WireSpace, WopiMetaDataContract {

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WopiMetaDataSpace.class);

	@Import
	private WopiTemplatesContract wopiTemplates;

	@Override
	@Managed
	public GmMetaModel dataModel(WopiTemplateContext context) {
		GmMetaModel rawDataModel = (GmMetaModel) context.lookup("model:" + WopiConstants.DATA_MODEL_QUALIFIEDNAME);

		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());

		GmMetaModel persistenceDataModel = null;
		if (context.getStorageType() == StorageType.external) {
			ExternalResourcesContext resourcesContext = context.getExternalResourcesContext();
			if (resourcesContext != null) {
				persistenceDataModel = resourcesContext.getPersistenceDataModel();
			}
		}

		setModelDetails(model, WopiTemplateUtil.resolveDataModelName(context), rawDataModel, persistenceDataModel);
		return model;
	}

	@Override
	@Managed
	public GmMetaModel serviceModel(WopiTemplateContext context) {

		GmMetaModel model = context.create(GmMetaModel.T, InstanceConfiguration.currentInstance());
		GmMetaModel rawServiceModel = (GmMetaModel) context.lookup("model:" + WopiConstants.SERVICE_MODEL_QUALIFIEDNAME);
		setModelDetails(model, WopiTemplateUtil.resolveServiceModelName(context), rawServiceModel);
		return model;
	}

	@Override
	public void metaData(WopiTemplateContext context) {
		// -----------------------------------------------------------------------
		// DATA MODEL
		// -----------------------------------------------------------------------

		GmMetaModel dataModel = dataModel(context);
		BasicModelMetaDataEditor dataModelEditor = new BasicModelMetaDataEditor(dataModel);

		StorageType storageType = context.getStorageType();
		switch (storageType) {
			case db:
				dataModelEditor.onEntityType(ResourceSource.T).addMetaData(binaryProcessWithDatabase(context));
				dataModelEditor.onEntityType(BlobSource.T).addMetaData(binaryProcessWithDatabase(context));
				break;
			case external:
				ExternalResourcesContext resourcesContext = context.getExternalResourcesContext();
				BinaryProcessWith binaryProcessWith = resourcesContext.getBinaryProcessWith();
				binaryProcessWith.setSelector(accessSelector(context));
				dataModelEditor.onEntityType(ResourceSource.T).addMetaData(binaryProcessWith);
				dataModelEditor.onEntityType(resourcesContext.getResourceSourceType()).addMetaData(binaryProcessWith);
				break;
			case fs:
				if (!StringTools.isBlank(context.getStorageFolder())) {
					dataModelEditor.onEntityType(ResourceSource.T).addMetaData(binaryProcessWithFilesystem(context));
					dataModelEditor.onEntityType(FileSystemSource.T).addMetaData(binaryProcessWithFilesystem(context));
				}
				break;
			default:
				throw new IllegalStateException("Unsupported storage type " + storageType);
		}

		IncrementalAccess wopiAccess = wopiTemplates.access(context);
		if (wopiAccess instanceof HibernateAccess) {
			dataModelEditor.onEntityType(WopiSession.T).addPropertyMetaData(WopiSession.wopiUrl, clobMapping(context));
		}

		// -----------------------------------------------------------------------
		// SERVICE MODEL
		// -----------------------------------------------------------------------

		GmMetaModel serviceModel = serviceModel(context);
		BasicModelMetaDataEditor serviceModelEditor = new BasicModelMetaDataEditor(serviceModel);

		serviceModelEditor.onEntityType(WopiRequest.T).addMetaData(processWithGenericWopiServiceExecutionRequest(context));

	}

	// -----------------------------------------------------------------------
	// META DATA - PROCESS WITH
	// -----------------------------------------------------------------------

	@Managed
	public ProcessWith processWithGenericWopiServiceExecutionRequest(WopiTemplateContext context) {
		ProcessWith bean = context.create(ProcessWith.T, InstanceConfiguration.currentInstance());

		WopiServiceProcessor wopiServiceProcessor = wopiTemplates.wopiServiceProcessor(context);
		bean.setProcessor(wopiServiceProcessor);
		return bean;
	}

	// -----------------------------------------------------------------------
	// META DATA - BINARY PROCESS WITH
	// -----------------------------------------------------------------------

	@Managed
	private BinaryProcessWith binaryProcessWithDatabase(WopiTemplateContext context) {
		BinaryProcessWith bean = context.create(BinaryProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setSelector(accessSelector(context));
		bean.setRetrieval(wopiTemplates.sqlBinaryProcessor(context));
		bean.setPersistence(wopiTemplates.sqlBinaryProcessor(context));
		return bean;
	}

	@Managed
	private BinaryProcessWith binaryProcessWithFilesystem(WopiTemplateContext context) {
		BinaryProcessWith bean = context.create(BinaryProcessWith.T, InstanceConfiguration.currentInstance());
		bean.setSelector(accessSelector(context));
		bean.setRetrieval(wopiTemplates.filesystemBinaryProcessor(context));
		bean.setPersistence(wopiTemplates.filesystemBinaryProcessor(context));
		return bean;
	}

	// -----------------------------------------------------------------------
	// ACCESS SELECTOR
	// -----------------------------------------------------------------------

	@Managed
	private AccessSelector accessSelector(WopiTemplateContext context) {
		AccessSelector bean = context.create(AccessSelector.T, InstanceConfiguration.currentInstance());
		IncrementalAccess access = wopiTemplates.access(context);
		bean.setExternalId(access.getExternalId());
		return bean;
	}

	// -----------------------------------------------------------------------
	// META DATA - DB
	// -----------------------------------------------------------------------

	@Managed
	private PropertyMapping clobMapping(WopiTemplateContext context) {
		PropertyMapping bean = context.create(PropertyMapping.T, InstanceConfiguration.currentInstance());
		bean.setType("materialized_clob");
		return bean;
	}

	// -----------------------------------------------------------------------
	// HELPER METHODS
	// -----------------------------------------------------------------------

	private static void setModelDetails(GmMetaModel targetModel, String modelName, GmMetaModel... dependencies) {
		targetModel.setName(modelName);
		targetModel.setVersion(WopiConstants.MAJOR_VERSION + ".0");
		if (dependencies != null) {
			for (GmMetaModel dependency : dependencies) {
				if (dependency != null) {
					targetModel.getDependencies().add(dependency);
				}
			}
		}
	}
}
