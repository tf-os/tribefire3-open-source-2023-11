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
package tribefire.extension.scheduling.wire.space;

import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.scheduling.job.SchedulerJobImpl;
import tribefire.extension.scheduling.model.deployment.SchedulerJob;
import tribefire.extension.scheduling.model.deployment.SchedulingProcessor;
import tribefire.extension.scheduling.service.SchedulingServiceProcessor;
import tribefire.module.wire.contract.ModuleResourcesContract;
import tribefire.module.wire.contract.PlatformResourcesContract;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class SchedulingDeployablesSpace implements WireSpace {

	@Import
	private TribefireWebPlatformContract tfPlatform;

	@Import
	private ModuleResourcesContract moduleResources;

	@Import
	private PlatformResourcesContract resources;

	@Managed
	public SchedulerJobImpl cleanupScheduledJob(@SuppressWarnings("unused") ExpertContext<SchedulerJob> context) {
		SchedulerJob deployable = context.getDeployable();
		SchedulerJobImpl bean = new SchedulerJobImpl();
		bean.setSystemServiceRequestEvaluator(tfPlatform.systemUserRelated().evaluator());
		bean.setAccessId(deployable.getAccessId());
		return bean;
	}

	@Managed
	public SchedulingServiceProcessor serviceProcessor(ExpertContext<? extends SchedulingProcessor> context) {
		SchedulingProcessor deployable = context.getDeployable();
		SchedulingServiceProcessor bean = new SchedulingServiceProcessor();

		bean.setDataSessionSupplier(() -> tfPlatform.systemUserRelated().sessionFactory().newSession(deployable.getAccessId()));
		bean.setRequestEvaluator(tfPlatform.systemUserRelated().evaluator());
		bean.setLockManager(tfPlatform.locking().manager());

		return bean;
	}

}
