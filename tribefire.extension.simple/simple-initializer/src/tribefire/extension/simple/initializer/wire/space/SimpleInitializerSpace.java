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
package tribefire.extension.simple.initializer.wire.space;

import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.data.constraint.NonInstantiable;
import com.braintribe.model.meta.data.prompt.Hidden;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.simple.SimpleConstants;
import tribefire.extension.simple.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.simple.initializer.wire.contract.SimpleInitializerContract;
import tribefire.extension.simple.initializer.wire.contract.SimpleInitializerModelsContract;
import tribefire.extension.simple.model.data.Department;
import tribefire.extension.simple.model.data.Person;
import tribefire.extension.simple.model.deployment.access.SimpleInMemoryAccess;
import tribefire.extension.simple.model.deployment.service.SimpleEchoService;
import tribefire.extension.simple.model.deployment.terminal.SimpleWebTerminal;
import tribefire.extension.simple.model.service.SimpleEchoRequest;
import tribefire.module.wire.contract.ModelApiContract;

@Managed
public class SimpleInitializerSpace extends AbstractInitializerSpace implements SimpleInitializerContract {

	@Import
	private SimpleInitializerModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private ModelApiContract modelApi;

	@Managed
	@Override
	public SimpleInMemoryAccess simpleInMemoryAccess() {
		SimpleInMemoryAccess bean = create(SimpleInMemoryAccess.T);

		bean.setModule(existingInstances.simpleModule());
		bean.setName("Simple In Memory Access");
		bean.setExternalId(SimpleConstants.SIMPLE_ACCESS_EXTERNALID);
		bean.setMetaModel(models.configuredSimpleDataModel());
		bean.setServiceModel(models.configuredSimpleServiceModel());
		bean.setInitializeWithExampleData(true);

		return bean;
	}

	@Managed
	@Override
	public SimpleEchoService simpleEchoProcessor() {
		SimpleEchoService bean = create(SimpleEchoService.T);
		bean.setExternalId(SimpleConstants.SIMPLE_SERVICE_EXTERNALID);

		bean.setName(SimpleEchoService.T.getShortName());
		bean.setModule(existingInstances.simpleModule());

		bean.setDelay(0L);
		bean.setEchoCount(1);
		return bean;
	}

	@Managed
	@Override
	public SimpleWebTerminal simpleWebTerminal() {
		SimpleWebTerminal bean = create(SimpleWebTerminal.T);

		bean.setModule(existingInstances.simpleModule());
		bean.setExternalId(SimpleConstants.SIMPLE_WEBTERMINAL_EXTERNALID);
		bean.setName(SimpleWebTerminal.T.getShortName());
		bean.setPathIdentifier("simpleWebTerminal");

		// configure web terminal to show headers and parameters
		bean.setPrintHeaders(true);
		bean.setPrintParameters(true);

		return bean;
	}

	@Override
	public void addMetaDataToModels() {
		// set some metadata, e.g. hide a property or make an entity type not instantiable
		// (you can e.g. verify this via the tribefire Explorer)
		ModelMetaDataEditor editor;

		editor = modelApi.newMetaDataEditor(models.configuredSimpleDataModel()).done();
		editor.onEntityType(Person.T) //
				.addPropertyMetaData(Person.gender, hidden());
		editor.onEntityType(Department.T) //
				.addMetaData(nonInstantiable());

		editor = modelApi.newMetaDataEditor(models.configuredSimpleServiceModel()).done();
		editor.onEntityType(SimpleEchoRequest.T) //
				.addMetaData(processWithSimpleEchoProcessor());
	}

	@Managed
	private MetaData hidden() {
		Hidden bean = create(Hidden.T);
		bean.setInherited(false);

		return bean;
	}

	@Managed
	private MetaData nonInstantiable() {
		NonInstantiable bean = create(NonInstantiable.T);
		bean.setInherited(false);

		return bean;
	}

	@Managed
	private MetaData processWithSimpleEchoProcessor() {
		ProcessWith bean = create(ProcessWith.T);
		bean.setProcessor(simpleEchoProcessor());
		return bean;
	}

}
