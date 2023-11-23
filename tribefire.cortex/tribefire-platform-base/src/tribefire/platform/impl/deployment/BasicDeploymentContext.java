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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.AbstractExpertContext;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.deployment.api.ResolvedComponent;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * <p>
 * Standard {@link MutableDeploymentContext} implementation.
 *
 * @author dirk.scheffler
 *
 * @param <D>
 *            The {@link Deployable} type bound to this context.
 * @param <T>
 *            The expert type bound to this context.
 */
public class BasicDeploymentContext<D extends Deployable, T> extends AbstractExpertContext<D> implements MutableDeploymentContext<D, T> {

	private DeployedComponentResolver resolverDelegate;
	private PersistenceGmSession session;
	private D contextDeployable;
	private T instanceToBeBound;
	private boolean isFullyFetched;
	private Supplier<? extends T> instanceToBeBoundSupplier;
	private boolean instanceSupplied;
	private String deployableExternalId;

	/**
	 * <p>
	 * Creates a BasicDeploymentContext for deployment.
	 */
	public BasicDeploymentContext(DeployedComponentResolver resolverDelegate, PersistenceGmSession session, D deployable, boolean isFullyFetched) {
		Objects.requireNonNull(resolverDelegate, "resolverDelegate must not be null");
		Objects.requireNonNull(session, "session must not be null");
		this.resolverDelegate = resolverDelegate;
		this.session = session;
		this.contextDeployable = deployable;
		this.isFullyFetched = isFullyFetched;
		this.deployableExternalId = deployable != null ? deployable.getExternalId() : null;
	}

	/**
	 * <p>
	 * Creates a BasicDeploymentContext for undeployment.
	 */
	public BasicDeploymentContext(D deployable) {
		super();
		Objects.requireNonNull(deployable, "deployable must not be null");
		this.contextDeployable = deployable;
		this.deployableExternalId = deployable.getExternalId();
	}

	@Override
	public String getDeployableExternalId() {
		return deployableExternalId;
	}

	public void onAfterDeployment() {
		// Removing references to free up memory
		this.session = null;
		this.contextDeployable = null;
	}

	@Override
	public PersistenceGmSession getSession() {
		if (session == null) {
			throw new UnsupportedOperationException("Context doesn't support PersistenceGmSessions during undeployment");
		}
		return session;
	}

	@Override
	public D getDeployable() {
		return contextDeployable;
	}

	@Override
	public <E> E resolve(Deployable deployable, EntityType<? extends Deployable> componentType) {
		return resolve(deployable.getExternalId(), componentType, null, null);
	}

	@Override
	public <E> E resolve(String externalId, EntityType<? extends Deployable> componentType) {
		return resolve(externalId, componentType, null, null);
	}

	@Override
	public <E> E resolve(Deployable deployable, EntityType<? extends Deployable> componentType, Class<E> expertInterface, E defaultDelegate) {
		Objects.requireNonNull(deployable, "deployable must not be null");
		return resolve(deployable.getExternalId(), componentType, expertInterface, defaultDelegate);
	}

	@Override
	public <E> Optional<ResolvedComponent<E>> resolveOptional(String externalId, EntityType<? extends Deployable> componentType,
			Class<E> expertInterface) {
		if (resolverDelegate == null) {
			// the premise is that only for contexts setup for undeployment can miss the delegate
			throw new UnsupportedOperationException("Context doesn't support optional resolving during undeployment");
		}

		return resolverDelegate.resolveOptional(externalId, componentType, expertInterface);
	}

	@Override
	public <E> E resolve(String externalId, EntityType<? extends Deployable> componentType, Class<E> expertInterface, E defaultDelegate) {
		if (resolverDelegate == null) {
			// the premise is that only for contexts setup for undeployment can miss the delegate
			throw new UnsupportedOperationException("Context doesn't support resolving during undeployment");
		}
		Objects.requireNonNull(externalId, "externalId must not be null");

		return resolverDelegate.resolve(externalId, componentType, expertInterface, defaultDelegate);
	}

	@Override
	public T getInstanceToBeBound() {
		if (!instanceSupplied) {
			instanceToBeBound = instanceToBeBoundSupplier.get();
			instanceSupplied = true;
		}
		return instanceToBeBound;
	}

	@Override
	public T getInstanceToBoundIfSupplied() {
		return instanceToBeBound;
	}

	@Override
	public void setInstanceToBeBoundSupplier(Supplier<? extends T> instanceToBeBoundSupplier) {
		this.instanceToBeBoundSupplier = instanceToBeBoundSupplier;
		instanceSupplied = false;
	}

	@Override
	public boolean isDeployableFullyFetched() {
		return isFullyFetched;
	}

}
