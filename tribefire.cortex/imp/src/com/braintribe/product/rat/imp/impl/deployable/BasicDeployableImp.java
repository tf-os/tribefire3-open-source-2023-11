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
package com.braintribe.product.rat.imp.impl.deployable;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.deploymentapi.request.DeployWithDeployables;
import com.braintribe.model.deploymentapi.request.RedeployWithDeployables;
import com.braintribe.model.deploymentapi.request.UndeployWithDeployables;
import com.braintribe.model.deploymentapi.response.DeployResponse;
import com.braintribe.model.deploymentapi.response.RedeployResponse;
import com.braintribe.model.deploymentapi.response.UndeployResponse;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImp;
import com.braintribe.product.rat.imp.ImpException;
import com.braintribe.product.rat.imp.impl.module.ModuleImpCave;
import com.braintribe.product.rat.imp.impl.service.CortexServiceHelperCave;
import com.braintribe.product.rat.imp.impl.service.ServiceHelper;
import com.braintribe.product.rat.imp.impl.service.ServiceHelperWithNotificationResponse;

/**
 * A {@link AbstractImp} specialized in {@link Deployable}
 */
public class BasicDeployableImp<T extends Deployable> extends AbstractImp<T> {

	public BasicDeployableImp(PersistenceGmSession session, T instance) {
		super(session, instance);
	}

	/**
	 * Returns a helper class, managing a deploy request for this imp's deployable
	 */
	public ServiceHelperWithNotificationResponse<DeployWithDeployables, DeployResponse> deployRequest() {
		return service().deployRequest(instance);
	}

	/**
	 * Returns a helper class, managing a undeploy request for this imp's deployable
	 */
	public ServiceHelperWithNotificationResponse<UndeployWithDeployables, UndeployResponse> undeployRequest() {
		return service().undeployRequest(instance);
	}

	/**
	 * Returns a helper class, managing a redeploy request for this imp's deployable
	 */
	public ServiceHelperWithNotificationResponse<RedeployWithDeployables, RedeployResponse> redeployRequest() {
		return service().redeployRequest(instance);
	}

	/**
	 * Calls a deploy request for this imp's deployable {@link ServiceHelper#call() in the imp way}
	 */
	public BasicDeployableImp<T> deploy() {
		deployRequest().call();
		return this;
	}

	/**
	 * Calls a undeploy request for this imp's deployable {@link ServiceHelper#call() in the imp way}
	 */
	public BasicDeployableImp<T> undeploy() {
		undeployRequest().call();
		return this;
	}

	/**
	 * Calls a redeploy request for this imp's deployable {@link ServiceHelper#call() in the imp way}
	 */
	public BasicDeployableImp<T> redeploy() {
		redeployRequest().call();
		return this;
	}

	/**
	 * {@link #commit()}s first and then calls a deploy request for this imp's deployable {@link ServiceHelper#call() in
	 * the imp way}
	 */
	public BasicDeployableImp<T> commitAndDeploy() {
		commit();
		return deploy();
	}

	/**
	 * {@link #commit()}s first and then calls a redeploy request for this imp's deployable {@link ServiceHelper#call()
	 * in the imp way}
	 */
	public BasicDeployableImp<T> commitAndRedeploy() {
		commit();
		return redeploy();
	}

	private CortexServiceHelperCave service() {
		return new CortexServiceHelperCave(session());
	}
	
	/**
	 * Finds the module with given <tt>globalId</tt> and sets this imp's deployable's <tt>module</tt> property to it.
	 * 
	 * @param globalId globalId of the module
	 * 
	 * @throws ImpException when the module was not found. 
	 */
	public BasicDeployableImp<T> setModule(String globalId) {
		Module module = new ModuleImpCave(session()).with(globalId).get();
		return setModule(module);
	}
	
	public BasicDeployableImp<T> setModule(Module module) {
		instance.setModule(module);
		return this;
	}

}
