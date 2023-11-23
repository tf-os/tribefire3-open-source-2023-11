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

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import com.braintribe.exception.ThrowableNormalizer;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.deployment.api.BasicDeployContext;
import com.braintribe.model.processing.deployment.api.DeploymentService;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * @author peter.gazdik
 */
public class PreCortexDeployer extends BasicDeployContext {

	public static void doPreCortexDeploy(DeploymentService deploymentService, PersistenceGmSession session, List<Deployable> deployables) {
		PreCortexDeployer deployContext = new PreCortexDeployer(session, deployables);

		deploymentService.deploy(deployContext);

		deployContext.failIfErrors();
	}

	private final List<Throwable> errors = newList();

	public PreCortexDeployer(PersistenceGmSession session, List<Deployable> deployables) {
		super(session, deployables);
	}

	@Override
	public void failed(Deployable deployable, Throwable failure) {
		errors.add(failure);
	}

	public void failIfErrors() {
		if (errors.isEmpty())
			return;

		if (errors.size() == 1)
			throwUnchecked(first(errors));

		throwSingleUnchecked();
	}

	private void throwUnchecked(Throwable t) {
		throw new RuntimeException(new ThrowableNormalizer(t).asThrowableOrThrowUnchecked());
	}

	private void throwSingleUnchecked() {
		RuntimeException e = new RuntimeException("Error(s) occurred while pre-cortex deployment");
		errors.forEach(e::addSuppressed);
		throw e;
	}

}
