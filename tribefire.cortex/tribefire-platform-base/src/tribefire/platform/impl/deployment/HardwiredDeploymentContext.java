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

import java.util.Optional;
import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.AbstractExpertContext;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.deployment.api.ResolvedComponent;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * <p>
 * A {@link MutableDeploymentContext} providing values if hardwired.
 *
 * @param <D>
 *            The {@link Deployable} type bound to this context.
 * @param <T>
 *            The expert type bound to this context.
 */
public class HardwiredDeploymentContext<D extends Deployable, T> extends AbstractExpertContext<D> implements MutableDeploymentContext<D, T> {

	private T instance;
	private D deployable;
	private PersistenceGmSession session;

	public HardwiredDeploymentContext(T instance) {
		this.instance = instance;
	}

	@Configurable
	public void setDeployable(D deployable) {
		this.deployable = deployable;
	}

	@Configurable
	public void setSession(PersistenceGmSession session) {
		this.session = session;
	}

	@Override
	public PersistenceGmSession getSession() {
		if (session != null) {
			return session;
		}
		throw unsupported("session");
	}

	@Override
	public <I extends D> I getDeployable() {
		if (deployable != null) {
			return (I) deployable;
		}
		throw unsupported("deployable");
	}

	@Override
	public <I extends T> I getInstanceToBeBound() {
		return (I) instance;
	}

	@Override
	public <E> E resolve(Deployable deployable, EntityType<? extends Deployable> componentType) {
		throw new UnsupportedOperationException("resolve() not supported by this "+this.getClass().getSimpleName());
	}

	@Override
	public <E> E resolve(String externalId, EntityType<? extends Deployable> componentType) {
		throw new UnsupportedOperationException("resolve() not supported by this "+this.getClass().getSimpleName());
	}

	@Override
	public <E> E resolve(Deployable deployable, EntityType<? extends Deployable> componentType, Class<E> expertInterface, E defaultDelegate) {
		throw new UnsupportedOperationException("resolve() not supported by this "+this.getClass().getSimpleName());
	}

	@Override
	public <E> E resolve(String externalId, EntityType<? extends Deployable> componentType, Class<E> expertInterface, E defaultDelegate) {
		throw new UnsupportedOperationException("resolve() not supported by this "+this.getClass().getSimpleName());
	}
	
	@Override
	public <E> Optional<ResolvedComponent<E>> resolveOptional(String externalId, EntityType<? extends Deployable> componentType,
			Class<E> expertInterface) {
		throw new UnsupportedOperationException("resolveOptional() not supported by this "+this.getClass().getSimpleName());
	}
	
	

	@Override
	public void setInstanceToBeBoundSupplier(Supplier<? extends T> instanceToBeBoundSupplier) {
		// noop
	}

	@Override
	public T getInstanceToBoundIfSupplied() {
		return instance;
	}
	
	private UnsupportedOperationException unsupported(String property) {
		return new UnsupportedOperationException("No "+property+" configured to this "+this.getClass().getSimpleName());
	}

}
