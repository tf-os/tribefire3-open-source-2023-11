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
package tribefire.cortex.module.loading;

import static com.braintribe.utils.lcd.CollectionTools2.acquireList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ExpertContext;
import com.braintribe.model.processing.deployment.api.binding.ComponentBindingBuilder;
import com.braintribe.model.processing.deployment.api.binding.DenotationBindingBuilder;
import com.braintribe.model.processing.deployment.api.binding.ExpertBindingBuilder;

import tribefire.cortex.module.loading.ModuleLoader.ModuleEntry;

/**
 * @author peter.gazdik
 */
public class ModuleDenotationBindingRegistry {

	private final Map<ModuleEntry, List<AbstractBindingRecord>> bindingRecords = newMap();

	public DenotationBindingBuilder newCortexExtendingDenotationBindingBuilder(ModuleEntry moduleEntry,
			ModulesCortexInitializer cortexInitializer) {
		return new CortexExtendingDenotationBindingBuilder(moduleEntry, cortexInitializer);
	}

	public void applyActual(ModuleEntry moduleEntry, DenotationBindingBuilder actualBindingBuilder) {
		List<AbstractBindingRecord> moduleBis = bindingRecords.get(moduleEntry);

		if (moduleBis != null)
			for (AbstractBindingRecord moduleBi : moduleBis)
				moduleBi.applyActual(actualBindingBuilder);
	}

	// ##################################################################
	// ## . . . . . . . . . . . . Binding Records . . . . . . . . . . . .##
	// ##################################################################

	static abstract class AbstractBindingRecord {

		abstract void applyActual(DenotationBindingBuilder actualBindingBuilder);
	}

	static class BindingRecord<D extends Deployable> extends AbstractBindingRecord {
		private final EntityType<D> denotationType;
		private final String externalId;
		private final List<ComponentBindingRecord<D, ?>> componentBindingRecords = newList();

		public BindingRecord(EntityType<D> denotationType, String externalId) {
			this.denotationType = denotationType;
			this.externalId = externalId;
		}

		void addComponentBindingRecord(ComponentBindingRecord<D, ?> cbr) {
			componentBindingRecords.add(cbr);
		}

		@Override
		/* package */ void applyActual(DenotationBindingBuilder actualBindingBuilder) {
			ComponentBindingBuilder<D> actualCbb = actualBindingBuilder.bind(denotationType, externalId);

			for (ComponentBindingRecord<D, ?> cbr : componentBindingRecords)
				actualCbb = cbr.applyActual(actualCbb);
		}

	}

	static class ComponentBindingRecord<D extends Deployable, T> {
		private final Function<ComponentBindingBuilder<D>, ExpertBindingBuilder<D, T>> componentSpecification;
		private final Function<ExpertBindingBuilder<D, T>, ComponentBindingBuilder<D>> expertSpecification;

		public ComponentBindingRecord(
				Function<ComponentBindingBuilder<D>, ExpertBindingBuilder<D, T>> componentSpecification,
				Function<ExpertBindingBuilder<D, T>, ComponentBindingBuilder<D>> expertSpecification) {

			this.componentSpecification = componentSpecification;
			this.expertSpecification = expertSpecification;
		}

		/* package */ ComponentBindingBuilder<D> applyActual(ComponentBindingBuilder<D> actualCbb) {
			ExpertBindingBuilder<D, T> actualEbb = componentSpecification.apply(actualCbb);
			return expertSpecification.apply(actualEbb);
		}
	}

	// ##################################################################
	// ## . . . . . . . . . DenotationBindingBuilder . . . . . . . . . ##
	// ##################################################################

	class CortexExtendingDenotationBindingBuilder implements DenotationBindingBuilder {

		private final ModuleEntry moduleEntry;
		private final ModulesCortexInitializer cortexInitializer;

		public CortexExtendingDenotationBindingBuilder(ModuleEntry moduleEntry,
				ModulesCortexInitializer cortexInitializer) {
			this.moduleEntry = moduleEntry;
			this.cortexInitializer = cortexInitializer;
		}

		@Override
		public <D extends Deployable> ComponentBindingBuilder<D> bind(EntityType<D> denotationType) {
			return bind(denotationType, null);
		}

		@Override
		public <D extends Deployable> ComponentBindingBuilder<D> bind(EntityType<D> denotationType, String externalId) {
			cortexInitializer.onBindDeployable(moduleEntry.descriptor);
			ensureInCortex(denotationType);
			return new RecordingComponentBindingBuilder<>(denotationType, externalId);
		}

			private void ensureInCortex(EntityType<? extends Deployable> denotationType) {
			cortexInitializer.ensureInCortex(denotationType);
		}

		/* package */ void addBindigRecord(AbstractBindingRecord bi) {
			acquireList(bindingRecords, moduleEntry).add(bi);
		}

		// ##################################################################
		// ## . . . . . . . . . . ExpertBindingBuilder . . . . . . . . . . ##
		// ##################################################################

		class RecordingComponentBindingBuilder<D extends Deployable> implements ComponentBindingBuilder<D> {

			private final BindingRecord<D> bindingRecord;

			public RecordingComponentBindingBuilder(EntityType<D> denotationType, String externalId) {
				bindingRecord = new BindingRecord<>(denotationType, externalId);

				addBindigRecord(bindingRecord);
			}

			@Override
			public <T> ExpertBindingBuilder<D, T> component(ComponentBinder<? super D, T> componentBinder) {
				return componentDelegator(delegate -> delegate.component(componentBinder));
			}

			@Override
			public <T> ExpertBindingBuilder<D, T> component(EntityType<? super D> componentType,
					Class<T> expertInterface, Class<?>... additionalExpertInterfaces) {
				return componentDelegator(
						delegate -> delegate.component(componentType, expertInterface, additionalExpertInterfaces));
			}

			@Override
			public <T> ExpertBindingBuilder<D, T> component(Class<T> expertInterface,
					Class<?>... additionalExpertInterfaces) {
				return componentDelegator(delegate -> delegate.component(expertInterface, additionalExpertInterfaces));
			}

			private <T> ExpertBindingBuilder<D, T> componentDelegator(
					Function<ComponentBindingBuilder<D>, ExpertBindingBuilder<D, T>> componentSpecification) {
				return new RecordingExpertBindingBuilder<>(componentSpecification);
			}

			class RecordingExpertBindingBuilder<T> implements ExpertBindingBuilder<D, T> {

				private final Function<ComponentBindingBuilder<D>, ExpertBindingBuilder<D, T>> componentSpecification;

				public RecordingExpertBindingBuilder(
						Function<ComponentBindingBuilder<D>, ExpertBindingBuilder<D, T>> componentSpecification) {
					this.componentSpecification = componentSpecification;
				}

				@Override
				public ComponentBindingBuilder<D> expertFactory(Function<ExpertContext<D>, ? extends T> factory) {
					return expertDelegator(delegate -> delegate.expertFactory(factory));
				}

				@Override
				public ComponentBindingBuilder<D> expert(T expert) {
					return expertDelegator(delegate -> delegate.expert(expert));
				}

				@Override
				public ComponentBindingBuilder<D> expertSupplier(Supplier<? extends T> supplier) {
					return expertDelegator(delegate -> delegate.expertSupplier(supplier));
				}

				private ComponentBindingBuilder<D> expertDelegator(
						Function<ExpertBindingBuilder<D, T>, ComponentBindingBuilder<D>> expertSpecification) {
					bindingRecord.addComponentBindingRecord(
							new ComponentBindingRecord<>(componentSpecification, expertSpecification));

					return RecordingComponentBindingBuilder.this;
				}

			}

		}

	}

}
