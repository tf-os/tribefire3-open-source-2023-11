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
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.braintribe.model.exchange.ExchangePackage;
import com.braintribe.model.exchange.GenericExchangePayload;
import com.braintribe.model.exchangeapi.Export;
import com.braintribe.model.exchangeapi.ExportDescriptor;
import com.braintribe.model.exchangeapi.ExportResponse;
import com.braintribe.model.exchangeapi.predicate.EntityPredicate;
import com.braintribe.model.exchangeapi.supplier.EntitySupplier;
import com.braintribe.model.exchangeapi.supplier.StaticSupplier;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.core.expert.api.GmExpertRegistry;
import com.braintribe.model.processing.core.expert.impl.ConfigurableGmExpertRegistry;
import com.braintribe.model.processing.exchange.service.iterator.EntityIterable;
import com.braintribe.model.processing.exchange.service.iterator.StaticIterable;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.usersession.UserSession;
import com.braintribe.utils.lcd.CollectionTools2;

public class Exporter extends ServiceBase {

	private UserSession userSession;
	
	private EntityIterable<EntitySupplier> entityIterable;
	private Predicate<GenericEntity> matcher;
	private Export request;
	
	private Set<GenericEntity> externalReferences = new HashSet<>();
	private Set<GenericEntity> followedReferences = new HashSet<>();
	private Set<GenericEntity> exportedEntities = new LinkedHashSet<>();
	private Set<String> requiredTypes = new HashSet<>();
	private Set<GmMetaModel> requiredModels = new HashSet<>();
	private ManagedGmSession modelSession;
	
	private GmExpertRegistry registry = 
			new ConfigurableGmExpertRegistry()
				.add(EntityIterable.class, StaticSupplier.class, new StaticIterable());
	
	public Exporter(Export request, UserSession userSession,  ManagedGmSession modelSession, Predicate<GenericEntity> isExternalReference) {
		this.userSession = userSession;
		this.matcher = isExternalReference;
		this.request = request;
		this.modelSession = modelSession;
	}
	
	public void setRegistry(GmExpertRegistry registry) {
		this.registry = registry;
	}
	
	public ExportResponse run() {
		
		
		ExportDescriptor descriptor = request.getDescriptor();
		
		if (descriptor == null) {
			return createConfirmationResponse("Please provide an export descriptor.", Level.WARNING, ExportResponse.T);
		}
		
		EntityPredicate shallowifyingPredicate = descriptor.getShallowifyingPredicate();
		
		if (shallowifyingPredicate == null) {
			notifyInfo("Using default shallowifying predicate.");
		}

		EntitySupplier entitySupplier = descriptor.getEntitySupplier();
		entityIterable = registry.findExpert(EntityIterable.class).forInstance(entitySupplier);
		if (entityIterable == null) {
			return createConfirmationResponse("No expert found for given iterator.", Level.WARNING, ExportResponse.T);
		}
		entityIterable.setDenotation(entitySupplier);

		Date timestamp = new Date();
		String name = buildName(request);
		String user = userSession.getUser().getName();
		String description = request.getDescription();
		String globalIdSuffix = buildGlobalIdSuffix(timestamp, name, user);
		
		export();
		
		if (exportedEntities.isEmpty()) {
			return createConfirmationResponse("Could not extract an exportable payload from the given request.", Level.WARNING, ExportResponse.T);
		}

		
		builRequiredModels(); 
		
		if (!requiredModels.isEmpty()) {
			
			addNotifications(
					Notifications.build()
						.add()
							.message().info(requiredModels.size()+" custom model(s) are expected in the target access.")
							.command()
								.gotoModelPath("Required Models")
									.addElement(BaseType.INSTANCE.getActualType(requiredModels).getTypeSignature(), requiredModels)
								.close()
							.close()
						.list()
					);
			
		}
		
		
		GenericExchangePayload payload = createInstance(GenericExchangePayload.T);
		payload.setGlobalId("exchangeGenericPayload:"+globalIdSuffix);
		payload.setExternalReferences(externalReferences);
		payload.setAssembly(exportedEntities);

		ExchangePackage exchangePackage = createInstance(ExchangePackage.T);
		exchangePackage.setName(name);
		exchangePackage.setDescription(description);
		exchangePackage.setExported(timestamp);
		exchangePackage.setExportedBy(user); 
		exchangePackage.setPayloads(CollectionTools2.asList(payload));
		
		addNotifications(
				Notifications.build()
					.add()
						.message().info("Created exchange package "+exchangePackage.getName()+".")
						.command()
							.gotoModelPath("Exchange Package")
								.addElement(exchangePackage)
							.close()
						.close()
					.list());
		
		
		
		
		String requiredTypesInfo = (requiredTypes.size() > 0) ? (requiredTypes.size() + " type(s) of "+requiredModels.size()+" custom model(s) are required and expected in target access.\n") : "\n";
		
		ExportResponse response = createConfirmationResponse("Created package '"+name+"' with \n\n"
				+ exportedEntities.size()+" exported entities with\n"
				+ followedReferences.size()+" followed and "	
				+ externalReferences.size()+" external references.\n\n"
				+ requiredTypesInfo
				+ "See notifications for futher details.",
				Level.INFO, 
				ExportResponse.T);
		
		response.setExchangePackage(exchangePackage);
		return response;
	}

	private void builRequiredModels() {
		
		List<GmEntityType> requiredGmEnttiyTypes = findGmEntityTypes(CollectionTools2.union(exportedEntities, followedReferences));
		for (GmEntityType entityType : requiredGmEnttiyTypes) {
			
			if (!matcher.test(entityType)) {
				requiredTypes.add(entityType.getTypeSignature());
				requiredModels.add(entityType.getDeclaringModel());
			}
			
		}
		
		for (GmMetaModel requiredModel : requiredModels) {
			notifyInfo("Custom Model: "+requiredModel.getName()+" is required and expected in target access.");
		}
			
			

	}
	
	private List<GmEntityType> findGmEntityTypes(Set<GenericEntity> entities) {
		
		Set<String> typeSignatures = entities.stream().map((e) -> e.entityType().getTypeSignature()).collect(Collectors.toSet());
		EntityQuery query = EntityQueryBuilder.from(GmEntityType.T).where().property("typeSignature").in(typeSignatures).done();
		return modelSession.query().entities(query).list();
	}

	private <T extends GenericEntity> T createInstance (EntityType<T> entityType) {
		return  entityType.create();
	}
	
	
	private void export() {
		
		List<GenericEntity> entitiesToExport = new ArrayList<>();
		
		for (GenericEntity entity : entityIterable) {
			if (!matcher.test(entity)) {
				entitiesToExport.add(entity);
			}
		}
		
		if (entitiesToExport.isEmpty()) {
			notifyWarning("No entities found that could be exported.");
			return;
		}
		
		exportAndCollect(entitiesToExport);
	}
	
	private void exportAndCollect(List<GenericEntity> entitiesToExport) {
		
		DetachingAndShallowfyingCloningContext cloningContext = 
				new DetachingAndShallowfyingCloningContext(matcher, request.getDescriptor());
		
		List<GenericEntity> detachedEntitiesToExport =
				BaseType.INSTANCE.clone(
					cloningContext, 
					entitiesToExport, 
					StrategyOnCriterionMatch.partialize);
		
		externalReferences = cloningContext.getExternalReferences();
		followedReferences = cloningContext.getFollowedReferences();
		exportedEntities.addAll(detachedEntitiesToExport);
	}
	
	private String buildName(Export request) {
		String name = request.getName();
		if (name == null || name.isEmpty()) {
			notifyInfo("No name for the ExchangePackage provided. Using default name: '"+Utils.exchangePackageDefaultName+"'");
			name = Utils.exchangePackageDefaultName;
		}
		return name;
	}

	private String buildGlobalIdSuffix(Date timestamp, String name, String user) {
		return name+"-"+user+"-"+timestamp.getTime();
	}

	
}
