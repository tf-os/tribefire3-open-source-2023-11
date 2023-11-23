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

import java.util.function.Consumer;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.processing.async.impl.HubPromise;

import tribefire.platform.impl.deployment.ParallelDeploymentStatistics.PromiseStatistics;

public class DeploymentPromise extends HubPromise<Void> {

	private QueueStatus queueStatus = QueueStatus.pending;
	private final Object statusMonitor = new Object();
	private final Deployable deployable;
	private final Deployable originalDeployable;
	private final Consumer<? super DeploymentPromise> eagerAccessCallback;
	private final PromiseStatistics promiseStats;

	public DeploymentPromise(Deployable deployable, Deployable originalDeployable, Consumer<? super DeploymentPromise> eagerAccessCallback,
			ParallelDeploymentStatistics stats) {
		this.deployable = deployable;
		this.originalDeployable = originalDeployable;
		this.eagerAccessCallback = eagerAccessCallback;
		this.promiseStats = stats.acquirePromiseStats(deployable);
	}

	public void setQueueStatus(QueueStatus queueStatus) {
		this.queueStatus = queueStatus;
	}

	public QueueStatus getQueueStatus() {
		return queueStatus;
	}

	public Deployable getDeployable() {
		return deployable;
	}

	public Deployable getOriginalDeployable() {
		return originalDeployable;
	}

	public Object getStatusMonitor() {
		return statusMonitor;
	}

	public void notifyEagerAccess() {
		eagerAccessCallback.accept(this);
	}

	public PromiseStatistics getPromiseStatistics() {
		return this.promiseStats;
	}
}
