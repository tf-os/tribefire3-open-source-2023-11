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

import static com.braintribe.utils.lcd.CollectionTools2.newLinkedMap;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeployRegistryListener;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.deployment.api.MutableDeployRegistry;

public class BasicDeployRegistry implements MutableDeployRegistry {

	private static final Logger log = Logger.getLogger(BasicDeployRegistry.class);

	private final Map<String, DeploymentEntry> registry = newLinkedMap();

	private final List<DeployRegistryListener> listeners = newList();

	@Override
	public void register(Deployable deployable, DeployedUnit deployedUnit) {
		requireNonNull(deployable, "deployable must not be null");
		requireNonNull(deployable.getExternalId(), "deployable's externalId must not be null");
		requireNonNull(deployedUnit, "deployedUnit must not be null");

		DeploymentEntry entry = new DeploymentEntry(deployable, deployedUnit);

		synchronized (registry) {
			registry.put(deployable.getExternalId(), entry);
		}

		notifyDeployment(entry);

		log.debug(() -> "Registered " + entry);
	}

	@Override
	public DeployedUnit unregister(Deployable deployable) {
		requireNonNull(deployable, "deployable must not be null");
		requireNonNull(deployable.getExternalId(), "deployable's externalId must not be null");

		DeploymentEntry removedEntry = null;

		synchronized (registry) {
			removedEntry = registry.remove(deployable.getExternalId());
		}

		if (removedEntry != null) {
			notifyUndeployment(removedEntry);

			if (log.isDebugEnabled())
				log.debug("Unregistered " + removedEntry);

			return removedEntry.getDeployedUnit();
		}

		if (log.isDebugEnabled())
			log.debug("No entry found for [ " + deployable.getExternalId() + " ]");

		return null;
	}

	@Override
	public DeployedUnit resolve(Deployable deployable) {
		requireNonNull(deployable, "deployable must not be null");

		return resolve(deployable.getExternalId());
	}

	@Override
	public DeployedUnit resolve(String externalId) {
		requireNonNull(externalId, "externalId must not be null");

		DeploymentEntry entry = null;
		synchronized (registry) {
			entry = registry.get(externalId);
		}

		if (entry != null) {
			if (log.isTraceEnabled())
				log.trace("External id [ " + externalId + " ] resolved " + entry);

			return entry.getDeployedUnit();
		}

		if (log.isDebugEnabled())
			log.debug("No entry resolved for [ " + externalId + " ]");

		return null;
	}

	@Override
	public <T> T resolve(Deployable deployable, EntityType<? extends Deployable> componentType) {
		requireNonNull(deployable, "deployable must not be null");

		return resolve(deployable.getExternalId(), componentType);
	}

	@Override
	public <T> T resolve(String externalId, EntityType<? extends Deployable> componentType) {
		requireNonNull(componentType, "componentTypeSignature must not be null");

		DeployedUnit deployedUnit = resolve(externalId);
		if (deployedUnit == null)
			return null;

		T component = deployedUnit.findComponent(componentType);

		if (log.isTraceEnabled())
			log.trace("Component type [ " + componentType.getTypeSignature() + " ] resolved " + component);

		return component;
	}

	@Override
	public boolean isDeployed(Deployable deployable) {
		boolean isDeployed = resolve(deployable) != null;
		return isDeployed;
	}

	@Override
	public boolean isDeployed(String externalId) {
		boolean isDeployed = resolve(externalId) != null;
		return isDeployed;
	}

	@Override
	public List<Deployable> getDeployables() {
		synchronized (registry) {
			return registry.values().stream() //
					.map(DeploymentEntry::getDeployable) //
					.collect(Collectors.toList());
		}
	}

	static class DeploymentEntry {

		private final Deployable deployable;
		private final DeployedUnit deployedUnit;

		public DeploymentEntry(Deployable deployable, DeployedUnit deployedUnit) {
			super();
			this.deployable = deployable;
			this.deployedUnit = deployedUnit;
		}

		public Deployable getDeployable() {
			return deployable;
		}

		public DeployedUnit getDeployedUnit() {
			return deployedUnit;
		}

		@Override
		public String toString() {
			return DeploymentEntry.class.getSimpleName() + "(" + deployable.getExternalId() + ") [deployable=" + deployable + ", deployedUnit=" + deployedUnit + "]";
		}

	}

	@Override
	public void addListener(DeployRegistryListener listener) {
		requireNonNull(listener, "listener must not be null");
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(DeployRegistryListener listener) {
		requireNonNull(listener, "listener must not be null");
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	private void notifyDeployment(DeploymentEntry deploymentEntry) {
		notifyDeployment(deploymentEntry.getDeployable(), deploymentEntry.getDeployedUnit());
	}

	private void notifyUndeployment(DeploymentEntry deploymentEntry) {
		notifyUndeployment(deploymentEntry.getDeployable(), deploymentEntry.getDeployedUnit());
	}

	private void notifyDeployment(Deployable deployable, DeployedUnit deployedUnit) {
		for (DeployRegistryListener listener : getListeners()) {
			try {
				listener.onDeploy(deployable, deployedUnit);
			} catch (Exception e) {
				log.error("Deployment (" + deployable + ") notification to listener (" + listener + ") failed", e);
			}
		}
	}

	private void notifyUndeployment(Deployable deployable, DeployedUnit deployedUnit) {
		for (DeployRegistryListener listener : getListeners()) {
			try {
				listener.onUndeploy(deployable, deployedUnit);
			} catch (Exception e) {
				log.error("Undeployment (" + deployable + ") notification to listener (" + listener + ") failed", e);
			}
		}
	}

	private DeployRegistryListener[] getListeners() {
		synchronized (listeners) {
			return listeners.toArray(new DeployRegistryListener[listeners.size()]);
		}
	}

}
