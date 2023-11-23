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
package com.braintribe.model.processing.cortex.service.model;

import java.util.List;

import com.braintribe.model.cortexapi.model.CreateModel;
import com.braintribe.model.cortexapi.model.CreateModelResponse;
import com.braintribe.model.cortexapi.model.ModelCreated;
import com.braintribe.model.cortexapi.model.ModelNotCreated;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.modellerfilter.NegationRelationshipFilter;
import com.braintribe.model.modellerfilter.WildcardEntityTypeFilter;
import com.braintribe.model.modellerfilter.meta.DefaultModellerView;
import com.braintribe.model.modellerfilter.view.ExcludesFilterContext;
import com.braintribe.model.modellerfilter.view.IncludesFilterContext;
import com.braintribe.model.modellerfilter.view.ModellerSettings;
import com.braintribe.model.modellerfilter.view.ModellerView;
import com.braintribe.model.modellerfilter.view.RelationshipKindFilterContext;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.cortex.service.ServiceBase;
import com.braintribe.model.processing.management.impl.validator.NamesHelper;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.transaction.NestedTransaction;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.utils.lcd.StringTools;

public class ModelCreator extends ServiceBase {

	private final CreateModel request;
	private final PersistenceGmSession session;
	private final String defaultBaseModel;
	
	public ModelCreator(CreateModel request, PersistenceGmSession session, String defaultBaseModel) {
		this.request = request;
		this.session = session;
		this.defaultBaseModel = defaultBaseModel;
	}
	
	public CreateModelResponse run() {
		
		List<GmMetaModel> requestedDependencies = request.getDependencies();
		if (requestedDependencies.isEmpty()) {
			GmMetaModel rootModel = lookupRootModel(session);
			if (rootModel == null) {
				return createConfirmationResponse("No dependencies given and default dependency: "+defaultBaseModel+" could not be found.", Level.WARNING, ModelNotCreated.T);
			}
			requestedDependencies.add(rootModel);
		}
		String modelName = request.getName();
		
		if (StringTools.isEmpty(modelName)) {
			return createConfirmationResponse("No model name given!\nPlease provide a name for the model.", Level.WARNING, ModelNotCreated.T);
		}
		
		
		String group = request.getGroupId();
		
		String defaultGroup = (String) CreateModel.T.getProperty("groupId").getInitializer();
		if (defaultGroup == null) {
			defaultGroup = "custom.model";
		}
		if (group == null || group.equals(defaultGroup)) {
			
			String calculatedGroup = defaultGroup;
			if (!calculatedGroup.endsWith(".")) {
				calculatedGroup += ".";
			}
			
			String subgroup = StringTools.replaceAllOccurences(modelName, "model", "");
			subgroup = StringTools.replaceAllOccurences(subgroup, "-", "");
			calculatedGroup += subgroup.toLowerCase();
			group = calculatedGroup;
			
		}
		
		modelName = group + ":" + modelName;
		
		if (!NamesHelper.validMetaModelName(modelName)) {
			
			String details = "Model name must not contain special characters.";
			return createConfirmationResponse("Invalid model name: "+modelName+"!\nPlease choose a different name.\n\n\n"+details, Level.WARNING, ModelNotCreated.T);
		}
		
		if (isModelNameAlreadyTaken(modelName)) {
			return createConfirmationResponse("A model with name: "+modelName+" already exists!\nPlease choose a different name.", Level.WARNING, ModelNotCreated.T);
		}
		
		
		
		GmMetaModel model = session.create(GmMetaModel.T, Model.modelGlobalId(modelName));
		model.setName(modelName);
		model.setVersion(request.getVersion());
		List<GmMetaModel> dependencies = model.getDependencies();
		for (GmMetaModel dependency : requestedDependencies) {
			if (!dependencies.contains(dependency)) {
				dependencies.add(dependency);
			}
		}
		
		prepareDefaultModellerView(session, model);
		
		ModelCreated response = ModelCreated.T.create();
		response.setModel(model);
		response.setNotifications(
				Notifications.build()
					.add()
						.message().info("Created Model: "+model.getName())
					.close()
					.add()
						.command().gotoModelPath("Goto Model").addElement(model).close()
					.close()
				.list()
				);
		
		return response;		
		
	}
	
	private GmMetaModel lookupRootModel(PersistenceGmSession session) {
		EntityQuery lookupQuery = 
				EntityQueryBuilder
					.from(GmMetaModel.T)
					.where()
					.property("name").eq(defaultBaseModel)
					.done();
		
		GmMetaModel defaultBaseModel = 
				session
					.query()
					.entities(lookupQuery)
					.unique();
		
		return defaultBaseModel;
	}

	private boolean isModelNameAlreadyTaken(String modelName) {
		EntityQuery query = EntityQueryBuilder.from(GmMetaModel.class).where().property("name").eq(modelName).done();
		return session.query().entities(query).unique() != null;
		
	}
	
	public void prepareDefaultModellerView(PersistenceGmSession session, GmMetaModel gmMetaModel) {
		NestedTransaction nt = session.getTransaction().beginNestedTransaction();
		final DefaultModellerView defaultModellerView = session.create(DefaultModellerView.T);
		ModellerView modellerView = session.create(ModellerView.T);
		
		String name = gmMetaModel.getName().substring(gmMetaModel.getName().indexOf(":")+1, gmMetaModel.getName().length());
		modellerView.setName(name + "_default");
		
		modellerView.setMetaModel(gmMetaModel);
		
		modellerView.setExcludesFilterContext(session.create(ExcludesFilterContext.T));

		WildcardEntityTypeFilter genericEntityTypeFilter = session.create(WildcardEntityTypeFilter.T);
		genericEntityTypeFilter.setWildcardExpression("*com.braintribe.model.generic.GenericEntity*");
		NegationRelationshipFilter negatedGenericEntityTypeFilter = session.create(NegationRelationshipFilter.T);
		negatedGenericEntityTypeFilter.setOperand(genericEntityTypeFilter);
		
		modellerView.getExcludesFilterContext().getOperands().add(negatedGenericEntityTypeFilter);
		
		modellerView.setIncludesFilterContext(session.create(IncludesFilterContext.T));
		modellerView.getIncludesFilterContext().setAllIncludedTypes(true);
		modellerView.getIncludesFilterContext().setDeclaredTypes(true);
		modellerView.getIncludesFilterContext().setExplicitTypes(true);
		
		modellerView.setRelationshipKindFilterContext(session.create(RelationshipKindFilterContext.T));
		modellerView.getRelationshipKindFilterContext().setAggregation(true);
		modellerView.getRelationshipKindFilterContext().setGeneralization(true);
		
		modellerView.setSettings(session.create(ModellerSettings.T));		
		
		modellerView.getSettings().setMaxElements(16);
		modellerView.getSettings().setDepth(3);
		
		//modellerView.setFocusedType(getGmType(modelGraphConfigurations.currentFocusedType));
		
		defaultModellerView.setDefaultView(modellerView);
		gmMetaModel.getMetaData().add(defaultModellerView);	
		nt.commit();
	}

}
