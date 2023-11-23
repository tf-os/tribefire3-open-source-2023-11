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
package com.braintribe.model.processing.service.weaving.impl.dispatch;

/**
 * This {@link RuntimeException} is thrown by {@link DispatchMap} when the analysis or synthesis during weaving is failing somehow 
 * @author dirk.scheffler
 *
 */
@SuppressWarnings("serial")
public class DispatchWeavingException extends RuntimeException {

	public DispatchWeavingException() {
		super();
	}

	public DispatchWeavingException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public DispatchWeavingException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public DispatchWeavingException(String arg0) {
		super(arg0);
	}

	public DispatchWeavingException(Throwable arg0) {
		super(arg0);
	}

}
