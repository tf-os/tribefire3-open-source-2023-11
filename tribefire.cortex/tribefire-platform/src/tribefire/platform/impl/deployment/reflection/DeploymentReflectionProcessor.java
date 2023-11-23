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
package tribefire.platform.impl.deployment.reflection;

import static com.braintribe.utils.lcd.CollectionTools2.newIdentityMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.braintribe.cfg.Required;
import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.HardwiredDeployable;
import com.braintribe.model.deploymentreflection.DeployedComponent;
import com.braintribe.model.deploymentreflection.DeployedUnit;
import com.braintribe.model.deploymentreflection.DeploymentStatus;
import com.braintribe.model.deploymentreflection.DeploymentSummary;
import com.braintribe.model.deploymentreflection.InstanceDescriptor;
import com.braintribe.model.deploymentreflection.QualifiedDeployedUnit;
import com.braintribe.model.deploymentreflection.QualifiedDeployedUnits;
import com.braintribe.model.deploymentreflection.request.DeploymentReflectionRequest;
import com.braintribe.model.deploymentreflection.request.GetDeployedDeployables;
import com.braintribe.model.deploymentreflection.request.GetDeploymentStatus;
import com.braintribe.model.deploymentreflection.request.GetDeploymentStatusNotification;
import com.braintribe.model.deploymentreflection.request.GetDeploymentSummary;
import com.braintribe.model.deploymentreflection.request.GetDeploymentSummaryPlain;
import com.braintribe.model.deploymentreflection.request.UnitFilter;
import com.braintribe.model.deploymentreflection.result.DeploymentStatusNotification;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.generic.typecondition.TypeCondition;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.notification.api.builder.Notifications;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.service.api.MulticastRequest;
import com.braintribe.model.service.api.result.MulticastResponse;
import com.braintribe.model.service.api.result.ResponseEnvelope;
import com.braintribe.model.service.api.result.ServiceResult;
import com.braintribe.wire.api.scope.InstanceHolder;

import tribefire.platform.impl.deployment.proxy.ProxyingDeployedComponentResolver;

/**
 * TODO JAVADOC
 * @author christina.wilpernig
 */
public class DeploymentReflectionProcessor extends AbstractDispatchingServiceProcessor<DeploymentReflectionRequest, Object> {

	private static final Logger logger = Logger.getLogger(DeploymentReflectionProcessor.class);

	private DeployRegistry deployRegistry;
	private Function<Object, InstanceHolder> beanHolderLookup;
	private Supplier<Set<String>> userRolesProvider;
	private Set<String> allowedRoles;

	private ProxyingDeployedComponentResolver proxyResolver;

	@Required
	public void setAllowedRoles(Set<String> allowedRoles) {
		this.allowedRoles = allowedRoles;
	}

	@Required
	public void setUserRolesProvider(Supplier<Set<String>> userRolesProvider) {
		this.userRolesProvider = userRolesProvider;
	}

	@Required
	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;
	}

	@Required
	public void setBeanHolderLookup(Function<Object, InstanceHolder> beanHolderLookup) {
		this.beanHolderLookup = beanHolderLookup;
	}
	
	@Required
	public void setProxyResolver(ProxyingDeployedComponentResolver proxyResolver) {
		this.proxyResolver = proxyResolver;
	}

	@Override
	protected void configureDispatching(DispatchConfiguration<DeploymentReflectionRequest, Object> dispatching) {
		dispatching.register(GetDeploymentStatus.T, (c, r) -> getDeploymentStatus(r));
		dispatching.register(GetDeploymentSummary.T, (c, r) -> getDeploymentSummary(c, r));
		dispatching.register(GetDeploymentSummaryPlain.T, (c, r) -> getDeploymentSummaryPlain(c, r));
		dispatching.register(GetDeploymentStatusNotification.T, (c, r) -> getDeploymentStatusNotification(r));
		dispatching.register(GetDeployedDeployables.T, (c, r) -> getDeployedDeployables());
	}
	
	private DeploymentStatus getDeploymentStatus(GetDeploymentStatus request) {
		checkPrivileges();

		TypeCondition deployableTypeCondition = null;
		String externalIdPattern = null;

		UnitFilter unitFilter = request.getUnitFilter();
		if (unitFilter != null) {
			deployableTypeCondition = request.getUnitFilter().getDeployableFilter();
			externalIdPattern = unitFilter.getExternalIdPattern();
		}

		Predicate<Deployable> deployableFilter = d -> true;
		if (deployableTypeCondition != null) {
			TypeCondition typeCondition = deployableTypeCondition;
			deployableFilter = deployableFilter.and(d -> typeCondition.matches(d.type()));
		}

		if (externalIdPattern != null) {
			String output = Stream.of(externalIdPattern.split(","))
			// @formatter:off
			.map(s -> s.endsWith("*") ? 
				Stream.of(s.split("\\*")).map(Pattern::quote).collect(Collectors.joining(".*")).concat(".*")
					:
				Stream.of(s.split("\\*")).map(Pattern::quote).collect(Collectors.joining(".*")))
			.collect(Collectors.joining("|"));
			// @formatter:on

			Pattern idPattern = Pattern.compile(output);
			deployableFilter = deployableFilter.and(d -> idPattern.matcher(d.getExternalId()).matches());
		}

		DeploymentStatus status = DeploymentStatus.T.create();
		for (Deployable deployable : deployRegistry.getDeployables()) {
			if (!deployableFilter.test(deployable)) {
				continue;
			}

			DeployedUnit unit = createReflectionDeployedUnit(deployable);
			if (unit != null) {
				status.getUnits().add(unit);
			}
		}

		return status;
	}

	private DeploymentSummary getDeploymentSummary(ServiceRequestContext context, GetDeploymentSummary request) {
		checkPrivileges();

		GetDeploymentStatus getDeploymentStatus = GetDeploymentStatus.T.create();
		getDeploymentStatus.setUnitFilter(request.getUnitFilter());

		MulticastRequest multicastRequest = MulticastRequest.T.create();
		multicastRequest.setServiceRequest(getDeploymentStatus);
		multicastRequest.setAddressee(request.getMulticastFilter());
		MulticastResponse response = multicastRequest.eval(context).get();
		
		DeploymentSummary summary = createSummary(response);
		logger.debug(() -> "Created deployment summary for request [" + request + "].");

		return summary;
	}

	private DeploymentSummary getDeploymentSummaryPlain(ServiceRequestContext context, GetDeploymentSummaryPlain request) {
		
		GetDeploymentSummary getDeploymentSummary = DeploymentReflectionContext.createGetDeploymentSummaryRequest(request);
		DeploymentSummary summary = getDeploymentSummary(context, getDeploymentSummary);
		return summary;
	}

	private DeploymentStatusNotification getDeploymentStatusNotification(GetDeploymentStatusNotification request) {
		
		List<Deployable> deployables = request.getDeployables();
		// @formatter:off
		String externalIds = deployables.stream().
	       map(d -> d.getExternalId()).
	       collect(Collectors.joining(",")).toString();
		// @formatter:on

		String url = TribefireRuntime.getPublicServicesUrl() + "/deployment-summary?externalId=" + externalIds + "&hideNav=true";

		// @formatter:off
		DeploymentStatusNotification response = DeploymentStatusNotification.T.create();
		response.setNotifications(
				Notifications.build()
				.add()
					.command()
						.gotoUrl("Deployment Status").url(url).target("_self")
					.close()
				.close()
				.list());
		// @formatter:on

		return response;
	}
	
	private List<String> getDeployedDeployables () {
		return deployRegistry.getDeployables().stream().map(Deployable::getExternalId).collect(Collectors.toList());
	}

	private DeploymentSummary createSummary(MulticastResponse multicastResponse) {
		Map<InstanceId, QualifiedDeployedUnits> totalQualifiedUnits = newIdentityMap();

		for (Map.Entry<InstanceId, ServiceResult> response : multicastResponse.getResponses().entrySet()) {
			QualifiedDeployedUnits qualifiedUnits = QualifiedDeployedUnits.T.create();
			qualifiedUnits.setIsComplete(true);

			createQualifiedDeployedUnits(response, qualifiedUnits);
			totalQualifiedUnits.put(response.getKey(), qualifiedUnits);
		}

		totalQualifiedUnits = identityManageDeployables(totalQualifiedUnits);

		DeploymentSummary summary = createDeploymentSummary(totalQualifiedUnits);
		return summary;
	}

	private Map<InstanceId, QualifiedDeployedUnits> identityManageDeployables(Map<InstanceId, QualifiedDeployedUnits> totalQualifiedUnits) {
		StandardCloningContext cloningContext = new StandardCloningContext() {
			// must be identity managed for each InstanceId an own instance of the same deployable is returned
			private final Map<String, Deployable> identityManagedDeployables = newMap();

			@Override
			public GenericEntity supplyRawClone(EntityType<? extends GenericEntity> entityType, GenericEntity instanceToBeCloned) {
				if (Deployable.T.isAssignableFrom(entityType)) {

					String externalId = ((Deployable) instanceToBeCloned).getExternalId();
					Deployable deployable = (Deployable) entityType.create();
					identityManagedDeployables.put(externalId, deployable);
					return deployable;

				} else {
					return instanceToBeCloned;
				}
			}

			@Override
			public <T> T getAssociated(GenericEntity entity) {
				T associated = super.getAssociated(entity);
				if (associated != null) {
					return associated;
				}

				if (entity instanceof Deployable) {
					Deployable d = (Deployable) entity;
					String externalId = d.getExternalId();

					Deployable deployable = identityManagedDeployables.get(externalId);
					if (deployable != null) {
						registerAsVisited(entity, deployable);
						return (T) deployable;
					}
				}

				return null;
			}
		};

		return GMF.getTypeReflection().getMapType(InstanceId.T, QualifiedDeployedUnits.T).clone(cloningContext, totalQualifiedUnits,
				StrategyOnCriterionMatch.reference);

	}

	private <T> QualifiedDeployedUnits aquireQualifiedDeployedUnits(T key, Map<T, QualifiedDeployedUnits> unitsMap) {
		QualifiedDeployedUnits units = unitsMap.get(key);

		if (units == null) {
			units = QualifiedDeployedUnits.T.create();
			units.setIsComplete(true);
			unitsMap.put(key, units);
		}
		return units;
	}

	private <T> void merge(T key, Map<T, QualifiedDeployedUnits> unitsMap, QualifiedDeployedUnits unitsToMerge) {
		QualifiedDeployedUnits units = aquireQualifiedDeployedUnits(key, unitsMap);

		units.getUnits().addAll(unitsToMerge.getUnits());
		if (!unitsToMerge.getIsComplete()) {
			units.setIsComplete(false);
		}
	}

	private <T> void merge(T key, Map<T, QualifiedDeployedUnits> unitsMap, QualifiedDeployedUnit unitToMerge) {
		QualifiedDeployedUnits units = aquireQualifiedDeployedUnits(key, unitsMap);
		units.getUnits().add(unitToMerge);
	}

	protected DeploymentSummary createDeploymentSummary(Map<InstanceId, QualifiedDeployedUnits> unitsByInstanceId) {
		Map<String, QualifiedDeployedUnits> unitsByNode = newMap();
		Map<String, QualifiedDeployedUnits> unitsByCartridge = newMap();
		Map<Deployable, QualifiedDeployedUnits> unitsByDeployable = newIdentityMap();
		List<QualifiedDeployedUnits> sourceUnits = newList();

		QualifiedDeployedUnits totalUnits = QualifiedDeployedUnits.T.create();
		totalUnits.setIsComplete(true);

		for (Entry<InstanceId, QualifiedDeployedUnits> entry : unitsByInstanceId.entrySet()) {
			QualifiedDeployedUnits units = entry.getValue();
			InstanceId instanceId = entry.getKey();

			merge(instanceId.getNodeId(), unitsByNode, units);
			merge(instanceId.getApplicationId(), unitsByCartridge, units);

			totalUnits.getUnits().addAll(units.getUnits());

			if (!units.getIsComplete()) {
				totalUnits.setIsComplete(false);
			}

			for (QualifiedDeployedUnit unit : units.getUnits()) {
				Deployable deployable = unit.getDeployedUnit().getDeployable();
				merge(deployable, unitsByDeployable, unit);
			}
			sourceUnits.add(units);
		}

		DeploymentSummary summary = DeploymentSummary.T.create();
		summary.setTotalUnits(totalUnits);
		summary.setUnitsByNode(unitsByNode);
		summary.setUnitsByCartridge(unitsByCartridge);
		summary.setUnitsByDeployable(unitsByDeployable);
		summary.setUnitsByInstanceId(unitsByInstanceId);
		summary.setSourceUnits(sourceUnits);

		return summary;
	}

	private void createQualifiedDeployedUnits(Entry<InstanceId, ServiceResult> response, QualifiedDeployedUnits qualifiedUnits) {
		InstanceId instanceId = response.getKey();
		ServiceResult serviceResult = response.getValue();
		ResponseEnvelope standardServiceResult;

		switch (serviceResult.resultType()) {
			case success:
				standardServiceResult = (ResponseEnvelope) response.getValue();
				break;
			default:
				qualifiedUnits.setIsComplete(false);
				return;
		}

		DeploymentStatus deploymentStatus = (DeploymentStatus) standardServiceResult.getResult();
		for (DeployedUnit unit : deploymentStatus.getUnits()) {
			QualifiedDeployedUnit qdu = QualifiedDeployedUnit.T.create();
			qdu.setInstanceId(instanceId);
			qdu.setDeployedUnit(unit);

			qualifiedUnits.getUnits().add(qdu);
		}
	}

	private DeployedUnit createReflectionDeployedUnit(Deployable deployable) {

		com.braintribe.model.processing.deployment.api.DeployedUnit resolvedUnit = deployRegistry.resolve(deployable);

		Map<EntityType<? extends Deployable>, com.braintribe.model.processing.deployment.api.DeployedComponent> componentsMap = resolvedUnit != null
				? resolvedUnit.getComponents()
				: Collections.emptyMap();

		DeployedUnit unit = DeployedUnit.T.create();

		Set<DeployedComponent> components = getReflectedDeployedComponents(componentsMap);
		unit.setComponents(components);

		proxyResolver.getResolvedProxyAdressings().forEach(p -> {
			if(p.getFirst().equals(deployable.getExternalId())) {
				if(!componentsMap.containsKey(p.getSecond())) {
					unit.getMissingComponentTypes().add(p.getSecond().getTypeSignature());
				}
			}
		});
		
		Deployable d = DeploymentReflectionContext.cloneDeployable(deployable);
		unit.setIsHardwired(deployable instanceof HardwiredDeployable);
		unit.setDeployable(d);

		return unit;
	}

	private Set<DeployedComponent> getReflectedDeployedComponents(
			Map<EntityType<? extends Deployable>, com.braintribe.model.processing.deployment.api.DeployedComponent> componentsMap) {

		Set<DeployedComponent> components = newSet();
		for (Map.Entry<EntityType<? extends Deployable>, com.braintribe.model.processing.deployment.api.DeployedComponent> entry : componentsMap
				.entrySet()) {
			DeployedComponent c = createReflectionDeployedComponent(entry.getKey(), entry.getValue());
			components.add(c);
		}
		return components;
	}

	private DeployedComponent createReflectionDeployedComponent(EntityType<? extends Deployable> type,
			com.braintribe.model.processing.deployment.api.DeployedComponent component) {

		DeployedComponent deployedComponent = DeployedComponent.T.create();

		Object suppliedImplementation = component.suppliedImplementation();
		if (suppliedImplementation != null) {
			deployedComponent.setSuppliedImplementation(createInstanceDescriptor(suppliedImplementation));
		}

		ComponentBinder<?, ?> binder = component.binder();
		if (binder != null) {
			deployedComponent.setComponentBinder(createInstanceDescriptor(binder));
		}
		deployedComponent.setComponentType(type.getTypeSignature());
		return deployedComponent;
	}

	public InstanceDescriptor createInstanceDescriptor(Object instance) {
		InstanceDescriptor descriptor = InstanceDescriptor.T.create();

		descriptor.setIdentityHint(String.valueOf(System.identityHashCode(instance)));
		descriptor.setType(instance.getClass().getName());
		InstanceHolder beanHolder = beanHolderLookup.apply(instance);
		if (beanHolder != null) {
			descriptor.setBeanName(beanHolder.name());
			descriptor.setBeanSpace(beanHolder.space().getClass().getName());
		}

		return descriptor;
	}

	private void checkPrivileges() {
		Set<String> roles = newSet(allowedRoles);
		roles.retainAll(userRolesProvider.get());
		if (roles.isEmpty()) {
			throw new AuthorizationException("User has not sufficient privileges to execute this request!");
		}
	}

}
