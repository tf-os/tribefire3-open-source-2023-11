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
package tribefire.platform.wire.space.cortex.deployment.deployables.access;

import static com.braintribe.wire.api.util.Lists.list;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.braintribe.model.access.impl.aop.AopAccess;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.deployment.api.DeploymentContext;
import com.braintribe.model.processing.sp.aspect.StateProcessingAspect;
import com.braintribe.model.processing.sp.commons.ConfigurableStateChangeProcessorRuleSet;
import com.braintribe.model.processing.sp.invocation.multithreaded.MultiThreadedSpInvocation;
import com.braintribe.model.spapi.StateChangeProcessorInvocationPacket;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.impl.binding.MasterIncrementalAccessBinder;
import tribefire.platform.wire.space.cortex.AccessAspectsSpace;
import tribefire.platform.wire.space.cortex.deployment.StateChangeProcessorsSpace;
import tribefire.platform.wire.space.cortex.deployment.deployables.DeployableBaseSpace;
import tribefire.platform.wire.space.cortex.services.AccessServiceSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;

@Managed
public class IncrementalAccessListenerSpace extends DeployableBaseSpace {

	@Import
	private AccessServiceSpace accessService;

	@Import
	private ResourceAccessSpace resourceAccess;

	@Import
	private AccessAspectsSpace aspects;

	@Import
	private StateChangeProcessorsSpace stateChangeProcessors;
	
	@Import
	private SimulatedAccessSpace simulatedAccess;

	@Managed
	public MasterIncrementalAccessBinder incrementalAccessBinder() {
		MasterIncrementalAccessBinder bean = new MasterIncrementalAccessBinder();
		bean.setAopAccessFactory(this::aopAccess);
		bean.setSimulatedAccessFactory(simulatedAccess::access);
		bean.setResourceAccessFactorySupplier(resourceAccess::resourceAccessFactory);
		bean.setAccessService(accessService.service());
		bean.setDefaultFolder(resources.database(".").asPath().toString());
		return bean;
	}

	@Managed
	private AopAccess aopAccess(DeploymentContext<? extends IncrementalAccess, ? extends com.braintribe.model.access.IncrementalAccess> context) {
		AopAccess bean = new AopAccess();
		bean.setAccessId(context.getDeployable().getExternalId());
		bean.setSystemSessionFactory(gmSessions.systemSessionFactory());
		bean.setUserSessionFactory(gmSessions.sessionFactory());
		bean.setDelegate(context.getInstanceToBeBound());
		bean.setAspects(aopAspects(context));
		return bean;
	}


	@Managed
	private StateProcessingAspect stateProcessingAspect(DeploymentContext<? extends IncrementalAccess, ? extends com.braintribe.model.access.IncrementalAccess> context) {
		StateProcessingAspect bean = new StateProcessingAspect();
		bean.setProcessorRuleSet(ruleSet(context));
		bean.setAsyncInvocationQueue(asyncInvocationQueue(context));
		return bean;
	}

	@Managed
	private Consumer<StateChangeProcessorInvocationPacket> asyncInvocationQueue(DeploymentContext<? extends IncrementalAccess, ? extends com.braintribe.model.access.IncrementalAccess> context) {
		MultiThreadedSpInvocation bean = new MultiThreadedSpInvocation();
		bean.setThreadCount(environment.property(TribefireRuntime.ENVIRONMENT_STATEPROCESSING_THREADS, Integer.class, 20));
		bean.setProcessorRuleSet(ruleSet(context));
		bean.setUserSessionScoping(authContext.masterUser().userSessionScoping());
		bean.setSessionFactory(gmSessions.sessionFactory());
		bean.setSystemSessionFactory(gmSessions.systemSessionFactory());
		bean.setName("Incremental Access Listener");
		return bean;
	}



	@Managed
	private ConfigurableStateChangeProcessorRuleSet ruleSet(@SuppressWarnings("unused") DeploymentContext<? extends IncrementalAccess, ? extends com.braintribe.model.access.IncrementalAccess> context) {
		ConfigurableStateChangeProcessorRuleSet bean = new ConfigurableStateChangeProcessorRuleSet();
		// @formatter:off
		bean.setProcessorRules(
				list(
					stateChangeProcessors.bidiProperty(),
					stateChangeProcessors.metadata()
				)
			);
		// @formatter:on
		return bean;
	}

	@Managed
	private List<com.braintribe.model.processing.aop.api.aspect.AccessAspect> aopAspects(DeploymentContext<? extends IncrementalAccess, ? extends com.braintribe.model.access.IncrementalAccess> context) {

		IncrementalAccess deployable = context.getDeployable();

		if (deployable.getAspectConfiguration() != null) {

			List<com.braintribe.model.processing.aop.api.aspect.AccessAspect> bean = new ArrayList<com.braintribe.model.processing.aop.api.aspect.AccessAspect>();

			if (deployable.getAspectConfiguration().getAspects() != null) {
				for (AccessAspect aspect : deployable.getAspectConfiguration().getAspects()) {
					bean.add(context.resolve(aspect, AccessAspect.T));
				}
			}
			return bean;

		} else {
			// @formatter:off
			return list(
					aspects.fulltext(),
					aspects.security(),
					// Disable audit aspect per default. ref.: BTT-5963
					aspects.idGenerator(),
					stateProcessingAspect(context)
			);
			// @formatter:on
		}

	}

}
