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

import java.util.Date;
import java.util.function.Supplier;

import com.braintribe.model.service.api.ServiceRequest;

import tribefire.extension.cache.model.deployment.service.cache2k.Mode;

public class CacheValueHolder {

	private final Object result; // TODO: rename to value?
	private final Date date;
	private final ServiceRequest request;

	// -----------------------------------------------------------------------
	// CONSTRUCTOR AND FACTORS
	// -----------------------------------------------------------------------

	public static CacheValueHolder create(Object result, Mode mode, Supplier<Object> resultProvider, ServiceRequest request) {
		if (mode == Mode.PRODUCTION) {
			return new CacheValueHolder(result);
		} else if (mode == Mode.DEBUG) {
			return new CacheValueHolder(resultProvider.get(), request);
		} else {
			throw new IllegalArgumentException("'mode': '" + mode + "' not supported");
		}
	}

	protected CacheValueHolder(Object result) {
		this.result = result;
		this.date = null;
		this.request = null;
	}

	protected CacheValueHolder(Object result, ServiceRequest request) {
		this.result = result;
		this.date = new Date();
		this.request = request;
	}

	// -----------------------------------------------------------------------
	// METHODS
	// -----------------------------------------------------------------------

	public boolean updateResult(@SuppressWarnings("unused") long refreshTime) {
		return false;
	}

	// -----------------------------------------------------------------------
	// GETTER & SETTER
	// -----------------------------------------------------------------------

	public Object getResult() {
		return result;
	}

	public Date getDate() {
		return date;
	}

	public ServiceRequest getRequest() {
		return request;
	}

}
