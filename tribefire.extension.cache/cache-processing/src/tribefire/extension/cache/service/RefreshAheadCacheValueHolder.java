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

import java.util.function.Supplier;

import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.cache.model.deployment.service.cache2k.Mode;

public class RefreshAheadCacheValueHolder extends CacheValueHolder {

	private final long executionTime;

	// -----------------------------------------------------------------------
	// CONSTRUCTOR AND FACTORS
	// -----------------------------------------------------------------------

	public static RefreshAheadCacheValueHolder create(Object result, Mode mode, Supplier<Object> resultProvider, ServiceRequest request) {
		if (mode == Mode.PRODUCTION) {
			return new RefreshAheadCacheValueHolder(result);
		} else if (mode == Mode.DEBUG) {
			return new RefreshAheadCacheValueHolder(resultProvider, request);
		} else {
			throw new IllegalArgumentException("'mode': '" + mode + "' not supported");
		}
	}

	protected RefreshAheadCacheValueHolder(Object result) {
		super(result);
		this.executionTime = System.currentTimeMillis();
	}

	protected RefreshAheadCacheValueHolder(Object result, ServiceRequest request) {
		super(result, request);
		this.executionTime = System.currentTimeMillis();
	}

	@Override
	public boolean updateResult(long refreshTime) {
		if (System.currentTimeMillis() - executionTime < refreshTime) {
			return true;
		}
		return false;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	public long getExecutionTime() {
		return executionTime;
	}
}
