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

import static com.braintribe.utils.lcd.CollectionTools2.newMap;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.braintribe.cfg.Required;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.braintribe.model.processing.accessory.impl.MdPerspectiveRegistry;
import com.braintribe.model.processing.meta.cmd.context.experts.SelectorExpert;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.service.api.InternalPushRequest;
import com.braintribe.model.service.api.result.PushResponse;

import tribefire.module.api.DenotationTransformerRegistry;
import tribefire.module.wire.contract.HardwiredExpertsContract;

/**
 * @author peter.gazdik
 */
public class PlatformHardwiredExpertsRegistry implements HardwiredExpertsContract {

	// Set by ModuleLoader
	public ModulesCortexInitializer cortexInitializer;

	public Map<EntityType<? extends MetaDataSelector>, SelectorExpert<?>> mdSelectorExperts;

	private Consumer<ServiceProcessor<InternalPushRequest, PushResponse>> pushHandlerAdder;

	private MdPerspectiveRegistry mdPerspectiveRegistry;

	private DenotationTransformerRegistry transformerRegistry;

	@Required
	public void setDenotationTransformerRegistry(DenotationTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@Required
	public void setPushHandlerAdder(Consumer<ServiceProcessor<InternalPushRequest, PushResponse>> pushHandlerAdder) {
		this.pushHandlerAdder = pushHandlerAdder;
	}

	@Required
	public void setMdPerspectiveRegistry(MdPerspectiveRegistry mdPerspectiveRegistry) {
		this.mdPerspectiveRegistry = mdPerspectiveRegistry;
	}

	// ###########################################################
	// ## . . . . . . . . . Hardwired binding . . . . . . . . . ##
	// ###########################################################

	@Override
	public DenotationTransformerRegistry denotationTransformationRegistry() {
		return transformerRegistry;
	}

	@Override
	public <S extends MetaDataSelector> void bindMetaDataSelectorExpert(EntityType<? extends S> selectorType, SelectorExpert<S> expert) {
		cortexInitializer.ensureInCortex(selectorType);

		if (mdSelectorExperts == null)
			mdSelectorExperts = newMap();

		mdSelectorExperts.put(selectorType, expert);
	}

	@Override
	public void addPushHandler(ServiceProcessor<InternalPushRequest, PushResponse> handler) {
		this.pushHandlerAdder.accept(handler);
	}

	@Override
	public void extendModelPerspective(String perspective, String... metaDataDomains) {
		mdPerspectiveRegistry.extendModelPerspective(perspective, metaDataDomains);
	}

	@Override
	public void extendMetaDataDomain(String domain, Predicate<MetaData> test) {
		mdPerspectiveRegistry.extendMdDomain(domain, test);
	}

}
