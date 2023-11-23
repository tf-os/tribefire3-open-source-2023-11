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
package tribefire.platform.wire.space.messaging.accesses;

import com.braintribe.gm.marshaller.threshold.ThresholdPersistenceCleanupWorker;
import com.braintribe.model.extensiondeployment.HardwiredWorker;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.context.WireContextConfiguration;
import com.braintribe.wire.api.space.WireSpace;

import tribefire.platform.wire.contract.MessagingRuntimePropertiesContract;
import tribefire.platform.wire.space.cortex.GmSessionsSpace;
import tribefire.platform.wire.space.cortex.services.WorkerSpace;

@Managed
public class TransientMessagingDataSpace implements WireSpace {
	
	@Import
	private WorkerSpace worker;
	
	@Import
	private GmSessionsSpace gmSession;
	
	@Import
	private MessagingRuntimePropertiesContract messagingRuntimeProperties;
	
	@Override
	public void onLoaded(WireContextConfiguration configuration) {
		worker.manager().deploy(cleanupWorker());
	}
	
	@Managed
	private ThresholdPersistenceCleanupWorker cleanupWorker() {
		ThresholdPersistenceCleanupWorker bean = new ThresholdPersistenceCleanupWorker();
		bean.setWorkerIdentification(workerIdentification());
		bean.setCleanupInterval(messagingRuntimeProperties.TRIBEFIRE_MESSAGING_TRANSIENT_PERSISTENCE_CLEANUP_INTERVAL());
		bean.setResourceTtl(messagingRuntimeProperties.TRIBEFIRE_MESSAGING_TRANSIENT_PERSISTENCE_TTL());
		bean.setSessionFactory(gmSession.systemSessionSupplier(TribefireConstants.ACCESS_TRANSIENT_MESSAGING_DATA));
		return bean;
	}
	
	private HardwiredWorker workerIdentification() {
		HardwiredWorker bean = HardwiredWorker.T.create();
		String id = "hardwired:" + ThresholdPersistenceCleanupWorker.class.getSimpleName();
		bean.setId(id);
		bean.setGlobalId(id);
		return bean;
	}
}
