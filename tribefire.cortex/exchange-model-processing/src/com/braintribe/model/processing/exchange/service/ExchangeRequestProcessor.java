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
package com.braintribe.model.processing.exchange.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchangeapi.EncodingType;
import com.braintribe.model.exchangeapi.ExchangeRequest;
import com.braintribe.model.exchangeapi.ExchangeResponse;
import com.braintribe.model.exchangeapi.Export;
import com.braintribe.model.exchangeapi.ExportAndWriteToResource;
import com.braintribe.model.exchangeapi.ExportAndWriteToResourceResponse;
import com.braintribe.model.exchangeapi.ExportResponse;
import com.braintribe.model.exchangeapi.Import;
import com.braintribe.model.exchangeapi.ImportResponse;
import com.braintribe.model.exchangeapi.ReadFromResource;
import com.braintribe.model.exchangeapi.ReadFromResourceAndImport;
import com.braintribe.model.exchangeapi.ReadFromResourceAndImportResponse;
import com.braintribe.model.exchangeapi.ReadFromResourceResponse;
import com.braintribe.model.exchangeapi.WriteToResource;
import com.braintribe.model.exchangeapi.WriteToResourceResponse;
import com.braintribe.model.exchangeapi.predicate.DefaultPredicate;
import com.braintribe.model.exchangeapi.predicate.EntityPredicate;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.accessrequest.api.AccessRequestContext;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessors;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.model.usersession.UserSession;


public class ExchangeRequestProcessor implements AccessRequestProcessor<ExchangeRequest, ExchangeResponse> {
	
	private Function<EntityReference, PersistenceStage> stageResolver;
	private Set<String> systemIndices = Collections.emptySet();
	private Supplier<UserSession> userSessionProvider;
	
	private static final Predicate<GenericEntity> FALSE_MATCHER = e -> false;
	
	// TODO why is this here?
	@SuppressWarnings("unused")
	private final Predicate<GenericEntity> defaultMatcher = e -> {
		PersistenceStage stage = stageResolver.apply(e.globalReference());
		
		if (stage != null) {
			switch (stage.stageType()) {
				case STATIC: return true;  
				case INDEXED: return systemIndices.contains(stage.getName());
			}
		}

		return false;
	};
	
	private GmExpertRegistry registry = 
			new ConfigurableGmExpertRegistry()
				.add(Predicate.class, EntityPredicate.class, FALSE_MATCHER);
	
	
	@Configurable
	@Required
	public void setUserSessionProvider(Supplier<UserSession> userSessionProvider) {
		this.userSessionProvider = userSessionProvider;
	}
	
	@Configurable
	@Required
	public void setStageResolver(Function<EntityReference, PersistenceStage> stageResolver) {
		this.stageResolver = stageResolver;
	}
	
	@Configurable
	public void setSystemIndices(Set<String> systemIndices) {
		this.systemIndices = systemIndices;
	}

	@Configurable
	public void setRegistry(GmExpertRegistry registry) {
		this.registry = registry;
	}
	
	private final AccessRequestProcessor<ExchangeRequest, ExchangeResponse> dispatcher = AccessRequestProcessors.dispatcher(config->{
		config.register(WriteToResource.T, this::writeToResource);
		config.register(ReadFromResource.T, this::readFromResource);
		config.register(Export.T, this::exportAssembly);
		config.register(Import.T, this::importAssembly);
		config.register(ExportAndWriteToResource.T, this::exportAndWriteToResource);
		config.register(ReadFromResourceAndImport.T, this::readFromResourceAndImport);
	});

	@Override
	public ExchangeResponse process(AccessRequestContext<ExchangeRequest> context) {
		return dispatcher.process(context);
	}
	
	protected WriteToResourceResponse writeToResource(AccessRequestContext<WriteToResource> context) {
		return new ToResourceWriter(
				context.getRequest(), 
				context.getSession()).run();
	}
	
	protected ReadFromResourceResponse readFromResource(AccessRequestContext<ReadFromResource> context) {
		
		return new FromResourceReader(
				context.getRequest()).run();
	}


	protected ExportResponse exportAssembly(AccessRequestContext<Export> context) {
		
		Export request = context.getRequest();
		return new Exporter(
				request,
				userSessionProvider.get(),
				context.getSession().getModelAccessory().getModelSession(),
				getReferenceMatcher(
						request.getDescriptor().getShallowifyingPredicate())).run();
	}
	
	protected ImportResponse importAssembly(AccessRequestContext<Import> context) {
		Import request = context.getRequest();
		return	new Importer(
					request, 
					request.getUseSystemSession() ? context.getSystemSession() : context.getSession()).run();
	}
	
	protected ExportAndWriteToResourceResponse exportAndWriteToResource(AccessRequestContext<ExportAndWriteToResource> context) {
		
		ExportAndWriteToResource request = context.getRequest();
		PersistenceGmSession session = context.getSession();
		
		
		Export exportRequest = Export.T.create();
		exportRequest.setDescriptor(request.getDescriptor());
		exportRequest.setName(request.getName());
		exportRequest.setDescription(request.getDescription());
		
		ExportResponse exportResponse = 
				new Exporter(
					exportRequest,
					userSessionProvider.get(),
					context.getSession().getModelAccessory().getModelSession(),
					getReferenceMatcher(
							request.getDescriptor().getShallowifyingPredicate())).run();
		
		ExchangePackage exchangePackage = exportResponse.getExchangePackage();
		
		if (exchangePackage == null) {
			return ServiceBase.createResponse(
					"Could not export given entities to exchange package.",
					Level.WARNING, 
					true, ExportAndWriteToResourceResponse.T);
		}
		
		WriteToResource writeToResourceRequest = WriteToResource.T.create();
		writeToResourceRequest.setAssembly(exchangePackage);
		writeToResourceRequest.setResourceBaseName(Utils.buildBaseName(exchangePackage));

		writeToResourceRequest.setEncodingType(EncodingType.xml); // exchangePackages have a fixed encoding type.
		writeToResourceRequest.setPrettyOutput(request.getPrettyOutput());
		writeToResourceRequest.setStabilizeOrder(request.getStabilizeOrder());
		writeToResourceRequest.setWriteEmptyProperties(request.getWriteEmptyProperties());
		writeToResourceRequest.setAddResourceBinaries(request.getAddResourceBinaries());
		
		
		WriteToResourceResponse writeToResourceResponse = new ToResourceWriter(writeToResourceRequest, session).run();
		
		ExportAndWriteToResourceResponse response = ExportAndWriteToResourceResponse.T.create();
		//response.getNotifications().addAll(exportResponse.getNotifications());
		response.getNotifications().addAll(writeToResourceResponse.getNotifications());
		response.setResource(writeToResourceResponse.getResource());
		//response.setExchangePackage(exportResponse.getExchangePackage());
		
		return response;
		
	}
	protected ReadFromResourceAndImportResponse readFromResourceAndImport(AccessRequestContext<ReadFromResourceAndImport> context) {
		
		ReadFromResourceAndImport request = context.getRequest();
		
		ReadFromResource readFromResourceRequest = ReadFromResource.T.create();
		readFromResourceRequest.setLenient(request.getLenient());
		readFromResourceRequest.setEncodingType(EncodingType.xml);
		readFromResourceRequest.setResource(request.getResource());
		
		List<Resource> unzippedResources = new ArrayList<>(); 
		
		
		ReadFromResourceResponse readFromResourceResponse = 
				new FromResourceReader(
						readFromResourceRequest,
						unzippedResources).run();
		
		Object assembly = readFromResourceResponse.getAssembly();
		if (assembly == null) {
			return ServiceBase.createResponse(
					"Could not decode an assembly from given resource.",
					Level.WARNING, 
					true, ReadFromResourceAndImportResponse.T);
		}

		if (!(assembly instanceof ExchangePackage)) {
			return ServiceBase.createResponse(
					"Decoded assembly is not an exchange package.",
					Level.WARNING, 
					true, ReadFromResourceAndImportResponse.T);
		}
		
		Import importRequest = Import.T.create();
		importRequest.setExchangePackage((ExchangePackage)assembly);
		
		importRequest.setIncludeEnvelope(request.getIncludeEnvelope());
		importRequest.setRequiresGlobalId(request.getRequiresGlobalId());
		importRequest.setCreateShallowInstanceForMissingReferences(request.getCreateShallowInstanceForMissingReferences());
		
		Importer importer = new Importer(
				importRequest, 
				request.getUseSystemSession() ? context.getSystemSession() : context.getSession(),
				unzippedResources);
		importer.setStageResolver(this.stageResolver);
		ImportResponse importResponse = 
				importer.run();
		
		ReadFromResourceAndImportResponse response = ReadFromResourceAndImportResponse.T.create();
		response.getNotifications().addAll(readFromResourceResponse.getNotifications());
		response.getNotifications().addAll(importResponse.getNotifications());
		response.setAssembly(assembly);
		
		return response;
		
	}
	

	

	
	private Predicate<GenericEntity> getReferenceMatcher(EntityPredicate matcher) {
		if (matcher == null) {
			matcher = DefaultPredicate.T.create();
		}
		return registry.getExpert(Predicate.class).forInstance(matcher);
	}

}
