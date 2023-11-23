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
package com.braintribe.gm.security.test.auth.wire.space;

import com.braintribe.gm.security.test.auth.model.ServiceRequest1;
import com.braintribe.gm.security.test.auth.model.ServiceRequest2;
import com.braintribe.gm.security.test.auth.model.ServiceRequest3;
import com.braintribe.gm.security.test.auth.model.ServiceRequest4;
import com.braintribe.gm.security.test.auth.model.ServiceRequest5;
import com.braintribe.gm.security.test.auth.processing.ServiceProcessor1;
import com.braintribe.gm.security.test.auth.processing.ServiceProcessor2;
import com.braintribe.gm.security.test.auth.processing.ServiceProcessor3;
import com.braintribe.gm.security.test.auth.processing.ServiceProcessor4;
import com.braintribe.gm.security.test.auth.processing.ServiceProcessor5;
import com.braintribe.gm.security.test.auth.wire.contract.AuthorizingServiceProcessorTestContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.securityservice.SecurityRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class AuthorizingServiceProcessorTestSpace implements AuthorizingServiceProcessorTestContract {

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;
	
	@Import
	private CommonServiceProcessingContract commonServiceProcessing;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
		serviceProcessingConfiguration.registerSecurityConfigurer(this::configureSecurity);
	}
	
	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.register(ServiceRequest1.T, serviceProcessor1());
		bean.register(ServiceRequest2.T, serviceProcessor2());
		bean.register(ServiceRequest3.T, serviceProcessor3());
		bean.register(ServiceRequest4.T, serviceProcessor4());
		bean.register(ServiceRequest5.T, serviceProcessor5());
	}
	
	private void configureSecurity(InMemorySecurityServiceProcessor bean) {
		User user = User.T.create();
		user.setId("tester");
		user.setName("tester");
		user.setPassword("7357");

		bean.addUser(user);
	}
	
	@Managed
	private ServiceProcessor1 serviceProcessor1() {
		ServiceProcessor1 bean = new ServiceProcessor1();
		return bean;
	}
	
	@Managed
	private ServiceProcessor2 serviceProcessor2() {
		ServiceProcessor2 bean = new ServiceProcessor2();
		return bean;
	}
	
	@Managed
	private ServiceProcessor3<ServiceRequest3, ?> serviceProcessor3() {
		ServiceProcessor3<ServiceRequest3, ?> bean = new ServiceProcessor3<>();
		return bean;
	}
	
	@Managed
	private ServiceProcessor4<ServiceRequest4, ?> serviceProcessor4() {
		ServiceProcessor4<ServiceRequest4, ?> bean = new ServiceProcessor4<>();
		return bean;
	}

	@Managed
	private ServiceProcessor5 serviceProcessor5() {
		ServiceProcessor5 bean = new ServiceProcessor5();
		return bean;
	}
	
	@Managed
	private ServiceProcessor5 serviceProcessor5Deployed() {
		ServiceProcessor5 bean = new ServiceProcessor5("service5Id");
		return bean;
	}
	
	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}
}
