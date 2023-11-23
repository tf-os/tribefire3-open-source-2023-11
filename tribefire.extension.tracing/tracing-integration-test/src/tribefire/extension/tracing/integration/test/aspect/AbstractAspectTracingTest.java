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
package tribefire.extension.tracing.integration.test.aspect;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import com.braintribe.model.cortexapi.model.NotifyModelChanged;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deploymentapi.request.Deploy;
import com.braintribe.model.deploymentapi.request.Redeploy;
import com.braintribe.model.deploymentapi.request.Undeploy;
import com.braintribe.model.extensiondeployment.meta.AroundProcessWith;
import com.braintribe.model.extensiondeployment.meta.ProcessWith;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.reflection.EntityVisitor;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.query.fluent.EntityQueryBuilder;
import com.braintribe.model.time.TimeSpan;
import com.braintribe.model.time.TimeUnit;

import tribefire.extension.tracing.integration.test.AbstractTracingTest;
import tribefire.extension.tracing.model.deployment.connector.TracingConnector;
import tribefire.extension.tracing.model.deployment.service.TracingAspect;
import tribefire.extension.tracing.model.deployment.service.TracingProcessor;
import tribefire.extension.tracing.model.deployment.service.demo.DemoTracingProcessor;
import tribefire.extension.tracing.model.service.TracingRequest;
import tribefire.extension.tracing.model.service.configuration.DisableTracing;
import tribefire.extension.tracing.model.service.configuration.DisableTracingResult;
import tribefire.extension.tracing.model.service.configuration.EnableTracing;
import tribefire.extension.tracing.model.service.configuration.EnableTracingResult;
import tribefire.extension.tracing.model.service.configuration.InitializeTracing;
import tribefire.extension.tracing.model.service.configuration.InitializeTracingResult;
import tribefire.extension.tracing.model.service.configuration.local.DisableTracingLocal;
import tribefire.extension.tracing.model.service.configuration.local.DisableTracingLocalResult;
import tribefire.extension.tracing.model.service.configuration.local.EnableTracingLocal;
import tribefire.extension.tracing.model.service.configuration.local.EnableTracingLocalResult;
import tribefire.extension.tracing.model.service.configuration.local.InitializeTracingLocal;
import tribefire.extension.tracing.model.service.configuration.local.InitializeTracingLocalResult;
import tribefire.extension.tracing.model.service.demo.DemoTracing;
import tribefire.extension.tracing.model.service.demo.DemoTracingRequest;
import tribefire.extension.tracing.model.service.demo.DemoTracingResult;
import tribefire.extension.tracing.model.service.status.TracingStatus;
import tribefire.extension.tracing.model.service.status.TracingStatusResult;
import tribefire.extension.tracing.model.service.status.local.InMemoryTracingStatusResult;
import tribefire.extension.tracing.model.service.status.local.TracingStatusLocal;

public abstract class AbstractAspectTracingTest extends AbstractTracingTest {

	protected TracingConnector connector;
	private TracingProcessor processor;
	private DemoTracingProcessor demoProcessor;
	private TracingAspect aspect;
	private ProcessWith processWithProcessor;
	private ProcessWith processWithConfigurationProcessor;
	private AroundProcessWith aroundProcessWithAspect;
	private static final String TEST_PROCESS_WITH_PROCESSOR_GLOBAL_ID = "process-with-processor";
	private static final String TEST_PROCESS_WITH_CONFIGURATION_PROCESSOR_GLOBAL_ID = "process-with-configuration-processor";
	private static final String TEST_AROUND_PROCESS_WITH_ASPECT_GLOBAL_ID = "around-process-with-aspect";

	private static final String TEST_CORTEX_SERVICE_MODEL = "model:tribefire.cortex:tribefire-cortex-service-model";
	private static final String TEST_TRACING_SERVICE_MODEL = "model:tribefire.extension.tracing:tracing-service-model";
	protected static final String TEST_CONNECTOR_EXTERNAL_ID = "test.inmemory.tracing.connector";
	private static final String TEST_PROCESSOR_EXTERNAL_ID = "test.inmemory.tracing.processor";
	private static final String TEST_DEMO_PROCESSOR_EXTERNAL_ID = "test.inmemory.tracing.demo.processor";
	private static final String TEST_TRACING_ASPECT_ID = "test.inmemory.tracing.aspect";

	private static final Set<String> DEPLOYABLES_EXTERNALIDS = asSet(TEST_CONNECTOR_EXTERNAL_ID, TEST_PROCESSOR_EXTERNAL_ID,
			TEST_DEMO_PROCESSOR_EXTERNAL_ID, TEST_TRACING_ASPECT_ID);

	// -----------------------------------------------------------------------
	// SETUP
	// -----------------------------------------------------------------------

	@Override
	@Before
	public void before() throws Exception {
		super.before();

		connector = null;
		processor = null;
		demoProcessor = null;
		aspect = null;

		processWithProcessor = null;
		processWithConfigurationProcessor = null;
		aroundProcessWithAspect = null;

		cleanup();
		setup();

		initializeTracing();
	}

	@Override
	@After
	public void after() throws Exception {
		super.after();

		cleanup();
	}

	// -----------------------------------------------------------------------
	// ABSTRACT
	// -----------------------------------------------------------------------

	protected abstract TracingConnector connector();

	// TODO: error cases
	// TODO: parallel execution
	// TODO: configuration
	// TODO: start/stop tracing
	// TODO: real jaeger tracing

	// -----------------------------------------------------------------------
	// HELPERS LOCAL
	// -----------------------------------------------------------------------

	protected DemoTracingResult demoTracing() {
		DemoTracing request = DemoTracing.T.create();
		DemoTracingResult result = request.eval(cortexSession).get();
		return result;
	}

	protected void demoTracingError() {
		try {
			DemoTracing request = DemoTracing.T.create();
			request.setThrowException(true);
			request.eval(cortexSession).get();
		} catch (Exception e) {
			// ignore
		}
	}

	protected InitializeTracingLocalResult initializeTracing() {
		InitializeTracingLocal request = InitializeTracingLocal.T.create();
		InitializeTracingLocalResult result = request.eval(cortexSession).get();
		return result;
	}

	protected InMemoryTracingStatusResult tracingStatus() {
		TracingStatusLocal request = TracingStatusLocal.T.create();
		InMemoryTracingStatusResult result = (InMemoryTracingStatusResult) request.eval(cortexSession).get();
		return result;
	}

	protected EnableTracingLocalResult enableTracing() {
		return enableTracing(null);
	}

	protected EnableTracingLocalResult enableTracing(TimeSpan enableDuration) {
		EnableTracingLocal request = EnableTracingLocal.T.create();
		request.setEnableDuration(enableDuration);
		EnableTracingLocalResult result = request.eval(cortexSession).get();
		return result;
	}

	protected DisableTracingLocalResult disableTracing() {
		DisableTracingLocal request = DisableTracingLocal.T.create();
		DisableTracingLocalResult result = request.eval(cortexSession).get();
		return result;
	}

	protected void setup() {
		long start = System.currentTimeMillis();

		tracingAspect();
		demoTracingProcessor();
		cortexSession.commit();

		GmMetaModel serviceModel = queryModel(TEST_TRACING_SERVICE_MODEL);
		GmMetaModel cortexServiceModel = queryModel(TEST_CORTEX_SERVICE_MODEL);

		BasicModelMetaDataEditor serviceModelEditor = new BasicModelMetaDataEditor(serviceModel);

		serviceModelEditor.onEntityType(TracingRequest.T).addMetaData(processWithTracingRequest());
		serviceModelEditor.onEntityType(DemoTracingRequest.T).addMetaData(processWithDemoTracingRequest());
		serviceModelEditor.onEntityType(DemoTracingRequest.T).addMetaData(aroundProcessWithTracingAspect());

		cortexSession.commit();

		notifyModelChanged(serviceModel);
		notifyModelChanged(cortexServiceModel);

		Deploy deploy = Deploy.T.create();
		deploy.setExternalIds(DEPLOYABLES_EXTERNALIDS);
		deploy.eval(cortexSession).get();

		logger.info("Setup took: '" + (System.currentTimeMillis() - start) + "'ms");
	}

	protected void cleanup() {
		long start = System.currentTimeMillis();
		Undeploy undeploy = Undeploy.T.create();
		undeploy.setExternalIds(DEPLOYABLES_EXTERNALIDS);
		undeploy.eval(cortexSession).get();

		queryAndDelete(TEST_PROCESS_WITH_PROCESSOR_GLOBAL_ID);
		queryAndDelete(TEST_PROCESS_WITH_CONFIGURATION_PROCESSOR_GLOBAL_ID);
		queryAndDelete(TEST_AROUND_PROCESS_WITH_ASPECT_GLOBAL_ID);

		queryAnDeleteDeployable(TEST_CONNECTOR_EXTERNAL_ID);
		queryAnDeleteDeployable(TEST_PROCESSOR_EXTERNAL_ID);
		queryAnDeleteDeployable(TEST_DEMO_PROCESSOR_EXTERNAL_ID);
		queryAnDeleteDeployable(TEST_TRACING_ASPECT_ID);

		cortexSession.commit();

		logger.info("Cleanup took: '" + (System.currentTimeMillis() - start) + "'ms");
	}

	// -----------------------------------------------------------------------
	// HELPERS MULTICAST
	// -----------------------------------------------------------------------

	protected InitializeTracingResult initializeTracingMulticast() {
		InitializeTracing request = InitializeTracing.T.create();
		InitializeTracingResult result = request.eval(cortexSession).get();
		return result;
	}

	protected TracingStatusResult tracingStatusMulticast() {
		TracingStatus request = TracingStatus.T.create();
		TracingStatusResult result = request.eval(cortexSession).get();
		return result;
	}

	protected EnableTracingResult enableTracingMulticast() {
		return enableTracingMulticast(null);
	}

	protected EnableTracingResult enableTracingMulticast(TimeSpan enableDuration) {
		EnableTracing request = EnableTracing.T.create();
		request.setEnableDuration(enableDuration);
		EnableTracingResult result = request.eval(cortexSession).get();
		return result;
	}

	protected DisableTracingResult disableTracingMulticast() {
		DisableTracing request = DisableTracing.T.create();
		DisableTracingResult result = request.eval(cortexSession).get();
		return result;
	}

	// -----------------------------------------------------------------------
	// PRIVATE
	// -----------------------------------------------------------------------

	protected TimeSpan timeSpan(long durationInMilliseconds) {
		TimeSpan timeSpan = TimeSpan.T.create();
		timeSpan.setUnit(TimeUnit.milliSecond);
		timeSpan.setValue(durationInMilliseconds);
		return timeSpan;
	}

	// -----------------------------------------------------------------------
	// PRIVATE HELPERS
	// -----------------------------------------------------------------------

	private TracingProcessor tracingProcessor() {
		if (processor == null) {
			processor = cortexSession.create(TracingProcessor.T);
			processor.setName(TEST_PROCESSOR_EXTERNAL_ID);
			processor.setExternalId(TEST_PROCESSOR_EXTERNAL_ID);

			processor.setTracingConnector(connector());
		}
		return processor;
	}

	private DemoTracingProcessor demoTracingProcessor() {
		if (demoProcessor == null) {
			demoProcessor = cortexSession.create(DemoTracingProcessor.T);
			demoProcessor.setName(TEST_DEMO_PROCESSOR_EXTERNAL_ID);
			demoProcessor.setExternalId(TEST_DEMO_PROCESSOR_EXTERNAL_ID);
		}
		return demoProcessor;
	}

	private TracingAspect tracingAspect() {
		if (aspect == null) {
			aspect = cortexSession.create(TracingAspect.T);
			aspect.setName(TEST_TRACING_ASPECT_ID);
			aspect.setExternalId(TEST_TRACING_ASPECT_ID);

			aspect.setTracingConnector(connector());
		}

		return aspect;
	}

	private ProcessWith processWithTracingRequest() {
		if (processWithProcessor == null) {
			processWithProcessor = cortexSession.create(ProcessWith.T);
			processWithProcessor.setGlobalId(TEST_PROCESS_WITH_PROCESSOR_GLOBAL_ID);
			TracingProcessor tracingProcessor = tracingProcessor();
			processWithProcessor.setProcessor(tracingProcessor);
		}
		return processWithProcessor;
	}

	private ProcessWith processWithDemoTracingRequest() {
		if (processWithConfigurationProcessor == null) {
			processWithConfigurationProcessor = cortexSession.create(ProcessWith.T);
			processWithConfigurationProcessor.setGlobalId(TEST_PROCESS_WITH_CONFIGURATION_PROCESSOR_GLOBAL_ID);
			DemoTracingProcessor tracingConfigurationProcessor = demoTracingProcessor();
			processWithConfigurationProcessor.setProcessor(tracingConfigurationProcessor);
		}
		return processWithConfigurationProcessor;
	}

	private AroundProcessWith aroundProcessWithTracingAspect() {
		if (aroundProcessWithAspect == null) {
			aroundProcessWithAspect = cortexSession.create(AroundProcessWith.T);
			aroundProcessWithAspect.setGlobalId(TEST_AROUND_PROCESS_WITH_ASPECT_GLOBAL_ID);
			TracingAspect tracingAspect = tracingAspect();
			aroundProcessWithAspect.setProcessor(tracingAspect);
		}
		return aroundProcessWithAspect;
	}

	private void queryAnDeleteDeployable(String externalId) {
		Deployable deployable = null;
		try {
			deployable = cortexSession.query()
					.abstractQuery(EntityQueryBuilder.from(Deployable.T).where().property(Deployable.externalId).eq(externalId).done()).first();
		} catch (Exception e) {
			// ignore
		}
		if (deployable != null) {
			cortexSession.deleteEntity(deployable);
		}
	}

	private void queryAndDelete(String globalId) {
		GenericEntity ge = null;
		try {
			ge = cortexSession.query()
					.abstractQuery(EntityQueryBuilder.from(GenericEntity.T).where().property(GenericEntity.globalId).eq(globalId).done()).first();
		} catch (Exception e) {
			// ignore
		}
		if (ge != null) {
			cortexSession.deleteEntity(ge);
		}
	}

	private GmMetaModel queryModel(String globalId) {
		//@formatter:off
		GmMetaModel serviceModel = cortexSession.query().abstractQuery(EntityQueryBuilder.from(GmMetaModel.T)
				.where().property(GmMetaModel.globalId).eq(globalId)
				.tc().pattern()
					.typeCondition(TypeConditions.isAssignableTo(MetaData.T))
						.conjunction()
							.property()
							.typeCondition(TypeConditions.not(TypeConditions.isKind(TypeKind.scalarType)))
						.close()
					.close()
				.done())
				.first();
		//@formatter:on
		return serviceModel;
	}

	private void notifyModelChanged(GmMetaModel model) {
		NotifyModelChanged modelChangedRequest = NotifyModelChanged.T.create();
		modelChangedRequest.setModel(model);
		modelChangedRequest.eval(cortexSession).get();
	}

	protected <T extends Deployable> void adaptDeployable(T skeleton, String externalId, Set<String> propertySet) {
		T deployable = cortexSession.query()
				.abstractQuery(EntityQueryBuilder.from(skeleton.entityType()).where().property(Deployable.externalId).eq(externalId).done()).first();

		skeleton.entityType().traverse(skeleton, null, new EntityVisitor() {

			@Override
			protected void visitEntity(GenericEntity entity, EntityCriterion criterion, TraversingContext traversingContext) {
				List<Property> properties = skeleton.entityType().getProperties();
				for (Property property : properties) {
					if (!propertySet.contains(property.getName())) {
						continue;
					}

					Object object = property.get(entity);
					deployable.entityType().getProperty(property.getName()).set(deployable, object);
				}
			}
		});
		cortexSession.commit();

		Redeploy deploy = Redeploy.T.create();
		// deploy.setExternalIds(propertySet);
		deploy.setExternalIds(DEPLOYABLES_EXTERNALIDS);
		deploy.eval(cortexSession).get();
	}
}
