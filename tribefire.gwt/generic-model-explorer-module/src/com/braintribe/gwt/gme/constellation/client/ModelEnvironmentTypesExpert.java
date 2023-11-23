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
package com.braintribe.gwt.gme.constellation.client;

import java.util.Arrays;
import java.util.Collection;

import com.braintribe.gwt.gmrpc.api.client.user.EmbeddedRequiredTypesExpert;
import com.braintribe.model.accessapi.ModelEnvironment;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmListType;
import com.braintribe.model.meta.GmMapType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmSetType;
import com.braintribe.model.meta.GmSimpleType;
import com.braintribe.model.service.api.result.CompositeResponse;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;

public class ModelEnvironmentTypesExpert implements EmbeddedRequiredTypesExpert {

	private static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	private static final Collection<GenericModelType> minimalTypes = 
			Arrays.<GenericModelType>asList(
					typeReflection.getEntityType(ModelEnvironment.class),
					typeReflection.getEntityType(GmMetaModel.class),
					typeReflection.getEntityType(GmEntityType.class),
					typeReflection.getEntityType(GmEnumType.class),
					typeReflection.getEntityType(GmProperty.class),
					typeReflection.getEntityType(GmEnumConstant.class),
					typeReflection.getEntityType(GmSimpleType.class),
					typeReflection.getEntityType(GmBaseType.class),
					typeReflection.getEntityType(GmMapType.class),
					typeReflection.getEntityType(GmSetType.class),
					typeReflection.getEntityType(GmListType.class));

	@Override
	public Collection<GenericModelType> getMinimalTypes() {
		return minimalTypes;
	}

	@Override
	public GmMetaModel getModelFromAssembly(Object assembly) {
		ModelEnvironment modelEnvironment;
		if (assembly instanceof ModelEnvironment)
			modelEnvironment = (ModelEnvironment) assembly;
		else
			modelEnvironment = getModelEnvironmentFromServiceResult((ServiceResult) assembly);
		
		if (modelEnvironment == null)
			return null;
		
		GmMetaModel dataModel = modelEnvironment.getDataModel();
		GmMetaModel serviceModel = modelEnvironment.getServiceModel();
		GmMetaModel workbenchModel = modelEnvironment.getWorkbenchModel();
		GmMetaModel modelToEnsure = GmMetaModel.T.create();
		modelToEnsure.setName("Virtual-TempModel");
		modelToEnsure.getDependencies().add(dataModel);
		if (serviceModel != null)
			modelToEnsure.getDependencies().add(serviceModel);
		if (workbenchModel != null) 
			modelToEnsure.getDependencies().add(workbenchModel);
		return modelToEnsure;
	}

	private ModelEnvironment getModelEnvironmentFromServiceResult(ServiceResult serviceResult) {
		switch (serviceResult.resultType()) {
			case success:
				Object result = ((ResponseEnvelope) serviceResult).getResult();
				if (result instanceof ModelEnvironment)
					return (ModelEnvironment) result;
				if (result instanceof CompositeResponse)
					return getModelEnvironmentFromCompositeResponse((CompositeResponse) result);
				return null;

			case failure:
				Failure failure = (Failure) serviceResult;
				throw new GenericModelException("ServiceResult was 'Failure': " + failure.getMessage() + ". Detail: " + failure.getDetails());

			default:
				throw new GenericModelException("Unexpected type of ServiceResult: " + serviceResult.resultType());
		}
	}

	private ModelEnvironment getModelEnvironmentFromCompositeResponse(CompositeResponse compositeResponse) {
		for (ServiceResult result : compositeResponse.getResults()) {
			ModelEnvironment modelEnvironment = getModelEnvironmentFromServiceResult(result);
			if (modelEnvironment != null)
				return modelEnvironment;
		}
		
		return null;
	}

}
