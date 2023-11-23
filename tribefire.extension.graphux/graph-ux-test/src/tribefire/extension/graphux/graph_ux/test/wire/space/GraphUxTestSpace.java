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
package tribefire.extension.graphux.graph_ux.test.wire.space;

import com.braintribe.gm.service.access.api.AccessProcessingConfiguration;
import com.braintribe.gm.service.access.wire.common.contract.AccessProcessingConfigurationContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.tools.gm.GmTestTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

import tribefire.extension.graphux.deployables.service.GraphUxServiceProcessor;
import tribefire.extension.graphux.graph_ux.test.wire.contract.GraphUxTestContract;
import tribefire.extension.graphux.model.service.GraphUxServiceRequest;
import tribefire.extension.simple.model.data.Company;

@Managed
public class GraphUxTestSpace implements GraphUxTestContract {
	
	private SmoodAccess access;
	
	@Import
	private AccessProcessingConfigurationContract accessProcessingConfiguration;

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;
	
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		accessProcessingConfiguration.registerAccessConfigurer(this::configureAccesses);
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
		serviceProcessingConfiguration.registerSecurityConfigurer(this::configureSecurity);
	}
	
	private void configureAccesses(AccessProcessingConfiguration configuration) {	
		configuration.registerAccess(access());
		configuration.registerAccessRequestProcessor(GraphUxServiceRequest.T, graphUxServiceProcessor());
	}
	
	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor("auth"); // remove this line if you want your requests authorized while testing	
	}

	private void configureSecurity(InMemorySecurityServiceProcessor bean) {
		// TODO configure security IF your requests are to be authorized while testing
		// (make sure the 'auth' interceptor is not removed in that case in the 'configureServices' method)
	}
	
	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}
	
	@Override
	public SmoodAccess access() {
		if (access == null) access = GmTestTools.newSmoodAccessMemoryOnly("test.access", Company.T.getModel().getMetaModel());
		return access;
	}
	
	@Managed
	private GraphUxServiceProcessor graphUxServiceProcessor() {
		GraphUxServiceProcessor bean = new GraphUxServiceProcessor();
		return bean;
	}
	
	
	
}
