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

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.integration.etcd.EtcdProcessing;
import com.braintribe.logging.Logger;
import com.braintribe.model.leadership.Candidate;
import com.braintribe.model.leadership.CandidateType;
import com.braintribe.model.leadership.service.LeadershipGranted;
import com.braintribe.model.leadership.service.SurrenderLeadership;
import com.braintribe.model.processing.leadership.CandidateIdentification;
import com.braintribe.model.processing.leadership.LeadershipListenerWrapper;
import com.braintribe.model.processing.leadership.LeadershipManagerBase;
import com.braintribe.model.processing.leadership.api.LeadershipException;
import com.braintribe.model.processing.leadership.api.LeadershipListener;
import com.braintribe.model.processing.leadership.api.LeadershipManager;
import com.braintribe.model.service.api.InstanceId;

import io.etcd.jetcd.Client;

public class EtcdLeadershipManager extends LeadershipManagerBase implements LeadershipManager {

	private static Logger logger = Logger.getLogger(EtcdLeadershipManager.class);

	protected static String leadershipPrefix = "leadership/";
	protected EtcdProcessing etcdProcessing;

	protected Supplier<Client> clientSupplier;

	protected LeadershipDaemon leadershipDaemon = null;

	protected Map<String, Map<String, LeadershipWrapper>> listeners = new HashMap<>();
	protected ReentrantLock localListenersLock = new ReentrantLock();

	protected long defaultLeadershipTimeout = Numbers.MILLISECONDS_PER_SECOND * 20; // 20 s
	protected long defaultCandidateTimeout = Numbers.MILLISECONDS_PER_MINUTE; // 1 min

	protected long checkInterval = Numbers.MILLISECONDS_PER_SECOND * 5; // 5 s

	@Override
	public void postConstruct() {

		super.postConstruct();

		logger.info(() -> "Starting leadership manager.");

		etcdProcessing = new EtcdProcessing(clientSupplier);
	}

	@Override
	public void pluginPostConstruct() {
		super.pluginPostConstruct();

		this.leadershipDaemon = new LeadershipDaemon(this);

		if (userSessionScoping != null) {
			super.executorService.scheduleWithFixedDelay(userSessionScoping.forDefaultUser().scoped(leadershipDaemon), 0L, super.daemonInterval,
					TimeUnit.MILLISECONDS);
		} else {
			super.executorService.scheduleWithFixedDelay(leadershipDaemon, 0L, super.daemonInterval, TimeUnit.MILLISECONDS);
		}

		logger.info(() -> "Started leadership daemon to be executed every " + super.daemonInterval + " ms");

	}

	@Override
	public void preDestroy() {

		logger.info(() -> "Stopping leadership manager.");

		if (this.leadershipDaemon != null) {
			logger.debug(() -> "Stopping leadership daemon.");
			this.leadershipDaemon.stopDaemon();
			this.leadershipDaemon = null;
		}

		etcdProcessing.preDestroy();

		super.preDestroy();

		logger.info(() -> "Stopped leadership manager.");
	}

	@Override
	public void addLeadershipListener(String domainId, String candidateId, LeadershipListener listener) throws LeadershipException {

		localListenersLock.lock();
		try {
			Map<String, LeadershipWrapper> map = listeners.computeIfAbsent(domainId, d -> new HashMap<>());

			LeadershipWrapper wrapper = map.get(candidateId);
			if (wrapper == null) {

				Candidate candidate = Candidate.T.create();
				candidate.setInstanceId(localInstanceId);
				candidate.setCandidateId(candidateId);
				candidate.setDomainId(domainId);
				candidate.setCandidateType(CandidateType.Dbl);
				candidate.setPingTimestamp(new Date());
				candidate.setIsLeader(false);

				wrapper = new LeadershipWrapper(domainId, candidateId, listener, candidate);

				map.put(candidateId, wrapper);
			}

		} finally {
			localListenersLock.unlock();
		}

	}

	public void addRemoteLeadershipListener(String domainId, String candidateId, InstanceId instanceId, LeadershipListener listener)
			throws LeadershipException {

		localListenersLock.lock();
		try {
			Map<String, LeadershipWrapper> map = listeners.computeIfAbsent(domainId, d -> new HashMap<>());

			LeadershipWrapper wrapper = map.get(candidateId);
			if (wrapper == null) {

				Candidate candidate = Candidate.T.create();
				candidate.setInstanceId(instanceId);
				candidate.setCandidateId(candidateId);
				candidate.setDomainId(domainId);
				candidate.setCandidateType(CandidateType.Remote);
				candidate.setPingTimestamp(new Date());
				candidate.setIsLeader(false);

				wrapper = new LeadershipWrapper(domainId, candidateId, listener, candidate);

				map.put(candidateId, wrapper);
			}

		} finally {
			localListenersLock.unlock();
		}

	}

	@Override
	public void removeLeadershipListener(String domainId, String candidateId) throws LeadershipException {

		localListenersLock.lock();
		try {
			Map<String, LeadershipWrapper> map = listeners.get(domainId);
			if (map != null) {
				map.remove(candidateId);
			}
		} finally {
			localListenersLock.unlock();
		}

	}

	protected boolean tryLeadership(String domainId, String candidateId) {

		try {

			String key = leadershipPrefix + URLEncoder.encode(domainId, "UTF-8");

			int leadershipTimeout = (int) (defaultLeadershipTimeout / 1000);

			return etcdProcessing.atomicPutIfNonExistent(key, candidateId, leadershipTimeout);

		} catch (Exception e) {
			logger.error("Error while trying to acquire leadership in domain " + domainId + " for candidate " + candidateId, e);
		}

		return false;
	}

	protected boolean refreshLeadership(String domainId, String candidateId) {

		try {

			String key = leadershipPrefix + URLEncoder.encode(domainId, "UTF-8");

			int leadershipTimeout = (int) (defaultLeadershipTimeout / 1000);

			return etcdProcessing.atomicPutIfSameValue(key, candidateId, leadershipTimeout);

		} catch (Exception e) {
			logger.error("Error while trying to acquire leadership in domain " + domainId + " for candidate " + candidateId, e);
		}

		return false;
	}

	private void removeStaleRemoteLeadershipListeners(Map<String, LeadershipWrapper> map) {
		Iterator<Map.Entry<String, LeadershipWrapper>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, LeadershipWrapper> entry = it.next();

			LeadershipWrapper wrapper = entry.getValue();

			if (wrapper.getMsSinceLastPing() > defaultCandidateTimeout) {
				Candidate candidate = wrapper.getCandidate();
				if (candidate.getCandidateType() == CandidateType.Remote) {
					logger.trace(
							() -> "Removing candidate " + candidate + " from the pool of listeners as it has exceeded the candidate ping timeout");

					it.remove();
				}
			}
		}
	}

	protected Set<LeadershipWrapper> getLeadershipListeners(String domainId) {

		Set<LeadershipWrapper> result = new HashSet<>();
		localListenersLock.lock();
		try {
			if (domainId != null) {
				Map<String, LeadershipWrapper> map = listeners.get(domainId);
				if (map != null) {
					removeStaleRemoteLeadershipListeners(map);
					result.addAll(map.values());
				}
			} else {
				for (Map.Entry<String, Map<String, LeadershipWrapper>> entry : listeners.entrySet()) {
					Map<String, LeadershipWrapper> map = entry.getValue();
					removeStaleRemoteLeadershipListeners(map);
					result.addAll(map.values());
				}
			}
		} finally {
			localListenersLock.unlock();
		}

		return result;
	}

	protected LeadershipWrapper getLeadershipListener(String domainId, String candidateId) {
		localListenersLock.lock();
		try {
			Map<String, LeadershipWrapper> map = listeners.get(domainId);
			if (map != null) {
				return map.get(candidateId);
			}
		} finally {
			localListenersLock.unlock();
		}
		return null;
	}

	protected void notifyLeaderToSurrender(String domainId, String candidateId) {

		LeadershipWrapper wrapper = getLeadershipListener(domainId, candidateId);
		if (wrapper != null) {
			Candidate candidate = wrapper.getCandidate();

			logger.trace(() -> "Notifying leader to surrender: " + candidate);

			super.sendNotification(candidate, SurrenderLeadership.T, true, candidate.getInstanceId());
		}
	}

	protected void notifyCandidatesOfNewLeader(String domainId, String candidateId) {

		LeadershipWrapper wrapper = getLeadershipListener(domainId, candidateId);
		if (wrapper != null) {
			Candidate candidate = wrapper.getCandidate();

			logger.trace(() -> "Notifying candidates of domain " + domainId + " that a new leader has been elected.");

			InstanceId appInstanceId = null;
			InstanceId instanceId = candidate.getInstanceId();
			if (instanceId != null && instanceId.getApplicationId() != null) {
				appInstanceId = InstanceId.T.create();
				appInstanceId.setApplicationId(instanceId.getApplicationId());
			}

			super.sendNotification(candidate, LeadershipGranted.T, true, appInstanceId);
		}

	}

	@Required
	public void setClientSupplier(Supplier<Client> clientSupplier) {
		this.clientSupplier = clientSupplier;
	}
	@Configurable
	public void setDefaultCandidateTimeout(long defaultCandidateTimeout) {
		this.defaultCandidateTimeout = defaultCandidateTimeout;
	}
	@Configurable
	public void setDefaultLeadershipTimeout(long defaultLeadershipTimeout) {
		this.defaultLeadershipTimeout = defaultLeadershipTimeout;
	}

	@Configurable
	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	@Override
	public void leadershipReleased(CandidateIdentification candidateIdentification) {
		logger.debug(() -> "A Listener registered at some remote manager has surrendered the leadership: " + candidateIdentification);

		String domainId = candidateIdentification.getDomainId();
		String candidateId = candidateIdentification.getCandidateId();

		LeadershipWrapper candidate = null;
		try {
			candidate = getLeadershipListener(domainId, candidateId);
			if (candidate != null) {
				candidate.setLeader(false);
			}
		} catch (Exception e) {
			logger.error("Unable to update leader status of candidate " + candidateIdentification, e);
		}
	}
	@Override
	public void leadershipGranted(CandidateIdentification candidateIdentification) {
		String domainId = candidateIdentification.getDomainId();
		leadershipDaemon.performElection(domainId);
	}
	@Override
	public void surrenderLeadership(CandidateIdentification candidateIdentification) {
		logger.debug(() -> "The local machine seems to run the leader. Stopping it.");

		String domainId = candidateIdentification.getDomainId();
		String candidateId = candidateIdentification.getCandidateId();

		LeadershipListenerWrapper listener = listenerRegistry.get(candidateIdentification);
		LeadershipWrapper wrapper = null;
		try {
			wrapper = getLeadershipListener(domainId, candidateId);
		} catch (Exception e) {
			logger.error("Could not get candidate " + candidateIdentification, e);
		}
		if (listener != null && wrapper != null) {
			if (wrapper.isLeader()) {
				EtcdLeadershipHandle handle = new EtcdLeadershipHandle(domainId, candidateId, etcdProcessing);
				listener.surrenderLeadership(handle);
			}
		}
	}

	@Override
	public String description() {
		return "etcd Leadership Manager (" + clientSupplier + ")";
	}

}
