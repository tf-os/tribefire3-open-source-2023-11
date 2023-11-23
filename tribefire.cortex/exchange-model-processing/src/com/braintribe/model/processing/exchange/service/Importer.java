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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.braintribe.logging.Logger;
import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchange.ExchangePayload;
import com.braintribe.model.exchangeapi.Import;
import com.braintribe.model.exchangeapi.ImportResponse;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.assembly.sync.api.ImportStatistics;
import com.braintribe.model.processing.assembly.sync.impl.AssemblyImporter;
import com.braintribe.model.processing.assembly.sync.impl.ExchangeImporterContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.Transaction;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.smoodstorage.stages.PersistenceStage;
import com.braintribe.model.smoodstorage.stages.StageType;

public class Importer extends ServiceBase {
	
	private static final Logger logger = Logger.getLogger(Importer.class);
	
	private Import request;
	private ExchangePackage exchangePackage;
	private PersistenceGmSession session;
	private Set<ExchangePayload> syncedPayloads = new HashSet<>();
	private Set<ExchangePayload> failedPayloads = new HashSet<>();
	private Set<GenericEntity> missingExternalReferences = new HashSet<>();
	private Set<GenericEntity> externalReferences = new HashSet<>();
	private ImportStatistics importStatistics = null;
	private Map<String, Resource> callResourcesPerGid = new HashMap<>();

	private List<Resource> unzippedResources;
	private Function<EntityReference, PersistenceStage> stageResolver;
	
	public Importer(Import request, PersistenceGmSession session) {
		this.request = request;
		this.session = session;
		this.exchangePackage = request.getExchangePackage();
	}

	public Importer(Import request, PersistenceGmSession session, List<Resource> unzippedResources) {
		this(request, session);
		this.unzippedResources = unzippedResources;
		fillCallResources();
	}
	
	public void setStageResolver(Function<EntityReference, PersistenceStage> stageResolver) {
		this.stageResolver = stageResolver;
	}
	
	public ImportResponse run() {
		
		if (exchangePackage == null) {
			return createConfirmationResponse("No exchnage package provided.", Level.WARNING, ImportResponse.T);
		}
		List<ExchangePayload> payloads = exchangePackage.getPayloads();
		
		if (payloads.isEmpty()) {
			return createConfirmationResponse("No payload found in exchange package.", Level.WARNING, ImportResponse.T);
		}
		
		checkExternalReferences(payloads);
		if (!request.getCreateShallowInstanceForMissingReferences()) {
			if (!missingExternalReferences.isEmpty()) {
				return createConfirmationResponse("Missing "+missingExternalReferences.size()+" external reference(s) in target.\nSee notifications for further details.", Level.WARNING, ImportResponse.T);
			}
		}
		
		syncPayloads(payloads);
		
		commitOrRollback();
		
		if (syncedPayloads.isEmpty()) {
			return createConfirmationResponse(
					"None of the given payloads could be imported.\n Check notifications for further details.",
					Level.WARNING,
					ImportResponse.T);
		}
		
		if (!failedPayloads.isEmpty()) {
			return createConfirmationResponse(
					"Successfully imported "+syncedPayloads.size()+" "+ "payload(s) but could not import"+failedPayloads.size()+" payloads.",
					Level.WARNING,
					ImportResponse.T);
			
		}
		
		notifyInfo("Imported exchange package: "+exchangePackage.getName()+"");
		
		return createConfirmationResponse(
				"Imported package '"+exchangePackage.getName()+"' with \n\n"
				+ importStatistics.getCreatedEntityCount() +" new entities,\n"
				+ importStatistics.getAffectedExistingEntityCount() + " updated entities and \n"
				+ externalReferences.size() + " external references.",
				Level.INFO,
						ImportResponse.T);
	}
	
	private void checkExternalReferences(List<ExchangePayload> payloads) {
		for (ExchangePayload payload : payloads) {
			for (GenericEntity externalReference : payload.getExternalReferences()) {
				
				externalReferences.add(externalReference);
				GenericEntity entity = session.query().findEntity(externalReference.getGlobalId());
				if (entity == null) {
					missingExternalReferences.add(entity);
					Level level = request.getCreateShallowInstanceForMissingReferences() ? Level.INFO : Level.WARNING;
					notify("Expected external reference with globalId: "+externalReference.getGlobalId()+" does not exist in target.", level, logger);
				}
			}
		}
	}

	private void commitOrRollback() {
		Transaction transaction = session.getTransaction();
		if (transaction.hasManipulations()) {
			try {
				session.commit();
			} catch (Exception e) {
				failedPayloads.addAll(syncedPayloads);
				syncedPayloads.clear();
				
				transaction.undo(transaction.getManipulationsDone().size());
				notifyError(e);
			}
		}
		
	}

	private Set<ExchangePayload> syncPayloads(Collection<ExchangePayload> payloads) {
		
		
		for (ExchangePayload payload : payloads) {
			
			ExchangeImporterContext importContext = 
					new ExchangeImporterContext(
							session, 
							payload, 
							request.getIncludeEnvelope(),
							request.getRequiresGlobalId(),
							session.getAccessId()) {
				
				
				@Override
				public boolean isExternalReference(GenericEntity entity) {
					boolean externalReference = super.isExternalReference(entity);
					if (stageResolver != null) {
						PersistenceStage stage = stageResolver.apply(entity.globalReference());
						
						if (stage == null) {
							return false;
						}
						
						return externalReference || "com.braintribe.model.processing.cortex.priming.CortexModelsPersistenceInitializer".equals(stage.getName()) || stage.stageType() == StageType.STATIC;
					}
					return externalReference;
				}
				
				@Override
				public void notifyImportStatistics(ImportStatistics statistics) {
					importStatistics = statistics;
				}
				@Override
				public Resource findUploadResource(String globalId) {
					return callResourcesPerGid.get(globalId);
				}
				
			};
			
			try {
				ExchangePayload syncedPayload = AssemblyImporter.importAssembly(importContext);
				syncedPayloads.add(syncedPayload);
				notify("Imported payload.",Level.INFO, logger);
			} catch (Exception e) {
				notifyError(e);
				failedPayloads.add(payload);
			}
		}
		return syncedPayloads;
	}

	private void notifyError(Exception e) {
		logger.error("Error in Importer.",e);
		String errorMessage = e.getMessage();
		Throwable rootCause = ExceptionUtils.getRootCause(e);
		if (rootCause != null) {
			errorMessage = rootCause.getMessage();
		}
		notifyError("Could not import payload: "+errorMessage);
	}

	private void fillCallResources() {
		if (unzippedResources != null) {
			unzippedResources.forEach((r) -> callResourcesPerGid.put(r.getGlobalId(),r));
		}
	}
}
