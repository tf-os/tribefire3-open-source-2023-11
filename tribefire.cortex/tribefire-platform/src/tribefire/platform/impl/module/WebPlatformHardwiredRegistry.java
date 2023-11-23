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
package tribefire.platform.impl.module;

import static com.braintribe.model.processing.cortex.CortexModelNames.TF_CORTEX_SERVICE_MODEL_NAME;
import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.Set;
import java.util.function.Supplier;

import javax.servlet.http.HttpServlet;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.collaboration.distributed.api.DcsaSharedStorage;
import com.braintribe.model.cortexapi.CortexRequest;
import com.braintribe.model.extensiondeployment.HardwiredBinaryPersistence;
import com.braintribe.model.extensiondeployment.HardwiredBinaryProcessor;
import com.braintribe.model.extensiondeployment.HardwiredBinaryRetrieval;
import com.braintribe.model.extensiondeployment.HardwiredResourceEnricher;
import com.braintribe.model.extensiondeployment.HardwiredServiceProcessor;
import com.braintribe.model.extensiondeployment.HardwiredStateChangeProcessor;
import com.braintribe.model.extensiondeployment.HardwiredWebTerminal;
import com.braintribe.model.extensiondeployment.HardwiredWorker;
import com.braintribe.model.extensiondeployment.check.CheckBundle;
import com.braintribe.model.extensiondeployment.check.HardwiredCheckProcessor;
import com.braintribe.model.extensiondeployment.check.HardwiredParameterizedCheckProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.marshallerdeployment.HardwiredMarshaller;
import com.braintribe.model.processing.check.api.CheckProcessor;
import com.braintribe.model.processing.check.api.ParameterizedCheckProcessor;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.session.api.collaboration.PersistenceInitializationContext;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.sp.api.StateChangeProcessor;
import com.braintribe.model.processing.worker.api.Worker;
import com.braintribe.model.resourceapi.enrichment.EnrichResource;
import com.braintribe.model.resourceapi.enrichment.EnrichResourceResponse;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceRequest;
import com.braintribe.model.resourceapi.persistence.BinaryPersistenceResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalRequest;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;

import tribefire.cortex.model.check.CheckCoverage;
import tribefire.cortex.module.loading.PlatformHardwiredDeployablesRegistry;
import tribefire.module.api.WebRegistryConfiguration;
import tribefire.module.wire.contract.WebPlatformHardwiredDeployablesContract;
import tribefire.platform.api.binding.ComponentBinders;

/**
 * @author peter.gazdik
 */
public class WebPlatformHardwiredRegistry extends PlatformHardwiredDeployablesRegistry implements WebPlatformHardwiredDeployablesContract {

	private static final Logger log = Logger.getLogger(WebPlatformHardwiredRegistry.class);

	// ###########################################################
	// ## . . . . . . . . . . Set by Creator. . . . . . . . . . ##
	// ###########################################################

	public WebRegistryConfiguration webRegistry;
	public ComponentBinders components;
	public Supplier<DcsaSharedStorage> sharedStorageSupplier;

	@Override
	protected ComponentBinder<com.braintribe.model.extensiondeployment.ServiceProcessor, ServiceProcessor<?, ?>> serviceProcessorBidner() {
		return components.serviceProcessor();
	}

	// ###########################################################
	// ## . . . . . . . . . Hardwired binding . . . . . . . . . ##
	// ###########################################################

	@Override
	public Supplier<DcsaSharedStorage> sharedStorageSupplier() {
		return sharedStorageSupplier;
	}

	@Override
	public WebRegistryConfiguration webRegistry() {
		return webRegistry;
	}

	@Override
	public <T extends CortexRequest> HardwiredServiceProcessor bindCortexServiceProcessor(String externalId, String name, EntityType<T> requestType,
			Supplier<ServiceProcessor<? super T, ?>> serviceProcessorSupplier) {

		return spForKnownDomain(externalId, name, "cortex", Model.modelGlobalId(TF_CORTEX_SERVICE_MODEL_NAME), requestType, serviceProcessorSupplier);
	}

	@Override
	public HardwiredMarshaller bindMarshaller(String externalId, String name, Supplier<Marshaller> marshallerSupplier, String... mimeTypes) {
		HardwiredMarshaller deployable = newHd(HardwiredMarshaller.T, externalId, name, "marshaller");
		deployable.setMimeTypes(asList(mimeTypes));

		return bindHd(deployable, components.marshaller(), marshallerSupplier);
	}

	@Override
	public HardwiredCheckProcessor bindCheckProcessor(String externalId, String name, Supplier<CheckProcessor> checkProcessorSupplier) {
		HardwiredCheckProcessor deployable = newHd(HardwiredCheckProcessor.T, externalId, name, "check");
		bindCortex(ctx -> createHealthCheck(ctx, deployable));

		return bindHd(deployable, components.checkProcessor(), checkProcessorSupplier);
	}

	private void createHealthCheck(PersistenceInitializationContext context, HardwiredCheckProcessor cp) {
		ManagedGmSession session = context.getSession();

		HardwiredCheckProcessor cortextCheckProcessor = session.findEntityByGlobalId(cp.getGlobalId());
		if (cortextCheckProcessor == null)
			log.error("Cannot find hardwired CheckProcessor with globalId '" + cp.getGlobalId()
					+ "', thus not corresponding HealthCheck instance will be created. This might be a bug related to ModuleLoader.");
		else {
			CheckBundle check = session.create(CheckBundle.T, "HealthCheck:" + cp.getGlobalId());
			check.setName(cortextCheckProcessor.getName());
			check.setCoverage(CheckCoverage.vitality);
			check.setIsPlatformRelevant(true);
			check.getChecks().add(cortextCheckProcessor);
		}
	}

	@Override
	public HardwiredParameterizedCheckProcessor bindParameterizedCheckProcessor(String externalId, String name,
			Supplier<ParameterizedCheckProcessor<?>> pcpSupplier) {

		HardwiredParameterizedCheckProcessor deployable = newHd(HardwiredParameterizedCheckProcessor.T, externalId, name, "check");
		return bindHd(deployable, components.parameterizedCheckProcessor(), pcpSupplier);
	}

	@Override
	public HardwiredBinaryProcessor bindBinaryServiceProcessor( //
			String externalId, String name,
			Supplier<ServiceProcessor<? super BinaryPersistenceRequest, ? super BinaryPersistenceResponse>> bpSupplier,
			Supplier<ServiceProcessor<? super BinaryRetrievalRequest, ? super BinaryRetrievalResponse>> brSupplier) {

		HardwiredBinaryProcessor deployable = newHd(HardwiredBinaryProcessor.T, externalId, name, "binaryServiceProcessor");
		bindInternal(deployable) //
				.component(components.binaryPersistenceProcessor(), bpSupplier) //
				.component(components.binaryRetrievalProcessor(), brSupplier);

		return deployable;
	}

	@Override
	public HardwiredBinaryPersistence bindBinaryPersistenceProcessor(String externalId, String name,
			Supplier<ServiceProcessor<? super BinaryPersistenceRequest, ? super BinaryPersistenceResponse>> binaryPersistenceSupplier) {
		HardwiredBinaryPersistence deployable = newHd(HardwiredBinaryPersistence.T, externalId, name, "binaryPersistenceProcessor");
		return bindHd(deployable, components.binaryPersistenceProcessor(), binaryPersistenceSupplier);
	}

	@Override
	public HardwiredBinaryRetrieval bindBinaryRetrievalProcessor(String externalId, String name,
			Supplier<ServiceProcessor<? super BinaryRetrievalRequest, ? super BinaryRetrievalResponse>> binaryRetrievalSupplier) {
		HardwiredBinaryRetrieval deployable = newHd(HardwiredBinaryRetrieval.T, externalId, name, "binaryRetrievalProcessor");
		return bindHd(deployable, components.binaryRetrievalProcessor(), binaryRetrievalSupplier);
	}

	@Override
	public HardwiredResourceEnricher bindResourceEnricherProcessor(String externalId, String name,
			Supplier<ServiceProcessor<? super EnrichResource, ? super EnrichResourceResponse>> resourceEnricherSupplier) {
		HardwiredResourceEnricher deployable = newHd(HardwiredResourceEnricher.T, externalId, name, "resourceEnricherProcessor");
		return bindHd(deployable, components.resourceEnricherProcessor(), resourceEnricherSupplier);
	}

	@Override
	public HardwiredResourceEnricher bindResourceEnricherForMimeTypes(String externalId, String name, Set<String> mimeTypes, Set<Model> models,
			Supplier<ServiceProcessor<? super EnrichResource, ? super EnrichResourceResponse>> resourceEnricherSupplier) {

		HardwiredResourceEnricher deployable = newHd(HardwiredResourceEnricher.T, externalId, name, "resourceEnricher");
		bindCortex(new DefaultMimeBasedEnrichersInitializer(deployable, mimeTypes, models));

		return bindHd(deployable, components.resourceEnricherProcessor(), resourceEnricherSupplier);
	}

	@Override
	public HardwiredWebTerminal bindWebTerminal(String externalId, String name, String pathIdentifier, Supplier<HttpServlet> servletSupplier) {
		HardwiredWebTerminal deployable = newHd(HardwiredWebTerminal.T, externalId, name, "webTerminal");
		deployable.setPathIdentifier(pathIdentifier);

		return bindHd(deployable, components.webTerminal(), servletSupplier);
	}

	@Override
	public HardwiredWorker bindWorker(String externalId, String name, Supplier<Worker> workerSupplier) {
		HardwiredWorker deployable = newHd(HardwiredWorker.T, externalId, name, "worker");
		return bindHd(deployable, components.worker(), workerSupplier);
	}
	
	@Override
	public HardwiredStateChangeProcessor bindStateChangeProcessor(String externalId, String name, Supplier<StateChangeProcessor<?, ?>> stateChangeProcessorSupplier) {
		HardwiredStateChangeProcessor deployable = newHd(HardwiredStateChangeProcessor.T, externalId, name, "state-change-processor");
		return bindHd(deployable, components.stateChangeProcessor(), stateChangeProcessorSupplier);
	}

}
