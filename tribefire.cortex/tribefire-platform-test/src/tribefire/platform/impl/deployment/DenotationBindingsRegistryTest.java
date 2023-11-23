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

import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;
import static com.braintribe.wire.api.util.Sets.set;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessdeployment.HardwiredAccess;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.deployment.Module;
import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.deployment.api.AbstractExpertContext;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.deployment.api.ResolvedComponent;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

import tribefire.platform.impl.deployment.DenotationBindingsRegistry.BindingEntry;

/**
 * {@link DenotationBindingsRegistry} tests.
 */
public class DenotationBindingsRegistryTest {

	private DenotationBindingsRegistry bindings;

	private static final MyServiceProcessorIfaceAB expert = new MyServiceProcessorImpl();
	private static final MyServiceProcessorIfaceA expertA = new MyServiceProcessorImpl();
	private static final MyServiceProcessorIfaceB expertB = new MyServiceProcessorImpl();

	@Before
	public void initialize() {
		bindings = new DenotationBindingsRegistry();
		bindings.setInterfaceBindings(new ComponentInterfaceBindingsRegistry());
	}

	// =================== //
	// ==== Multiplex ==== //
	// =================== //

	@Test
	public void testMultiplexBinding() {

		// @formatter:off
		bindings.bind(MyServiceProcessor.T)
				.component(TestDirectBinderA.instance).expertSupplier(() -> expert)
				.component(TestDirectBinderB.instance).expertSupplier(() -> expert);
		// @formatter:on

		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// @formatter:off
		assertMultiplexBinding(denotation, false, set(MyServiceProcessor.T),
			map(
				entry(TestDirectBinderA.instance, expert), 
				entry(TestDirectBinderB.instance, expert)
			)
		);
		assertComponentInterfaceBindings(
				TestDirectBinderA.instance, 
				TestDirectBinderB.instance
		);
		// @formatter:on

	}

	@Test
	public void testMultiplexInstanceBinding() {

		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// @formatter:off
		
		// entry 1: the expected instance entry
		bindings.bind(MyServiceProcessor.T, denotation.getExternalId())
						.component(TestDirectBinderA.instance).expertSupplier(() -> expert)
						.component(TestDirectBinderB.instance).expertSupplier(() -> expert);
		
		//entry 2: non-instance bound to the same denotation type
		bindings.bind(MyServiceProcessor.T)
			.component(TestDirectBinderA.instance).expertSupplier(() -> { return null; })
			.component(TestDirectBinderB.instance).expertSupplier(() -> { return null; });

		// entry 3: instance bound to the same denotation type but different externalId
		bindings.bind(MyServiceProcessor.T, "falseId")
			.component(TestDirectBinderA.instance).expertSupplier(() -> { return null; })
			.component(TestDirectBinderB.instance).expertSupplier(() -> { return null; });
		
		// @formatter:on

		// @formatter:off
		assertMultiplexBinding(denotation, true, set(MyServiceProcessor.T),
			map(
				entry(TestDirectBinderA.instance, expert), 
				entry(TestDirectBinderB.instance, expert)
			)
		);
		assertComponentInterfaceBindings(
				TestDirectBinderA.instance, 
				TestDirectBinderB.instance
		);
		// @formatter:on

	}
	
	@Test(expected=IllegalStateException.class)
	public void testDuplicatedMultiplexInstanceBinding() {
		// @formatter:off
		//non-instance bound to the same denotation type
		bindings.bind(MyServiceProcessor.T)
			.component(TestIndirectBinder.instance).expertSupplier(() -> { return null; })
			.component(TestIndirectBinder.instance).expertSupplier(() -> { return null; });
		// @formatter:on
	}

	@Test
	public void testMultiplexBindingWithDistinctValues() {
		// @formatter:off
		bindings.bind(MyServiceProcessor.T)
			.component(TestDirectBinderA.instance).expertSupplier(() -> expertA)
			.component(TestDirectBinderB.instance).expertSupplier(() -> expertB);
		// @formatter:on

		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// @formatter:off
		assertMultiplexBinding(denotation, false, set(MyServiceProcessor.T),
			map(
				entry(TestDirectBinderA.instance, expertA), 
				entry(TestDirectBinderB.instance, expertB)
			)
		);
		assertComponentInterfaceBindings(
				TestDirectBinderA.instance, 
				TestDirectBinderB.instance
		);
		// @formatter:on
	}

	@Test
	public void testMultiplexInstanceBindingWithDistinctValues() {
		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// @formatter:off

		// entry 1: the expected instance entry
		bindings.bind(MyServiceProcessor.T, denotation.getExternalId())
			.component(TestDirectBinderA.instance).expertSupplier(() -> expertA)
			.component(TestDirectBinderB.instance).expertSupplier(() -> expertB);

		// entry 2: non-instance bound to the same denotation type
		bindings.bind(MyServiceProcessor.T)
			.component(TestDirectBinderA.instance).expertSupplier(() -> { return null; })
			.component(TestDirectBinderB.instance).expertSupplier(() -> { return null; });

		// entry 3: instance bound to the same denotation type but different externalId
		bindings.bind(MyServiceProcessor.T, "falseId")
			.component(TestDirectBinderA.instance).expertSupplier(() -> { return null; })
			.component(TestDirectBinderB.instance).expertSupplier(() -> { return null; });

		assertMultiplexBinding(denotation, true, set(MyServiceProcessor.T),
			map(
				entry(TestDirectBinderA.instance, expertA), 
				entry(TestDirectBinderB.instance, expertB)
			)
		);
		assertComponentInterfaceBindings(
			TestDirectBinderA.instance, 
			TestDirectBinderB.instance
		);
		// @formatter:on
	}

	@Test
	public void testMultiplexPlainBinding() {
		// @formatter:off
		bindings.bind(MyServiceProcessorA.T)
			.component(MyServiceProcessorA.T, MyServiceProcessorIfaceA.class).expertSupplier(() -> expert)
			.component(MyServiceProcessor.T, MyServiceProcessorIfaceB.class).expertSupplier(() -> expert);
		// @formatter:on

		MyServiceProcessorA denotation = create(MyServiceProcessorA.T);

		// @formatter:off
		assertMultiplexPlainBinding(denotation, false, set(MyServiceProcessorA.T), 
			map(
				entry(MyServiceProcessorA.T, expert),
				entry(MyServiceProcessor.T, expert)
			)
		);
		assertComponentInterfaceBindings(
			map(
				entry(MyServiceProcessorA.T, new Class[] { MyServiceProcessorIfaceA.class }),
				entry(MyServiceProcessor.T, new Class[] { MyServiceProcessorIfaceB.class })
			)
		);
		// @formatter:on
	}

	@Test
	public void testMultiplexPlainBindingWithDistinctValues() {
		// @formatter:off
		bindings.bind(MyServiceProcessorA.T)
			.component(MyServiceProcessorA.T, MyServiceProcessorIfaceA.class).expertSupplier(() -> expertA)
			.component(MyServiceProcessor.T, MyServiceProcessorIfaceB.class).expertSupplier(() -> expertB);
		// @formatter:on

		MyServiceProcessorA denotation = create(MyServiceProcessorA.T);

		// @formatter:off
		assertMultiplexPlainBinding(denotation, false, set(MyServiceProcessorA.T),
			map(
				entry(MyServiceProcessorA.T, expertA), 
				entry(MyServiceProcessor.T, expertB)
			)
		);
		assertComponentInterfaceBindings(
			map(
				entry(MyServiceProcessorA.T, new Class[] { MyServiceProcessorIfaceA.class }),
				entry(MyServiceProcessor.T, new Class[] { MyServiceProcessorIfaceB.class })
			)
		);
		// @formatter:on
	}

	// =================== //
	// ===== Uniplex ===== //
	// =================== //

	@Test
	public void testBindingWithDirectBinder() {
		bindings.bind(MyServiceProcessor.T).component(TestDirectBinder.instance).expertSupplier(() -> expert);

		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		bindings.resolveDeployedUnitSupplier(denotation);

		assertBinding(denotation, false, set(MyServiceProcessor.T), MyServiceProcessor.T, expert);

		assertComponentInterfaceBindings(TestDirectBinder.instance);
	}

	@Test
	public void testBindingWithIndirectBinder() {
		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// entry 1: the expected entry
		bindings.bind(MyServiceProcessor.T).component(TestIndirectBinder.instance).expertSupplier(() -> expert);

		// entry 2: entry bound to different denotation type
		bindings.bind(MyServiceProcessorA.T).component(TestIndirectBinder.instance).expertSupplier(() -> {
			return null;
		});

		// entry 3: entry bound to different denotation type
		bindings.bind(MyServiceProcessorB.T).component(TestIndirectBinder.instance).expertSupplier(() -> {
			return null;
		});

		assertBinding(denotation, false, set(MyServiceProcessor.T, MyServiceProcessorA.T, MyServiceProcessorB.T),
				TestIndirectBinder.instance.componentType(), expert);

		assertComponentInterfaceBindings(TestIndirectBinder.instance);
	}

	@Test
	public void testPlainBinding() {

		bindings.bind(MyServiceProcessor.T).component(MyServiceProcessorIface.class).expertSupplier(() -> expert);

		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		assertBinding(denotation, false, set(MyServiceProcessor.T), MyServiceProcessor.T, expert);

		// @formatter:off
		assertComponentInterfaceBindings(
				map(
					entry(MyServiceProcessor.T, new Class[] { MyServiceProcessorIface.class })
				)
		);
		// @formatter:on

	}

	@Test
	public void testPlainBindingWithDistinctComponent() {
		bindings.bind(MyServiceProcessor.T).component(ServiceProcessor.T, MyServiceProcessorIface.class).expertSupplier(() -> expert);

		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		assertBinding(denotation, false, set(MyServiceProcessor.T), ServiceProcessor.T, expert);

		// @formatter:off
		assertComponentInterfaceBindings(
				map(
					entry(ServiceProcessor.T, new Class[] { MyServiceProcessorIface.class })
				)
		);
		// @formatter:on
	}

	@Test
	public void testInstanceBindingAndDirectBinder() {
		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// entry 1: the expected instance entry
		bindings.bind(MyServiceProcessor.T, denotation.getExternalId()).component(TestDirectBinder.instance).expertSupplier(() -> expert);

		// entry 2: non-instance bound to the same denotation type
		bindings.bind(MyServiceProcessor.T).component(TestDirectBinder.instance).expertSupplier(() -> {
			return null;
		});

		// entry 3: instance bound to the same denotation type but different externalId
		bindings.bind(MyServiceProcessor.T, "falseId").component(TestDirectBinder.instance).expertSupplier(() -> {
			return null;
		});

		assertBinding(denotation, true, set(MyServiceProcessor.T), MyServiceProcessor.T, expert);

		assertComponentInterfaceBindings(TestDirectBinder.instance);
	}

	@Test
	public void testInstanceBindingWithIndirectBinder() {
		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// entry 1: the expected instance entry
		bindings.bind(MyServiceProcessor.T, denotation.getExternalId()).component(TestIndirectBinder.instance).expertSupplier(() -> expert);

		// entry 2: non-instance bound to the same denotation type
		bindings.bind(MyServiceProcessor.T).component(TestDirectBinder.instance).expertSupplier(() -> {
			return null;
		});

		// entry 3: instance bound to the same denotation type but different externalId
		bindings.bind(MyServiceProcessor.T, "falseId").component(TestDirectBinder.instance).expertSupplier(() -> {
			return null;
		});

		assertBinding(denotation, true, set(MyServiceProcessor.T), TestIndirectBinder.instance.componentType(), expert);

		assertComponentInterfaceBindings(TestDirectBinder.instance);
	}

	@Test
	public void testPlainInstanceBindingWithDistinctComponent() {
		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// @formatter:off
		bindings
			.bind(MyServiceProcessor.T, denotation.getExternalId())
				.component(ServiceProcessor.T, MyServiceProcessorIface.class)
				.expertSupplier(() -> expert);
		// @formatter:on

		assertBinding(denotation, true, set(MyServiceProcessor.T), ServiceProcessor.T, expert);

		// @formatter:off
		assertComponentInterfaceBindings(
			map(
				entry(ServiceProcessor.T, new Class[] { MyServiceProcessorIface.class })
			)
		);
		// @formatter:on
	}

	@Test
	public void testPlainInstanceBinding() {
		MyServiceProcessor denotation = create(MyServiceProcessor.T);

		// entry 1: the expected instance entry
		bindings.bind(MyServiceProcessor.T, denotation.getExternalId()).component(MyServiceProcessorIface.class).expertSupplier(() -> expert);

		// entry 2: non-instance bound to the same denotation type
		bindings.bind(MyServiceProcessor.T).component(MyServiceProcessorIface.class).expertSupplier(() -> {
			return null;
		});

		// entry 3: instance bound to the same denotation type but different externalId
		bindings.bind(MyServiceProcessor.T, "falseId").component(MyServiceProcessorIface.class).expertSupplier(() -> {
			return null;
		});

		assertBinding(denotation, true, set(MyServiceProcessor.T), MyServiceProcessor.T, expert);

		// @formatter:off
		assertComponentInterfaceBindings(
				map(
					entry(MyServiceProcessor.T, new Class[] { MyServiceProcessorIface.class })
				)
		);
		// @formatter:on
	}

	// ====================== //
	// ===== Assertions ===== //
	// ====================== //

	private void assertBinding(Deployable denotationInstance, boolean instanceMapping, Set<EntityType<? extends Deployable>> boundTypes,
			EntityType<? extends Deployable> componentType, Object value) {

		DeployedUnit du = baseBindingAssertions(denotationInstance, instanceMapping, boundTypes);

		Object resolvedValue = du.getComponent(componentType);
		Assert.assertNotNull(resolvedValue);

		Assert.assertEquals(value, resolvedValue);
	}

	private void assertMultiplexBinding(Deployable denotationInstance, boolean instanceMapping, Set<EntityType<? extends Deployable>> boundTypes,
			Map<ComponentBinder<?, ?>, Object> bindings) {

		DeployedUnit du = baseBindingAssertions(denotationInstance, instanceMapping, boundTypes);

		for (Entry<ComponentBinder<?, ?>, Object> entry : bindings.entrySet()) {
			// DeployedUnit assertions
			Object resolvedValue = du.getComponent(entry.getKey().componentType());
			Assert.assertNotNull(resolvedValue);
			Assert.assertEquals(entry.getValue(), resolvedValue);
		}
	}

	private void assertMultiplexPlainBinding(Deployable denotationInstance, boolean instanceMapping, Set<EntityType<? extends Deployable>> boundTypes,
			Map<EntityType<? extends Deployable>, Object> bindings) {

		DeployedUnit du = baseBindingAssertions(denotationInstance, instanceMapping, boundTypes);

		for (Entry<EntityType<? extends Deployable>, Object> entry : bindings.entrySet()) {
			// DeployedUnit assertions
			Object resolvedValue = du.getComponent(entry.getKey());
			Assert.assertNotNull(resolvedValue);
			Assert.assertEquals(entry.getValue(), resolvedValue);
		}
	}


	private DeployedUnit baseBindingAssertions(Deployable denotationInstance, boolean instanceMapping,
			Set<EntityType<? extends Deployable>> boundTypes) {

		Assert.assertNotNull(bindings.boundTypes());
		Assert.assertEquals(boundTypes.size(), bindings.boundTypes().size());
		Assert.assertTrue(bindings.boundTypes().containsAll(boundTypes));
		Assert.assertTrue(bindings.boundTypes().contains(denotationInstance.entityType()));

		BindingEntry denotationMapping = instanceMapping?
				bindings.acquire(denotationInstance.entityType(), denotationInstance.getExternalId(), Module.PLATFORM_MODULE_GLOBAL_ID):
				bindings.acquire(denotationInstance.entityType(), null, Module.PLATFORM_MODULE_GLOBAL_ID);
		Assert.assertNotNull(denotationMapping);

		Function<MutableDeploymentContext<?, ?>, DeployedUnit> function = denotationMapping::deploy;

		return function.apply(new TestMutableDeploymentContext(denotationInstance));
	}

	private void assertComponentInterfaceBindings(ComponentBinder<?, ?>... binders) {
		for (ComponentBinder<?, ?> binder : binders)
			assertComponentInterfaceBinding(binder.componentType(), binder.componentInterfaces());

		assertUnboundComponentInterface();
	}

	private void assertComponentInterfaceBindings(Map<EntityType<? extends Deployable>, Class<?>[]> componentTypeToInterfaces) {
		componentTypeToInterfaces.forEach(this::assertComponentInterfaceBinding);

		assertUnboundComponentInterface();
	}

	private void assertComponentInterfaceBinding(EntityType<? extends Deployable> componentType, Class<?>[] expectedComponentInterfaces) {
		Class<?>[] getResult = bindings.getComponentInterfaces(componentType);
		Class<?>[] findResult = bindings.findComponentInterfaces(componentType);

		Assert.assertArrayEquals("Inconsistent getComponentInterfaces() <-> findComponentInterfaces() results", getResult, findResult);
		Assert.assertArrayEquals("getComponentInterfaces() differs from expected interfaces", expectedComponentInterfaces, getResult);
	}

	private void assertUnboundComponentInterface() {

		EntityType<? extends Deployable> unboundComponentType = HardwiredAccess.T;

		Assert.assertNull("This findComponentInterfaces() should have returned null", bindings.findComponentInterfaces(unboundComponentType));
		try {
			bindings.getComponentInterfaces(unboundComponentType);
			Assert.fail("This getComponentInterfaces() should have failed with a DeploymentException");
		} catch (DeploymentException e) {
			// Expected in getComponentInterfaces() call.
		}

	}

	// ============================== //
	// ==== Denotation instances ==== //
	// ============================== //

	public <D extends Deployable> D create(EntityType<D> type) {
		return create(type, null);
	}

	public <D extends Deployable> D create(EntityType<D> type, String externalId) {
		D instance = type.create();
		if (externalId == null) {
			instance.setExternalId("random." + type.getShortName() + "." + System.currentTimeMillis());
		} else {
			instance.setExternalId(externalId);
		}
		return instance;
	}

	// ================= //
	// ==== Experts ==== //
	// ================= //

	interface MyServiceProcessorIface {
		void bindWith(ComponentBinder<?, ?> binder);
		List<ComponentBinder<?, ?>> boundBy();
	}

	interface MyServiceProcessorIfaceA extends MyServiceProcessorIface {
		String a();
	}

	interface MyServiceProcessorIfaceB extends MyServiceProcessorIface {
		String b();
	}

	interface MyServiceProcessorIfaceAB extends MyServiceProcessorIfaceA, MyServiceProcessorIfaceB {
		String ab();
	}

	static class MyServiceProcessorImpl implements MyServiceProcessorIfaceAB {

		List<ComponentBinder<?, ?>> binders = new ArrayList<>();

		// @formatter:off
		@Override public void bindWith(ComponentBinder<?, ?> binder) { binders.add(binder); }
		@Override public List<ComponentBinder<?, ?>> boundBy() { return binders; }
		@Override public String a() { return "a"; }
		@Override public String b() { return "b"; }
		@Override public String ab() { return "ab"; }
		// @formatter:on

	}

	// ===================== //
	// ==== Deployables ==== //
	// ===================== //

	public interface MyServiceProcessor extends ServiceProcessor {
		EntityType<MyServiceProcessor> T = EntityTypes.T(MyServiceProcessor.class);
	}

	public interface MyServiceProcessorA extends MyServiceProcessor {
		EntityType<MyServiceProcessorA> T = EntityTypes.T(MyServiceProcessorA.class);
	}

	public interface MyServiceProcessorB extends MyServiceProcessor {
		EntityType<MyServiceProcessorB> T = EntityTypes.T(MyServiceProcessorB.class);
	}

	// ================= //
	// ==== Binders ==== //
	// ================= //

	private static abstract class TestAbstractBinder implements ComponentBinder<MyServiceProcessor, MyServiceProcessorIface> {

		@Override
		public Object bind(MutableDeploymentContext<MyServiceProcessor, MyServiceProcessorIface> context) throws DeploymentException {
			MyServiceProcessorIface instance = context.getInstanceToBeBound();
			instance.bindWith(this);
			return instance;
		}

		@Override
		public Class<?>[] componentInterfaces() {
			return new Class<?>[] { MyServiceProcessorIface.class };
		}

	}

	private static class TestDirectBinder extends TestAbstractBinder {

		private static final TestDirectBinder instance = new TestDirectBinder();

		@Override
		public EntityType<MyServiceProcessor> componentType() {
			return MyServiceProcessor.T;
		}

	}

	private static class TestDirectBinderA extends TestAbstractBinder {
		private static final TestDirectBinderA instance = new TestDirectBinderA();

		@Override
		public EntityType<MyServiceProcessorA> componentType() {
			return MyServiceProcessorA.T;
		}

		@Override
		public Class<?>[] componentInterfaces() {
			return new Class<?>[] { MyServiceProcessorIfaceA.class };
		}
	}

	private static class TestDirectBinderB extends TestAbstractBinder {
		private static final TestDirectBinderB instance = new TestDirectBinderB();

		@Override
		public EntityType<MyServiceProcessorB> componentType() {
			return MyServiceProcessorB.T;
		}

		@Override
		public Class<?>[] componentInterfaces() {
			return new Class<?>[] { MyServiceProcessorIfaceB.class };
		}
	}

	private static class TestIndirectBinder extends TestAbstractBinder {
		private static final TestIndirectBinder instance = new TestIndirectBinder();

		@Override
		public EntityType<ServiceProcessor> componentType() {
			return ServiceProcessor.T;
		}

		@Override
		public Class<?>[] componentInterfaces() {
			return new Class<?>[] { MyServiceProcessorIfaceA.class, MyServiceProcessorIfaceB.class };
		}
	}

	private class TestMutableDeploymentContext extends AbstractExpertContext<Deployable> implements MutableDeploymentContext<Deployable, Object> {

		private final Deployable deployable;

		private Object instanceToBeBound;
		private Supplier<?> instanceToBeBoundSupplier;
		private boolean instanceSupplied;


		public TestMutableDeploymentContext(Deployable deployable) {
			this.deployable = deployable;
		}

		@Override
		public <I> I getInstanceToBeBound() {
			if (!instanceSupplied) {
				instanceToBeBound = instanceToBeBoundSupplier.get();
				instanceSupplied = true;
			}
			return (I) instanceToBeBound;
		}

		@Override
		public Object getInstanceToBoundIfSupplied() {
			return instanceToBeBound;
		}

		@Override
		public void setInstanceToBeBoundSupplier(Supplier<? extends Object> instanceToBeBoundSupplier) {
			this.instanceToBeBoundSupplier = instanceToBeBoundSupplier;
			instanceSupplied = false;
		}

		@Override
		public PersistenceGmSession getSession() {
			throw new UnsupportedOperationException("Not supported. Just for test purposes");
		}

		@Override
		public <I extends Deployable> I getDeployable() {
			return (I) deployable;
		}

		@Override
		public <E> E resolve(Deployable deployable, EntityType<? extends Deployable> componentType) {
			throw new UnsupportedOperationException("Not supported. Just for test purposes");
		}

		@Override
		public <E> E resolve(String externalId, EntityType<? extends Deployable> componentType) {
			throw new UnsupportedOperationException("Not supported. Just for test purposes");
		}

		@Override
		public <E> E resolve(Deployable deployable, EntityType<? extends Deployable> componentType, Class<E> expertInterface, E defaultDelegate) {
			throw new UnsupportedOperationException("Not supported. Just for test purposes");
		}

		@Override
		public <E> E resolve(String externalId, EntityType<? extends Deployable> componentType, Class<E> expertInterface, E defaultDelegate) {
			throw new UnsupportedOperationException("Not supported. Just for test purposes");
		}
		
		@Override
		public <E> Optional<ResolvedComponent<E>> resolveOptional(String externalId, EntityType<? extends Deployable> componentType,
				Class<E> expertInterface) {
			throw new UnsupportedOperationException("Not supported. Just for test purposes");
		}

	}

}
