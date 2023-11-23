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
package tribefire.platform.wire.space.module;

import javax.servlet.http.HttpServlet;
import javax.sql.DataSource;

import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.dcsadeployment.DcsaSharedStorage;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionInfoProvider;
import com.braintribe.model.deployment.database.pool.DatabaseConnectionPool;
import com.braintribe.model.extensiondeployment.AccessAspect;
import com.braintribe.model.extensiondeployment.BinaryPersistence;
import com.braintribe.model.extensiondeployment.BinaryRetrieval;
import com.braintribe.model.extensiondeployment.ResourceEnricher;
import com.braintribe.model.extensiondeployment.ServiceAroundProcessor;
import com.braintribe.model.extensiondeployment.ServicePostProcessor;
import com.braintribe.model.extensiondeployment.ServicePreProcessor;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.extensiondeployment.StateChangeProcessor;
import com.braintribe.model.extensiondeployment.StateChangeProcessorRule;
import com.braintribe.model.extensiondeployment.WebTerminal;
import com.braintribe.model.extensiondeployment.Worker;
import com.braintribe.model.extensiondeployment.access.AccessRequestProcessor;
import com.braintribe.model.extensiondeployment.check.CheckProcessor;
import com.braintribe.model.extensiondeployment.check.ParameterizedCheckProcessor;
import com.braintribe.model.leadershipdeployment.LeadershipManager;
import com.braintribe.model.lockingdeployment.Locking;
import com.braintribe.model.marshallerdeployment.Marshaller;
import com.braintribe.model.messagingdeployment.Messaging;
import com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.resourceapi.enrichment.EnrichResource;
import com.braintribe.model.resourceapi.enrichment.EnrichResourceResponse;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceRequest;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.transport.messaging.api.MessagingConnectionProvider;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;

import tribefire.cortex.model.deployment.mimetypedetection.MimeTypeDetector;
import tribefire.module.wire.contract.WebPlatformBindersContract;
import tribefire.platform.wire.space.common.BindersSpace;

// This is pointless right now, but will be relevant when we start extracting stuff - this will be moved to a module which will aggregate all the extracted stuff.
/**
 * Defines the basic {@link ComponentBinder}s.
 */
@Managed
public class WebPlatformBindersSpace implements WebPlatformBindersContract {

	@Import
	private BindersSpace binders;

	@Override
	public ComponentBinder<ServiceProcessor, com.braintribe.model.processing.service.api.ServiceProcessor<?, ?>> serviceProcessor() {
		return binders.serviceProcessor();
	}

	@Override
	public ComponentBinder<ServicePreProcessor, com.braintribe.model.processing.service.api.ServicePreProcessor<?>> servicePreProcessor() {
		return binders.servicePreProcessor();
	}

	@Override
	public ComponentBinder<ServiceAroundProcessor, com.braintribe.model.processing.service.api.ServiceAroundProcessor<?, ?>> serviceAroundProcessor() {
		return binders.serviceAroundProcessor();
	}

	@Override
	public ComponentBinder<ServicePostProcessor, com.braintribe.model.processing.service.api.ServicePostProcessor<?>> servicePostProcessor() {
		return binders.servicePostProcessor();
	}

	// Access

	@Override
	public ComponentBinder<IncrementalAccess, com.braintribe.model.access.IncrementalAccess> incrementalAccess() {
		return binders.incrementalAccess();
	}

	@Override
	public ComponentBinder<AccessRequestProcessor, com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor<?, ?>> accessRequestProcessor() {
		return binders.accessRequestProcessor();
	}

	@Override
	public ComponentBinder<AccessAspect, com.braintribe.model.processing.aop.api.aspect.AccessAspect> accessAspect() {
		return binders.accessAspect();
	}

	// Check

	@Override
	public ComponentBinder<CheckProcessor, com.braintribe.model.processing.check.api.CheckProcessor> checkProcessor() {
		return binders.checkProcessor();
	}

	@Override
	public ComponentBinder<ParameterizedCheckProcessor, com.braintribe.model.processing.check.api.ParameterizedCheckProcessor<?>> parameterizedCheckProcessor() {
		return binders.parameterizedCheckProcessor();
	}

	@Override
	public ComponentBinder<ParameterizedCheckProcessor, ParameterizedAccessCheckProcessor<? extends AccessRequest>> parameterizedAccessCheckProcessor() {
		return binders.parameterizedAccessCheckProcessor();
	}

	// Cluster

	@Override
	public ComponentBinder<Messaging, MessagingConnectionProvider<?>> messaging() {
		return binders.messaging();
	}

	@Override
	public ComponentBinder<MimeTypeDetector, com.braintribe.mimetype.MimeTypeDetector> mimeTypeDetector() {
		return binders.mimeTypeDetector();
	}

	@Override
	public ComponentBinder<Locking, com.braintribe.model.processing.lock.api.Locking> locking() {
		return binders.locking();
	}

	@Override
	public ComponentBinder<LeadershipManager, tribefire.cortex.leadership.api.LeadershipManager> leadershipManager() {
		return binders.leadershipManager();
	}

	@Override
	public ComponentBinder<DcsaSharedStorage, com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage> dcsaSharedStorage() {
		return binders.dcsaSharedStorage();
	}

	// Marshalling

	@Override
	public ComponentBinder<Marshaller, com.braintribe.codec.marshaller.api.Marshaller> marshaller() {
		return binders.marshaller();
	}

	// Resource Processing

	@Override
	public ComponentBinder<BinaryPersistence, com.braintribe.model.processing.service.api.ServiceProcessor<? super BinaryPersistenceRequest, ? super BinaryPersistenceResponse>> binaryPersistenceProcessor() {
		return binders.binaryPersistenceProcessor();
	}

	@Override
	public ComponentBinder<BinaryRetrieval, com.braintribe.model.processing.service.api.ServiceProcessor<? super BinaryRetrievalRequest, ? super BinaryRetrievalResponse>> binaryRetrievalProcessor() {
		return binders.binaryRetrievalProcessor();
	}

	@Override
	public ComponentBinder<ResourceEnricher, com.braintribe.model.processing.service.api.ServiceProcessor<? super EnrichResource, ? super EnrichResourceResponse>> resourceEnricherProcessor() {
		return binders.resourceEnricherProcessor();
	}

	// State Processing

	@Override
	public ComponentBinder<StateChangeProcessor, com.braintribe.model.processing.sp.api.StateChangeProcessor<?, ?>> stateChangeProcessor() {
		return binders.stateChangeProcessor();
	}

	@Override
	public ComponentBinder<StateChangeProcessorRule, com.braintribe.model.processing.sp.api.StateChangeProcessorRule> stateChangeProcessorRule() {
		return binders.stateChangeProcessorRule();
	}

	// Web Platform

	@Override
	public ComponentBinder<Worker, com.braintribe.model.processing.worker.api.Worker> worker() {
		return binders.worker();
	}

	@Override
	public ComponentBinder<WebTerminal, HttpServlet> webTerminal() {
		return binders.webTerminal();
	}

	@Override
	public ComponentBinder<DatabaseConnectionPool, DataSource> databaseConnectionPool() {
		return binders.databaseConnectionPool();
	}

	@Override
	public ComponentBinder<DatabaseConnectionInfoProvider, tribefire.module.api.DatabaseConnectionInfoProvider> databaseConnectionInfoProvider() {
		return binders.databaseConnectionInfoProvider();
	}

}
