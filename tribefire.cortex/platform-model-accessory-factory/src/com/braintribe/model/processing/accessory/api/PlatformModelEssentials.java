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
package com.braintribe.model.processing.accessory.api;

import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryListener;

/**
 * @author peter.gazdik
 */
public interface PlatformModelEssentials {

	GmMetaModel getModel();

	ManagedGmSession getModelSession();

	ModelOracle getOracle();

	String getOwnerType();

	void addListener(ModelAccessoryListener modelAccessoryListener);

	/**
	 * Signals (from the outside) that this instance is out-dated. As a result, the listeners are notified with
	 * {@link ModelAccessoryListener#onOutdated()}.
	 * <p>
	 * With the current implementation the first listener is the one that removes this entry from the cache.
	 */
	// See PmeSupplierBase.getForAccess(...)
	// See PmeSupplierBase.getForServiceDomain(...)
	void outdated();

	PmeKey key();

}
