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
package tribefire.extension.vitals.dmb.dmb_locking.wire.space;

import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.lock.api.LockingException;
import com.braintribe.model.processing.lock.dmb.impl.DmbLockManager;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.extension.dmb.locking.model.deployment.DmbLocking;
import tribefire.module.wire.contract.ClusterBindersContract;
import tribefire.module.wire.contract.TribefireModuleContract;
import tribefire.module.wire.contract.TribefirePlatformContract;

/**
 * This module's javadoc is yet be written.
 */
@Managed
public class DmbLockingModuleSpace implements TribefireModuleContract {

	@Import
	private TribefirePlatformContract tfPlatform;

	@Import
	private ClusterBindersContract clusterBinders;

	@Override
	public void bindDeployables(DenotationBindingBuilder bindings) {
		bindings.bind(DmbLocking.T) //
				.component(clusterBinders.lockingManager()) //
				.expertFactory(this::lockManager);
	}

	private DmbLockManager lockManager(ExpertContext<DmbLocking> expertContext) {
		DmbLocking deployable = expertContext.getDeployable();

		DmbLockManager expert = null;
		try {
			expert = new DmbLockManager();
		} catch (LockingException e) {
			throw Exceptions.unchecked(e,
					"Cannot create an instance of lock manager: " + DmbLockManager.class.getName());
		}
		if (deployable.getEvictionThreshold() != null) {
			expert.setEvictionThreshold(deployable.getEvictionThreshold());
		}
		if (deployable.getEvictionInterval() != null) {
			expert.setEvictionInterval(deployable.getEvictionInterval());
		}

		return expert;
	}
}
