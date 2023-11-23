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
package com.braintribe.model.processing.leadership;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.execution.ExtendedScheduledThreadPoolExecutor;
import com.braintribe.execution.NamedThreadFactory;
import com.braintribe.logging.Logger;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.leadership.Candidate;
import com.braintribe.model.leadership.CandidateType;
import com.braintribe.model.leadership.service.LeadershipGranted;
import com.braintribe.model.leadership.service.LeadershipReleased;
import com.braintribe.model.leadership.service.LeadershipRequest;
import com.braintribe.model.leadership.service.SurrenderLeadership;
import com.braintribe.model.processing.securityservice.api.UserSessionScoping;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Neutral;

public abstract class LeadershipManagerBase extends AbstractDispatchingServiceProcessor<LeadershipRequest, Neutral> implements LifecycleAware {

	private static Logger logger = Logger.getLogger(LeadershipManagerBase.class);

	protected Map<CandidateIdentification, LeadershipListenerWrapper> listenerRegistry = new ConcurrentHashMap<CandidateIdentification, LeadershipListenerWrapper>();
	protected ReentrantLock listenerLock = new ReentrantLock();

	protected InstanceId localInstanceId;
	protected ScheduledExecutorService executorService = null;
	protected boolean selfCreatedExecutorService = false;
	protected Evaluator<ServiceRequest> requestEvaluator;

	protected long daemonInterval = 1000L;
	
	protected UserSessionScoping userSessionScoping;
	protected Supplier<String> sessionIdSupplier;

	protected static InstanceId masterAddressee;


	@Override
	public void postConstruct() {

		masterAddressee = InstanceId.of(null, TribefireConstants.TRIBEFIRE_SERVICES_APPLICATION_ID);

		if (executorService == null) {
			logger.debug(() -> "Starting a new executor service.");
			
			NamedThreadFactory threadFactory = new NamedThreadFactory();
			threadFactory.setNamePrefix("leadership");
			ExtendedScheduledThreadPoolExecutor eses = new ExtendedScheduledThreadPoolExecutor(1, threadFactory);
			eses.setDescription("Leadership Daemon");
			eses.postConstruct();
			
			executorService = eses;
			selfCreatedExecutorService = true;
		} else {
			logger.debug(() -> "An executor service has been provided.");
		}
		
	}

	@Override
	public void preDestroy() {
		logger.debug(() -> "Shutting down leadership manager.");
		
		if (selfCreatedExecutorService) {
			if (executorService instanceof ExtendedScheduledThreadPoolExecutor) {
				((ExtendedScheduledThreadPoolExecutor) executorService).preDestroy();
			} else {
				executorService.shutdownNow();
			}
		}
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<LeadershipRequest, Neutral> dispatching) {
		dispatching.register(LeadershipGranted.T, (c, r) -> onLeadershipGranted(r));
		dispatching.register(LeadershipReleased.T, (c, r) -> onLeadershipReleased(r));
		dispatching.register(SurrenderLeadership.T, (c, r) -> onSurrenderLeadership(r));
	}

	private Neutral onLeadershipGranted(LeadershipGranted request) {
		leadershipGranted(requestToIdentification(request));
		return Neutral.NEUTRAL;
	}

	private Neutral onLeadershipReleased(LeadershipReleased request) {
		leadershipReleased(requestToIdentification(request));
		return Neutral.NEUTRAL;
	}

	private Neutral onSurrenderLeadership(SurrenderLeadership request) {
		surrenderLeadership(requestToIdentification(request));
		return Neutral.NEUTRAL;
	}

	protected abstract void leadershipGranted(CandidateIdentification candidateIdentification);

	protected abstract void leadershipReleased(CandidateIdentification candidateIdentification);

	protected abstract void surrenderLeadership(CandidateIdentification candidateIdentification);

	private CandidateIdentification requestToIdentification(LeadershipRequest request) {
		String domainId = request.getDomainId();
		String candidateId = request.getCandidateId();
		InstanceId instanceId = request.getInstanceId();
		return new CandidateIdentification(domainId, instanceId, candidateId, CandidateType.Remote);
	}

	protected CandidateIdentification generateIdentification(Candidate candidate) {
		InstanceId instanceId = candidate.getInstanceId();
		String domainId = candidate.getDomainId();
		String candidateId = candidate.getCandidateId();
		CandidateType candidateType = candidate.getCandidateType();
		CandidateIdentification identification = new CandidateIdentification(domainId, instanceId, candidateId, candidateType);
		return identification;
	}

	protected CandidateIdentification generateIdentification(String domainId, InstanceId instanceId, String candidateId, CandidateType candidateType) {
		if (instanceId == null) {
			instanceId = localInstanceId;
		}
		CandidateIdentification identification = new CandidateIdentification(domainId, instanceId, candidateId, candidateType);
		return identification;
	}

	protected <T extends LeadershipRequest> void sendNotification(Candidate candidate, EntityType<T> type, boolean multicast, InstanceId addressee) {
		LeadershipRequest leadershipMessage = type.create();
		CandidateIdentification candidateIdentification = generateIdentification(candidate);
		sendNotification(candidateIdentification, leadershipMessage, multicast, addressee);
	}

	protected <T extends LeadershipRequest> void sendNotification(CandidateIdentification candidateIdentification, EntityType<T> type, boolean multicast, InstanceId addressee) {
		LeadershipRequest leadershipMessage = type.create();
		sendNotification(candidateIdentification, leadershipMessage, multicast, addressee);
	}

	protected void sendNotification(CandidateIdentification candidateIdentification, LeadershipRequest leadershipRequest, boolean multicast, InstanceId addressee) {
		if (requestEvaluator == null) {
			logger.debug("No request evaluator configured. Not sending the notification "+leadershipRequest);
			return;
		}
		try {
			leadershipRequest = populateStandardMessageProperties(candidateIdentification, leadershipRequest);
			
			if (multicast) {
				MulticastRequest mcR = MulticastRequest.T.create();
				mcR.setAsynchronous(false);
				mcR.setAddressee(addressee);
				mcR.setServiceRequest(leadershipRequest);
				mcR.setTimeout((long) Numbers.MILLISECONDS_PER_MINUTE);
				if (sessionIdSupplier != null) {
					mcR.setSessionId(sessionIdSupplier.get());
				}
				
				mcR.eval(requestEvaluator).get();
				
			} else {
				EvalContext<Neutral> eval = leadershipRequest.eval(requestEvaluator);
				eval.get();
			}

		} catch (Exception e) {
			logger.error("Unable to invoke request [" + leadershipRequest + "]", e);
		}
	}

	private LeadershipRequest populateStandardMessageProperties(CandidateIdentification candidateIdentification, LeadershipRequest leadershipRequest) {
		leadershipRequest.setCandidateId(candidateIdentification.getCandidateId());
		leadershipRequest.setDomainId(candidateIdentification.getDomainId());
		leadershipRequest.setInstanceId(candidateIdentification.getInstanceId());
		leadershipRequest.setOriginatorInstanceId(localInstanceId);
		return leadershipRequest;
	}
	
	public Map<CandidateIdentification, LeadershipListenerWrapper> getListeners(String domainId) {
		Map<CandidateIdentification, LeadershipListenerWrapper> clone = new HashMap<CandidateIdentification, LeadershipListenerWrapper>();
		this.listenerLock.lock();
		try {
			if (domainId == null) {
				clone.putAll(this.listenerRegistry);
			} else {
				
				for (Map.Entry<CandidateIdentification, LeadershipListenerWrapper> entry : listenerRegistry.entrySet()) {
					CandidateIdentification identification = entry.getKey();
					if (identification.getDomainId().equals(domainId)) {
						clone.put(identification, entry.getValue());
					}
				}
			}
			
		} finally {
			this.listenerLock.unlock();
		}
		return clone;
	}
	
	public void pluginPostConstruct() {
		//Do nothing
	}
	
	
	
	@Configurable
	@Required
	public void setLocalInstanceId(InstanceId localInstanceId) {
		this.localInstanceId = localInstanceId;
	}
	public InstanceId getLocalInstanceId() {
		return localInstanceId;
	}
	@Configurable
	public void setExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}
	@Configurable
	@Required
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}
	@Configurable
	public void setDaemonInterval(long daemonInterval) {
		this.daemonInterval = daemonInterval;
	}
	@Configurable
	@Required
	public void setUserSessionScoping(UserSessionScoping userSessionScoping) {
		this.userSessionScoping = userSessionScoping;
	}
	@Configurable
	@Required
	public void setSessionIdSupplier(Supplier<String> sessionIdSupplier) {
		this.sessionIdSupplier = sessionIdSupplier;
	}


}
