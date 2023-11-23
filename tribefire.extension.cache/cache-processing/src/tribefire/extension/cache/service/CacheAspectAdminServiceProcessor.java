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
package tribefire.extension.cache.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.InitializationAware;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.extensiondeployment.ServiceAroundProcessor;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.common.FailureCodec;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.utils.lcd.CommonTools;

import tribefire.extension.cache.model.ContextualizedCacheAspectContains;
import tribefire.extension.cache.model.ContextualizedCacheAspectEntry;
import tribefire.extension.cache.model.ContextualizedCacheAspectStateChange;
import tribefire.extension.cache.model.ContextualizedCacheAspectValue;
import tribefire.extension.cache.model.service.admin.CacheAdmin;
import tribefire.extension.cache.model.service.admin.CacheAdminResult;
import tribefire.extension.cache.model.service.admin.CacheChangeState;
import tribefire.extension.cache.model.service.admin.CacheChangeStateResult;
import tribefire.extension.cache.model.service.admin.CacheClear;
import tribefire.extension.cache.model.service.admin.CacheClearResult;
import tribefire.extension.cache.model.service.admin.CacheContainsEntryByKey;
import tribefire.extension.cache.model.service.admin.CacheContainsEntryByServiceRequest;
import tribefire.extension.cache.model.service.admin.CacheContainsEntryResult;
import tribefire.extension.cache.model.service.admin.CacheGetAllEntries;
import tribefire.extension.cache.model.service.admin.CacheGetEntryByKey;
import tribefire.extension.cache.model.service.admin.CacheGetEntryByServiceRequest;
import tribefire.extension.cache.model.service.admin.CacheGetEntryResult;
import tribefire.extension.cache.model.service.admin.CacheGetValueResult;
import tribefire.extension.cache.model.service.admin.CacheRemoveEntryByKey;
import tribefire.extension.cache.model.service.admin.CacheRemoveEntryByServiceRequest;
import tribefire.extension.cache.model.service.admin.CacheRemoveEntryResult;
import tribefire.extension.cache.model.service.admin.CacheState;
import tribefire.extension.cache.model.service.admin.CacheStatus;
import tribefire.extension.cache.model.service.admin.CacheStatusResult;
import tribefire.extension.cache.model.service.admin.MulticastCacheAdmin;
import tribefire.extension.cache.model.service.admin.local.LocalCacheChangeState;
import tribefire.extension.cache.model.service.admin.local.LocalCacheClear;
import tribefire.extension.cache.model.service.admin.local.LocalCacheContainsEntryByKey;
import tribefire.extension.cache.model.service.admin.local.LocalCacheContainsEntryByServiceRequest;
import tribefire.extension.cache.model.service.admin.local.LocalCacheGetAllEntries;
import tribefire.extension.cache.model.service.admin.local.LocalCacheGetEntryByKey;
import tribefire.extension.cache.model.service.admin.local.LocalCacheGetEntryByServiceRequest;
import tribefire.extension.cache.model.service.admin.local.LocalCacheRemoveEntryByKey;
import tribefire.extension.cache.model.service.admin.local.LocalCacheRemoveEntryByServiceRequest;
import tribefire.extension.cache.model.service.admin.local.LocalCacheStatus;
import tribefire.extension.cache.model.status.CacheAspectStatus;
import tribefire.extension.cache.model.status.ContextualizedCacheAspectStatus;

public class CacheAspectAdminServiceProcessor<T extends CacheAspectStatus> extends AbstractDispatchingServiceProcessor<CacheAdmin, CacheAdminResult>
		implements InitializationAware {

	private static final Logger logger = Logger.getLogger(CacheAspectAdminServiceProcessor.class);

	private tribefire.extension.cache.model.deployment.service.CacheAspectAdminServiceProcessor deployable;

	protected Supplier<PersistenceGmSession> cortexSessionProvider;
	private DeployRegistry deployRegistry;

	private TimeSpan multicastTimeout;
	private long multicastTimeoutInMs;
	private InstanceId multicastInstanceId;

	private Set<String> adminServiceProcessorExternalIds;

	// -----------------------------------------------------------------------
	// INITIALIZATION AWARE
	// -----------------------------------------------------------------------

	@Override
	public void postConstruct() {
		multicastTimeoutInMs = multicastTimeout.toDuration().toMillis();

		adminServiceProcessorExternalIds = deployable.getCacheAspects().stream().map(ca -> ca.getExternalId()).collect(Collectors.toSet());
	}

	// -----------------------------------------------------------------------
	// Dispatching
	// -----------------------------------------------------------------------

	@Override
	protected void configureDispatching(DispatchConfiguration<CacheAdmin, CacheAdminResult> dispatching) {

		// ----------------
		// LOCAL
		// ----------------
		dispatching.register(LocalCacheStatus.T, this::process);
		dispatching.register(LocalCacheClear.T, this::process);
		dispatching.register(LocalCacheRemoveEntryByKey.T, this::process);
		dispatching.register(LocalCacheRemoveEntryByServiceRequest.T, this::process);
		dispatching.register(LocalCacheContainsEntryByKey.T, this::process);
		dispatching.register(LocalCacheContainsEntryByServiceRequest.T, this::process);
		dispatching.register(LocalCacheGetEntryByKey.T, this::process);
		dispatching.register(LocalCacheGetEntryByServiceRequest.T, this::process);
		dispatching.register(LocalCacheGetAllEntries.T, this::process);
		dispatching.register(LocalCacheChangeState.T, this::process);

		// ----------------
		// MULTICAST
		// ----------------
		dispatching.register(CacheStatus.T, this::process);
		dispatching.register(CacheClear.T, this::process);
		dispatching.register(CacheRemoveEntryByKey.T, this::process);
		dispatching.register(CacheRemoveEntryByServiceRequest.T, this::process);
		dispatching.register(CacheContainsEntryByKey.T, this::process);
		dispatching.register(CacheContainsEntryByServiceRequest.T, this::process);
		dispatching.register(CacheGetEntryByKey.T, this::process);
		dispatching.register(CacheGetEntryByServiceRequest.T, this::process);
		dispatching.register(CacheGetAllEntries.T, this::process);
		dispatching.register(CacheChangeState.T, this::process);
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	// ----------------
	// LOCAL
	// ----------------
	private CacheStatusResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheStatus request) {

		List<CacheAspect> cacheAspects = retrieveCacheAspectExperts(request);

		Set<ContextualizedCacheAspectStatus> statusList = cacheAspects.stream().map(ca -> {
			ContextualizedCacheAspectStatus contextualizedCacheAspectStatus = ContextualizedCacheAspectStatus.T.create();
			contextualizedCacheAspectStatus.setStatus(ca.expert().retriveCacheStatus());
			contextualizedCacheAspectStatus.setCacheAspectExternalId(ca.deployable().getExternalId());
			contextualizedCacheAspectStatus.setInstanceId(this.multicastInstanceId.stringify());

			return contextualizedCacheAspectStatus;
		}).collect(Collectors.toSet());

		CacheStatusResult result = CacheStatusResult.T.create();
		result.setStatusSet(statusList);
		return result;
	}

	private CacheClearResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheClear request) {

		List<CacheAspect> cacheAspects = retrieveCacheAspectExperts(request);
		cacheAspects.forEach(ca -> ca.expert().clearCache());

		CacheClearResult result = CacheClearResult.T.create();
		return result;
	}

	private CacheRemoveEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheRemoveEntryByKey request) {
		return getLocalCacheRemoveEntryResult(request, request.getKey(), null);
	}
	private CacheRemoveEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheRemoveEntryByServiceRequest request) {
		return getLocalCacheRemoveEntryResult(request, null, request.getRequest());
	}

	private CacheContainsEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheContainsEntryByKey request) {
		return getLocalCacheContainsEntryResult(request, request.getKey(), null);
	}
	private CacheContainsEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context,
			LocalCacheContainsEntryByServiceRequest request) {
		return getLocalCacheContainsEntryResult(request, null, request.getRequest());
	}

	private CacheGetValueResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheGetEntryByKey request) {
		return getLocalCacheGetEntryResult(request, request.getKey(), null);
	}
	private CacheGetValueResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheGetEntryByServiceRequest request) {
		return getLocalCacheGetEntryResult(request, null, request.getRequest());
	}

	private CacheGetEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheGetAllEntries request) {
		List<CacheAspect> cacheAspects = retrieveCacheAspectExperts(request);

		List<ContextualizedCacheAspectEntry> allEntries = new ArrayList<>();

		cacheAspects.stream().forEach(ca -> {

			Map<String, CacheValueHolder> map = ca.expert().getAllEntries();
			List<ContextualizedCacheAspectEntry> allEntriesPerAspect = map.entrySet().stream().map(e -> {
				ContextualizedCacheAspectEntry contextualizedCacheAspectEntry = ContextualizedCacheAspectEntry.T.create();

				contextualizedCacheAspectEntry.setHash(e.getKey());
				contextualizedCacheAspectEntry.setResult(e.getValue().getRequest());
				contextualizedCacheAspectEntry.setCacheAspectExternalId(ca.deployable().getExternalId());
				contextualizedCacheAspectEntry.setInstanceId(this.multicastInstanceId.stringify());

				return contextualizedCacheAspectEntry;
			}).collect(Collectors.toList());

			allEntries.addAll(allEntriesPerAspect);
		});

		CacheGetEntryResult result = CacheGetEntryResult.T.create();
		result.setResultList(allEntries);
		return result;
	}

	private CacheChangeStateResult process(@SuppressWarnings("unused") ServiceRequestContext context, LocalCacheChangeState request) {

		List<CacheAspect> cacheAspects = retrieveCacheAspectExperts(request);

		Set<ContextualizedCacheAspectStateChange> stateChanges = cacheAspects.stream().map(ca -> {
			boolean oldIsActive = ca.isActive();
			boolean newIsActive;
			CacheState state = request.getState();
			switch (state) {
				case ACTIVE:
					newIsActive = true;
					break;
				case INACTIVE:
					newIsActive = false;
					break;
				default:
					throw new IllegalArgumentException("State: '" + state + "' not supported");
			}
			ca.activate(newIsActive);

			ContextualizedCacheAspectStateChange contextualizedCacheAspectStateChange = ContextualizedCacheAspectStateChange.T.create();
			contextualizedCacheAspectStateChange.setOldIsActive(oldIsActive);
			contextualizedCacheAspectStateChange.setNewIsActive(newIsActive);
			contextualizedCacheAspectStateChange.setCacheAspectExternalId(ca.deployable().getExternalId());
			contextualizedCacheAspectStateChange.setInstanceId(this.multicastInstanceId.stringify());

			return contextualizedCacheAspectStateChange;
		}).collect(Collectors.toSet());
		CacheChangeStateResult result = CacheChangeStateResult.T.create();
		result.setStateChanges(stateChanges);
		return result;
	}

	// ----------------
	// MULTICAST
	// ----------------

	private CacheStatusResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheStatus request) {
		CacheStatusResult result = CacheStatusResult.T.create();

		LocalCacheStatus localRequest = LocalCacheStatus.T.create();

		enrichMulticastResults(request, localRequest, response -> {
			result.getStatusSet().addAll(((CacheStatusResult) response).getStatusSet());
		});

		return result;
	}

	private CacheClearResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheClear request) {
		CacheClearResult result = CacheClearResult.T.create();

		LocalCacheClear localRequest = LocalCacheClear.T.create();

		enrichMulticastResults(request, localRequest, response -> {
			// nothing
		});

		return result;
	}

	private CacheRemoveEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheRemoveEntryByKey request) {
		CacheRemoveEntryResult result = CacheRemoveEntryResult.T.create();

		LocalCacheRemoveEntryByKey localRequest = LocalCacheRemoveEntryByKey.T.create();
		localRequest.setKey(request.getKey());

		enrichMulticastResults(request, localRequest, response -> {
			// nothing
		});

		return result;
	}
	private CacheRemoveEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheRemoveEntryByServiceRequest request) {
		CacheRemoveEntryResult result = CacheRemoveEntryResult.T.create();

		LocalCacheRemoveEntryByServiceRequest localRequest = LocalCacheRemoveEntryByServiceRequest.T.create();
		localRequest.setRequest(request.getRequest());

		enrichMulticastResults(request, localRequest, response -> {
			// nothing
		});

		return result;
	}

	private CacheContainsEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheContainsEntryByKey request) {
		CacheContainsEntryResult result = CacheContainsEntryResult.T.create();

		LocalCacheContainsEntryByKey localRequest = LocalCacheContainsEntryByKey.T.create();
		localRequest.setKey(request.getKey());
		enrichMulticastResults(request, localRequest, response -> {
			result.getContainsList().addAll(((CacheContainsEntryResult) response).getContainsList());
		});

		return result;
	}
	private CacheContainsEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheContainsEntryByServiceRequest request) {
		CacheContainsEntryResult result = CacheContainsEntryResult.T.create();

		LocalCacheContainsEntryByServiceRequest localRequest = LocalCacheContainsEntryByServiceRequest.T.create();
		localRequest.setRequest(request.getRequest());
		enrichMulticastResults(request, localRequest, response -> {
			result.getContainsList().addAll(((CacheContainsEntryResult) response).getContainsList());
		});

		return result;
	}

	private CacheGetValueResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheGetEntryByKey request) {
		CacheGetValueResult result = CacheGetValueResult.T.create();

		LocalCacheGetEntryByKey localRequest = LocalCacheGetEntryByKey.T.create();
		localRequest.setKey(request.getKey());
		enrichMulticastResults(request, localRequest, response -> {
			result.getResultList().addAll(((CacheGetValueResult) response).getResultList());
		});

		return result;
	}
	private CacheGetValueResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheGetEntryByServiceRequest request) {
		CacheGetValueResult result = CacheGetValueResult.T.create();

		LocalCacheGetEntryByServiceRequest localRequest = LocalCacheGetEntryByServiceRequest.T.create();
		localRequest.setRequest(request.getRequest());
		enrichMulticastResults(request, localRequest, response -> {
			result.getResultList().addAll(((CacheGetValueResult) response).getResultList());
		});

		return result;
	}

	private CacheGetEntryResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheGetAllEntries request) {
		CacheGetEntryResult result = CacheGetEntryResult.T.create();

		LocalCacheGetAllEntries localRequest = LocalCacheGetAllEntries.T.create();

		enrichMulticastResults(request, localRequest, response -> {
			result.getResultList().addAll(((CacheGetEntryResult) response).getResultList());
		});

		return result;
	}

	private CacheChangeStateResult process(@SuppressWarnings("unused") ServiceRequestContext context, CacheChangeState request) {
		CacheChangeStateResult result = CacheChangeStateResult.T.create();

		LocalCacheChangeState localRequest = LocalCacheChangeState.T.create();
		localRequest.setState(request.getState());

		enrichMulticastResults(request, localRequest, response -> {
			result.getStateChanges().addAll(((CacheChangeStateResult) response).getStateChanges());
		});

		return result;
	}

	// -----------------------------------------------------------------------
	// HELPERS
	// -----------------------------------------------------------------------

	// ----------------
	// LOCAL
	// ----------------
	private List<CacheAspect> retrieveCacheAspectExperts(CacheAdmin request) {
		Set<String> externalIds = request.getExternalIds();
		externalIds.addAll(adminServiceProcessorExternalIds);

		EntityQueryBuilder builder = EntityQueryBuilder.from(tribefire.extension.cache.model.deployment.service.CacheAspect.T);
		if (!CommonTools.isEmpty(externalIds)) {
			builder.where().property(Deployable.externalId).in(externalIds);
		}
		builder.orderBy(Deployable.externalId);

		List<tribefire.extension.cache.model.deployment.service.CacheAspect> list = cortexSessionProvider.get().query().entities(builder.done())
				.list();

		List<CacheAspect> cacheAspects = list.stream().map(ca -> {
			CacheAspect cacheAspect = deployRegistry.resolve(ca).getComponent(ServiceAroundProcessor.T);
			return cacheAspect;
		}).collect(Collectors.toList());
		return cacheAspects;
	}

	private CacheRemoveEntryResult getLocalCacheRemoveEntryResult(CacheAdmin request, String hash, ServiceRequest r) {
		List<CacheAspect> cacheAspects = retrieveCacheAspectExperts(request);
		cacheAspects.forEach(ca -> {
			String _hash;
			if (hash == null) {
				_hash = ca.calculateRequestHash(r);
			} else {
				_hash = hash;
			}
			ca.expert().removeEntry(_hash);
		});

		CacheRemoveEntryResult result = CacheRemoveEntryResult.T.create();
		return result;
	}

	private CacheContainsEntryResult getLocalCacheContainsEntryResult(CacheAdmin request, String hash, ServiceRequest r) {
		List<CacheAspect> cacheAspects = retrieveCacheAspectExperts(request);
		List<ContextualizedCacheAspectContains> containsList = cacheAspects.stream().map(ca -> {
			ContextualizedCacheAspectContains contextualizedCacheAspectContains = ContextualizedCacheAspectContains.T.create();
			String _hash;
			if (hash == null) {
				_hash = ca.calculateRequestHash(r);
			} else {
				_hash = hash;
			}
			contextualizedCacheAspectContains.setContains(ca.expert().containsEntry(_hash));
			contextualizedCacheAspectContains.setCacheAspectExternalId(ca.deployable().getExternalId());
			contextualizedCacheAspectContains.setInstanceId(this.multicastInstanceId.stringify());
			return contextualizedCacheAspectContains;
		}).collect(Collectors.toList());

		CacheContainsEntryResult result = CacheContainsEntryResult.T.create();
		result.setContainsList(containsList);
		return result;
	}

	private CacheGetValueResult getLocalCacheGetEntryResult(CacheAdmin request, String hash, ServiceRequest r) {
		List<CacheAspect> cacheAspects = retrieveCacheAspectExperts(request);
		List<ContextualizedCacheAspectValue> resultList = cacheAspects.stream().map(ca -> {
			ContextualizedCacheAspectValue contextualizedCacheAspectEntry = ContextualizedCacheAspectValue.T.create();
			String _hash;
			if (hash == null) {
				_hash = ca.calculateRequestHash(r);
			} else {
				_hash = hash;
			}
			contextualizedCacheAspectEntry.setResult(ca.expert().getEntry(_hash).getResult());
			contextualizedCacheAspectEntry.setCacheAspectExternalId(ca.deployable().getExternalId());
			contextualizedCacheAspectEntry.setInstanceId(this.multicastInstanceId.stringify());

			return contextualizedCacheAspectEntry;
		}).collect(Collectors.toList());

		CacheGetValueResult result = CacheGetValueResult.T.create();
		result.setResultList(resultList);
		return result;
	}

	// ----------------
	// MULTICAST
	// ----------------

	private void enrichMulticastResults(MulticastCacheAdmin request, CacheAdmin localRequest, Consumer<CacheAdminResult> response) {

		// enrich local request
		localRequest.setExternalIds(request.getExternalIds());

		long timeout;
		TimeSpan _timeout = request.getTimeout();
		if (_timeout != null) {
			timeout = _timeout.toDuration().toMillis();
			if (timeout == 0) {
				timeout = multicastTimeoutInMs;
			}
		} else {
			timeout = multicastTimeoutInMs;
		}

		MulticastRequest mc = MulticastRequest.T.create();
		mc.setAddressee(multicastInstanceId);
		mc.setTimeout(timeout);
		mc.setServiceRequest(localRequest);

		MulticastResponse multicastResponse = mc.eval(cortexSessionProvider.get()).get();

		Map<InstanceId, ServiceResult> responses = multicastResponse.getResponses();
		for (Map.Entry<InstanceId, ServiceResult> entry : responses.entrySet()) {
			InstanceId sender = entry.getKey();
			ServiceResult value = entry.getValue();
			ResponseEnvelope responseEnvelope = value.asResponse();
			if (responseEnvelope != null) {
				CacheAdminResult localCacheAdminResult = (CacheAdminResult) responseEnvelope.getResult();
				response.accept(localCacheAdminResult);
			} else {
				Failure failure = value.asFailure();
				if (failure != null) {
					Throwable throwable = FailureCodec.INSTANCE.decode(failure);
					logger.info(() -> "Received a failure from instance '" + sender + "'", throwable);
				} else {
					logger.info(() -> "Received neither a response nor a failure from '" + sender + "'");
				}
			}
		}
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	@Configurable
	@Required
	public void setDeployable(tribefire.extension.cache.model.deployment.service.CacheAspectAdminServiceProcessor deployable) {
		this.deployable = deployable;
	}

	@Required
	@Configurable
	public void setCortexSessionProvider(Supplier<PersistenceGmSession> cortexSessionProvider) {
		this.cortexSessionProvider = cortexSessionProvider;
	}

	@Configurable
	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

	@Configurable
	@Required
	public void setMulticastInstanceId(InstanceId multicastInstanceId) {
		this.multicastInstanceId = multicastInstanceId;
	}

	@Configurable
	@Required
	public void setMulticastTimeout(TimeSpan multicastTimeout) {
		this.multicastTimeout = multicastTimeout;
	}

}