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

/**
 * UnsatisfiedMaybeTunneling is a vehicle to transport an unsatisfied Maybe {@link Reason} from methods that do not return a {@link Maybe} or other
 * structures that can hold a {@link Reason}. In that sense it is not there to communicate a real exception
 * but a structural error with meaning and potential expectation.
 *  
 * @author Dirk Scheffler
 */
public class UnsatisfiedMaybeTunneling extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private Maybe<?> maybe;

	/**
	 * Constructs a ReasonException with no stacktrace.
	 * 
	 * @param maybe The {@link Maybe} to be transported
	 */
	public UnsatisfiedMaybeTunneling(Maybe<?> maybe) {
		super(maybe.whyUnsatisfied().getText(), null, false, false);
		this.maybe = maybe;
	}
	
	public <T> Maybe<T> getMaybe() {
		return (Maybe<T>) maybe;
	}
	
	public Reason whyUnsatisfied() {
		return maybe.whyUnsatisfied();
	}
	
	public static <T> T getOrTunnel(Maybe<T> maybe) {
		if (maybe.isUnsatisfied())
			throw new UnsatisfiedMaybeTunneling(maybe);
		
		return maybe.get();
	}
	
}
