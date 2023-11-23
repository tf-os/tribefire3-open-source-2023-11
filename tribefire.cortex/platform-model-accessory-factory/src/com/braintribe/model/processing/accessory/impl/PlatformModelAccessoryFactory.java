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
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.accessory.api.PlatformModelEssentialsSupplier;
import com.braintribe.model.processing.meta.cmd.CmdResolverBuilder;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessAspect;
import com.braintribe.model.processing.meta.cmd.context.aspects.AccessTypeAspect;
import com.braintribe.model.processing.session.api.managed.ModelAccessory;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.managed.ModelChangeListener;
import com.braintribe.model.processing.session.api.managed.NotifiableModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * {@link NotifiableModelAccessoryFactory} implementation which builds {@link ModelAccessory modelAccessories} on top of the underlying
 * {@link #setModelEssentialsSupplier(PlatformModelEssentialsSupplier) PlatforModelEssentials}.
 * 
 * @author peter.gazdik
 */
public class PlatformModelAccessoryFactory implements NotifiableModelAccessoryFactory, ModelChangeListener {

	// constants
	private static final Logger log = Logger.getLogger(PlatformModelAccessoryFactory.class);

	// configured
	private PlatformModelEssentialsSupplier pmeSupplier;
	private Supplier<PersistenceGmSession> cortexSessionProvider;
	private Supplier<?> accessSessionProvider;
	private Supplier<?> serviceSessionProvider;
	private Supplier<Set<String>> userRolesProvider;
	private Consumer<CmdResolverBuilder> cmdInitializer;

	// cached
	private final MaCache accessMaCache = new MaCache(this::createAccessModelAccessory, "accessId");
	private final MaCache serviceDomainMaCache = new MaCache(this::createServiceDomainModelAccessory, "serviceDomainId");
	private final MaCache maCache = new MaCache(this::createModelAccessory, "modelName");

	public PlatformModelAccessoryFactory() {
		accessMaCache.setCacheModelAccessories(true);
		serviceDomainMaCache.setCacheModelAccessories(true);
		maCache.setCacheModelAccessories(true);
	}

	/** @see PlatformModelEssentialsSupplier */
	@Configurable
	public void setModelEssentialsSupplier(PlatformModelEssentialsSupplier pmeSupplier) {
		this.pmeSupplier = pmeSupplier;
	}

	@Required // only needed for CMD caching
	public void setCortexSessionSupplier(Supplier<PersistenceGmSession> cortexSessionSupplier) {
		this.cortexSessionProvider = cortexSessionSupplier;
	}

	@Required // only needed for CMD caching
	public void setAccessSessionProvider(Supplier<?> accessSessionProvider) {
		this.accessSessionProvider = accessSessionProvider;
	}

	@Required // only needed for CMD caching
	public void setServiceSessionProvider(Supplier<?> serviceSessionProvider) {
		this.serviceSessionProvider = serviceSessionProvider;
	}

	@Configurable
	public void setCmdInitializer(Consumer<CmdResolverBuilder> cmdInitializer) {
		this.cmdInitializer = cmdInitializer;
	}

	@Required
	public void setUserRolesProvider(Supplier<Set<String>> userRolesProvider) {
		this.userRolesProvider = userRolesProvider;
	}

	private PlatformModelAccessory createAccessModelAccessory(String accessId, String perspective) {
		PlatformModelAccessory result = new PlatformModelAccessory();
		result.setDescription("accessId:" + accessId);
		result.setUserRolesProvider(userRolesProvider); // always the same
		result.setSessionProvider(accessSessionProvider);
		result.setModelEssentialsSupplier(() -> pmeSupplier.getForAccess(accessId, perspective, true));

		result.setCmdConfigurer((cmdrBuilder, me) -> {
			cmdrBuilder.addStaticAspect(AccessAspect.class, accessId);
			cmdrBuilder.addStaticAspect(AccessTypeAspect.class, me.getOwnerType());
			cmdrBuilder.initialize(cmdInitializer);
		});

		result.addListener(() -> {
			log.debug(() -> "Notifying onOutdated: " + result);
			onAccessChange(accessId);
		});

		return result;
	}

	private PlatformModelAccessory createServiceDomainModelAccessory(String serviceDomainId, String perspective) {
		PlatformModelAccessory result = new PlatformModelAccessory();
		result.setDescription("serviceDomainId:" + serviceDomainId);
		result.setUserRolesProvider(userRolesProvider); // always the same
		result.setSessionProvider(serviceSessionProvider);
		result.setModelEssentialsSupplier(() -> pmeSupplier.getForServiceDomain(serviceDomainId, perspective, true));

		result.addListener(() -> {
			log.debug(() -> "Notifying onOutdated: " + result);
			onServiceDomainChange(serviceDomainId);
		});

		return result;
	}

	private PlatformModelAccessory createModelAccessory(String modelName, String perspective) {
		PlatformModelAccessory result = new PlatformModelAccessory();
		result.setDescription("model:" + modelName);
		result.setUserRolesProvider(userRolesProvider); // always the same
		result.setSessionProvider(cortexSessionProvider);
		result.setModelEssentialsSupplier(() -> pmeSupplier.getForModelName(modelName, perspective));

		result.addListener(() -> {
			log.debug(() -> "Notifying onOutdated: " + result);
			onModelChange(modelName);
		});

		return result;
	}

	@Override
	public ModelAccessory getForAccess(String accessId) {
		return getForAccess(accessId, null);
	}

	@Override
	public ModelAccessory getForServiceDomain(String serviceDomainId) {
		return getForServiceDomain(serviceDomainId, null);
	}

	@Override
	public ModelAccessory getForModel(String modelName) {
		return getForModel(modelName, null);
	}

	/* package */ ModelAccessory getForAccess(String accessId, String perspective) {
		return accessMaCache.getModelAccessory(accessId, perspective);
	}

	/* package */ ModelAccessory getForServiceDomain(String serviceDomainId, String perspective) {
		return serviceDomainMaCache.getModelAccessory(serviceDomainId, perspective);
	}

	/* package */ ModelAccessory getForModel(String modelName, String perspective) {
		return maCache.getModelAccessory(modelName, perspective);
	}

	@Override
	public void onAccessChange(String externalId) {
		accessMaCache.onChange(externalId);
	}

	@Override
	public void onServiceDomainChange(String serviceDomainId) {
		serviceDomainMaCache.onChange(serviceDomainId);
	}

	@Override
	public void onModelChange(String modelName) {
		maCache.onChange(modelName);
	}

	@Override
	public ModelAccessoryFactory forPerspective(String perspective) {
		if (perspective == null)
			return this;
		else
			return new PerspectiveAwarePlatformModelAccessoryFactory(perspective);
	}

	class PerspectiveAwarePlatformModelAccessoryFactory implements NotifiableModelAccessoryFactory, ModelChangeListener {

		private final String perspective;

		public PerspectiveAwarePlatformModelAccessoryFactory(String perspective) {
			this.perspective = perspective;
		}

		@Override
		public ModelAccessory getForAccess(String accessId) {
			return PlatformModelAccessoryFactory.this.getForAccess(accessId, perspective);
		}

		@Override
		public ModelAccessory getForServiceDomain(String serviceDomainId) {
			return PlatformModelAccessoryFactory.this.getForServiceDomain(serviceDomainId, perspective);
		}

		@Override
		public ModelAccessory getForModel(String modelName) {
			return PlatformModelAccessoryFactory.this.getForModel(modelName, perspective);
		}

		@Override
		public ModelAccessoryFactory forPerspective(String perspective) {
			return PlatformModelAccessoryFactory.this.forPerspective(perspective);
		}

		@Override
		public void onModelChange(String modelName) {
			PlatformModelAccessoryFactory.this.onModelChange(modelName);
		}

		@Override
		public void onAccessChange(String accessId) {
			PlatformModelAccessoryFactory.this.onAccessChange(accessId);
		}

		@Override
		public void onServiceDomainChange(String serviceDomainId) {
			PlatformModelAccessoryFactory.this.onServiceDomainChange(serviceDomainId);
		}

	}

}
