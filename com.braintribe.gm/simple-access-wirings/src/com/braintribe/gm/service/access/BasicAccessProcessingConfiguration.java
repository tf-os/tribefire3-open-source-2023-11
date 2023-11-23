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
package com.braintribe.gm.service.access;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.braintribe.cartridge.common.processing.accessrequest.InternalizingAccessRequestProcessor;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.service.access.api.AccessProcessingConfiguration;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.access.smood.basic.SmoodAccess;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.service.api.ServiceProcessor;
import com.braintribe.model.processing.service.common.ConfigurableDispatchingServiceProcessor;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.testing.tools.gm.GmTestTools;

public class BasicAccessProcessingConfiguration implements AccessProcessingConfiguration, Consumer<ConfigurableDispatchingServiceProcessor> {
	private final Map<EntityType<? extends AccessRequest>, AccessRequestProcessor<?, ?>> registeredProcessors = new LinkedHashMap<>();
	private List<Consumer<AccessProcessingConfiguration>> accessConfigurers;
	private final SimpleAccessService simpleAccessService = new SimpleAccessService();
	private PersistenceGmSessionFactory sessionFactory;

	public BasicAccessProcessingConfiguration() {
	}

	public <R extends AccessRequest, T> ServiceProcessor<R, T> toServiceProcessor(AccessRequestProcessor<R, T> serviceProcessor) {
		InternalizingAccessRequestProcessor<R, AccessRequestProcessor<R, T>> wrappingProcessor = new InternalizingAccessRequestProcessor<>(
				serviceProcessor, sessionFactory, sessionFactory);

		return (ServiceProcessor<R, T>) wrappingProcessor;
	}

	@Configurable
	@Required
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void registerAccess(IncrementalAccess access) {
		simpleAccessService.addAccess(access);
	}

	@Override
	public SmoodAccess registerAccess(String accessId, GmMetaModel metaModel) {
		SmoodAccess access = GmTestTools.newSmoodAccessMemoryOnly(accessId, metaModel);
		registerAccess(access);
		return access;
	}

	@Override
	public <A extends AccessRequest> void registerAccessRequestProcessor(EntityType<A> requestType, AccessRequestProcessor<? super A, ?> processor) {
		registeredProcessors.put(requestType, processor);
	}

	public SimpleAccessService getSimpleAccessService() {
		return simpleAccessService;
	}

	@Override
	public void accept(ConfigurableDispatchingServiceProcessor dispatcher) {
		accessConfigurers.forEach(c -> c.accept(this));
		registeredProcessors.forEach((t, p) -> dispatcher.register(t, (ServiceProcessor<ServiceRequest, ?>) toServiceProcessor(p)));
	}

	public void setAccessConfigurers(List<Consumer<AccessProcessingConfiguration>> accessConfigurers) {
		this.accessConfigurers = accessConfigurers;
	}

	@Override
	public void registerAccess(BiFunction<String, GmMetaModel, IncrementalAccess> accessFactory, String accessId, GmMetaModel model) {
		IncrementalAccess access = accessFactory.apply(accessId, model);
		registerAccess(access);
	}
}
