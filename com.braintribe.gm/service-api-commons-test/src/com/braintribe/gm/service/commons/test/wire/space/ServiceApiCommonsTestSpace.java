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
package com.braintribe.gm.service.commons.test.wire.space;

import com.braintribe.gm.service.access.api.AccessProcessingConfiguration;
import com.braintribe.gm.service.access.wire.common.contract.AccessProcessingConfigurationContract;
import com.braintribe.gm.service.commons.test.model.EvalTestAccessAuthRequest;
import com.braintribe.gm.service.commons.test.model.EvalTestAccessRequest;
import com.braintribe.gm.service.commons.test.model.EvalTestServiceRequest1;
import com.braintribe.gm.service.commons.test.model.EvalTestServiceRequest2;
import com.braintribe.gm.service.commons.test.model.EvalTestServiceRequest3;
import com.braintribe.gm.service.commons.test.model.EvalTestServiceRequest4;
import com.braintribe.gm.service.commons.test.model.ServiceRequest1;
import com.braintribe.gm.service.commons.test.model.ServiceRequest2;
import com.braintribe.gm.service.commons.test.model.ServiceRequest3;
import com.braintribe.gm.service.commons.test.model.ServiceRequest4;
import com.braintribe.gm.service.commons.test.model.ServiceRequest5;
import com.braintribe.gm.service.commons.test.processing.EvalTestServiceProcessor1;
import com.braintribe.gm.service.commons.test.processing.EvalTestServiceProcessor2;
import com.braintribe.gm.service.commons.test.processing.EvalTestServiceProcessor3;
import com.braintribe.gm.service.commons.test.processing.EvalTestServiceProcessor4;
import com.braintribe.gm.service.commons.test.processing.ServiceProcessor1;
import com.braintribe.gm.service.commons.test.processing.ServiceProcessor2;
import com.braintribe.gm.service.commons.test.processing.ServiceProcessor3;
import com.braintribe.gm.service.commons.test.processing.ServiceProcessor4;
import com.braintribe.gm.service.commons.test.processing.ServiceProcessor5;
import com.braintribe.gm.service.commons.test.wire.contract.ServiceApiCommonsTestContract;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.securityservice.commons.service.InMemorySecurityServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class ServiceApiCommonsTestSpace implements ServiceApiCommonsTestContract {

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
		configuration.registerAccess("test.access", User.T.getModel().getMetaModel());
		configuration.registerAccessRequestProcessor(EvalTestAccessRequest.T, evalTestAccessRequestProcessor());
		configuration.registerAccessRequestProcessor(EvalTestAccessAuthRequest.T, evalTestAccessAuthRequestProcessor());
	}

	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.register(ServiceRequest1.T, serviceProcessor1());
		bean.register(ServiceRequest2.T, serviceProcessor2());
		bean.register(ServiceRequest3.T, serviceProcessor3());
		bean.register(ServiceRequest4.T, serviceProcessor4());
		bean.register(ServiceRequest5.T, serviceProcessor5());
		bean.register(EvalTestServiceRequest1.T, evalTestServiceProcessor1());
		bean.register(EvalTestServiceRequest2.T, evalTestServiceProcessor2());
		bean.register(EvalTestServiceRequest3.T, evalTestServiceProcessor3());
		bean.register(EvalTestServiceRequest4.T, evalTestServiceProcessor4());
	}

	private AccessRequestProcessor<EvalTestAccessRequest, String> evalTestAccessRequestProcessor() {
		AccessRequestProcessor<EvalTestAccessRequest, String> bean = context -> context.getSession().getAccessId();

		return bean;
	}

	private AccessRequestProcessor<EvalTestAccessAuthRequest, String> evalTestAccessAuthRequestProcessor() {
		AccessRequestProcessor<EvalTestAccessAuthRequest, String> bean = context -> {
			SessionAuthorization sessionAuthorization = context.getSession().getSessionAuthorization();
			return sessionAuthorization.getUserName();
		};

		return bean;
	}

	@Managed
	public EvalTestServiceProcessor1 evalTestServiceProcessor1() {
		EvalTestServiceProcessor1 bean = new EvalTestServiceProcessor1();
		return bean;
	}

	@Managed
	public EvalTestServiceProcessor2 evalTestServiceProcessor2() {
		EvalTestServiceProcessor2 bean = new EvalTestServiceProcessor2();
		return bean;
	}

	@Managed
	public EvalTestServiceProcessor3<EvalTestServiceRequest3, Number> evalTestServiceProcessor3() {
		EvalTestServiceProcessor3<EvalTestServiceRequest3, Number> bean = new EvalTestServiceProcessor3<>();
		return bean;
	}

	@Managed
	public EvalTestServiceProcessor4<EvalTestServiceRequest3, Number> evalTestServiceProcessor4() {
		EvalTestServiceProcessor4<EvalTestServiceRequest3, Number> bean = new EvalTestServiceProcessor4<>();
		return bean;
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
