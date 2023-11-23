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
package tribefire.extension.cache.service;

import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.cfg.LifecycleAware;
import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.cache.model.status.CacheAspectStatus;

public interface CacheAspectInterface<T extends CacheAspectStatus> extends LifecycleAware {

	T retriveCacheStatus();

	Object retrieveCacheResult(Supplier<Object> resultProvider, ServiceRequest request, String hash);

	void clearCache();

	boolean containsEntry(String hash);

	void removeEntry(String hash);

	CacheValueHolder getEntry(String hash);

	Map<String, CacheValueHolder> getAllEntries();

}
