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
package com.braintribe.gm.service.access.wire.common.space;

import com.braintribe.gm.service.access.BasicAccessProcessingConfiguration;
import com.braintribe.gm.service.access.wire.common.contract.CommonAccessProcessingContract;
import com.braintribe.gm.service.wire.common.space.CommonServiceProcessingSpace;
import com.braintribe.gm.service.wire.common.space.ServiceProcessingConfigurationSpace;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;

@Managed
public class CommonAccessProcessingSpace implements CommonAccessProcessingContract {
	@Import
	private ServiceProcessingConfigurationSpace serviceProcessingConfiguration;
	@Import
	private AccessProcessingConfigurationSpace accessProcessingConfiguration;
	@Import
	private CommonServiceProcessingSpace commonServiceProcessing;
	@Import
	private SessionsSpace sessions;

	@Managed
	public BasicAccessProcessingConfiguration accessRegistry() {
		BasicAccessProcessingConfiguration bean = new BasicAccessProcessingConfiguration();
		bean.setAccessConfigurers(accessProcessingConfiguration.accessConfigurers());
		bean.setSessionFactory(sessionFactory());

		return bean;
	}

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		serviceProcessingConfiguration.registerServiceConfigurer(c -> accessRegistry().accept(c));
	}

	@Override
	public PersistenceGmSessionFactory sessionFactory() {
		return sessions.sessionFactory();
	}
}
