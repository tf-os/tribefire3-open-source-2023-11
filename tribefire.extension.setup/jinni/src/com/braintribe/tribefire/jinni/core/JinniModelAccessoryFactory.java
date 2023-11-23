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
package com.braintribe.tribefire.jinni.core;

import java.util.List;
import java.util.NoSuchElementException;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.devrock.templates.model.ArtifactTemplateRequest;
import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.accessapi.PersistenceRequest;
import com.braintribe.model.asset.natures.LocalSetupConfig;
import com.braintribe.model.asset.natures.LocalSetupTomcatConfig;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.cortexapi.access.collaboration.PushCollaborativeStage;
import com.braintribe.model.cortexapi.access.collaboration.ReadOnlyCollaborativePersistenceRequest;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.jinni.api.ListAliases;
import com.braintribe.model.jinni.api.Help;
import com.braintribe.model.jinni.api.History;
import com.braintribe.model.jinni.api.Introduce;
import com.braintribe.model.jinni.api.SpawnedJinniRequest;
import com.braintribe.model.jinni.meta.FolderName;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.meta.selector.UseCaseSelector;
import com.braintribe.model.platform.setup.api.Encrypt;
import com.braintribe.model.platform.setup.api.FileSystemPlatformSetupConfig;
import com.braintribe.model.platform.setup.api.logging.LoggingOptions;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.impl.managed.StaticAccessModelAccessory;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.resourceapi.enrichment.EnrichResource;
import com.braintribe.model.resourceapi.persistence.ManageResource;
import com.braintribe.model.resourceapi.request.GetPreview;
import com.braintribe.model.resourceapi.request.ResourceStreamingRequest;
import com.braintribe.model.resourceapi.stream.DownloadResource;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.PlatformRequest;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.js.model.api.request.AssembleJsDeps;

public class JinniModelAccessoryFactory implements ModelAccessoryFactory {

	/** In conjunction with {@link Hidden} marks a request to be omitted from the {@link Help} output. */
	public static final String USE_CASE_HELP = "help";

	/** In conjunction with {@link Hidden} marks a request to be omitted from the {@link History} output. */
	public static final String USE_CASE_HISTORY = "history";

	/**
	 * In conjunction with {@link Hidden} marks a {@link ServiceRequest} non-executable by the user. This means it cannot be called from the outside
	 * via command-line, but the requests can be evaluated internally if needed.
	 */
	public static final String USE_CASE_EXECUTION = "execution";

	private ModelAccessory platformDomainModelAccessory;

	private List<GmMetaModel> platformDomainModels;

	@Configurable
	@Required
	public void setPlatformDomainModels(List<GmMetaModel> platformDomainModels) {
		this.platformDomainModels = platformDomainModels;
	}

	@Override
	public ModelAccessory getForServiceDomain(String serviceDomainId) {
		if (PlatformRequest.platformDomainId.equals(serviceDomainId))
			return getPlatformDomainModelAccessory();
		else
			throw new NoSuchElementException("The service domain [" + serviceDomainId + "] is not supported by this setup");
	}

	private ModelAccessory getPlatformDomainModelAccessory() {
		if (platformDomainModelAccessory == null) {
			GmMetaModel metaModel = GmMetaModel.T.create();
			metaModel.setName("tribefire.extension.setup:platform-domain-model");
			metaModel.getDependencies().addAll(platformDomainModels);

			metaModel = metaModel.clone(new StandardCloningContext());

			enrichWithMetaData(metaModel);

			platformDomainModelAccessory = new StaticAccessModelAccessory(metaModel, PlatformRequest.platformDomainId);
		}

		return platformDomainModelAccessory;
	}

	private final UseCaseSelector helpUseCase = useCase(USE_CASE_HELP);
	private final UseCaseSelector historyUseCase = useCase(USE_CASE_HISTORY);
	private final UseCaseSelector executionUseCase = useCase(USE_CASE_EXECUTION);

	private void enrichWithMetaData(GmMetaModel metaModel) {
		ModelMetaDataEditor mdEditor = new BasicModelMetaDataEditor(metaModel);

		hideRelevantTypes(mdEditor);
		configurePropertyInfoForCompletionGenerator(mdEditor);
	}

	private void hideRelevantTypes(ModelMetaDataEditor mdEditor) {
		Hidden hiddenInHelp = hidden(helpUseCase);
		Hidden hiddenInHistory = hidden(historyUseCase);
		Hidden hiddenInExecution = hidden(executionUseCase);
		Visible visibleInExecution = visible(executionUseCase);

		mdEditor.onEntityType(ServiceRequest.T).addPropertyMetaData("metaData", hiddenInHelp);
		mdEditor.onEntityType(DispatchableRequest.T).addPropertyMetaData("serviceId", hiddenInHelp);
		mdEditor.onEntityType(AuthorizedRequest.T).addPropertyMetaData("sessionId", hiddenInHelp);

		mdEditor.onEntityType(History.T).addMetaData(hiddenInHistory);
		mdEditor.onEntityType(Help.T).addMetaData(hiddenInHistory);
		mdEditor.onEntityType(ListAliases.T).addMetaData(hiddenInHistory);
		mdEditor.onEntityType(Encrypt.T).addMetaData(hiddenInHistory);

		mdEditor.onEntityType(SpawnedJinniRequest.T).addMetaData(hiddenInHelp);
		mdEditor.onEntityType(Introduce.T).addMetaData(hiddenInHelp);

		mdEditor.onEntityType(GetPreview.T).addMetaData(hiddenInHelp);

		mdEditor.onEntityType(BinaryRequest.T).addMetaData(hiddenInExecution);
		mdEditor.onEntityType(DownloadResource.T).addMetaData(hiddenInExecution);
		mdEditor.onEntityType(ManageResource.T).addMetaData(hiddenInExecution);
		mdEditor.onEntityType(EnrichResource.T).addMetaData(hiddenInExecution);

		mdEditor.onEntityType(ResourceStreamingRequest.T).addMetaData(hiddenInExecution);

		// Brought together with CollaborativePersistenceRequest (collaborative-smood-api-model)
		mdEditor.onEntityType(PersistenceRequest.T).addMetaData(hiddenInExecution);
		mdEditor.onEntityType(GmqlRequest.T).addMetaData(hiddenInExecution);

		mdEditor.onEntityType(CollaborativePersistenceRequest.T).addMetaData(visibleInExecution);
		mdEditor.onEntityType(ReadOnlyCollaborativePersistenceRequest.T).addMetaData(hiddenInExecution);
		mdEditor.onEntityType(PushCollaborativeStage.T).addMetaData(hiddenInExecution);
	}

	private void configurePropertyInfoForCompletionGenerator(ModelMetaDataEditor mdEditor) {
		FolderName folder = FolderName.T.create();

		mdEditor.onEntityType(LocalSetupConfig.T) //
				.addPropertyMetaData(LocalSetupConfig.installationPath, folder) //
				.addPropertyMetaData(LocalSetupConfig.tempDir, folder) //
				.addPropertyMetaData(LocalSetupConfig.checkWriteAccessForDirs, folder) //
		;
		mdEditor.onEntityType(LocalSetupTomcatConfig.T) //
				.addPropertyMetaData(LocalSetupTomcatConfig.javaHome, folder) //
				.addPropertyMetaData(LocalSetupTomcatConfig.jreHome, folder) //
		;
		mdEditor.onEntityType(FileSystemPlatformSetupConfig.T) //
				.addPropertyMetaData(FileSystemPlatformSetupConfig.packageBaseDir, folder) //
		;
		mdEditor.onEntityType(LoggingOptions.T) //
				.addPropertyMetaData(LoggingOptions.logFilesDir, folder) //
		;
		mdEditor.onEntityType(ArtifactTemplateRequest.T) //
				.addPropertyMetaData("installationPath", folder) //
				.addPropertyMetaData("directoryName", folder) //
		;
		mdEditor.onEntityType(AssembleJsDeps.T) //
				.addPropertyMetaData("projectPath", folder) //
		;
	}

	private UseCaseSelector useCase(String useCase) {
		UseCaseSelector result = UseCaseSelector.T.create();
		result.setUseCase(useCase);

		return result;
	}

	private Hidden hidden(UseCaseSelector selector) {
		Hidden result = Hidden.T.create();
		result.setSelector(selector);

		return result;
	}

	private Visible visible(UseCaseSelector selector) {
		Visible result = Visible.T.create();
		result.setSelector(selector);

		return result;
	}

}
