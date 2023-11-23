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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

import com.braintribe.cfg.InitializationAware;
import com.braintribe.logging.Logger;
import com.braintribe.model.deployment.Deployable;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.ConfigurableDcProxyDelegation;
import com.braintribe.model.processing.deployment.api.DcProxy;
import com.braintribe.model.processing.deployment.api.DcProxyDelegation;
import com.braintribe.model.processing.deployment.api.DcProxyListener;
import com.braintribe.model.processing.deployment.api.DeployRegistry;
import com.braintribe.model.processing.deployment.api.DeployRegistryListener;
import com.braintribe.model.processing.deployment.api.DeployedUnit;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.ResolvedComponent;
import com.braintribe.model.processing.deployment.api.SchrodingerBean;
import com.braintribe.model.service.api.InstanceId;

/**
 * Internal delegation handler of {@link DcProxy deployable proxies}, which upon deployment notification, can handle it's proxied interface method
 * calls using a deployed component as a delegate.
 * 
 * @see DcProxyDelegation
 * 
 * @author dirk.scheffler
 */
public class DcProxyDelegationImpl implements DeployRegistryListener, InitializationAware, ConfigurableDcProxyDelegation {

	private static final Logger log = Logger.getLogger(DcProxyDelegationImpl.class);

	private Object delegate;
	private Object defaultDelegate;
	private EntityType<? extends Deployable> componentType;
	private String externalId;
	private DeployRegistry deployRegistry;
	private final Object delegateMonitor = new Object();
	private InstanceId processingInstanceId;
	private Consumer<String> inDeploymentBlocker = s -> { /* noop */ };

	private DeployedUnit deployedUnit;

	private final Set<DcProxyListener> dcProxyListeners = new CopyOnWriteArraySet<>();
	
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public void changeExternalId(String externalId) {
		this.externalId = externalId;
		this.fetchDelegate();
	}

	public void setComponentType(EntityType<? extends Deployable> componentKey) {
		this.componentType = componentKey;
	}

	@Override
	public void setDefaultDelegate(Object defaultDelegate) {
		this.defaultDelegate = defaultDelegate;
		
		dcProxyListeners.forEach(l -> l.onDefaultDelegateSet(defaultDelegate));
	}

	public void setDeployRegistry(DeployRegistry deployRegistry) {
		this.deployRegistry = deployRegistry;

		this.deployRegistry.addListener(this);
	}

	public void setProcessingInstanceId(InstanceId processingInstanceId) {
		this.processingInstanceId = processingInstanceId;
	}

	public void setInDeploymentBlocker(Consumer<String> inDeploymentBlocker) {
		this.inDeploymentBlocker = inDeploymentBlocker;
	}

	@Override
	public void addDcProxyListener(DcProxyListener listener) {
		dcProxyListeners.add(listener);
	}

	@Override
	public void removeDcProxyListener(DcProxyListener listener) {
		dcProxyListeners.remove(listener);
	}

	@Override
	public final Object getDelegate() throws DeploymentException {
		if (delegate == null)
			// wait for deployment if externalId is currently in deployment
			inDeploymentBlocker.accept(externalId);

		Object component = delegate;
		if (component != null)
			return component;

		if (defaultDelegate != null)
			return defaultDelegate;

		throw new DeploymentException(deployableCannotBeAccessedMsg());
	}

	private String deployableCannotBeAccessedMsg() {
		String msg = processingInstanceId.getApplicationId() + " cannot access deployable with external id [" + externalId + "]. Component type: "
				+ componentType.getTypeSignature() + " It is probably not deployed.";

		if (SchrodingerBean.isSchrodingerBeanId(externalId))
			msg += " As this is a Schrodinger bean, it can only be accessed after cortex has been initialied. Maybe it's accessed too early? "
					+ "Seems probabale, as later it's externalId would be changed to that of the deployable, and we would not even be able to tell it's a Schrodinger Bean";

		return msg;
	}

	@Override
	public DeployedUnit getDeployedUnit() {
		return deployedUnit;
	}

	@Override
	public final <E> ResolvedComponent<E> getDelegateOptional() {
		if (delegate == null)
			// wait for deployment if externalId is currently in deployment
			inDeploymentBlocker.accept(externalId);

		DeployedUnit resolvedUnit;
		Object resolvedDelegate;

		synchronized (delegateMonitor) {
			resolvedUnit = deployedUnit;
			resolvedDelegate = delegate;
		}

		if (resolvedUnit == null)
			return null;

		return new ResolvedComponent<E>() {
			// @formatter:off
			@Override public E component() { return (E) resolvedDelegate; }
			@Override public DeployedUnit deployedUnit() { return deployedUnit; }
			// @formatter:on
		};
	}

	@Override
	public void onDeploy(Deployable deployable, DeployedUnit deployedUnit) {
		if (haltCallback(deployable, deployedUnit))
			return;

		if (externalId.equals(deployable.getExternalId())) {
			synchronized (delegateMonitor) {
				takeDelegate(deployedUnit, "deployment");
			}

		} else if (log.isTraceEnabled()) {
			log.trace("Proxy for [ " + externalId + " ] got notification from unrelated deployment: [ " + deployable.getExternalId() + " ]");
		}
	}

	@Override
	public void onUndeploy(Deployable deployable, DeployedUnit deployedUnit) {
		if (haltCallback(deployable, deployedUnit))
			return;

		if (externalId.equals(deployable.getExternalId())) {
			synchronized (delegateMonitor) {
				clearDelegate();
				if (log.isDebugEnabled())
					log.debug("Proxy for [ " + externalId + " ] unreferenced delegate upon undeploy");
			}

		} else if (log.isTraceEnabled()) {
			log.trace("Proxy for [ " + externalId + " ] got notification from unrelated undeployment: [ " + deployable.getExternalId() + " ]");
		}

	}

	@Override
	public void postConstruct() {
		fetchDelegate();
	}

	private void fetchDelegate() {
		synchronized (delegateMonitor) {
			DeployedUnit deployedUnit = deployRegistry.resolve(externalId);

			if (deployedUnit != null)
				takeDelegate(deployedUnit, "construction");
			else
				clearDelegate();
		}
	}

	private void takeDelegate(DeployedUnit deployedUnit, String source) {
		this.deployedUnit = deployedUnit;
		this.delegate = deployedUnit.getComponent(componentType);

		dcProxyListeners.forEach(l -> l.onDelegateSet(delegate));

		if (log.isDebugEnabled())
			log.debug("Proxy for [ " + externalId + " ] resolved delegate upon " + source + ": " + delegate);
	}

	private void clearDelegate() {
		Object oldDelegate = delegate;

		this.deployedUnit = null;
		this.delegate = null;

		if (oldDelegate != null)
			dcProxyListeners.forEach(l -> l.onDelegateCleared(oldDelegate));
	}

	private boolean haltCallback(Deployable deployable, DeployedUnit deployedUnit) {
		if (deployable != null && deployedUnit != null)
			return false;

		log.error("Invalid callback state. deployable: [ " + deployable + " ] deployedUnit: [ " + deployedUnit + " ]", new NullPointerException());
		return true;
	}
}
