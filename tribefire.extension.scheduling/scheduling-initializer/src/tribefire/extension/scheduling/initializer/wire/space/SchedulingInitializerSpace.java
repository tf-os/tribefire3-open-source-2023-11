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
package tribefire.extension.scheduling.initializer.wire.space;

import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.initializer.support.integrity.wire.contract.CoreInstancesContract;
import tribefire.cortex.initializer.support.wire.space.AbstractInitializerSpace;
import tribefire.extension.scheduling.initializer.wire.contract.ExistingInstancesContract;
import tribefire.extension.scheduling.initializer.wire.contract.RuntimePropertiesContract;
import tribefire.extension.scheduling.initializer.wire.contract.SchedulingInitializerContract;
import tribefire.extension.scheduling.initializer.wire.contract.SchedulingInitializerModelsContract;
import tribefire.extension.scheduling.templates.api.SchedulingTemplateContext;
import tribefire.extension.scheduling.templates.wire.contract.SchedulingTemplatesContract;

@Managed
public class SchedulingInitializerSpace extends AbstractInitializerSpace implements SchedulingInitializerContract {

	private static final String GLOBAL_ID_SCHEDULING_DB_CONNECTION_POOL = "scheduling-db-connection-pool";

	@Import
	private SchedulingInitializerModelsContract models;

	@Import
	private ExistingInstancesContract existingInstances;

	@Import
	private CoreInstancesContract coreInstances;

	@Import
	private RuntimePropertiesContract runtime;

	@Import
	private SchedulingTemplatesContract templates;

	@Override
	public void configure() {
		if (runtime.SCHEDULING_DEFAULT_CONFIG()) {
			SchedulingTemplateContext context = defaultContext();
			templates.configure(context);
		}
	}

	@Managed
	@Override
	public SchedulingTemplateContext defaultContext() {

		//@formatter:off
		SchedulingTemplateContext context = SchedulingTemplateContext.builder()
			.setDatabaseConnectionGlobalId(GLOBAL_ID_SCHEDULING_DB_CONNECTION_POOL)
			.setDatabaseUrl(runtime.SCHEDULING_DB_DATA_URL())
			.setDatabaseUser(runtime.SCHEDULING_DB_DATA_USER())
			.setDatabasePassword(runtime.SCHEDULING_DB_DATA_PASSWORD_ENCRYPTED())
			.setModule(existingInstances.module())
			.setName("Scheduling")
			.setIdPrefix("scheduling")
			.setEntityFactory(super::create)
			.setLookupFunction(super::lookup)
			.setLookupExternalIdFunction(super::lookupExternalId)
			.build();
		//@formatter:on
		return context;
	}
}
