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
package tribefire.platform.wire.space.system;

import java.util.concurrent.ExecutorService;

import com.braintribe.codec.marshaller.api.HasStringCodec;
import com.braintribe.common.lcd.Numbers;
import com.braintribe.execution.virtual.VirtualThreadExecutorBuilder;
import com.braintribe.logging.Logger;
import com.braintribe.model.extensiondeployment.HardwiredWorker;
import com.braintribe.model.processing.bootstrapping.TribefireRuntime;
import com.braintribe.utils.lcd.StringTools;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.impl.service.execution.ExecutionPersistenceCleanupWorker;
import tribefire.platform.impl.service.execution.ExecutionPersistenceScheduler;
import tribefire.platform.impl.service.execution.ExecutionPersistenceServiceProcessor;
import tribefire.platform.impl.worker.impl.BasicWorkerManager;
import tribefire.platform.wire.space.common.MarshallingSpace;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.accesses.CortexAccessSpace;
import tribefire.platform.wire.space.cortex.services.ClusterSpace;
import tribefire.platform.wire.space.cortex.services.WorkerSpace;
import tribefire.platform.wire.space.rpc.RpcSpace;

@Managed
public class ExecutionPersistenceSpace implements WireSpace {

	private static final Logger logger = Logger.getLogger(ExecutionPersistenceSpace.class);

	@Import
	private RpcSpace rpc;

	@Import
	private SystemTasksSpace systemTasks;

	@Import
	private ClusterSpace cluster;

	@Import
	private CortexAccessSpace cortexAccess;

	@Import
	protected WorkerSpace worker;

	@Import
	private MarshallingSpace marshalling;

	@Import
	private GmSessionsSpace gmSessions;

	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		BasicWorkerManager workerManager = worker.manager();
		// workerManager.deploy(jobScheduler());
		workerManager.deploy(executionPersistenceCleanupWorker());
	}

	@Managed
	public ExecutionPersistenceScheduler executionPersistenceScheduler() {

		ExecutionPersistenceScheduler bean = new ExecutionPersistenceScheduler();
		bean.setMaxInactivityBeforeRetry(Numbers.MILLISECONDS_PER_MINUTE * 10);
		bean.setCheckInterval(Numbers.MILLISECONDS_PER_MINUTE * 5);
		bean.setLocking(cluster.locking());
		bean.setSessionSupplier(cortexAccess.sessionProvider());

		HardwiredWorker workerIdentification = HardwiredWorker.T.create();
		workerIdentification.setId("JobScheduler-8c103e2f-0fb9-4431-8890-95d5e8cd2044");

		bean.setWorkerIdentification(workerIdentification);
		bean.setProcessor(rpc.persistingAsynchronousServiceProcessor());
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());
		bean.setStringCodec((HasStringCodec) marshalling.jsonMarshaller());
		bean.setMaxTries(3);

		return bean;
	}

	@Managed
	public ExecutionPersistenceCleanupWorker executionPersistenceCleanupWorker() {

		ExecutionPersistenceCleanupWorker bean = new ExecutionPersistenceCleanupWorker();

		long checkInterval = Numbers.MILLISECONDS_PER_HOUR * 2;
		String checkIntervalString = TribefireRuntime.getProperty("TRIBEFIRE_EXECUTION_PERSISTENCE_CLEANUP_CHECK_INTERVAL");
		if (!StringTools.isBlank(checkIntervalString)) {
			try {
				checkInterval = Long.parseLong(checkIntervalString);
			} catch (NumberFormatException nfe) {
				logger.warn(
						"Configured execution persistence cleanup check interval TRIBEFIRE_EXECUTION_PERSISTENCE_CLEANUP_CHECK_INTERVAL is not configured correctly: "
								+ checkIntervalString,
						nfe);
			}
		}
		bean.setCheckInterval(checkInterval);
		bean.setSessionSupplier(cortexAccess.sessionProvider());
		bean.setRequestEvaluator(rpc.serviceRequestEvaluator());

		HardwiredWorker workerIdentification = HardwiredWorker.T.create();
		workerIdentification.setId("JobCleanupWorker-8c103e2f-0fb9-4431-8890-95d5e8cd2044");

		bean.setWorkerIdentification(workerIdentification);

		return bean;
	}

	@Managed
	public ExecutionPersistenceServiceProcessor executionPersistenceServiceProcessor() {
		ExecutionPersistenceServiceProcessor bean = new ExecutionPersistenceServiceProcessor();
		bean.setAccessSessionSupplier(accessId -> gmSessions.systemSessionSupplier(accessId));

		long maxAge = Numbers.MILLISECONDS_PER_YEAR * 3;
		String maxAgeString = TribefireRuntime.getProperty("TRIBEFIRE_EXECUTION_PERSISTENCE_CLEANUP_MAX_AGE");
		if (!StringTools.isBlank(maxAgeString)) {
			try {
				maxAge = Long.parseLong(maxAgeString);
			} catch (NumberFormatException nfe) {
				logger.warn(
						"Configured execution persistence cleanup maximum age TRIBEFIRE_EXECUTION_PERSISTENCE_CLEANUP_MAX_AGE is not configured correctly: "
								+ maxAgeString,
						nfe);
			}
		}
		bean.setMaxAge(maxAge);
		bean.setLockingSupplier(() -> cluster.locking());
		bean.setCleanupExecutorService(cleanupExecutorService());
		bean.setCortexSessionSupplier(cortexAccess.sessionProvider());
		return bean;
	}

	@Managed
	private ExecutorService cleanupExecutorService() {
		return VirtualThreadExecutorBuilder.newPool().concurrency(3).threadNamePrefix("JobCleanup").description("Job Cleanup").build();
	}
}
