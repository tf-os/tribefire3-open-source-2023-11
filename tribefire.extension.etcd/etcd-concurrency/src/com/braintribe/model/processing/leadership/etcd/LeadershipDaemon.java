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
package com.braintribe.model.processing.leadership.etcd;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.braintribe.logging.Logger;

public class LeadershipDaemon implements Runnable {

	protected static Logger logger = Logger.getLogger(LeadershipDaemon.class);

	protected EtcdLeadershipManager manager = null;
	protected boolean exitLoop = false;

	protected long lastElection = 0L;
	protected long lastCheck = 0L;

	protected CountDownLatch hasStopped = new CountDownLatch(1);

	public LeadershipDaemon(EtcdLeadershipManager manager) {
		this.manager = manager;
	}

	@Override
	public void run() {

		logger.trace(() -> "Running leadership daemon");

		try {
			if (exitLoop) {
				logger.debug(() -> "Not executing daemon further as this daemon should shut down.");
				hasStopped.countDown();
				return;
			}

			long now = System.currentTimeMillis();

			boolean doElection = false;
			if ((now - lastElection) > this.manager.checkInterval) {
				logger.trace(() -> "The election interval "+this.manager.checkInterval+" has been exceeded.");
				doElection = true;
			}

			if (doElection) {

				this.performElection(null);
				logger.trace(() -> "Performed election");
				lastElection = now;

			}
		} catch(Exception e) {
			logger.error("Error while executing LeadershipDaemon", e);
		}

		logger.trace(() -> "Leadership daemon is done with this run.");
	}

	protected void performElection(String domainId) {

		Set<LeadershipWrapper> leadershipListeners = this.manager.getLeadershipListeners(domainId);
		
		logger.trace(() -> "Performing an election run among "+leadershipListeners.size()+" registered listeners.");
		
		for (LeadershipWrapper listenerWrapper : leadershipListeners) {
			if (listenerWrapper.isLeader()) {
				logger.trace(() -> "Currently assumed leader will be checked: "+listenerWrapper);
				
				if (!manager.refreshLeadership(listenerWrapper.getDomainId(), listenerWrapper.getCandidateId())) {
					
					logger.trace(() -> "Revoking leadership in domain "+listenerWrapper.getDomainId()+" from candidate "+listenerWrapper.getCandidateId()+" located at "+listenerWrapper.getCandidate().getInstanceId());

					listenerWrapper.setLeader(false);
					listenerWrapper.getListener().surrenderLeadership(new EtcdLeadershipHandle(listenerWrapper.getDomainId(), listenerWrapper.getCandidateId(), manager.etcdProcessing));
					this.manager.notifyLeaderToSurrender(listenerWrapper.getDomainId(), listenerWrapper.getCandidateId());
				} else {
					logger.trace(() -> "The current leader is alive and will not be changed: "+listenerWrapper);
				}
				
			} else {
				logger.trace(() -> "Probing to make Listener leader: "+listenerWrapper);
				
				if (manager.tryLeadership(listenerWrapper.getDomainId(), listenerWrapper.getCandidateId())) {
					
					logger.trace(() -> "Granting leadership in domain "+listenerWrapper.getDomainId()+" to candidate "+listenerWrapper.getCandidateId()+" located at "+listenerWrapper.getCandidate().getInstanceId());
					
					listenerWrapper.setLeader(true);
					listenerWrapper.getListener().onLeadershipGranted(new EtcdLeadershipHandle(listenerWrapper.getDomainId(), listenerWrapper.getCandidateId(), manager.etcdProcessing));
					manager.notifyCandidatesOfNewLeader(listenerWrapper.getDomainId(), listenerWrapper.getCandidateId());
				} else {
					logger.trace(() -> "Probing did not succeed to make Listener leader: "+listenerWrapper);
				}
			}

		}

	}

	public void stopDaemon() {
		this.exitLoop = true;
		try {
			if (!hasStopped.await(30L, TimeUnit.SECONDS)) {
				logger.error("Timeout while waiting for stop.");
			}
		} catch (InterruptedException e) {
			logger.trace(() -> "Interrupted while waiting for stop.", e);
		}
	}

}
