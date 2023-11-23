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
package com.braintribe.gm.model.reason;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;

import jsinterop.annotations.JsType;

/**
 * A ReasonException is thrown in cases a Reason is not handled which occurs when one accesses an unsatisfied Maybe via {@link Maybe#get()}. 
 * This turns the potential of a controlled handling of unsatisfied values into an exception situation which is not meant to be handled.
 * 
 * @author Dirk Scheffler
 */
@JsType(namespace = GmCoreApiInteropNamespaces.reason)
public class ReasonException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final Reason reason;

	/**
	 * Constructs a ReasonException with no stacktrace.
	 * 
	 * @param reason The {@link Reason} to be transported
	 */
	public ReasonException(Reason reason) {
		super(reason.stringify());
		this.reason = reason;
	}
	
	public Reason getReason() {
		return reason;
	}
}
