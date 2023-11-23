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
package tribefire.platform.wire.space.cortex.accesses;

import static java.util.Collections.singletonList;

import java.util.List;

import com.braintribe.model.access.collaboration.CollaborativeSmoodAccess;
import com.braintribe.model.access.impl.aop.AopAccess;
import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.accessdeployment.HardwiredCollaborativeAccess;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aop.api.service.AopIncrementalAccess;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.model.processing.sp.api.StateChangeProcessorRule;
import com.braintribe.model.processing.sp.aspect.StateProcessingAspect;
import com.braintribe.model.processing.sp.commons.ConfigurableStateChangeProcessorRuleSet;
import com.braintribe.model.processing.sp.invocation.multithreaded.MultiThreadedSpInvocation;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.platform.wire.space.common.EnvironmentSpace;
import tribefire.platform.wire.space.cortex.AccessAspectsSpace;
import tribefire.platform.wire.space.cortex.TraversingCriteriaSpace;
import tribefire.platform.wire.space.cortex.deployment.DeploymentSpace;
import tribefire.platform.wire.space.cortex.deployment.StateChangeProcessorsSpace;
import tribefire.platform.wire.space.security.AuthContextSpace;
import tribefire.platform.wire.space.streaming.ResourceAccessSpace;

@Managed
public abstract class HardwiredAccessSpaceBase extends SystemAccessSpaceBase {

	// @formatter:off
	@Import protected AccessAspectsSpace aspects;
	@Import protected AuthContextSpace authContext;
	@Import protected DeploymentSpace deployment;
	@Import protected EnvironmentSpace environment;
	@Import protected ResourceAccessSpace resourceAccess;
	@Import private StateChangeProcessorsSpace stateChangeProcessors;
	@Import protected SystemAccessCommonsSpace systemAccessCommons;
	@Import protected SystemAccessesSpace systemAccesses;
	@Import protected TraversingCriteriaSpace traversingCriteria;
	// @formatter:on

	@Managed
	public AopIncrementalAccess aopAccess() {
		AopAccess bean = new AopAccess();
		bean.setAccessId(id());
		bean.setDelegate(access());
		bean.setSystemSessionFactory(gmSessions.systemSessionFactory());
		bean.setUserSessionFactory(gmSessions.sessionFactory());
		bean.setAspects(aopAspects());

		return bean;
	}

	protected abstract List<AccessAspect> aopAspects();

	public CollaborativeSmoodAccess collaborativeAccess() {
		if (!isCollaborativeAccess())
			throw new IllegalStateException("Access '" + id() + "' is not a collaboratie access!");
		return (CollaborativeSmoodAccess) access();
	}

	public abstract boolean isCollaborativeAccess();

	public abstract HardwiredAccessSpaceBase workbenchAccessSpace();

	@Managed
	protected StateProcessingAspect stateProcessingAspect() {
		StateProcessingAspect bean = new StateProcessingAspect();
		bean.setProcessorRuleSet(stateChangeRuleSet());
		bean.setAsyncInvocationQueue(asyncInvocationQueue());
		return bean;
	}

	@Managed
	private MultiThreadedSpInvocation asyncInvocationQueue() {
		MultiThreadedSpInvocation bean = new MultiThreadedSpInvocation();
		bean.setThreadCount(environment.property(TribefireRuntime.ENVIRONMENT_STATEPROCESSING_THREADS, Integer.class, 20));
		bean.setProcessorRuleSet(stateChangeRuleSet());
		bean.setUserSessionScoping(authContext.masterUser().userSessionScoping());
		bean.setSessionFactory(gmSessions.sessionFactory());
		bean.setSystemSessionFactory(gmSessions.systemSessionFactory());
		bean.setName(name());
		return bean;
	}

	@Managed
	private ConfigurableStateChangeProcessorRuleSet stateChangeRuleSet() {
		ConfigurableStateChangeProcessorRuleSet bean = new ConfigurableStateChangeProcessorRuleSet();
		bean.setProcessorRules(stateChangeProcessorRules());
		return bean;
	}

	protected List<StateChangeProcessorRule> stateChangeProcessorRules() {
		return singletonList(stateChangeProcessors.bidiProperty());
	}

	@Managed
	public HardwiredAccess hardwiredDeployable() {
		HardwiredAccess bean = isCollaborativeAccess() ? HardwiredCollaborativeAccess.T.create() : HardwiredAccess.T.create();

		bean.setExternalId(id());
		bean.setName(name());
		bean.setGlobalId(globalId());

		String modelName = modelName();
		GmMetaModel metaModel = GmMetaModel.T.create(Model.modelGlobalId(modelName));
		metaModel.setName(modelName);

		bean.setMetaModel(metaModel);

		if (workbenchAccessSpace() != null) {
			bean.setWorkbenchAccess(workbenchAccessSpace().hardwiredDeployable());
		}

		String serviceModelName = serviceModelName();
		if (serviceModelName != null) {
			GmMetaModel serviceModel = GmMetaModel.T.create(Model.modelGlobalId(serviceModelName));
			serviceModel.setName(serviceModelName);
			bean.setServiceModel(serviceModel);
		}

		return bean;
	}

}
