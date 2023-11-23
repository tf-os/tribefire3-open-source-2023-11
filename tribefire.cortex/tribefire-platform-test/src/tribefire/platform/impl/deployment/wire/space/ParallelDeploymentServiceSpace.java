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
package tribefire.platform.impl.deployment.wire.space;

import static com.braintribe.utils.lcd.CollectionTools2.asList;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cartridge.common.processing.deployment.DeploymentScope;
import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.Model;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.deployment.api.DeploymentService;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.managed.StaticAccessModelAccessory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.tfconstants.TribefireConstants;
import com.braintribe.model.service.api.InstanceId;
import com.braintribe.model.util.meta.NewMetaModelGeneration;
import com.braintribe.thread.api.EmptyThreadContextScoping;
import com.braintribe.wire.api.annotation.Import;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.annotation.Scope;

import tribefire.platform.impl.deployment.BasicDeployRegistry;
import tribefire.platform.impl.deployment.ComponentInterfaceBindingsRegistry;
import tribefire.platform.impl.deployment.DenotationBindingsRegistry;
import tribefire.platform.impl.deployment.ParallelDeploymentService;
import tribefire.platform.impl.deployment.TestPrefixedIdGenerator;
import tribefire.platform.impl.deployment.TestPrefixedIdGeneratorExpert;
import tribefire.platform.impl.deployment.TestWorker;
import tribefire.platform.impl.deployment.TestWorkerExpert;
import tribefire.platform.impl.deployment.WireDeploymentScoping;
import tribefire.platform.impl.deployment.proxy.ProxyingDeployedComponentResolver;
import tribefire.platform.impl.deployment.wire.contract.ParallelDeploymentServiceContract;

/**
 * @author christina.wilpernig
 */
@Managed
public class ParallelDeploymentServiceSpace implements ParallelDeploymentServiceContract {

	@Import
	private DeploymentScope deploymentScope;

	@Managed
	public InstanceId processingInstanceId() {
		InstanceId $ = InstanceId.T.create();

		$.setApplicationId(TribefireConstants.TRIBEFIRE_SERVICES_APPLICATION_ID);
		$.setNodeId("testNode");

		return $;
	}

	@Override
	public DeploymentService service() {
		return serviceNew();
	}

	@Managed
	public ParallelDeploymentService serviceNew() {
		ParallelDeploymentService $ = new ParallelDeploymentService();

		$.setDeploymentScoping(scoping());
		$.setDeployRegistry(deployRegistry());
		$.setDenotationTypeBindings(denotationTypeBindingsConfig());
		$.setDeployedComponentResolver(proxyingDeployedComponentResolver());

		// $.setStandardParallelDeployments(1);

		$.setThreadContextScoping(EmptyThreadContextScoping.INSTANCE);

		return $;
	}

	@Managed
	private StaticAccessModelAccessory modelAccessory() {
		StaticAccessModelAccessory $ = new StaticAccessModelAccessory(model(), "test");

		return $;
	}

	@Managed(Scope.prototype)
	private PersistenceGmSession session() {
		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setAccessId("testedAccess");
		smood.setMetaModel(model());

		BasicPersistenceGmSession session = new BasicPersistenceGmSession();
		session.setIncrementalAccess(smood);
		session.setModelAccessory(modelAccessory());

		return session;
	}

	@Managed
	private GmMetaModel model() {
		Model deploymentModel = Deployable.T.getModel();

		NewMetaModelGeneration mmg = new NewMetaModelGeneration(asList(GenericEntity.T.getModel(), deploymentModel));

		GmMetaModel model = mmg.buildMetaModel( //
				"tribefire.cortex:test-deployment-model", //
				asList(TestPrefixedIdGenerator.T, TestWorker.T), //
				asList(deploymentModel.getMetaModel()));

		return model;
	}

	@Managed
	public WireDeploymentScoping scoping() {
		WireDeploymentScoping $ = new WireDeploymentScoping();

		$.setScope(deploymentScope);

		return $;
	}

	@Override
	@Managed
	public BasicDeployRegistry deployRegistry() {
		BasicDeployRegistry $ = new BasicDeployRegistry();

		return $;
	}

	@Managed
	public DenotationBindingsRegistry denotationTypeBindingsConfig() {
		DenotationBindingsRegistry $ = new DenotationBindingsRegistry();
		$.setInterfaceBindings(new ComponentInterfaceBindingsRegistry());
		$.bind(TestPrefixedIdGenerator.T).component(Supplier.class).expertFactory(this::idGenerator);
		$.bind(TestWorker.T).component(Callable.class).expertFactory(this::worker);

		return $;

	}

	@Managed
	private Supplier<String> idGenerator(ExpertContext<TestPrefixedIdGenerator> context) {

		TestPrefixedIdGeneratorExpert $ = new TestPrefixedIdGeneratorExpert();
		$.setPrefix(context.getDeployable().getTestPrefix());

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// noop
		}

		return $;
	}

	@Managed
	private Callable<Void> worker(ExpertContext<TestWorker> context) {

		TestWorkerExpert $ = new TestWorkerExpert();
		$.setGenerator(context.resolve(context.getDeployable().getGenerator(), TestPrefixedIdGenerator.T));
		$.setTestQueue(testQueue());

		return $;
	}

	@Managed
	public ProxyingDeployedComponentResolver proxyingDeployedComponentResolver() {
		ProxyingDeployedComponentResolver bean = new ProxyingDeployedComponentResolver();
		bean.setDeployRegistry(deployRegistry());
		bean.setProcessingInstanceId(processingInstanceId());
		bean.setComponentInterfaceBindings(denotationTypeBindingsConfig());

		bean.setInDeploymentBlocker(serviceNew()::waitForDeployableIfInDeployment);

		return bean;
	}

	@Override
	public Supplier<PersistenceGmSession> sessionProvider() {
		return this::session;
	}

	@Override
	@Managed
	public BlockingQueue<String> testQueue() {
		return new LinkedBlockingQueue<>();
	}

	@Override
	public Consumer<String> inDeploymentBlocker() {
		return serviceNew()::waitForDeployableIfInDeployment;
	}

}
