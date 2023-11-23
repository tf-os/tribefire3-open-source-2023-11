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
package com.braintribe.model.processing.ddra.endpoints.wire.space;

import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.ddra.endpoints.api.v1.ResponseCodeOverridingProcessor;
import com.braintribe.model.processing.ddra.endpoints.api.v1.SimpleZipperProcessor;
import com.braintribe.model.processing.ddra.endpoints.api.v1.TestAmbigousNestingProcessor;
import com.braintribe.model.processing.ddra.endpoints.api.v1.TestReasonServiceProcessor;
import com.braintribe.model.processing.ddra.endpoints.api.v1.ZipperProcessor;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.BasicTestServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.NeutralRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.NullRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ResponseCodeOverridingRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestAmbigiousNestingRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestReasoningServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ZipRequestSimple;
import com.braintribe.model.processing.ddra.endpoints.wire.contract.DdraTestContract;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.service.common.IdentityServiceProcessor;
import com.braintribe.model.prototyping.api.StaticPrototyping;
import com.braintribe.model.prototyping.impl.StaticPrototypingProcessor;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class DdraTestSpace implements DdraTestContract{

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;
	
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
	}
	
	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor("auth");
	
		//bean.register(Authenticate.T, IdentityServiceProcessor.instance());
		bean.register(GmqlRequest.T, IdentityServiceProcessor.instance());
		bean.register(OpenUserSessionWithUserAndPassword.T, IdentityServiceProcessor.instance());
		bean.register(BasicTestServiceRequest.T, IdentityServiceProcessor.instance());
		bean.register(ZipRequest.T, new ZipperProcessor());
		bean.register(ZipRequestSimple.T, new SimpleZipperProcessor());
		bean.register(StaticPrototyping.T, new StaticPrototypingProcessor());
		bean.register(ResponseCodeOverridingRequest.T, new ResponseCodeOverridingProcessor());
		bean.register(NullRequest.T, (c, r) -> null);
		bean.register(NeutralRequest.T, (c, r) -> Neutral.NEUTRAL);
		bean.register(TestReasoningServiceRequest.T, new TestReasonServiceProcessor());
		bean.register(TestAmbigiousNestingRequest.T, new TestAmbigousNestingProcessor());
	}
	
	@Override
	public Evaluator<ServiceRequest> serviceRequestEvaluator() {
		return commonServiceProcessing.evaluator();
	}
}
