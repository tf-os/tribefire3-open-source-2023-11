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
package com.braintribe.model.processing.etcd.service.wire.space;

import java.util.List;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.gm.service.wire.common.contract.CommonServiceProcessingContract;
import com.braintribe.gm.service.wire.common.contract.ServiceProcessingConfigurationContract;
import com.braintribe.integration.etcd.supplier.ClientSupplier;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.processing.etcd.service.EtcdBinaryProcessor;
import com.braintribe.model.processing.etcd.service.wire.contract.MainContract;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.resourceapi.base.BinaryRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class MainSpace implements MainContract {

	protected final static List<String> endpointUrls = List.of("http://localhost:2379");

	@Import
	private ServiceProcessingConfigurationContract serviceProcessingConfiguration;

	@Import
	private CommonServiceProcessingContract commonServiceProcessing;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		serviceProcessingConfiguration.registerServiceConfigurer(this::configureServices);
	}

	@Override
	public Evaluator<ServiceRequest> evaluator() {
		return commonServiceProcessing.evaluator();
	}

	private void configureServices(ConfigurableDispatchingServiceProcessor bean) {
		bean.removeInterceptor("auth");
		bean.register(BinaryRequest.T, etcdBinaryProcessor());
	}

	@Override
	@Managed
	public EtcdBinaryProcessor etcdBinaryProcessor() {
		EtcdBinaryProcessor bean = new EtcdBinaryProcessor();
		bean.setTtlInSeconds(60);
		// TODO: add authentication case here if needed
		bean.setClientSupplier(new ClientSupplier(endpointUrls, null, null));
		bean.setProject("etcd-binary-streamer-test");
		bean.setChunkSize(1 * (int) Numbers.MEGABYTE);
		return bean;
	}

	//
//	@Managed
//	private PersistenceGmSessionFactory sessionFactory() {
//		TestSessionFactory bean = new TestSessionFactory(evaluator());
//		bean.addAccess(access());
//		return bean;
//	}
//

}
