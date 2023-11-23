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

import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import com.braintribe.cfg.Required;

import tribefire.module.wire.contract.DeploymentContract;

/**
 * The name might change, it might later implement an interface (SystemDeployment?), currently it just serves the
 * purpose to support {@link DeploymentContract#runWhenSystemIsDeployed(Runnable)}
 * 
 * @author peter.gazdik
 */
public class SystemDeploymentListenerRegistry {

	private ExecutorService executor;
	private ReentrantLock lock = new ReentrantLock();

	private volatile List<Runnable> runnables = newList();

	@Required
	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public void onSystemDeployed() {
		lock.lock();
		try {
			for (Runnable r : runnables)
				executor.submit(r);

			runnables = null;
		} finally {
			lock.unlock();
		}
	}

	public void runWhenSystemIsDeployed(Runnable r) {
		if (runnables != null)
			if (rememberToRunLater(r))
				return;

		executor.submit(r);
	}

	private boolean rememberToRunLater(Runnable r) {
		lock.lock();
		try {
			return runnables != null && runnables.add(r);
		} finally {
			lock.unlock();
		}
	}

}
