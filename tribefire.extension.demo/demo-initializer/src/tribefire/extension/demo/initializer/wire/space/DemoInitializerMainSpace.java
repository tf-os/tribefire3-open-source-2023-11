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
package tribefire.extension.demo.initializer.wire.space;

import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.extension.demo.initializer.wire.contract.DemoInitializerContract;
import tribefire.extension.demo.initializer.wire.contract.DemoInitializerMainContract;
import tribefire.extension.demo.initializer.wire.contract.DemoInitializerModelsContract;
import tribefire.extension.demo.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.demo.model.api.EntityMarshallingRequest;
import tribefire.extension.demo.model.api.FindByText;
import tribefire.extension.demo.model.api.GetEmployeesByGenderRequest;
import tribefire.extension.demo.model.api.GetOrphanedEmployees;
import tribefire.extension.demo.model.api.GetPersonsByName;
import tribefire.extension.demo.model.api.NewEmployee;
import tribefire.extension.demo.model.api.streaming.ResourceStreamingRequest;
import tribefire.extension.demo.model.cortex.api.TestAccessRequest;
import tribefire.extension.demo.model.data.Company;
import tribefire.extension.demo.model.data.Department;
import tribefire.extension.demo.model.data.Person;
import tribefire.extension.demo.model.deployment.DemoApp;
import tribefire.module.wire.contract.ModelApiContract;

/**
 * @see DemoInitializerMainContract
 */
@Managed
public class DemoInitializerMainSpace implements DemoInitializerMainContract {

	@Import
	private DemoInitializerContract initializer;

	@Import
	private DemoInitializerModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private ModelApiContract modelApi;

	@Override
	public DemoInitializerContract initializerContract() {
		return initializer;
	}

	@Override
	public DemoInitializerModelsContract initializerModelsContract() {
		return models;
	}

	@Override
	public CoreInstancesContract coreInstancesContract() {
		return coreInstances;
	}

	@Override
	public ExistingInstancesContract existingInstancesContract() {
		return existingInstances;
	}

	@Override
	public void metaData() {
		dataModelMetaData();
		deploymentModelMetaData();
		serviceModelMetaData();
		cortexModelMetaData();
	}

	private void dataModelMetaData() {
		ModelMetaDataEditor editor = modelApi.newMetaDataEditor(models.configuredDemoModel()).done();
		editor.onEntityType(Department.T).addPropertyMetaData(Department.profitable, initializer.onChangeProfitable());
		editor.onEntityType(Person.T).addMetaData(initializer.onCreateAudit());
		editor.onEntityType(Person.T).addMetaData(initializer.onDeleteAudit());
		editor.onEntityType(Person.T).addPropertyMetaData(initializer.onChangeAudit());
		editor.onEntityType(Person.T).addPropertyMetaData(Person.birthday, initializer.dateClippingDay());
		editor.onEntityType(Company.T).addPropertyMetaData(Company.averageRevenue, initializer.onChangeRevenue());
	}

	private void deploymentModelMetaData() {
		ModelMetaDataEditor editor = modelApi.newMetaDataEditor(models.configuredDemoDeploymentModel()).done();
		editor.onEntityType(DemoApp.T).addPropertyMetaData(DemoApp.password, initializer.confidential());
	}

	private void serviceModelMetaData() {
		ModelMetaDataEditor editor = modelApi.newMetaDataEditor(models.configuredDemoServiceModel()).done();
		editor.onEntityType(GetOrphanedEmployees.T).addMetaData(initializer.processWithGetOrphanedEmployees());
		editor.onEntityType(GetEmployeesByGenderRequest.T).addMetaData(initializer.processWithGetEmployeeByGender());
		editor.onEntityType(GetPersonsByName.T).addMetaData(initializer.processWithGetPersonsByName());
		editor.onEntityType(FindByText.T).addMetaData(initializer.processWithFindByText());
		editor.onEntityType(EntityMarshallingRequest.T).addMetaData(initializer.processWithEntityMarshalling());
		editor.onEntityType(NewEmployee.T).addMetaData(initializer.processWithNewEmployee());
		editor.onEntityType(ResourceStreamingRequest.T).addMetaData(initializer.processWithResourceStreaming());
		editor.onEntityType(ResourceStreamingRequest.T).addMetaData(initializer.hidden());
	}

	private void cortexModelMetaData() {
		ModelMetaDataEditor editor = modelApi.newMetaDataEditor(models.configuredDemoCortexServiceModel()).done();
		editor.onEntityType(TestAccessRequest.T).addMetaData(initializer.processWithTestAccess());
	}
}
