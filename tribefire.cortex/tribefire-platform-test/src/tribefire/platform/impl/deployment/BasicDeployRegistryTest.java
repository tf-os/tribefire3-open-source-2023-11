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

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedUnit;

public class BasicDeployRegistryTest {

	private BasicDeployRegistry deployRegistry;

	@Before
	public void setupRegistry() {
		deployRegistry = new BasicDeployRegistry();

		deployRegistry.register(createTestDeployable("first"), createTestDeployedUnit());
		deployRegistry.register(createTestDeployable("second"), createTestDeployedUnit());
		deployRegistry.register(createTestDeployable("third"), createTestDeployedUnit());
		deployRegistry.register(createTestDeployable("fourth"), createTestDeployedUnit());
	}

	@Test
	public void testResolve() {

		DeployRegistry registry = getTestDeployRegistry();

		Deployable deployable = createTestDeployable("first");
		DeployedUnit deployedUnit = registry.resolve(deployable);
		Assert.assertNotNull("Unexpected null deployed unit returned for [" + deployable.getExternalId() + "]", deployedUnit);

		deployable = createTestDeployable("second");
		deployedUnit = registry.resolve(deployable);
		Assert.assertNotNull("Unexpected null deployed unit returned for [" + deployable.getExternalId() + "]", deployedUnit);

		deployable = createTestDeployable("third");
		deployedUnit = registry.resolve(deployable);
		Assert.assertNotNull("Unexpected null deployed unit returned for [" + deployable.getExternalId() + "]", deployedUnit);

		deployable = createTestDeployable("fourth");
		deployedUnit = registry.resolve(deployable);
		Assert.assertNotNull("Unexpected null deployed unit returned for [" + deployable.getExternalId() + "]", deployedUnit);

		deployable = createTestDeployable("fifth");
		deployedUnit = registry.resolve(deployable);
		Assert.assertNull("Unexpected not-null deployed unit returned for [" + deployable.getExternalId() + "]", deployedUnit);

	}

	@Test
	public void testRegister() {

		BasicDeployRegistry registry = getTestDeployRegistry();

		Deployable deployable = createTestDeployable("fifth");
		registry.register(deployable, createTestDeployedUnit());

		DeployedUnit deployedUnit = registry.resolve(deployable);
		Assert.assertNotNull("Unexpected null deployed unit returned for just registered [" + deployable.getExternalId() + "]", deployedUnit);

	}

	@Test
	public void testUnregister() {

		BasicDeployRegistry registry = getTestDeployRegistry();

		Deployable deployable = createTestDeployable("second");
		DeployedUnit deployedUnit = registry.unregister(deployable);
		Assert.assertNotNull("Unexpected null deployed unit returned after unregistering [" + deployable.getExternalId() + "]", deployedUnit);

		Assert.assertNull("Unexpected not-null deployed unit resolved after unregistering [" + deployable.getExternalId() + "]", registry.resolve(deployable));

		// Unregistering a never-registered deployable should throw no exception and return null
		deployable = createTestDeployable("fifth");
		deployedUnit = registry.unregister(deployable);
		Assert.assertNull("Unexpected not-null deployed unit returned for never registered [" + deployable.getExternalId() + "]", deployedUnit);

	}

	@Test
	public void testListDeployables() {

		BasicDeployRegistry registry = getTestDeployRegistry();

		testListDeployables(registry, "first", "second", "third", "fourth");

		Deployable deployable = createTestDeployable("fifth");
		registry.register(deployable, createTestDeployedUnit());

		testListDeployables(registry, "first", "second", "third", "fourth", "fifth");

		registry.unregister(createTestDeployable("second"));

		testListDeployables(registry, "first", "third", "fourth", "fifth");

		registry.unregister(createTestDeployable("first"));

		testListDeployables(registry, "third", "fourth", "fifth");

	}

	protected void testListDeployables(DeployRegistry registry, String... expectedExternalIds) {

		List<Deployable> deployables = registry.getDeployables();

		Assert.assertEquals("Unexpected quantity of deployables", expectedExternalIds.length, deployables.size());

		// Asserts insertion order is kept
		for (int i = 0; i < deployables.size(); i++) {
			String expected = expectedExternalIds[i];
			String actual = deployables.get(i).getExternalId();
			Assert.assertEquals("Unexpected deployable [" + actual + "] not found on index ["+i+"] of DeployRegistry.getDeployedDeployables() returned collection", expected, actual);
			
		}

	}

	public BasicDeployRegistry getTestDeployRegistry() {
		return deployRegistry;
	}

	protected Deployable createTestDeployable(String externalId) {
		TestDeployable testDeployable = TestDeployable.T.create();
		testDeployable.setExternalId(externalId);
		return testDeployable;
	}

	protected DeployedUnit createTestDeployedUnit() {
		return new ConfigurableDeployedUnit();
	}

	
	public interface TestDeployable extends Deployable {
		final EntityType<TestDeployable> T = EntityTypes.T(TestDeployable.class);
	}

}
