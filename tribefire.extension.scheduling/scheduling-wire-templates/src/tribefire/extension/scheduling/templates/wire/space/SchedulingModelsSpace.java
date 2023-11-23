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
package tribefire.extension.scheduling.templates.wire.space;

import java.util.List;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.scheduling.templates.wire.contract.ExistingInstancesContract;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingModelsContract;

@Managed
public class SchedulingModelsSpace extends AbstractInitializerSpace implements SchedulingModelsContract {

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Override
	@Managed
	public GmMetaModel configuredSchedulingApiModel() {
		GmMetaModel bean = create(GmMetaModel.T);

		GmMetaModel schedulingApiModel = existingInstances.schedulingApiModel();

		bean.setName(ExistingInstancesContract.GROUPID + ":configured-scheduling-api-model");
		bean.setVersion(schedulingApiModel.getVersion());
		bean.getDependencies().addAll(List.of(schedulingApiModel));

		return bean;
	}

	@Override
	@Managed
	public GmMetaModel configuredSchedulingAccessModel() {
		GmMetaModel bean = create(GmMetaModel.T);

		GmMetaModel schedulingModel = existingInstances.schedulingModel();

		bean.setName(ExistingInstancesContract.GROUPID + ":configured-scheduling-access-model");
		bean.setVersion(schedulingModel.getVersion());
		bean.getDependencies().add(schedulingModel);

		return bean;
	}

	@Override
	@Managed
	public GmMetaModel configuredSchedulingWbModel() {
		GmMetaModel model = create(GmMetaModel.T);

		model.setName(ExistingInstancesContract.GROUPID + ":configured-scheduling-wb-model");
		model.getDependencies().add(configuredSchedulingAccessModel());
		model.getDependencies().add(existingInstances.schedulingApiModel());
		model.getDependencies().add(coreInstances.workbenchModel());
		model.getDependencies().add(coreInstances.essentialMetaDataModel());

		return model;
	}

	@Override
	@Managed
	public GmMetaModel configuredSchedulingDeploymentModel() {
		GmMetaModel bean = create(GmMetaModel.T);

		GmMetaModel schedulingDeploymentModel = existingInstances.schedulingDeploymentModel();

		bean.setName(ExistingInstancesContract.GROUPID + ":configured-scheduling-deployment-model");
		bean.setVersion(schedulingDeploymentModel.getVersion());
		bean.getDependencies().addAll(List.of(schedulingDeploymentModel));

		return bean;
	}
}
