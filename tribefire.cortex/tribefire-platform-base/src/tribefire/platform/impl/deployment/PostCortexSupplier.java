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
package tribefire.platform.impl.deployment;

import java.util.function.Supplier;

import com.braintribe.cfg.Required;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployRegistryListener;
import com.braintribe.model.processing.deployment.api.DeployedUnit;

/**
 * Wrapper around a supplier that throws an exception if the {@link #get()} method is called before cortex is deployed. Useful for cases when
 * resolving the to-be-supplied object depends on the cortex or triggers cortex deployment (e.g. DCSA shared storage).
 * 
 * @author peter.gazdik
 */
public class PostCortexSupplier<T> implements Supplier<T>, DeployRegistryListener {

	private String componentName;
	private Supplier<? extends T> delegate;
	private DeployRegistry deployRegistry;

	private volatile boolean cortexDeployed;

	@Required
	public void setSuppliedCommponentName(String componentName) {
		this.componentName = componentName;
	}

	@Required
	public void setDelegate(Supplier<? extends T> delegate) {
		this.delegate = delegate;
	}

	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
		this.deployRegistry.addListener(this);
	}

	@Override
	public T get() {
		if (!cortexDeployed)
			throw new IllegalStateException("Cannot access '" + componentName + "' before cortex is deployed!");

		return delegate.get();
	}

	@Override
	public void onDeploy(Deployable deployable, DeployedUnit deployedUnit) {
		if ("cortex".equals(deployable.getExternalId())) {
			cortexDeployed = true;
			deployRegistry.removeListener(this);
		}
	}

	@Override
	public void onUndeploy(Deployable deployable, DeployedUnit deployedUnit) {
		// NO OP
	}

}
