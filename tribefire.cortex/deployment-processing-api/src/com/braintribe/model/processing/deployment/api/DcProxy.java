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
package com.braintribe.model.processing.deployment.api;

/**
 * Interface that every deployable proxy implements.
 * <p>
 * Every deployable is deployed as a proxy with a delegate, which among other things allows for it's re-deployment without the need to re-deploy the
 * components that depend on it. These generated proxies have to extend/implement all the class/interfaces relevant for given component type (which is
 * usually the same type - IncrementalAccess as component type and also as the interface) and additionally we add this interface to be able to access
 * the underlying {@link DcProxyDelegation delegation} handler.
 * 
 * @author peter.gazdik
 */
public interface DcProxy {

	static DcProxyDelegation getDelegateManager(Object o) {
		return ((DcProxy) o).$_delegatorAligator();
	}

	static ConfigurableDcProxyDelegation getConfigurableDelegateManager(Object o) {
		return ((DcProxy) o).$_delegatorAligator();
	}

	/**
	 * Returns the underlying {@link ConfigurableDcProxyDelegation}
	 * <p>
	 * Because our proxies implement this interface and the actual deployable component interface (IncrementalAccess or ServiceProcessor or whatever),
	 * we needed a method name that will not conflict with the deployable component. We're confident this does the trick.
	 */
	ConfigurableDcProxyDelegation $_delegatorAligator();

}
