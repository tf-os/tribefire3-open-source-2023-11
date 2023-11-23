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
package tribefire.extension.job_scheduling.deployment.model;

import com.braintribe.model.descriptive.HasCredentials;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.extensiondeployment.Worker;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * <p>
 * This interface is designed to provide an extension point for running scheduled {@link Job Jobs} in an interval
 * defined by {@link #setCronExpression(String)}.
 * 
 * <p>
 * As {@link HasCredentials} is defined, meaning that user credentials can be configured to define the execution scope
 * (in case user and password credentials are left empty, the internal default user session scope is used).
 * 
 * <p>
 * In case the scheduler is unable to execute scheduled jobs (e.g. the scheduler shut down),
 * {@link #setCoalescing(boolean)} defines, if job executions are coalesced to avoid execution several times in
 * succession.
 */
@Abstract
public interface JobScheduling extends Worker, HasCredentials {

	EntityType<JobScheduling> T = EntityTypes.T(JobScheduling.class);

	/**
	 * The task to be executed by the scheduler.
	 */
	void setJobRequestProcessor(ServiceProcessor jobRequestProcessor);
	ServiceProcessor getJobRequestProcessor();

	/**
	 * If set to <code>true</code> missed job executions will be coalesced to avoid execution several times in
	 * succession.
	 */
	void setCoalescing(boolean coalescing);
	boolean getCoalescing();
}
