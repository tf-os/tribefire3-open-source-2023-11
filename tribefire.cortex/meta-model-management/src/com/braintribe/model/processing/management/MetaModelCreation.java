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
package com.braintribe.model.processing.management;

import java.util.Objects;
import java.util.function.Function;

import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;


public class MetaModelCreation {
	
	
	private String defaultBaseModel = GenericModelTypeReflection.rootModelName;
	
	private Function<MetaModelCreationContext, String> modelNameProvider = new Function<MetaModelCreationContext, String>() {
		@Override
		public String apply(MetaModelCreationContext context) throws RuntimeException {
			return context.getGroupId()+":"+context.getName();//+"#"+context.getVersion();
		}
	};
	
	public void setModelNameProvider(Function<MetaModelCreationContext, String> modelNameProvider) {
		this.modelNameProvider = modelNameProvider;
	}
	
	public void setDefaultBaseModel(String defaultBaseModel) {
		this.defaultBaseModel = defaultBaseModel;
	}
	
	public GmMetaModel createModel(MetaModelCreationContext context) throws Exception{
		
		GmMetaModel baseModel = context.getBaseModel();
		if (baseModel == null) {
			baseModel = lookupRootModel(context.getSession());
			Objects.requireNonNull(baseModel, "No BaseModel given and default BaseModel: "+defaultBaseModel+" not found.");
		}
		
		String modelName = getModelNameAndAssertAvailable(context);
		
		PersistenceGmSession session = context.getSession();
		GmMetaModel model = session.create(GmMetaModel.T, Model.modelGlobalId(modelName));
		model.setName(modelName);
		model.setVersion(context.getVersion());
		model.getDependencies().add(baseModel);
			
		return model;
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

	private String getModelNameAndAssertAvailable(MetaModelCreationContext context) throws Exception {
		String newModelName = modelNameProvider.apply(context);
		EntityQuery query = EntityQueryBuilder.from(GmMetaModel.class).where().property("name").eq(newModelName).done();
		if (context.getSession().query().entities(query).first() != null) {
			throw new Exception("Model with name '" + newModelName + "' already exists.");
		}
		return newModelName;
	}


}
