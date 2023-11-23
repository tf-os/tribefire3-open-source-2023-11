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
package tribefire.platform.impl.deployment.proxy;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.common.lcd.Pair;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.ComponentInterfaceBindings;
import com.braintribe.model.processing.deployment.api.DcProxy;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.deployment.api.ResolvedComponent;
import com.braintribe.model.service.api.InstanceId;

/**
 * <p>
 * A {@link DeployedComponentResolver} which returns proxies capable of being notified about the deployment of requested
 * components, thus enabling yet undeployed deployable's components to be referenced during deployment of other
 * deployables.
 * 
 * @author dirk.scheffler
 */
public class ProxyingDeployedComponentResolver implements DeployedComponentResolver {

	// configurable
	private DeployRegistry registry;
	private InstanceId processingInstanceId;
	private ComponentInterfaceBindings componentInterfaceBindings;
	
	// cached
	private final Map<ProxyKey, DcProxy> proxies = new ConcurrentHashMap<>();
	private Consumer<String> inDeploymentBlocker = s -> { /* noop */ };
	
	public Stream<String> getExternalIdsOfResolvedProxies() {
		return proxies.entrySet().stream().map(e -> e.getKey().externalId);
	}
	
	public Stream<Pair<String, EntityType<? extends Deployable>>> getResolvedProxyAdressings() {
		return proxies.entrySet().stream().map(e -> new Pair<String, EntityType<? extends Deployable>>(e.getKey().externalId, e.getKey().componentType));
	}
	
	@Required
	@Configurable
	public void setDeployRegistry(DeployRegistry registry) {
		this.registry = registry;
	}

	@Required
	@Configurable
	public void setProcessingInstanceId(InstanceId processingInstanceId) {
		this.processingInstanceId = processingInstanceId;
	}

	@Required
	@Configurable
	public void setComponentInterfaceBindings(ComponentInterfaceBindings componentInterfaceBindings) {
		this.componentInterfaceBindings = componentInterfaceBindings;
	}
	
	@Configurable
	public void setInDeploymentBlocker(Consumer<String> inDeploymentBlocker) {
		this.inDeploymentBlocker = inDeploymentBlocker;
	}

	@Override
	public <T> T resolve(Deployable deployable, EntityType<? extends Deployable> componentType) {
		return resolve(deployable, componentType, null, null);
	}

	@Override
	public <T> T resolve(String externalId, EntityType<? extends Deployable> componentType) {
		return resolve(externalId, componentType, null, null);
	}
	
	@Override
	public <T> T resolve(Deployable deployable, EntityType<? extends Deployable> componentType, Class<T> expertInterface, T defaultDelegate) {
		Objects.requireNonNull(deployable, "deployable must not be null");
		return resolve(deployable.getExternalId(), componentType, expertInterface, defaultDelegate);
	}

	@Override
	public <T> T resolve(String externalId, EntityType<? extends Deployable> componentType, Class<T> expertInterface, T defaultDelegate) {
		DcProxy proxy = resolveProxy(externalId, componentType, defaultDelegate);
		return expertInterface != null ? expertInterface.cast(proxy) : (T) proxy;
	}

	private DcProxy resolveProxy(String externalId, EntityType<? extends Deployable> componentType, Object defaultDelegate) {
		Class<?>[] interfaceClasses = componentInterfaceBindings.getComponentInterfaces(componentType);
		return resolveProxy(externalId, componentType, interfaceClasses, defaultDelegate);
	}
		
	public <T> T resolve(String externalId, ComponentBinder<?, ?> componentBinder) {
		return (T) resolveProxy(externalId, componentBinder.componentType(), componentBinder.componentInterfaces(), null);
	}
	
	private DcProxy resolveProxy(String externalId, EntityType<? extends Deployable> componentType, Class<?>[] interfaceClasses, Object defaultDelegate) {
		Objects.requireNonNull(externalId, "externalId must not be null");

		ProxyKey proxyKey = new ProxyKey(externalId, componentType, defaultDelegate);

		DcProxy existingProxy = proxies.get(proxyKey);
		if (existingProxy != null) {
			return existingProxy;
		}

		DcProxy proxy = DcProxyFactory.forInterfaces(interfaceClasses, externalId, componentType, registry, processingInstanceId);

		DcProxyDelegationImpl delegateManager = (DcProxyDelegationImpl) proxy.$_delegatorAligator();
		
		delegateManager.setInDeploymentBlocker(inDeploymentBlocker);

		if (defaultDelegate != null)
			delegateManager.setDefaultDelegate(defaultDelegate);

		proxies.put(proxyKey, proxy);

		return proxy;
	}

	@Override
	public <T> Optional<ResolvedComponent<T>> resolveOptional(String externalId, EntityType<? extends Deployable> componentType,
			Class<T> expertInterface) {

		DcProxy proxy = resolveProxy(externalId, componentType, null);

		ResolvedComponent<T> resolvedComponent = null;

		if (proxy != null)
			resolvedComponent = proxy.$_delegatorAligator().getDelegateOptional();

		return Optional.ofNullable(resolvedComponent);

	}

	public static class ProxyKey {

		String externalId;
		EntityType<? extends Deployable> componentType;
		Object defaultDelegate;

		public ProxyKey(String externalId, EntityType<? extends Deployable> componentType, Object defaultDelegate) {
			super();
			this.externalId = externalId;
			this.componentType = componentType;
			this.defaultDelegate = defaultDelegate;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
			result = prime * result + ((defaultDelegate == null) ? 0 : defaultDelegate.hashCode());
			result = prime * result + ((externalId == null) ? 0 : externalId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ProxyKey other = (ProxyKey) obj;
			if (componentType == null) {
				if (other.componentType != null)
					return false;
			} else if (componentType != other.componentType)
				return false;
			if (defaultDelegate == null) {
				if (other.defaultDelegate != null)
					return false;
			} else if (defaultDelegate != other.defaultDelegate)
				return false;
			if (externalId == null) {
				if (other.externalId != null)
					return false;
			} else if (!externalId.equals(other.externalId))
				return false;
			return true;
		}

	}

}
