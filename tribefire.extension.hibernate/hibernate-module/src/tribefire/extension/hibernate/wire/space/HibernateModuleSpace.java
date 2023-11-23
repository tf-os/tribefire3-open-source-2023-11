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
package tribefire.extension.hibernate.wire.space;

import com.braintribe.model.accessdeployment.hibernate.HibernateAccess;
import com.braintribe.model.accessdeployment.hibernate.selector.HibernateDbVendorSelector;
import com.braintribe.model.accessdeployment.hibernate.selector.HibernateDialectSelector;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.hibernate.meta.experts.HibernateDbVendorSelectorExpert;
import tribefire.extension.hibernate.meta.experts.HibernateDialectSelectorExpert;
import tribefire.module.api.DenotationMorpher;
import tribefire.module.api.InitializerBindingBuilder;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;
import tribefire.module.wire.contract.WebPlatformHardwiredExpertsContract;

/**
 * This module binds the basic components of the standard Hibernate extension, namely:
 * 
 * <ul>
 * <li>Experts for {@link HibernateDialectSelector} and {@link HibernateDbVendorSelector}.
 * <li>{@link DenotationMorpher} that converts {@link DatabaseConnectionPool} to {@link HibernateAccess}
 * <li>TODO HibernateAccess deployment expert
 * </ul>
 */
@Managed
public class HibernateModuleSpace implements TribefireModuleContract {

	// @formatter:off
	@Import private TribefireWebPlatformContract tfPlatform;
	@Import private WebPlatformHardwiredExpertsContract hardwiredExperts;

	@Import private HibernateDeployablesSpace hibernateDeployables;
	@Import private HibernateDenoTransSpace hibernateDenoTrans;
	@Import private HibernateInitializerSpace hibernateInitializer;
	// @formatter:on

	@Override
	public void bindHardwired() {
		hibernateDenoTrans.bind();

		hardwiredExperts.bindMetaDataSelectorExpert(HibernateDialectSelector.T, new HibernateDialectSelectorExpert());
		hardwiredExperts.bindMetaDataSelectorExpert(HibernateDbVendorSelector.T, new HibernateDbVendorSelectorExpert());
	}

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		hibernateDeployables.bindDeployables(bindings);
	}

	@Override
	public void bindInitializers(InitializerBindingBuilder bindings) {
		hibernateInitializer.bindInitializers(bindings);
	}

}
