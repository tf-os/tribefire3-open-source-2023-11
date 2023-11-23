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
package tribefire.platform.wire.space.cortex;

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;

import java.util.Collections;

import com.braintribe.model.cortexapi.access.ExplorerStyle;
import com.braintribe.model.processing.cortex.service.access.GarbageCollectionProcessor;
import com.braintribe.model.processing.cortex.service.access.SetupAccessProcessor;
import com.braintribe.model.processing.cortex.service.connection.DbSchemaRequestProcessor;
import com.braintribe.model.processing.cortex.service.connection.TestConnectionRequestProcessor;
import com.braintribe.model.processing.cortex.service.model.ModelRequestProcessor;
import com.braintribe.model.processing.cortex.service.workbench.CreateServiceRequestTemplateProcessor;
import com.braintribe.model.processing.deployment.PublishModelProcessor;
import com.braintribe.model.processing.exchange.service.ExchangeRequestProcessor;
import com.braintribe.model.processing.garbagecollection.InMemoryGarbageCollection;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.service.ExternalTribefireRequestProcessor;
import tribefire.platform.impl.service.RuntimePropertiesProcessor;
import tribefire.platform.wire.space.common.HttpSpace;
import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.common.ResourceProcessingSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.accesses.DefaultDeployablesSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.services.AccessServiceSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.system.ChecksSpace;

@Managed
public class ServicesSpace implements WireSpace {

	// @formatter:off
	@Import private AccessServiceSpace accessService;	
	@Import private AuthContextSpace authContext;
	@Import private ChecksSpace checks;
	@Import private CortexAccessSpace cortexAccess;
	@Import private DefaultDeployablesSpace defaultDeployables;	
	@Import private DeploymentSpace deployment;
	@Import private GmSessionsSpace gmSessions;
	@Import private HttpSpace http;
	@Import private MarshallingSpace marshalling;	
	@Import private ResourceProcessingSpace resourceProcessing;	
	@Import private RpcSpace rpc;
	@Import private WorkbenchSpace workbench;
	// @formatter:on

	@Managed
	public PublishModelProcessor publishModelProcessor() {
		PublishModelProcessor bean = new PublishModelProcessor();
		bean.setUserNameSupplier(authContext.currentUser().userNameProvider()::get);
		bean.setResourceBuilder(resourceProcessing.transientResourceBuilder());

		return bean;
	}

	@Managed
	public RuntimePropertiesProcessor runtimePropertiesProcessor() {
		RuntimePropertiesProcessor bean = new RuntimePropertiesProcessor();
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		return bean;
	}

	@Managed
	public GarbageCollectionProcessor garbageCollectionProcessor() {
		GarbageCollectionProcessor bean = new GarbageCollectionProcessor();
		bean.setGarbageCollection(new InMemoryGarbageCollection());
		bean.setSessionFactory(gmSessions.systemSessionFactory());
		return bean;

	}
	@Managed
	public SetupAccessProcessor setupAccessProcessor() {
		SetupAccessProcessor bean = new SetupAccessProcessor();
		bean.setDefaultAspects(defaultDeployables.defaultAspects());
		bean.setSessionFactory(gmSessions.sessionFactory());
		bean.setStandardWorkbenchInstructions(workbench.defaultInstructions());
		bean.setStyleInstructions( //
				map( //
						entry(ExplorerStyle.tribefireOrange, workbench.tribefireOrangeStyleInstructions()), //
						entry(ExplorerStyle.grayishBlue, workbench.grayishBlueStyleInstructions()) //
				)); //

		return bean;
	}

	@Managed
	@Deprecated
	public ExchangeRequestProcessor exchangeRequestProcessor() {
		ExchangeRequestProcessor bean = new ExchangeRequestProcessor();
		bean.setStageResolver((r) -> cortexAccess.access().findStageForReference(r));
		bean.setSystemIndices(Collections.singleton("initial"));
		bean.setUserSessionProvider(authContext.currentUser().userSessionSupplier());
		return bean;
	}

	@Managed
	public ModelRequestProcessor modelRequestProcessor() {
		ModelRequestProcessor bean = new ModelRequestProcessor();
		return bean;
	}

	@Managed
	public CreateServiceRequestTemplateProcessor createServiceRequestTemplateProcessor() {
		CreateServiceRequestTemplateProcessor bean = new CreateServiceRequestTemplateProcessor();
		bean.setSessionFactory(gmSessions.systemSessionFactory());
		bean.setAccessService(accessService.service());
		return bean;
	}

	@Managed
	public TestConnectionRequestProcessor createTestConnectionRequestProcessor() {
		TestConnectionRequestProcessor bean = new TestConnectionRequestProcessor();
		bean.setDatabaseConnectionCheck(checks.selectedDatabaseConnectionsCheck());
		return bean;
	}

	@Managed
	public DbSchemaRequestProcessor createDbSchemaRequestProcessor() {
		DbSchemaRequestProcessor bean = new DbSchemaRequestProcessor();
		bean.setDeployRegistry(deployment.registry());
		return bean;
	}

	@Managed
	public ExternalTribefireRequestProcessor externalTribefireRequestProcessor() {
		ExternalTribefireRequestProcessor bean = new ExternalTribefireRequestProcessor();
		bean.setMarshallerRegistry(marshalling.registry());
		bean.setHttpClientProvider(http.clientProvider());
		bean.setMimeType("application/json");
		return bean;
	}
}
