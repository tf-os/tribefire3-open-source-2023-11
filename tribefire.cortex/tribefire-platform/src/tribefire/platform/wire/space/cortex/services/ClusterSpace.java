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
package tribefire.platform.wire.space.cortex.services;

import java.util.concurrent.TimeUnit;

import com.braintribe.common.concurrent.ScheduledTask;
import com.braintribe.common.concurrent.TaskScheduler;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.cortex.deployment.CortexConfiguration;
import com.braintribe.model.processing.deployment.api.DcProxyListener;
import com.braintribe.model.processing.deployment.api.SchrodingerBean;
import com.braintribe.model.processing.lock.api.Locking;
import com.braintribe.model.processing.lock.dmb.impl.DmbLocking;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.leadership.api.LeadershipManager;
import tribefire.cortex.leadership.impl.LockingBasedLeadershipManager;
import tribefire.module.wire.contract.ClusterContract;
import tribefire.platform.wire.space.SchrodingerBeansSpace;
import tribefire.platform.wire.space.common.BindersSpace;

@Managed
public class ClusterSpace implements ClusterContract {

	public static final String DEFAULT_LOCKING_EXTERNAL_ID = "default.Locking";

	private static int LEADERSHIP_REFRESH_INTERVAL_MS = 10_000;

	@Import
	private BindersSpace binders;

	@Import
	private SchrodingerBeansSpace schrodingerBeans;

	@Import
	private WorkerSpace worker;

	@Override
	@Managed
	public Locking locking() {
		return lockingSchrodingerBean().proxy();
	}

	@Override
	@Managed
	public LeadershipManager leadershipManager() {
		LockingBasedLeadershipManager bean = new LockingBasedLeadershipManager();
		bean.setLocking(locking());
		bean.setName("Default Platform Leadership Manager");

		configureLeadershipRefreshOnceLockingAvailable(bean);

		return bean;
	}

	private void configureLeadershipRefreshOnceLockingAvailable(LockingBasedLeadershipManager bean) {
		LeadershipRefresher refresher = new LeadershipRefresher(bean, worker.taskScheduler());

		lockingSchrodingerBean() //
				.proxyDelegation() //
				.addDcProxyListener(refresher);
	}

	static class LeadershipRefresher implements DcProxyListener {
		private final LockingBasedLeadershipManager leadershipManager;
		private final TaskScheduler taskScheduler;

		private ScheduledTask task;

		public LeadershipRefresher(LockingBasedLeadershipManager leadershipManager, TaskScheduler taskScheduler) {
			this.leadershipManager = leadershipManager;
			this.taskScheduler = taskScheduler;
		}

		@Override
		public void onDefaultDelegateSet(Object defaultDelegate) {
			onDelegateSet(defaultDelegate);
		}

		@Override
		public void onDelegateSet(Object delegate) {
			task = taskScheduler.scheduleAtFixedRate( //
					"leadership-refresher", //
					() -> leadershipManager.refreshLeadershipsForEligibleDomains(), //
					LEADERSHIP_REFRESH_INTERVAL_MS, LEADERSHIP_REFRESH_INTERVAL_MS, TimeUnit.MILLISECONDS) //
					.done();
		}

		@Override
		public void onDelegateCleared(Object delegate) {
			if (task != null) {
				task.cancel();
				task = null;
			}
		}
	}

	@Managed
	public SchrodingerBean<Locking> lockingSchrodingerBean() {
		return schrodingerBeans.newBean("Locking", CortexConfiguration::getLocking, binders.locking());
	}

	@Managed
	public Locking defaultLocking() {
		try {
			return new DmbLocking();

		} catch (Exception e) {
			throw Exceptions.unchecked(e, "Failed to create LockManager.");
		}
	}

}