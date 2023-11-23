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

import java.util.IdentityHashMap;
import java.util.Map;

import com.braintribe.cartridge.common.processing.deployment.DeploymentScope;
import com.braintribe.cartridge.common.processing.deployment.ReflectBeansForDeployment;
import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.wire.api.scope.InstanceHolder;
import com.braintribe.wire.api.scope.LifecycleListener;

public class DeploymentScopeOriginManager implements LifecycleListener {
	private Map<Object, InstanceHolder> originMap = new IdentityHashMap<>();
	private DeploymentScope scope;
	
	@Configurable @Required
	public void setScope(DeploymentScope scope) {
		this.scope = scope;
	}

	@Override
	public void onPostConstruct(InstanceHolder beanHolder, Object bean) {
		if (beanHolder.scope() == scope || beanHolder.space() instanceof ReflectBeansForDeployment) {
			synchronized (originMap) {
				originMap.put(bean, beanHolder);
			}
		}
	}

	@Override
	public void onPreDestroy(InstanceHolder beanHolder, Object bean) {
		if (beanHolder.scope() == scope || beanHolder.space() instanceof ReflectBeansForDeployment) {
			synchronized (originMap) {
				originMap.remove(bean);
			}
		}
	}

	/**
	 * @return the BeanHolder that was responsible for creating the bean if existing otherwise null
	 */
	public InstanceHolder resolveBeanHolder(Object instance) {
		synchronized (originMap) {
			return originMap.get(instance);
		}
	}
}


