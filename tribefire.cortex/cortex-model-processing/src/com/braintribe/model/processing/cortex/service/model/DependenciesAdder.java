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

import com.braintribe.model.cortexapi.model.AddDependencies;
import com.braintribe.model.cortexapi.model.DependenciesAdded;
import com.braintribe.model.cortexapi.model.DependenciesNotAdded;
import com.braintribe.model.cortexapi.model.MergeModelsResponse;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.notification.Level;
import com.braintribe.model.processing.cortex.service.ServiceBase;

public class DependenciesAdder extends ServiceBase {

	private AddDependencies request;
	
	public DependenciesAdder(AddDependencies request) {
		this.request = request;
	}
	
	public MergeModelsResponse run() {
		
		GmMetaModel model = request.getModel();
		List<GmMetaModel> dependencies = request.getDependencies();
		if (model == null) {
			return createConfirmationResponse("Please provide a model!", Level.WARNING, DependenciesNotAdded.T);
		}
		if (dependencies.isEmpty()) {
			return createConfirmationResponse("Please provide dependencies that should be added to model: "+model.getName(), Level.WARNING, DependenciesNotAdded.T);
		}
		List<GmMetaModel> modelDependencies = model.getDependencies();
		int count = 0;
		for (GmMetaModel dependency : dependencies) {
			if (modelDependencies.contains(dependency)) {
				notifyWarning("Model: "+model.getName()+" already has a dependency to model: "+dependency.getName());
				continue;
			}
			modelDependencies.add(dependency);
			count++;
		}
		
		
		DependenciesAdded response = null;
		switch (count) {
			case 0: 
				return createResponse("No dependencies added to model: "+model.getName(), DependenciesNotAdded.T);
			case 1: 
				response = createResponse("Added "+count+" dependency to model: "+model.getName(), DependenciesAdded.T);
				break;
			default:
				response = createResponse("Added "+count+" dependencies to model: "+model.getName(), DependenciesAdded.T);
		}
		response.setCount(count);		
		return response;
		
		
	}
}
