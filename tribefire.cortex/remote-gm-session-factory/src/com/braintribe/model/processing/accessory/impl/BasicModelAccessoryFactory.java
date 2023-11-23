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
package com.braintribe.model.processing.accessory.impl;

import java.util.Set;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.NotifiableModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Standard {@link NotifiableModelAccessoryFactory} implementation.
 * 
 * @author dirk.scheffler
 */
public class BasicModelAccessoryFactory implements NotifiableModelAccessoryFactory {

	// constants
	private static final Logger log = Logger.getLogger(BasicModelAccessoryFactory.class);

	// configured
	private BasicModelAccessorySupplier modelAccessorySupplier;
	private Supplier<?> accessSessionProvider;
	private Supplier<?> serviceSessionProvider;
	private Supplier<Set<String>> userRolesProvider;
	protected Supplier<PersistenceGmSession> cortexSessionSupplier;
	private String defaultServiceDomainId;

	// cached
	private final MaCache accessMaCache = new MaCache(this::createAccessModelAccessory, "accessId");
	private final MaCache serviceDomainMaCache = new MaCache(this::createServiceDomainModelAccessory, "serviceDomainId");

	@Required
	@Configurable
	public void setModelAccessorySupplier(BasicModelAccessorySupplier modelAccessorySupplier) {
		this.modelAccessorySupplier = modelAccessorySupplier;
	}

	@Required
	@Configurable
	public void setAccessSessionProvider(Supplier<?> accessSessionProvider) {
		this.accessSessionProvider = accessSessionProvider;
	}

	@Required
	@Configurable
	public void setServiceSessionProvider(Supplier<?> serviceSessionProvider) {
		this.serviceSessionProvider = serviceSessionProvider;
	}

	@Required
	@Configurable
	public void setUserRolesProvider(Supplier<Set<String>> userRolesProvider) {
		this.userRolesProvider = userRolesProvider;
	}

	@Required
	@Configurable
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionSupplier = cortexSessionSupplier;
	}

	@Configurable
	public void setCacheModelAccessories(boolean cacheModelAccessories) {
		accessMaCache.setCacheModelAccessories(cacheModelAccessories);
		serviceDomainMaCache.setCacheModelAccessories(cacheModelAccessories);
	}

	@Configurable
	public void setDefaultServiceDomainId(String defaultServiceDomainId) {
		this.defaultServiceDomainId = defaultServiceDomainId;
	}

	private AccessModelAccessory createAccessModelAccessory(String accessId, String perspective) {
		BasicModelAccessorySupplier.checkPerspectiveNull(perspective);

		AccessModelAccessory modelAccessory = new AccessModelAccessory();
		modelAccessory.setAccessId(accessId);
		modelAccessory.setCortexSessionProvider(cortexSessionSupplier);
		modelAccessory.setSessionProvider(accessSessionProvider);
		modelAccessory.setUserRolesProvider(userRolesProvider);
		modelAccessory.setModelAccessorySupplier(modelAccessorySupplier);
		modelAccessory.addListener(() -> {
			log.debug(() -> "Notifying onOutdated: " + modelAccessory);
			onAccessChange(accessId);
		});

		return modelAccessory;
	}

	private ServiceDomainModelAccessory createServiceDomainModelAccessory(String serviceDomainId, String perspective) {
		BasicModelAccessorySupplier.checkPerspectiveNull(perspective);

		ServiceDomainModelAccessory modelAccessory = new ServiceDomainModelAccessory();
		modelAccessory.setServiceDomainId(serviceDomainId);
		modelAccessory.setCortexSessionProvider(cortexSessionSupplier);
		modelAccessory.setSessionProvider(serviceSessionProvider);
		modelAccessory.setUserRolesProvider(userRolesProvider);
		modelAccessory.setModelAccessorySupplier(modelAccessorySupplier);
		modelAccessory.setDefaultServiceDomainId(defaultServiceDomainId);
		modelAccessory.addListener(() -> {
			log.debug(() -> "Notifying onOutdated: " + modelAccessory);
			onServiceDomainChange(serviceDomainId);
		});

		log.debug(() -> "Created " + modelAccessory);

		return modelAccessory;
	}

	@Override
	public ModelAccessory getForAccess(String accessId) {
		return accessMaCache.getModelAccessory(accessId, null);
	}

	@Override
	public ModelAccessory getForServiceDomain(String serviceDomainId) {
		return serviceDomainMaCache.getModelAccessory(serviceDomainId, null);
	}

	@Override
	public ModelAccessory getForModel(String modelName) {
		return modelAccessorySupplier.getForModel(modelName);
	}

	@Override
	public void onAccessChange(String externalId) {
		accessMaCache.onChange(externalId);
	}

	@Override
	public void onServiceDomainChange(String serviceDomainId) {
		serviceDomainMaCache.onChange(serviceDomainId);
	}

}
