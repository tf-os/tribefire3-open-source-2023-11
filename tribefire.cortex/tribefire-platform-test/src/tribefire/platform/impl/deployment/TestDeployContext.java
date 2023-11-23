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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.deployment.api.DeployContext;
import com.braintribe.model.processing.deployment.api.UndeployContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * @author christina.wilpernig
 */
public class TestDeployContext implements DeployContext, UndeployContext {

	private PersistenceGmSession session;
	private List<Deployable> deployables;
	
	public Set<Deployable> successfulDeployables = new HashSet<>();
	public Map<Deployable,Throwable> failedDeployables = new HashMap<>();

	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}
	
	public void setDeployables(List<Deployable> deployables) {
		this.deployables = deployables;
	}
	
	@Override
	public List<Deployable> deployables() {
		return deployables;
	}

	@Override
	public void succeeded(Deployable deployable) {
		successfulDeployables.add(deployable);
	}

	@Override
	public void failed(Deployable deployable, Throwable failure) {
		failedDeployables.put(deployable, failure);
	}

	@Override
	public PersistenceGmSession session() {
		return session;
	}

}
