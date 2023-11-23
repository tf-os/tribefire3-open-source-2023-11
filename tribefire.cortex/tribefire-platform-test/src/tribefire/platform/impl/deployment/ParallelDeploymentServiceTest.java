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
package tribefire.platform.impl.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.processing.deployment.api.DeploymentService;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

import tribefire.platform.impl.deployment.wire.contract.ParallelDeploymentServiceContract;

public class ParallelDeploymentServiceTest {

	private List<Deployable> deployables;

	@Before
	public void setup() {

		deployables = createDeployables();

	}

	private List<Deployable> createDeployables() {
		List<Deployable> deployables = new ArrayList<>();

		TestPrefixedIdGenerator generator = TestPrefixedIdGenerator.T.create();
		generator.setExternalId("generator.id");
		generator.setTestPrefix("somePrefix");
		
		
		TestWorker worker = TestWorker.T.create();
		worker.setExternalId("worker.id");
		worker.setGenerator(generator);
		

		deployables.add(worker);
		deployables.add(generator);
		
		return deployables;
	}

	@Test
	public void test() throws Exception {

		WireContext<ParallelDeploymentServiceContract> context = Wire
				.contextWithStandardContractBinding(ParallelDeploymentServiceContract.class).build();

		ParallelDeploymentServiceContract contract = context.contract();
		PersistenceGmSession session = contract.sessionProvider().get();

		TestDeployContext deployContext = new TestDeployContext();
		deployContext.setSession(session);
		deployContext.setDeployables(deployables);

		DeploymentService service = contract.service();

		service.deploy(deployContext);
		
		
		
		BlockingQueue<String> testQueue = contract.testQueue();
		
		
		Assertions.assertThat(testQueue.take()).isNotEqualTo("test-failed");
		Assertions.assertThat(testQueue.take()).isNotEqualTo("test-failed");
		Assertions.assertThat(testQueue.take()).isNotEqualTo("test-failed");

		service.undeploy(deployContext);
		
	}

//	private Future<Boolean> waitForAndCheckDeployables(ConfigurableDeploymentServiceContract contract, List<Deployable> deployables) {
//
//		Consumer<String> inDeploymentBlocker = contract.inDeploymentBlocker();
//
//		return Executors.newSingleThreadExecutor().submit(() -> {
//			deployables.forEach(d -> inDeploymentBlocker.accept(d.getExternalId()));
//			DeployRegistry deployRegistry = contract.deployRegistry();
//			
//			for (Deployable d : deployables) {
//				DeployedUnit unit = deployRegistry.resolve(d);
//				
//				TestPrefixedIdGeneratorExpert idGenerator = unit.findComponent(TestPrefixedIdGenerator.T);
////				System.out.println(idGenerator.get());
//
//			}
//
//			return true;
//		});
//
//	}

}
