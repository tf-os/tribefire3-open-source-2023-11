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
package tribefire.platform.impl.binding;

import com.braintribe.cfg.Required;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DirectComponentBinder;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.deployment.api.UndeploymentContext;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.processing.worker.api.WorkerManager;

public class WorkerBinder implements DirectComponentBinder<com.braintribe.model.extensiondeployment.Worker, Worker> {
	
	private WorkerManager workerManager;

	@Required
	public void setWorkerManager(WorkerManager workerManager) {
		this.workerManager = workerManager;
	}
	
	@Override
	public EntityType<com.braintribe.model.extensiondeployment.Worker> componentType() {
		return com.braintribe.model.extensiondeployment.Worker.T;
	}

	@Override
	public Worker bind(MutableDeploymentContext<com.braintribe.model.extensiondeployment.Worker, Worker> context) throws DeploymentException {
		Worker worker = context.getInstanceToBeBound();
		
		workerManager.deploy(worker);
		
		return worker;
	}

	@Override
	public void unbind(UndeploymentContext<com.braintribe.model.extensiondeployment.Worker, Worker> context) {
		workerManager.undeploy(context.getBoundInstance());
	}
	
	@Override
	public Class<?>[] componentInterfaces() {
		return new Class<?>[] { Worker.class };
	}

}
