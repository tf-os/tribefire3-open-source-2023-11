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
package com.braintribe.model.processing.deployment.api;

import java.util.List;

import com.braintribe.model.deployment.Deployable;

/**
 * The context for {@link DeploymentService}'s operations.
 * 
 *
 */
public interface DeploymentServiceContext {

	List<Deployable> deployables();

	default boolean areDeployablesFullyFetched() {
		return false;
	}
	
	/**
	 * Callback method which is called by the {@link DeploymentService} when all deployables are internally marked
	 * to be in deployment but just before the actual deployments.
	 */
	default void deploymentStarted() {
		// NO OP
	}
	
	/**
	 * Callback method which is called by the {@link DeploymentService} immediately after the deployment of given
	 * deployable is started.
	 * 
	 * @param deployable
	 *            the deployable whose deployment has just started
	 */
	default void started(Deployable deployable) {
		// NO OP
	}

	/**
	 * Callback method which is called by the {@link DeploymentService} immediately after the deployment of given
	 * deployable is succeeded.
	 * 
	 * @param deployable
	 *            the successfully deployed deployable
	 */
	default void succeeded(Deployable deployable) {
		// NO OP		
	}

	/**
	 * Callback method which is called by the {@link DeploymentService} immediately after the deployment of given
	 * deployable is failed.
	 * 
	 * @param deployable
	 *            the deployable whose deployment failed
	 * @param failure
	 *            the error that happened during the deployment attempt
	 */
	default void failed(Deployable deployable, Throwable failure) {
		// NO OP		
	}

}
