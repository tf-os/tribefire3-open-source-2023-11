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
package tribefire.extension.aws.initializer.wire.space;

import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.aws.initializer.wire.contract.AwsInitializerModuleModelsContract;
import tribefire.extension.aws.initializer.wire.contract.ExistingInstancesContract;

/**
 * @see AwsInitializerModuleModelsContract
 */
@Managed
public class AwsInitializerModuleModelsSpace extends AbstractInitializerSpace implements AwsInitializerModuleModelsContract {

	@Import
	private ExistingInstancesContract existingInstances;
	
	@Managed
	@Override
	public GmMetaModel configuredDeploymentModel() {
		GmMetaModel model = create(GmMetaModel.T);
		model.setName("tribefire.extension.aws:configured-aws-deployment-model");
		model.getDependencies().add(existingInstances.deploymentModel());
		
		return model;
	}
	
	@Managed
	@Override
	public GmMetaModel configuredServiceModel() {
		GmMetaModel model = create(GmMetaModel.T);
		model.setName("tribefire.extension.aws:configured-aws-service-model");
		model.getDependencies().add(existingInstances.serviceModel());
		
		return model;
	}
	
	@Managed
	@Override
	public GmMetaModel configuredDataModel() {
		GmMetaModel model = create(GmMetaModel.T);
		model.setName("tribefire.extension.aws:configured-aws-model");
		model.getDependencies().add(existingInstances.dataModel());
		
		return model;
	}
}
