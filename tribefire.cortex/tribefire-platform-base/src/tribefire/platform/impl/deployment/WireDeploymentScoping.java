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

import com.braintribe.cartridge.common.processing.deployment.DeploymentScope;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.deployment.api.DeploymentContext;
import com.braintribe.model.processing.deployment.api.DeploymentScoping;

/**
 * <p>
 * A {@link DeploymentScoping} implementation for the wire-based deployment scope ({@link DeploymentScope}).
 * 
 * @author dirk.scheffler
 */
public class WireDeploymentScoping implements DeploymentScoping {

	private DeploymentScope scope;

	@Required
	public void setScope(DeploymentScope scope) {
		this.scope = scope;
	}

	@Override
	public void push(DeploymentContext<?, ?> context) {
		scope.push(context);
	}

	@Override
	public DeploymentContext<?, ?> pop(DeploymentContext<?, ?> context) {
		return scope.pop();
	}

	@Override
	public void end(DeploymentContext<?, ?> context) {
		scope.end(context);
	}

}
