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
package tribefire.cortex.check.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.braintribe.cc.lcd.CodingMap;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.LifecycleAware;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.exception.Exceptions;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.check.service.CheckRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.CheckResultEntry;
import com.braintribe.model.check.service.CheckStatus;
import com.braintribe.model.deploymentapi.check.data.CheckBundleFilters;
import com.braintribe.model.deploymentapi.check.data.CheckBundlesResponse;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregatable;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregation;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrAggregationKind;
import com.braintribe.model.deploymentapi.check.data.aggr.CbrContainer;
import com.braintribe.model.deploymentapi.check.data.aggr.CheckBundleResult;
import com.braintribe.model.deploymentapi.check.request.CheckBundlesRequest;
import com.braintribe.model.deploymentapi.check.request.RunAimedCheckBundles;
import com.braintribe.model.deploymentapi.check.request.RunCheckBundles;
import com.braintribe.model.deploymentapi.check.request.RunDistributedCheckBundles;
import com.braintribe.model.deploymentapi.check.request.RunHealthChecks;
import com.braintribe.model.deploymentapi.check.request.RunVitalityCheckBundles;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.check.CheckBundleQualification;
import com.braintribe.model.extensiondeployment.check.CheckProcessor;
import com.braintribe.model.extensiondeployment.check.HealthCheck;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.api.aspect.HttpStatusCodeNotification;
import com.braintribe.model.processing.service.common.topology.InstanceIdHashingComparator;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.service.api.UnicastRequest;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.thread.api.ThreadContextScoping;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.xml.XmlTools;

import tribefire.cortex.model.check.CheckCoverage;

/**
 * This service processor handles {@link CheckBundlesRequest Check Bundle Requests} covering local instance checks as
 * well as distributed checks. Also, vitality/health checks are treated here. These kind of low-level checks can be
 * executed in an unauthorized way.
 * 
 * @author christina.wilpernig
 */
public class CheckBundlesProcessor extends AbstractDispatchingServiceProcessor<CheckBundlesRequest, Object> implements LifecycleAware {
	private static Logger log = Logger.getLogger(CheckBundlesProcessor.class);

	// IOC
	private Supplier<PersistenceGmSession> cortexSessionSupplier;
	private Evaluator<ServiceRequest> systemEvaluator;
	private InstanceId instanceId;

	private ThreadContextScoping threadContextScoping;
	private ExecutorService executionThreadPool;

	// comparators
	private static Comparator<CbrAggregatable> comparator;

	static {
		Comparator<CbrAggregatable> statusComparator = (a1, a2) -> a1.getStatus().compareTo(a2.getStatus());
		Comparator<CbrAggregatable> nameComparator = (a1, a2) -> CheckBundlesUtils.getIdentification(a1)
				.compareTo(CheckBundlesUtils.getIdentification(a2));

		comparator = statusComparator.reversed().thenComparing(nameComparator);
	}

	@Configurable
	@Required
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}

	@Configurable
	@Required
	public void setEvaluator(Evaluator<ServiceRequest> evaluator) {
		this.systemEvaluator = evaluator;
	}

	@Configurable
	@Required
	public void setInstanceId(InstanceId instanceId) {
		this.instanceId = instanceId;
	}

	@Configurable
	@Required
	public void setThreadContextScoping(ThreadContextScoping threadContextScoping) {
		this.threadContextScoping = threadContextScoping;
	}

	// Service Processor preparation

	@Override
	public void postConstruct() {
		executionThreadPool = VirtualThreadExecutorBuilder.newPool().concurrency(50).threadNamePrefix("CheckBundleExecution").build();
	}

	@Override
	public void preDestroy() {
		executionThreadPool.close();
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<CheckBundlesRequest, Object> dispatching) {
		// do require permission
		dispatching.register(RunCheckBundles.T, this::runCheckBundles);
		dispatching.register(RunAimedCheckBundles.T, this::runAimedCheckBundles);
		dispatching.register(RunDistributedCheckBundles.T, this::runDistributedCheckBundles);

		// do not require permissions
		dispatching.register(RunVitalityCheckBundles.T, this::runVitalityCheckBundles);
		dispatching.register(RunHealthChecks.T, this::runHealthChecks);
	}

	// Requests

	/** Executes {@link RunCheckBundles} */
	private CheckBundlesResponse runCheckBundles(ServiceRequestContext requestContext, RunCheckBundles request) {
		long t0 = System.nanoTime();

		CheckBundlesResponse response = CheckBundlesResponse.T.create();
		response.setStatus(CheckStatus.ok);
		response.setCreatedAt(new Date());

		try {
			List<CheckBundle> bundles = getFilteredCheckBundles(request);

			if (bundles.size() == 0) {
				log.trace(() -> "Nothing to do here.");

				response.setElapsedTimeInMs((System.nanoTime() - t0) / 1_000_000.0);

				return response;
			}

			List<CheckBundlesContext> contexts = buildCheckContext(bundles);
			List<Pair<CheckBundlesContext, Future<CheckResult>>> contextToResultFutureList = new ArrayList<>();

			Map<CheckBundlesContext, CheckResult> results = new LinkedHashMap<>();
			for (CheckBundlesContext c : contexts) {
				Future<CheckResult> resultFuture = evaluateParallel(c.request);
				contextToResultFutureList.add(Pair.of(c, resultFuture));
			}
			for (Pair<CheckBundlesContext, Future<CheckResult>> pair : contextToResultFutureList) {
				CheckBundlesContext context = pair.first();
				Future<CheckResult> future = pair.second();

				CheckResult r = null;
				try {
					r = future.get();
				} catch (Exception e) {
					r = CheckResult.T.create();

					CheckResultEntry entry = createFailureCheckResultEntry(e);

					r.getEntries().add(entry);
					r.setElapsedTimeInMs(0);
				}

				results.put(context, r);
			}

			if (log.isTraceEnabled()) {
				StringBuilder sb = new StringBuilder("Health check:\n");
				for (Map.Entry<CheckBundlesContext, CheckResult> entry : results.entrySet()) {
					CheckBundlesContext ctx = entry.getKey();
					CheckResult value = entry.getValue();
					sb.append("Service: " + ctx.request.getServiceId() + ", time: " + value.getElapsedTimeInMs() + "\n");
				}
				log.trace(StringTools.asciiBoxMessage(sb.toString()));
			}

			response = buildResponse(request.getAggregateBy(), results);

		} catch (Exception e) {
			log.error("Error while evaluating checks: ", e);
			CheckBundleResult result = createFailureCheckBundleResult(e, t0);

			response.getElements().add(result);
			response.setStatus(CheckStatus.fail);
		}

		response.setElapsedTimeInMs((System.nanoTime() - t0) / 1_000_000.0);
		return response;
	}

	/** Executes {@link RunAimedCheckBundles} via Unicast */
	private CheckBundlesResponse runAimedCheckBundles(ServiceRequestContext requestContext, RunAimedCheckBundles request) {
		long t0 = System.nanoTime();
		CheckBundlesResponse response;

		try {
			String nodeId = request.getNodeId();
			if (nodeId == null || nodeId.isEmpty())
				throw new IllegalArgumentException("nodeId must be set");

			RunCheckBundles run = RunCheckBundles.T.create();
			for (Property p : CheckBundleFilters.T.getProperties()) {
				if (p.getDeclaringType() == GenericEntity.T)
					continue;

				Object v = p.get(request);
				p.set(run, v);
			}

			UnicastRequest r = UnicastRequest.T.create();
			r.setServiceRequest(run);
			r.setAddressee(InstanceId.of(nodeId, null));

			response = (CheckBundlesResponse) r.eval(systemEvaluator).get();

		} catch (Exception e) {
			log.error("Error while evaluating aimed checks: ", e);
			response = CheckBundlesResponse.T.create();

			response.getElements().add(createFailureCheckBundleResult(e, t0));
			response.setStatus(CheckStatus.fail);
			response.setCreatedAt(new Date());
			response.setElapsedTimeInMs((System.nanoTime() - t0) / 1_000_000.0);
		}

		return response;
	}

	/** Executes {@link RunDistributedCheckBundles} via Multicast */
	private CheckBundlesResponse runDistributedCheckBundles(ServiceRequestContext requestContext, RunDistributedCheckBundles request) {
		long t0 = System.nanoTime();

		CheckBundlesResponse response = CheckBundlesResponse.T.create();
		response.setCreatedAt(new Date());

		List<CheckBundleResult> results = new ArrayList<>();
		try {
			MulticastRequest m = buildMulticast(request);
			Map<InstanceId, CheckBundlesResponse> responsePerNode = getDistributedCheckResponses(m);

			for (CheckBundlesResponse r : responsePerNode.values()) {
				for (CbrAggregatable a : r.getElements()) {
					results.add((CheckBundleResult) a);
				}
			}

		} catch (Exception e) {
			log.error("Error while evaluating distributed checks: ", e);
			results.add(createFailureCheckBundleResult(e, t0));
		}

		aggregate(response, results, request.getAggregateBy(), 0);

		response.setElapsedTimeInMs((System.nanoTime() - t0) / 1_000_000.0);
		return response;
	}

	/** Executes unauthorized {@link RunVitalityCheckBundles} */
	private CheckBundlesResponse runVitalityCheckBundles(ServiceRequestContext requestContext, RunVitalityCheckBundles request) {
		long t0 = System.nanoTime();
		CheckBundlesResponse response;
		Consumer<Integer> httpStatus = requestContext.getAspect(HttpStatusCodeNotification.class).orElse(s -> {
		});

		try {
			Integer warnStatusCode = request.getWarnStatusCode();
			if (warnStatusCode == null)
				warnStatusCode = 503;

			RunCheckBundles run = RunCheckBundles.T.create();

			run.setAggregateBy(request.getAggregateBy());
			run.setCoverage(Collections.singleton(CheckCoverage.vitality));
			run.setIsPlatformRelevant(true);

			response = run.eval(systemEvaluator).get();

			switch (response.getStatus()) {
				case ok:
					httpStatus.accept(200);
					break;
				case warn:
					httpStatus.accept(warnStatusCode);
					break;
				case fail:
				default:
					httpStatus.accept(503);
					break;
			}

		} catch (Exception e) {
			log.error("Error while evaluating vitality checks: ", e);
			response = CheckBundlesResponse.T.create();
			response.getElements().add(createFailureCheckBundleResult(e, t0));
			response.setStatus(CheckStatus.fail);

			response.setCreatedAt(new Date());
			response.setElapsedTimeInMs((System.nanoTime() - t0) / 1_000_000.0);

			httpStatus.accept(503);
		}

		return response;
	}

	/** Executes {@link RunHealthChecks} */
	private Map<InstanceId, Object> runHealthChecks(ServiceRequestContext requestContext, RunHealthChecks request) {
		long t0 = System.nanoTime();
		Map<InstanceId, Object> response = new HashMap<>();
		List<CheckResult> results = new ArrayList<>();
		CheckBundlesResponse cbr;
		try {

			Integer warnStatusCode = request.getWarnStatusCode();
			if (warnStatusCode == null)
				warnStatusCode = 503;

			RunVitalityCheckBundles run = RunVitalityCheckBundles.T.create();
			run.setWarnStatusCode(warnStatusCode);

			cbr = run.eval(requestContext).get();

			// no aggregation has been defined so we get back a flat list of check bundle results
			for (CbrAggregatable a : cbr.getElements()) {
				if (a.isResult())
					results.add(((CheckBundleResult) a).getResult());
			}

		} catch (Exception e) {
			log.error("Error while evaluating health checks: ", e);
			results.add(createFailureCheckResult(e, t0));

			Consumer<Integer> httpStatus = requestContext.getAspect(HttpStatusCodeNotification.class).orElse(s -> {
			});
			httpStatus.accept(503);
		}

		response.put(instanceId, results);
		return response;
	}

	// HELPERS

	private CheckBundleResult createFailureCheckBundleResult(Exception e, long t0) {
		CheckResult r = createFailureCheckResult(e, t0);

		CheckBundleResult cbr = CheckBundleResult.T.create();
		cbr.setResult(r);
		cbr.setNode(this.instanceId.getNodeId());
		cbr.setStatus(CheckStatus.fail);
		cbr.setName("Check Bundle Framework - Internal Error");
		// TODO properly solve this
		cbr.setCheck(CheckProcessor.T.create());

		return cbr;
	}

	private CheckResult createFailureCheckResult(Exception e, long t0) {
		CheckResultEntry entry = createFailureCheckResultEntry(e);

		CheckResult r = CheckResult.T.create();
		r.getEntries().add(entry);
		r.setElapsedTimeInMs((System.nanoTime() - t0) / 1_000_000.0);

		return r;
	}

	private CheckResultEntry createFailureCheckResultEntry(Exception e) {
		CheckResultEntry entry = CheckResultEntry.T.create();
		entry.setCheckStatus(CheckStatus.fail);
		entry.setName("Check Bundle Framework");

		StringBuilder sb = new StringBuilder();
		sb.append("Evaluation of checks on node " + this.instanceId.getNodeId() + " failed with " + e.getClass().getName());

		String m = e.getMessage();
		if (m != null) {
			sb.append(": ");
			sb.append(m);
		}
		sb.append("");

		entry.setMessage(sb.toString());
		entry.setDetails(Exceptions.stringify(e));
		return entry;
	}

	private MulticastRequest buildMulticast(CheckBundlesRequest request) {
		RunCheckBundles run = RunCheckBundles.T.create();

		for (Property p : CheckBundleFilters.T.getProperties()) {
			if (p.getDeclaringType() == GenericEntity.T)
				continue;

			Object v = p.get(request);
			p.set(run, v);
		}
		run.setAggregateBy(Collections.emptyList());

		MulticastRequest m = MulticastRequest.T.create();
		m.setServiceRequest(run);
		m.setAddressee(InstanceId.of(null, "master"));

		return m;
	}

	private Map<InstanceId, CheckBundlesResponse> getDistributedCheckResponses(MulticastRequest multicastRequest) {
		Map<InstanceId, CheckBundlesResponse> responsePerNode = CodingMap.create(new ConcurrentHashMap<>(), InstanceIdHashingComparator.instance);
		MulticastResponse mcResponse = multicastRequest.eval(systemEvaluator).get();

		for (Map.Entry<InstanceId, ServiceResult> entry : mcResponse.getResponses().entrySet()) {
			ServiceResult serviceResult = entry.getValue();
			CheckBundlesResponse response;

			// can this be null?
			InstanceId instanceId = entry.getKey();

			switch (serviceResult.resultType()) {
				case success:
					ResponseEnvelope standardServiceResult = (ResponseEnvelope) serviceResult;
					response = (CheckBundlesResponse) standardServiceResult.getResult();
					break;
				case failure:
					Failure fail = (Failure) serviceResult;
					String nodeId = instanceId.getNodeId();

					CheckResultEntry cre = CheckResultEntry.T.create();
					cre.setCheckStatus(CheckStatus.fail);
					cre.setName("Check Bundle Framework Distributed");
					cre.setMessage("Evaluation of distributed checks on node " + nodeId + " failed with " + fail.getType());
					cre.setDetails(fail.getType() + "\n" + fail.getDetails());

					CheckResult r = CheckResult.T.create();
					r.getEntries().add(cre);

					CheckBundleResult cbr = CheckBundleResult.T.create();
					cbr.setResult(r);
					cbr.setNode(nodeId);
					cbr.setStatus(CheckStatus.fail);
					cbr.setName("Check Bundle Framework - Internal Error");

					response = CheckBundlesResponse.T.create();
					response.getElements().add(cbr);
					response.setStatus(CheckStatus.fail);

					break;
				default:
					throw new IllegalStateException("Unexpected Multicast result type: " + serviceResult.resultType());
			}

			responsePerNode.put(instanceId, response);
		}

		return responsePerNode;
	}

	private CheckBundlesResponse buildResponse(List<CbrAggregationKind> aggregatedBy, Map<CheckBundlesContext, CheckResult> results) {
		CheckBundlesResponse response = CheckBundlesResponse.T.create();

		// # Calculate CheckStatus: ok, fail or warn
		CheckStatus status = CheckBundlesUtils.getStatus(results.values());
		response.setStatus(status);

		// # Create results
		List<CheckBundleResult> bundleResults = new ArrayList<>();
		for (Map.Entry<CheckBundlesContext, CheckResult> entry : results.entrySet()) {
			CheckBundlesContext context = entry.getKey();

			CheckBundleResult cbr = CheckBundleResult.T.create();
			List<Property> qualProperties = CheckBundleQualification.T.getProperties();
			for (Property p : qualProperties) {
				if (p.getDeclaringType() == GenericEntity.T)
					continue;

				if (!p.isAbsent(context.bundle)) {
					Object value = p.get(context.bundle);
					p.set(cbr, value);
				}
			}

			// TODO properly solve this
			if (cbr.getName() == null)
				cbr.setName("n/a");

			CheckResult result = entry.getValue();
			cbr.setResult(result);

			cbr.setCheck(context.processor);
			cbr.setNode(this.instanceId.getNodeId());

			CheckStatus bundleStatus = CheckBundlesUtils.getStatus(result);
			cbr.setStatus(bundleStatus);

			bundleResults.add(cbr);
		}

		// # Aggregate
		aggregate(response, bundleResults, aggregatedBy, 0);

		return response;
	}

	private void aggregate(CbrContainer container, List<CheckBundleResult> results, List<CbrAggregationKind> aggregateBy, int aggregateByIndex) {
		List<CbrAggregatable> elements = container.getElements();

		if (aggregateByIndex < aggregateBy.size()) {
			CbrAggregationKind kind = aggregateBy.get(aggregateByIndex);

			Function<CheckBundleResult, Collection<?>> accessor = CheckBundlesUtils.getAccessor(kind);

			Map<Object, Pair<CbrAggregation, List<CheckBundleResult>>> aggregationByKindSpecificValue = new LinkedHashMap<>();

			Iterator<CheckBundleResult> iterator = results.iterator();
			while (iterator.hasNext()) {
				boolean matched = false;
				CheckBundleResult result = iterator.next();

				Collection<?> values = accessor.apply(result);
				for (Object v : values) {
					if (v == null) {
						continue;
					}
					List<CheckBundleResult> filteredResults = aggregationByKindSpecificValue
							.computeIfAbsent(v, k -> Pair.of(createAggregation(kind, v), new ArrayList<>())).second();
					filteredResults.add(result);
					matched = true;
				}

				if (matched)
					iterator.remove();

			}

			for (Pair<CbrAggregation, List<CheckBundleResult>> aggregationPair : aggregationByKindSpecificValue.values()) {
				CbrAggregation aggregation = aggregationPair.first();
				List<CheckBundleResult> filteredResults = aggregationPair.second();
				aggregate(aggregation, filteredResults, aggregateBy, aggregateByIndex + 1);
				elements.add(aggregation);
			}

			results.sort(comparator);
			elements.addAll(0, results);

		} else {
			results.forEach(elements::add);
			results.clear();
		}

		CheckStatus containerStatus = CheckStatus.ok;

		for (CbrAggregatable aggregatable : elements) {
			CheckStatus aggregatableStatus = aggregatable.getStatus();

			if (aggregatableStatus.ordinal() > containerStatus.ordinal())
				containerStatus = aggregatableStatus;
		}

		elements.sort(comparator);

		container.setStatus(containerStatus);
	}

	static CbrAggregation createAggregation(CbrAggregationKind kind, Object discriminator) {
		CbrAggregation newAggregation = CbrAggregation.T.create();
		newAggregation.setKind(kind);
		newAggregation.setDiscriminator(discriminator);
		return newAggregation;
	}

	private List<CheckBundlesContext> buildCheckContext(List<CheckBundle> bundles) {
		List<CheckBundlesContext> contexts = new ArrayList<>();

		for (CheckBundle b : bundles) {
			for (CheckProcessor p : b.getChecks()) {
				CheckRequest check = CheckRequest.T.create();
				check.setServiceId(p.getExternalId());

				CheckBundlesContext context = new CheckBundlesContext(p, check, b);
				contexts.add(context);
			}
		}

		return contexts;
	}

	@SuppressWarnings("deprecation")
	private List<CheckBundle> getFilteredCheckBundles(RunCheckBundles request) {
		EntityQuery query = EntityQueryBuilder.from(CheckBundle.T).tc(TC.create().negation().joker().done()).done();

		PersistenceGmSession session = cortexSessionSupplier.get();
		List<CheckBundle> result = session.query().entities(query).list();

		for (CheckBundle b : result) {
			if (b instanceof HealthCheck) {
				b.setCoverage(CheckCoverage.vitality);
				b.setName("Health Check");

				b.setIsPlatformRelevant(true);
			}
		}

		Predicate<CheckBundle> bundleFilter = CheckBundlesUtils.buildBundleFilter(request);
		List<CheckBundle> checks = result.stream().filter(bundleFilter).collect(Collectors.toList());

		log.trace(() -> "Found " + checks.size() + " module checks.");
		return checks;
	}

	private Future<CheckResult> evaluateParallel(CheckRequest request) {
		Callable<CheckResult> callable = () -> evaluate(request);
		Callable<CheckResult> contextualizedCallable = threadContextScoping.bindContext(callable);
		return executionThreadPool.submit(contextualizedCallable);
	}

	private CheckResult evaluate(CheckRequest check) {
		CheckResult result;
		long t0 = System.nanoTime();
		try {
			log.trace(() -> "Evaluating check: " + check);
			result = check.eval(systemEvaluator).get();
			log.trace(() -> "Done with evaluating check: " + check.getServiceId());

		} catch (Exception e) {
			log.debug(() -> "Got an exception while evaluating check: " + check.getServiceId(), e);
			result = CheckResult.T.create();

			CheckResultEntry entry = createFailureCheckResultEntry(e);

			result.getEntries().add(entry);
		}

		// Validate
		if (result != null) {

			ListIterator<CheckResultEntry> iterator = result.getEntries().listIterator();
			while (iterator.hasNext()) {
				CheckResultEntry entry = iterator.next();

				if (entry != null) {
					if (entry.getCheckStatus() == null) {
						entry.setCheckStatus(CheckStatus.warn);

						String name = entry.getName();
						String message = entry.getMessage();

						entry.setName("Check Bundle Framework Result Entry Validation");
						entry.setMessage("CheckResultEntry.status must not be null");

						StringBuilder builder = new StringBuilder();

						String details = entry.getDetails();

						builder.append("# Original Entry Values\n");
						builder.append("The system has kept the original values of this entry:<br><br>\n");

						builder.append("\n\nEntry Property | Original Value\n");
						builder.append("--- | ---\n");
						builder.append("Name | ");
						builder.append(XmlTools.escape(name));
						builder.append('\n');
						builder.append("Message | ");
						builder.append(XmlTools.escape(message));

						if (details != null) {
							builder.append("\n\n# Original Details\n");

							if (entry.getDetailsAsMarkdown()) {
								builder.append(details);
							} else {
								builder.append("<pre>");
								builder.append(XmlTools.escape(details));
								builder.append("</pre>");
							}
						}

						entry.setDetails(builder.toString());
					}

				} else {
					entry = CheckResultEntry.T.create();

					entry.setCheckStatus(CheckStatus.warn);
					entry.setName("Check Bundle Framework Result Entry Validation");
					entry.setMessage("Received a null CheckResultEntry when evaluating check: " + check.getServiceId());
				}

				iterator.set(entry);
			}

		} else {
			result = CheckResult.T.create();

			CheckResultEntry cre = CheckResultEntry.T.create();
			cre.setCheckStatus(CheckStatus.warn);
			cre.setName("Check Bundle Framework Result Validation");
			cre.setMessage("Received a null CheckResult from processor when evaluating check: " + check.getServiceId());

			result.getEntries().add(cre);

			result.setElapsedTimeInMs((System.nanoTime() - t0) / 1_000_000.0);

		}

		return result;
	}

}
