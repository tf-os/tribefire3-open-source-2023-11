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
package tribefire.extension.jobscheduling.job_scheduling_quartz.wire.space;

import static com.braintribe.wire.api.scope.InstanceConfiguration.currentInstance;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.braintribe.exception.Exceptions;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.extension.job_scheduling.deployment.model.JobCronScheduling;
import tribefire.extension.job_scheduling.processing.QuartzScheduling;
import tribefire.module.wire.contract.TribefireWebPlatformContract;

@Managed
public class QuartzSpace implements WireSpace {
	private static Logger logger = Logger.getLogger(QuartzSpace.class);

	@Import
	private TribefireWebPlatformContract tfPlatform;
	// TODO: requestUserRelated() masterUserAuthContext() aus WebPlatform in Platform ! (alles, das nicht "web" ist)
	
	@Managed
	private Scheduler scheduler() {
		try {
			Scheduler bean = StdSchedulerFactory.getDefaultScheduler();

			currentInstance().onDestroy(() -> {
				try {
					bean.shutdown(true);
				} catch (SchedulerException e) {
					logger.error("error while shutting down standard quartz scheduler", e);
				}
			});

			bean.start();
			return bean;

		} catch (SchedulerException e) {
			throw Exceptions.unchecked(e, "Error while creating quartz standard scheduler");
		}
	}

	@Managed
	public QuartzScheduling quartzScheduling(ExpertContext<JobCronScheduling> context) {
		QuartzScheduling bean = new QuartzScheduling();
		bean.setDeployable(context.getDeployable());
		bean.setRequestEvaluator(tfPlatform.requestUserRelated().evaluator());
		bean.setUserSessionScoping(tfPlatform.masterUserAuthContext().userSessionScoping());
		bean.setScheduler(scheduler());
		return bean;
	}

}
